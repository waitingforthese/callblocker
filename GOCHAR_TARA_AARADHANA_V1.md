# LifeAlarm – Gochar Tara Aaradhana V1

Built from the locked V14.2.2.4 BASELINE.

## Feature
For all 9 Grahas, compare the current transit Nakshatra with the saved birth Nakshatra using the 27-Nakshatra Tara Bala cycle. The user can independently enable Vipat, Pratyari and Vadha (Naidhana) Tara triggers.

## Settings
Each Graha has:
- ON/OFF
- Vipat checkbox
- Pratyari checkbox
- Vadha checkbox
- Start Date (YYYY-MM-DD)
- End Date (YYYY-MM-DD)
- Save button

## Trigger
Only entry into a selected Tara Nakshatra triggers the event. If a planet is already inside a selected Tara Nakshatra, the app does not immediately repeat the Aaradhana; it waits for the next entry.

When the event fires:
- High-priority notification is shown.
- The Graha-specific mantra is spoken using the existing Aaradhana voice/Japa engine.
- Existing Master Alarm gate is respected.
- The next matching transit entry is then scheduled.

Stable alarm IDs: 401..409 (one per Graha).

## Tara mapping
Relative to the birth Nakshatra:
1 Janma, 2 Sampat, 3 Vipat, 4 Kshema, 5 Pratyari, 6 Sadhaka, 7 Vadha/Naidhana, 8 Mitra, 9 Ati-Mitra, repeating across 27 Nakshatras.

## Preservation
Existing Kundli, Live Transit single-source fix, Freeze Pane, Complete Gochar Analysis, notification engine, Special Aaradhana, change-triggered Nakshatra/Yoga/Karana Aaradhana, Master Alarm and User Management are preserved.
