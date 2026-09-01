package com.rahul.selectedcallfilter;

import android.content.SharedPreferences;
import android.telecom.Call;
import android.telecom.CallScreeningService;

/**
 * Allows only numbers explicitly stored in the app allow-list.
 * All other incoming calls are rejected immediately when the filter is ON.
 * We intentionally do NOT suppress notifications: rejected calls may remain
 * visible in the phone/call log according to the Vivo/Android Phone app.
 */
public class SelectedCallScreeningService extends CallScreeningService {
    @Override
    public void onScreenCall(Call.Details details) {
        SharedPreferences prefs = getSharedPreferences("filter", MODE_PRIVATE);

        if (!prefs.getBoolean("enabled", false)) {
            respondToCall(details, new CallResponse.Builder()
                    .setDisallowCall(false)
                    .build());
            return;
        }

        String raw = details.getHandle() == null
                ? ""
                : details.getHandle().getSchemeSpecificPart();
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
