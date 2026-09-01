package com.rahul.selectedcallfilterv6v6;

import android.content.SharedPreferences;
import android.telecom.Call;
import android.telecom.CallScreeningService;
import android.telephony.SmsManager;
import android.Manifest;
import android.content.pm.PackageManager;

/**
 * Allows only numbers explicitly stored in the app allow-list.
 * All other incoming calls are rejected immediately when the filter is ON.
 * We intentionally do NOT suppress notifications: rejected calls may remain
 * visible in the phone/call log according to the Vivo/Android Phone app.
 */
public class SelectedCallScreeningService extends CallScreeningService {
    private static final String PREFS = "filter";
    private static final String ENABLED = "enabled";
    private static final String SMS_ENABLED = "sms_enabled";
    private static final String DEFAULT_SMS = "सध्या मी फोन घेऊ शकत नाही. ऑफिस मध्ये संपर्क करा.";
    @Override
    public void onScreenCall(Call.Details details) {
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);

        if (!prefs.getBoolean("enabled", false)) {
            respondToCall(details, new CallResponse.Builder()
                    .setDisallowCall(false)
                    .build());
            return;
        }

        String raw = details.getHandle() != null
                && "tel".equalsIgnoreCase(details.getHandle().getScheme())
                ? details.getHandle().getSchemeSpecificPart()
                : "";
        String number = normalize(raw);

        // Empty/unknown number is not in the allow-list, so reject it.
        boolean allowed = !number.isEmpty()
                && prefs.getBoolean("allow_" + number, false);

        if (allowed) {
            respondToCall(details, new CallResponse.Builder()
                    .setDisallowCall(false)
                    .build());
        } else {
            // Instant reject. Do NOT use setSkipNotification(true).
            respondToCall(details, new CallResponse.Builder()
                    .setDisallowCall(true)
                    .setRejectCall(true)
                    .build());
            // Send the configured SMS only after a rejected call.
            if (!number.isEmpty() && prefs.getBoolean(SMS_ENABLED, false)
                    && checkSelfPermission(Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                String message = prefs.getString("sms_message", DEFAULT_SMS);
                try {
                    SmsManager.getDefault().sendTextMessage(number, null, message, null, null);
                } catch (Exception ignored) { }
            }
        }
    }

    private String normalize(String value) {
        if (value == null) return "";
        String digits = value.replaceAll("\\D", "");
        if (digits.startsWith("00")) digits = digits.substring(2);
        // Indian mobile numbers: compare using the final 10 digits.
        if (digits.length() > 10) digits = digits.substring(digits.length() - 10);
        return digits;
    }
}
