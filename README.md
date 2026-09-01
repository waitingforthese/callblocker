# Selected Contacts Only — V5

V5 keeps the V4 instant-reject allow-list behavior and adds an optional automatic SMS after a rejected incoming call.

- Allowed contacts: ALLOW
- Other/unknown callers: instant REJECT
- Rejected-call notifications/call log are not suppressed by the screening response
- Optional automatic SMS, default message:
  “सध्या मी फोन घेऊ शकत नाही. ऑफिस मध्ये संपर्क करा.”
- SMS message can be edited in the app
- SMS is sent only when the SMS switch is ON and SEND_SMS permission is granted
- Airtel forwarding/network toggling is not used

Build with GitHub Actions using the included workflow.
