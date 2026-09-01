package com.rahul.selectedcallfilter;
import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.graphics.Color;
import android.view.View;
import android.view.WindowInsets;
import android.provider.ContactsContract;
import android.app.role.RoleManager;
import android.widget.*;
import java.util.*;

public class MainActivity extends Activity {
    private static final int CONTACTS = 11;
    private SharedPreferences prefs;
    private Switch master;
    private TextView status, screeningStatus, allowedCount, smsStatus;
    private TextView todayCalls, todaySms, yesterdayCalls, yesterdaySms, dayBeforeCalls, dayBeforeSms;
    private Switch smsSwitch;
    private EditText smsMessage;
    private static final int SMS = 12;
    private static final String DEFAULT_SMS = "सध्या मी फोन घेऊ शकत नाही. ऑफिस मध्ये संपर्क करा.";

    @Override public void onCreate(Bundle b) {
        super.onCreate(b); setContentView(R.layout.activity_main);
        getWindow().setStatusBarColor(Color.rgb(0, 83, 170));
        getWindow().setNavigationBarColor(Color.rgb(8, 55, 110));
        View root = findViewById(R.id.root);
        if (root != null) {
            root.setOnApplyWindowInsetsListener((v, insets) -> {
                android.graphics.Insets bars = insets.getInsets(WindowInsets.Type.systemBars());
                v.setPadding(v.getPaddingLeft(), bars.top, v.getPaddingRight(), bars.bottom);
                return insets;
            });
            root.requestApplyInsets();
        }
        prefs = getSharedPreferences("filter", MODE_PRIVATE);
        master = findViewById(R.id.masterSwitch); status = findViewById(R.id.status);
        smsSwitch = findViewById(R.id.smsSwitch);
        smsMessage = findViewById(R.id.smsMessage);
        screeningStatus = findViewById(R.id.screeningStatus);
        allowedCount = findViewById(R.id.allowedCount);
        smsStatus = findViewById(R.id.smsStatus);
        todayCalls = findViewById(R.id.todayCalls);
        todaySms = findViewById(R.id.todaySms);
        yesterdayCalls = findViewById(R.id.yesterdayCalls);
        yesterdaySms = findViewById(R.id.yesterdaySms);
        dayBeforeCalls = findViewById(R.id.dayBeforeCalls);
        dayBeforeSms = findViewById(R.id.dayBeforeSms);
        smsSwitch.setChecked(prefs.getBoolean("sms_enabled", false));
        smsSwitch.setText(smsSwitch.isChecked() ? "ON" : "OFF");
        smsMessage.setText(prefs.getString("sms_message", DEFAULT_SMS));
        smsSwitch.setOnCheckedChangeListener((v,c)-> {
            if (c && checkSelfPermission(Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.SEND_SMS}, SMS);
            }
            smsSwitch.setText(c ? "ON" : "OFF");
            prefs.edit().putBoolean("sms_enabled", c).apply();
            Toast.makeText(this, c ? "✓ SMS Settings Saved • ON" : "⏹ SMS Auto Reply Stopped", Toast.LENGTH_SHORT).show();
        });
        smsMessage.setOnFocusChangeListener((v,hasFocus)-> { if (!hasFocus) { saveSmsMessage(); Toast.makeText(this, "✓ SMS message setting saved", Toast.LENGTH_SHORT).show(); } });
        master.setChecked(prefs.getBoolean("enabled", false));
        master.setText(master.isChecked() ? "ON" : "OFF");
        master.setOnCheckedChangeListener((v,c)->{
            if (c && !isCallScreeningRoleHeld()) {
                master.setChecked(false);
                prefs.edit().putBoolean("enabled", false).apply();
                Toast.makeText(this, "आधी RECEIVED CALL FILTER ला Call Screening App म्हणून सेट करा.", Toast.LENGTH_LONG).show();
                openScreeningSettings();
                return;
            }
            master.setText(c ? "ON" : "OFF");
            prefs.edit().putBoolean("enabled",c).apply();
            refresh();
            Toast.makeText(this, c ? "✓ Settings Saved • Protection ON" : "⏹ Protection Stopped • Settings Saved", Toast.LENGTH_SHORT).show();
        });
        findViewById(R.id.openAllowedContacts).setOnClickListener(v -> startActivity(new Intent(this, AllowedContactsActivity.class)));
        findViewById(R.id.chooseScreening).setOnClickListener(v -> openScreeningSettings());
        findViewById(R.id.tutorialButton).setOnClickListener(v -> showTutorial());
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

    private String normalize(String n){ String d=n==null?"":n.replaceAll("\\D",""); return d.length()>10?d.substring(d.length()-10):d; }

    private void refresh(){
        boolean on=prefs.getBoolean("enabled",false);
        status.setText(on ? "FILTER ON — only selected numbers are allowed" : "FILTER OFF");
        int count=0;
        Map<String,?> all=prefs.getAll();
        for(String k:all.keySet()) {
            if(k.startsWith("allow_") && Boolean.TRUE.equals(all.get(k))) count++;
        }
        if (allowedCount != null) allowedCount.setText(String.valueOf(count));
        if (smsStatus != null) smsStatus.setText(prefs.getBoolean("sms_enabled", false) ? "ON" : "OFF");
        refreshStatistics();
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

    private void refreshStatistics() {
        if (todayCalls == null) return;
        Calendar c = Calendar.getInstance();
        String today = dateKey(c);
        c.add(Calendar.DAY_OF_YEAR, -1);
        String yesterday = dateKey(c);
        c.add(Calendar.DAY_OF_YEAR, -1);
        String dayBefore = dateKey(c);
        todayCalls.setText(String.valueOf(prefs.getInt("rejected_calls_" + today, 0)));
        todaySms.setText(String.valueOf(prefs.getInt("sent_sms_" + today, 0)));
        yesterdayCalls.setText(String.valueOf(prefs.getInt("rejected_calls_" + yesterday, 0)));
        yesterdaySms.setText(String.valueOf(prefs.getInt("sent_sms_" + yesterday, 0)));
        dayBeforeCalls.setText(String.valueOf(prefs.getInt("rejected_calls_" + dayBefore, 0)));
        dayBeforeSms.setText(String.valueOf(prefs.getInt("sent_sms_" + dayBefore, 0)));
    }

    private String dateKey(Calendar c) {
        return String.format(Locale.US, "%04d-%02d-%02d", c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH));
    }

    private void showTutorial() {
        new AlertDialog.Builder(this)
                .setTitle("मार्गदर्शक / Tutorial")
                .setMessage("1. आधी 'SET / CHECK CALL SCREENING APP' दाबून RECEIVED CALL FILTER ला Call Screening App म्हणून निवडा.\n\n" +
                        "2. 'MANAGE ALLOWED CONTACTS' मधून फक्त ज्या contacts चे कॉल घ्यायचे आहेत ते निवडा.\n\n" +
                        "3. Master Protection ON केल्यावर निवडलेल्या contacts व्यतिरिक्त येणारे कॉल कट होतील.\n\n" +
                        "4. Automatic SMS ON केल्यास कट झालेल्या कॉलवर तुमचा संदेश पाठवला जाईल.\n\n" +
                        "5. खालील आकडेवारीमध्ये आज, काल आणि परवा किती कॉल कट झाले व किती SMS पाठवले हे दिसेल.")
                .setPositiveButton("समजले", null)
                .show();
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
