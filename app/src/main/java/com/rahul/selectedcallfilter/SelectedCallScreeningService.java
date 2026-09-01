package com.rahul.selectedcallfilter;

import android.telecom.Call;
import android.telecom.CallScreeningService;
import android.content.SharedPreferences;

public class SelectedCallScreeningService extends CallScreeningService {
    @Override public void onScreenCall(Call.Details details) {
        SharedPreferences p=getSharedPreferences("filter",MODE_PRIVATE);
        if(!p.getBoolean("enabled",false)){ allow(details); return; }
        String n=details.getHandle()==null?"":details.getHandle().getSchemeSpecificPart();
        String d=n.replaceAll("\\D",""); if(d.length()>10)d=d.substring(d.length()-10);
        boolean allowed=p.getBoolean("allow_"+d,false);
        if(allowed) allow(details); else block(details);
    }
    private void allow(Call.Details d){ respondToCall(d,new CallResponse.Builder().setDisallowCall(false).build()); }
    private void block(Call.Details d){ respondToCall(d,new CallResponse.Builder().setDisallowCall(true).setRejectCall(true).setSkipNotification(true).build()); }
}
