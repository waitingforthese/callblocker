package com.rahul.selectedcallfilter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.InputType;
import android.graphics.Typeface;
import android.view.Gravity;
import android.widget.*;
import java.util.*;

public class AllowedContactsActivity extends Activity {
    private static final int PICK = 10;
    private SharedPreferences prefs;
    private LinearLayout listContainer;
    private TextView countText;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_allowed_contacts);
        prefs = getSharedPreferences("filter", MODE_PRIVATE);
        listContainer = findViewById(R.id.allowedList);
        countText = findViewById(R.id.contactCount);
        findViewById(R.id.addContact).setOnClickListener(v -> pickContact());
        findViewById(R.id.backButton).setOnClickListener(v -> finish());
        refresh();
    }

    @Override protected void onResume() { super.onResume(); refresh(); }

    private void pickContact() {
        Intent i = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        startActivityForResult(i, PICK);
    }

    @Override protected void onActivityResult(int r, int c, Intent d) {
        super.onActivityResult(r,c,d);
        if (r==PICK && c==RESULT_OK && d!=null) {
            Uri u=d.getData();
            String[] p={ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME};
            try (android.database.Cursor cur=getContentResolver().query(u,p,null,null,null)) {
                if(cur!=null && cur.moveToFirst()) {
                    String num=cur.getString(0), name=cur.getString(1);
                    saveContact(num, name);
                }
            }
        }
    }

    private void saveContact(String num, String name) {
        String key=normalize(num);
        if(key.isEmpty()) return;
        String safeName=(name==null || name.trim().isEmpty()) ? "Contact" : name.trim();
        prefs.edit().putBoolean("allow_"+key,true).putString("name_"+key,safeName).apply();
        refresh();
        Toast.makeText(this,"✓ Contact added • Setting saved",Toast.LENGTH_SHORT).show();
    }

    private String normalize(String n){ String d=n==null?"":n.replaceAll("\\D",""); return d.length()>10?d.substring(d.length()-10):d; }

    private void refresh() {
        listContainer.removeAllViews();
        int count=0;
        Map<String,?> all=prefs.getAll();
        for(String k:all.keySet()) {
            if(k.startsWith("allow_") && Boolean.TRUE.equals(all.get(k))) {
                String n=k.substring(6);
                String name=String.valueOf(all.get("name_"+n));
                addContactRow(n,name);
                count++;
            }
        }
        countText.setText(count + " Allowed Contact" + (count==1?"":"s"));
        if(count==0){
            TextView empty=new TextView(this);
            empty.setText("अजून कोणताही Allowed Contact निवडलेला नाही.\n\n+ Add Contact वरून contact निवडा.");
            empty.setTextColor(0xFF5A6872); empty.setTextSize(14); empty.setGravity(Gravity.CENTER);
            empty.setPadding(16,32,16,32); listContainer.addView(empty);
        }
    }

    private void addContactRow(String number,String name) {
        LinearLayout row=new LinearLayout(this); row.setOrientation(LinearLayout.HORIZONTAL); row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(12),dp(10),dp(8),dp(10)); row.setBackgroundResource(R.drawable.bg_contact_row);
        LinearLayout info=new LinearLayout(this); info.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams ilp=new LinearLayout.LayoutParams(0,-2,1f); ilp.setMargins(0,0,dp(4),0); row.addView(info,ilp);
        TextView n=new TextView(this); n.setText("👤  "+name); n.setTextColor(0xFF173B56); n.setTextSize(15); n.setTypeface(null,Typeface.BOLD); info.addView(n);
        TextView p=new TextView(this); p.setText("📞  "+number); p.setTextColor(0xFF5A6872); p.setTextSize(13); info.addView(p);
        Button edit=smallButton("EDIT"), remove=smallButton("REMOVE"); row.addView(edit); row.addView(remove);
        edit.setOnClickListener(v->showEdit(number,name));
        remove.setOnClickListener(v->confirmRemove(number,name));
        LinearLayout.LayoutParams rlp=new LinearLayout.LayoutParams(-1,-2); rlp.setMargins(0,0,0,dp(6)); listContainer.addView(row,rlp);
    }

    private Button smallButton(String text){ Button b=new Button(this); b.setText(text); b.setTextSize(10); b.setTextColor(0xFF087ACC); b.setAllCaps(false); b.setMinWidth(0); b.setMinimumWidth(0); b.setPadding(dp(4),0,dp(4),0); b.setBackgroundResource(R.drawable.bg_outline_button); b.setLayoutParams(new LinearLayout.LayoutParams(dp(72),dp(40))); return b; }

    private void showEdit(String oldNumber,String oldName){
        LinearLayout box=new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setPadding(dp(22),dp(4),dp(22),0);
        EditText name=new EditText(this); name.setHint("Contact name"); name.setSingleLine(true); name.setText(oldName); box.addView(name);
        EditText number=new EditText(this); number.setHint("Mobile number"); number.setInputType(InputType.TYPE_CLASS_PHONE); number.setSingleLine(true); number.setText(oldNumber); box.addView(number);
        AlertDialog dialog=new AlertDialog.Builder(this).setTitle("Edit Allowed Contact").setView(box).setNegativeButton("CANCEL",null).setPositiveButton("SAVE",null).create();
        dialog.setOnShowListener(x->dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{
            String nn=normalize(number.getText().toString()); String nm=name.getText().toString().trim();
            if(nn.isEmpty()){ number.setError("Mobile number required"); return; }
            prefs.edit().remove("allow_"+oldNumber).remove("name_"+oldNumber).putBoolean("allow_"+nn,true).putString("name_"+nn,nm.isEmpty()?"Contact":nm).apply();
            refresh(); dialog.dismiss(); Toast.makeText(this,"✓ Contact updated • Setting saved",Toast.LENGTH_SHORT).show();
        }));
        dialog.show();
    }

    private void confirmRemove(String number,String name){
        new AlertDialog.Builder(this).setTitle("Remove Allowed Contact?").setMessage(name+"\n"+number+"\n\nहा contact Allowed list मधून काढायचा आहे का?").setNegativeButton("CANCEL",null).setPositiveButton("REMOVE",(d,w)->{ prefs.edit().remove("allow_"+number).remove("name_"+number).apply(); refresh(); Toast.makeText(this,"✓ Contact removed • Setting saved",Toast.LENGTH_SHORT).show(); }).show();
    }
    private int dp(int n){return (int)(n*getResources().getDisplayMetrics().density+0.5f);}
}
