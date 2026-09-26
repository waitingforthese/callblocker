RECEIVED CALL FILTER - V3 Incoming Reject + SMS Fix

Changes:
1. Outgoing/non-incoming calls are immediately allowed and NEVER trigger auto-SMS.
2. Blocked incoming calls call respondToCall() with setDisallowCall(true) and setRejectCall(true) as the FIRST action in the blocked branch.
3. Notification and call-log suppression remain disabled.
4. Statistics and SMS are processed only after the reject response is sent.
5. Auto-SMS is sent only for a blocked incoming number, when SMS is enabled and SEND_SMS permission is granted.
6. Existing package, UI, allow-list, statistics and settings are preserved.

Important Android/Vivo limitation:
CallScreeningService can reject/disallow the call, but Android does not provide an API that guarantees the caller will hear zero ringback tones or a carrier-level "not reachable" announcement. A short ringback may occur before the telecom/network path completes the screening decision.

Testing:
- Set this app as the system Call Screening app.
- Turn Protection ON.
- Turn Auto SMS ON and grant SMS permission.
- Call from an allowed number: phone should ring normally; no auto-SMS.
- Call from a blocked number: app immediately rejects; auto-SMS should be attempted.
- Make an outgoing call: no auto-SMS and no filtering.
