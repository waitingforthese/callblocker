package com.rahul.selectedcallfilterv6;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.app.role.RoleManager;
import android.widget.*;
import java.util.*;

public class MainActivity extends Activity {
    private static final int PICK = 10;
    private static final int CONTACTS = 11;
    private SharedPreferences prefs;
    private Switch master;
    private TextView status, list, screeningStatus, allowedCount, smsStatus;
    private Switch smsSwitch;
    private EditText smsMessage;
    private static final int SMS = 12;
    private static final String DEFAULT_SMS = "सध्या मी फोन घेऊ शकत नाही. ऑफिस मध्ये संपर्क करा.";

    @Override public void onCreate(Bundle b) {
        super.onCreate(b); setContentView(R.layout.activity_main);
        prefs = getSharedPreferences("filter", MODE_PRIVATE);
        master = findViewById(R.id.masterSwitch); status = findViewById(R.id.status); list = findViewById(R.id.list);
        smsSwitch = findViewById(R.id.smsSwitch);
        smsMessage = findViewById(R.id.smsMessage);
        screeningStatus = findViewById(R.id.screeningStatus);
        allowedCount = findViewById(R.id.allowedCount);
        smsStatus = findViewById(R.id.smsStatus);
        smsSwitch.setChecked(prefs.getBoolean("sms_enabled", false));
        smsMessage.setText(prefs.getString("sms_message", DEFAULT_SMS));
        smsSwitch.setOnCheckedChangeListener((v,c)-> {
            if (c && checkSelfPermission(Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.SEND_SMS}, SMS);
            }
            prefs.edit().putBoolean("sms_enabled", c).apply();
        });
        smsMessage.setOnFocusChangeListener((v,hasFocus)-> { if (!hasFocus) saveSmsMessage(); });
        master.setChecked(prefs.getBoolean("enabled", false));
        master.setOnCheckedChangeListener((v,c)->{
            if (c && !isCallScreeningRoleHeld()) {
                master.setChecked(false);
                prefs.edit().putBoolean("enabled", false).apply();
                Toast.makeText(this, "First set this app as the Call Screening app.", Toast.LENGTH_LONG).show();
                openScreeningSettings();
                return;
            }
            prefs.edit().putBoolean("enabled",c).apply();
            refresh();
        });
        findViewById(R.id.selectContact).setOnClickListener(v -> pickContact());
        findViewById(R.id.chooseScreening).setOnClickListener(v -> openScreeningSettings());
        if (checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, CONTACTS);
        refresh();
        updateScreeningStatus();
    }
    @Override protected void onResume() { super.onResume(); updateScreeningStatus(); refresh(); }
    @Override protected void onPause() { super.onPause(); saveSmsMessage(); }
    private void saveSmsMessage() { if (smsMessage != null) { String m=smsMessage.getText().toString().trim(); if (m.isEmpty()) m=DEFAULT_SMS; prefs.edit().putString("sms_message",m).apply(); } }
    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS) {
            boolean granted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
            if (!granted) {
                smsSwitch.setChecked(false);
                prefs.edit().putBoolean("sms_enabled", false).apply();
                Toast.makeText(this, "SMS permission is required for automatic rejected-call SMS.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void pickContact() {
        Intent i = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI); startActivityForResult(i, PICK);
    }
    @Override protected void onActivityResult(int r,int c,Intent d){ super.onActivityResult(r,c,d); if(r==PICK && c==RESULT_OK && d!=null){
        Uri u=d.getData(); String[] p={ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME};
        try(android.database.Cursor cur=getContentResolver().query(u,p,null,null,null)){ if(cur!=null && cur.moveToFirst()){
            String num=cur.getString(0), name=cur.getString(1); String key=normalize(num);
            prefs.edit().putBoolean("allow_"+key,true).putString("name_"+key,name).apply(); refresh();
        }}
    }}
    private String normalize(String n){ String d=n==null?"":n.replaceAll("\\D",""); return d.length()>10?d.substring(d.length()-10):d; }
    private void refresh(){
        boolean on=prefs.getBoolean("enabled",false); status.setText(on ? "FILTER ON — only selected numbers are allowed" : "FILTER OFF");
        StringBuilder s=new StringBuilder("Allowed contacts:\n"); boolean any=false; int count=0;
        Map<String,?> all=prefs.getAll(); for(String k:all.keySet()) if(k.startsWith("allow_") && Boolean.TRUE.equals(all.get(k))){ String n=k.substring(6); s.append("• ").append(all.get("name_"+n)).append("  (").append(n).append(")\n"); any=true; count++; }
        list.setText(any?s.toString():"Allowed contacts:\nNo contacts selected yet.");
        if (allowedCount != null) allowedCount.setText(String.valueOf(count));
        if (smsStatus != null) smsStatus.setText(prefs.getBoolean("sms_enabled", false) ? "ON" : "OFF");
    }

    private void updateScreeningStatus() {
        if (screeningStatus == null) return;
        boolean held = isCallScreeningRoleHeld();
        screeningStatus.setText(held ? "✓  Call Screening Active  •  Protected" : "⚠  Call Screening not selected yet");
        screeningStatus.setTextColor(held ? 0xFF087A34 : 0xFF9A6700);
    }
    private boolean isCallScreeningRoleHeld() {
        try {
            RoleManager rm = getSystemService(RoleManager.class);
            return rm != null && rm.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING)
                    && rm.isRoleHeld(RoleManager.ROLE_CALL_SCREENING);
        } catch (Exception ignored) {
            return false;
        }
    }

    private void openScreeningSettings(){
        try {
            RoleManager rm = getSystemService(RoleManager.class);
            if (rm != null && rm.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING)) {
                startActivityForResult(rm.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING), 20);
                return;
            }
        } catch(Exception ignored) { }
        startActivity(new Intent("android.settings.MANAGE_DEFAULT_APPS_SETTINGS"));
        Toast.makeText(this,"Select this app as the Call Screening app if Vivo shows the option.",Toast.LENGTH_LONG).show();
    }
}
