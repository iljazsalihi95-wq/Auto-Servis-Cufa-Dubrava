# CUFA Workshop Android

Native Android app architecture for Auto Servis Cufa – Dubrava.

Modules planned:
- Admin login
- Terminet / applications
- Customers
- Vehicles and vehicle history
- AI diagnostics
- OBD-II Bluetooth live diagnostics
- DTC storage/history

## OBD architecture
The app uses an adapter abstraction so the final physical OBD device can be selected later without rewriting the workshop UI.

Safety: OBD functions start read-only (connection, VIN where supported, DTC read, live PIDs). Destructive/service commands are not enabled by default.
