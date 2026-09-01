# Selected Contacts Only — Android APK project

## What it does
- Master ON/OFF switch.
- Select allowed people from the phone Contacts app.
- When ON, only numbers in the local allow-list are permitted through Android CallScreeningService.
- Other incoming calls are rejected and their normal blocked-call notification is suppressed.
- No internet permission is requested.

## Important limitation
This app cannot force Airtel to play the network-level "not reachable/switched off" announcement. It rejects the call at Android call-screening level. The exact announcement heard by the caller depends on the device/network.

## Installation / setup
1. Build and install the debug APK.
2. Open the app and grant Contacts permission.
3. Add the people you want to allow.
4. Tap `Set as Call Screening app` and grant the Android Call Screening role.
5. Turn `Allow only selected people` ON.
6. Test from an allowed number and a non-allowed number.

## Vivo note
Vivo may apply battery/background restrictions. If call screening stops working, set this app to unrestricted battery/background usage and allow it to run automatically where the Vivo model exposes those controls.
