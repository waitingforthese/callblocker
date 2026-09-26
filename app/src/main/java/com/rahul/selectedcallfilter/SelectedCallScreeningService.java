package com.rahul.selectedcallfilter;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.telecom.Call;
import android.telecom.CallScreeningService;
import android.telephony.SmsManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Incoming-call screening service.
 *
 * IMPORTANT:
 * Android's CallScreeningService can reject/disallow an incoming call,
 * but it cannot guarantee a carrier/network-level "0 ring / not reachable"
 * result for the caller. The response below is sent immediately; no
 * database/network work is done before the reject response.
 */
public class SelectedCallScreeningService extends CallScreeningService {
    private static final String PREFS = "filter";
    private static final String SMS_ENABLED = "sms_enabled";
    private static final String DEFAULT_SMS =
            "सध्या मी फोन घेऊ शकत नाही. ऑफिस मध्ये संपर्क करा.";

    @Override
    public void onScreenCall(Call.Details details) {
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);

        // NEVER filter or send SMS for outgoing/non-incoming calls.
        if (details.getCallDirection() != Call.Details.DIRECTION_INCOMING) {
            respondToCall(details, new CallResponse.Builder()
                    .setDisallowCall(false)
                    .setRejectCall(false)
                    .build());
            return;
        }

        // Protection OFF -> allow immediately.
        if (!prefs.getBoolean("enabled", false)) {
            respondToCall(details, new CallResponse.Builder()
                    .setDisallowCall(false)
                    .build());
            return;
        }

        String raw = "";
        if (details.getHandle() != null
                && "tel".equalsIgnoreCase(details.getHandle().getScheme())) {
            raw = details.getHandle().getSchemeSpecificPart();
        }

        final String number = normalize(raw);
        final boolean allowed = !number.isEmpty()
                && prefs.getBoolean("allow_" + number, false);

        if (allowed) {
            // Allowed contact: immediately allow.
            respondToCall(details, new CallResponse.Builder()
                    .setDisallowCall(false)
                    .build());
            return;
        }

        /*
         * BLOCK FIRST.
         *
         * This is deliberately the first action in the blocked branch.
         * Statistics and SMS are performed only after Android has received
         * the reject response, so they cannot delay the screening decision.
         */
        respondToCall(details, new CallResponse.Builder()
                .setDisallowCall(true)
                .setRejectCall(true)
                .setSkipNotification(false)
                .setSkipCallLog(false)
                .build());

        final String day = new SimpleDateFormat(
                "yyyy-MM-dd", Locale.US).format(new Date());

        prefs.edit()
                .putInt("rejected_calls_" + day,
                        prefs.getInt("rejected_calls_" + day, 0) + 1)
                .apply();

        // Auto-SMS is ONLY for a rejected incoming call.
        if (!number.isEmpty()
                && prefs.getBoolean(SMS_ENABLED, false)
                && checkSelfPermission(Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED) {

            final String message = prefs.getString(
                    "sms_message", DEFAULT_SMS);

            try {
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(
                        number, null, message, null, null);

                prefs.edit()
                        .putInt("sent_sms_" + day,
                                prefs.getInt("sent_sms_" + day, 0) + 1)
                        .apply();
            } catch (Exception ignored) {
                // SMS failure must never prevent the call from being rejected.
            }
        }
    }

    private String normalize(String value) {
        if (value == null) return "";

        String digits = value.replaceAll("\\D", "");

        if (digits.startsWith("00")) {
            digits = digits.substring(2);
        }

        // Indian mobile comparison: use the final 10 digits.
        if (digits.length() > 10) {
            digits = digits.substring(digits.length() - 10);
        }

        return digits;
    }
}
