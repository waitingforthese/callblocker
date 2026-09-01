package com.rahul.selectedcallfilter;
import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
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
    private TextView status, screeningStatus, allowedCount, smsStatus;
    private LinearLayout listContainer;
    private Switch smsSwitch;
    private EditText smsMessage;
    private static final int SMS = 12;
    private static final String DEFAULT_SMS = "सध्या मी फोन घेऊ शकत नाही. ऑफिस मध्ये संपर्क करा.";

    @Override public void onCreate(Bundle b) {
        super.onCreate(b); setContentView(R.layout.activity_main);
        prefs = getSharedPreferences("filter", MODE_PRIVATE);
        master = findViewById(R.id.masterSwitch); status = findViewById(R.id.status); listContainer = findViewById(R.id.listContainer);
        smsSwitch = findViewById(R.id.smsSwitch);
        smsMessage = findViewById(R.id.smsMessage);
        screeningStatus = findViewById(R.id.screeningStatus);
        allowedCount = findViewById(R.id.allowedCount);
        smsStatus = findViewById(R.id.smsStatus);
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
            if (!key.isEmpty()) {
                prefs.edit().putBoolean("allow_"+key,true).putString("name_"+key,name == null || name.trim().isEmpty() ? "Contact" : name.trim()).apply();
                refresh();
                Toast.makeText(this, "✓ Contact added • Setting saved", Toast.LENGTH_SHORT).show();
            }
        }}
    }}
    private String normalize(String n){ String d=n==null?"":n.replaceAll("\\D",""); return d.length()>10?d.substring(d.length()-10):d; }
    private void refresh(){
        boolean on=prefs.getBoolean("enabled",false);
        status.setText(on ? "FILTER ON — only selected numbers are allowed" : "FILTER OFF");
        if (listContainer == null) return;
        listContainer.removeAllViews();
        boolean any=false; int count=0;
        Map<String,?> all=prefs.getAll();
        for(String k:all.keySet()) {
            if(k.startsWith("allow_") && Boolean.TRUE.equals(all.get(k))) {
                String n=k.substring(6);
                String name=String.valueOf(all.get("name_"+n));
                addContactRow(n, name);
                any=true; count++;
            }
        }
        if (!any) {
            TextView empty = new TextView(this);
            empty.setText("Allowed contacts:\nNo contacts selected yet.");
            empty.setTextColor(0xFF183B56);
            empty.setTextSize(14);
            empty.setPadding(dp(16), dp(14), dp(16), dp(14));
            listContainer.addView(empty);
        }
        if (allowedCount != null) allowedCount.setText(String.valueOf(count));
        if (smsStatus != null) smsStatus.setText(prefs.getBoolean("sms_enabled", false) ? "ON" : "OFF");
    }

    private void addContactRow(String number, String name) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);
        row.setPadding(dp(12), dp(10), dp(8), dp(10));
        row.setBackgroundResource(R.drawable.bg_contact_row);

        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams infoLp = new LinearLayout.LayoutParams(0, -2, 1f);
        infoLp.setMargins(0, 0, dp(6), 0);
        row.addView(info, infoLp);

        TextView tvName = new TextView(this);
        tvName.setText("👤  " + name);
        tvName.setTextColor(0xFF173B56);
        tvName.setTextSize(15);
        tvName.setTypeface(null, android.graphics.Typeface.BOLD);
        info.addView(tvName);

        TextView tvNumber = new TextView(this);
        tvNumber.setText("📞  " + number);
        tvNumber.setTextColor(0xFF5A6872);
        tvNumber.setTextSize(13);
        info.addView(tvNumber);

        Button edit = smallButton("EDIT");
        Button remove = smallButton("REMOVE");
        row.addView(edit);
        row.addView(remove);

        edit.setOnClickListener(v -> showEditContactDialog(number, name));
        remove.setOnClickListener(v -> confirmRemoveContact(number, name));
        listContainer.addView(row);
    }

    private Button smallButton(String text) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextSize(10);
        b.setTextColor(0xFF087ACC);
        b.setAllCaps(false);
        b.setMinWidth(0);
        b.setMinimumWidth(0);
        b.setPadding(dp(4), 0, dp(4), 0);
        b.setBackgroundResource(R.drawable.bg_outline_button);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(72), dp(40));
        lp.setMargins(dp(3), 0, dp(3), 0);
        b.setLayoutParams(lp);
        return b;
    }

    private void showEditContactDialog(String oldNumber, String oldName) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(22), dp(4), dp(22), 0);

        EditText name = new EditText(this);
        name.setHint("Contact name");
        name.setSingleLine(true);
        name.setText(oldName);
        box.addView(name);

        EditText number = new EditText(this);
        number.setHint("Mobile number");
        number.setInputType(android.text.InputType.TYPE_CLASS_PHONE);
        number.setSingleLine(true);
        number.setText(oldNumber);
        box.addView(number);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Edit Allowed Contact")
                .setView(box)
                .setNegativeButton("CANCEL", null)
                .setPositiveButton("SAVE", null)
                .create();
        dialog.setOnShowListener(x -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String newName = name.getText().toString().trim();
            String newNumber = normalize(number.getText().toString());
            if (newName.isEmpty()) newName = "Contact";
            if (newNumber.length() < 7) {
                number.setError("Enter a valid mobile number");
                return;
            }
            SharedPreferences.Editor e = prefs.edit();
            e.remove("allow_" + oldNumber).remove("name_" + oldNumber);
            e.putBoolean("allow_" + newNumber, true).putString("name_" + newNumber, newName).apply();
            dialog.dismiss();
            refresh();
            Toast.makeText(this, "✓ Contact updated • Setting saved", Toast.LENGTH_SHORT).show();
        }));
        dialog.show();
    }

    private void confirmRemoveContact(String number, String name) {
        new AlertDialog.Builder(this)
                .setTitle("Remove Allowed Contact?")
                .setMessage(name + "\n" + number + "\n\nहा संपर्क Allowed Contacts मधून काढायचा आहे का?")
                .setNegativeButton("CANCEL", null)
                .setPositiveButton("REMOVE", (d, w) -> {
                    prefs.edit().remove("allow_" + number).remove("name_" + number).apply();
                    refresh();
                    Toast.makeText(this, "✓ Contact removed • Setting saved", Toast.LENGTH_SHORT).show();
                }).show();
    }

    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }

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
