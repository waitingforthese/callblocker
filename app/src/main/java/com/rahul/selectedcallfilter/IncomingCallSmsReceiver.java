package com.rahul.selectedcallfilter;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.telephony.SmsManager;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Separate optional service: after an incoming call ends, send one fixed SMS
 * whether the user answered, rejected/cut the call, or did not answer it.
 * Outgoing calls are never handled here.
 */
public class IncomingCallSmsReceiver extends BroadcastReceiver {
    private static final String PREFS = "filter";
    private static final String DEFAULT_MESSAGE = "सध्या मी फोन घेऊ शकत नाही. ऑफिस मध्ये संपर्क करा.";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!"android.intent.action.PHONE_STATE".equals(intent.getAction())) return;

        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        if (!prefs.getBoolean("call_end_sms_enabled", false)) return;

        String state = intent.getStringExtra("state");
        if (state == null) return;

        if ("RINGING".equals(state)) {
            String incoming = intent.getStringExtra("incoming_number");
            if (incoming != null && !incoming.trim().isEmpty()) {
                prefs.edit()
                        .putString("pending_incoming_number", normalize(incoming))
                        .putLong("pending_incoming_started", System.currentTimeMillis())
                        .putBoolean("pending_incoming", true)
                        .apply();
            }
            return;
        }

        if (!"IDLE".equals(state)) return;

        // Incoming call ended: send the separate Call-End SMS unless the
        // rejected-call filter already sent its own filter SMS.
        if (prefs.getBoolean("pending_incoming", false)) {
            long started = prefs.getLong("pending_incoming_started", 0L);
            if (started <= 0L || System.currentTimeMillis() - started > 30 * 60 * 1000L) {
                clearPending(prefs);
            } else {
                String number = prefs.getString("pending_incoming_number", "");
                boolean alreadySentByRejectedFilter = prefs.getBoolean("pending_filter_sms_sent", false);
                clearPending(prefs);
                if (!number.isEmpty() && !alreadySentByRejectedFilter) {
                    sendSms(context, prefs, number, prefs.getString("call_end_sms_message", DEFAULT_MESSAGE));
                }
            }
            return;
        }

        // Outgoing call ended: the outgoing number is captured by the
        // CallScreeningService before the call starts. It is intentionally
        // handled separately from the filter SMS.
        if (prefs.getBoolean("pending_outgoing", false)) {
            long started = prefs.getLong("pending_outgoing_started", 0L);
            String number = prefs.getString("pending_outgoing_number", "");
            clearOutgoing(prefs);
            if (started > 0L && System.currentTimeMillis() - started <= 30 * 60 * 1000L
                    && !number.isEmpty()) {
                sendSms(context, prefs, number, prefs.getString("call_end_sms_message", DEFAULT_MESSAGE));
            }
        }
    }

    private void clearPending(SharedPreferences prefs) {
        prefs.edit()
                .remove("pending_incoming_number")
                .remove("pending_incoming_started")
                .remove("pending_filter_sms_sent")
                .putBoolean("pending_incoming", false)
                .apply();
    }

    private void clearOutgoing(SharedPreferences prefs) {
        prefs.edit()
                .remove("pending_outgoing_number")
                .remove("pending_outgoing_started")
                .putBoolean("pending_outgoing", false)
                .apply();
    }

    private void sendSms(Context context, SharedPreferences prefs, String number, String message) {
        if (context.checkSelfPermission(Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) return;
        try {
            SmsManager.getDefault().sendTextMessage(number, null, message, null, null);
            String day = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
            prefs.edit().putInt("call_end_sms_" + day,
                    prefs.getInt("call_end_sms_" + day, 0) + 1).apply();
        } catch (Exception ignored) { }
    }

    private String normalize(String value) {
        if (value == null) return "";
        String digits = value.replaceAll("\\D", "");
        if (digits.startsWith("00")) digits = digits.substring(2);
        if (digits.length() > 10) digits = digits.substring(digits.length() - 10);
        return digits;
    }
}
