# UI update

- Forest-green primary actions, warm backgrounds, white Material surfaces, and distinct blue dry-waste indicators.
- Pickup overview uses the localized schedule and a prominent accessible QR button. Removed the unsupported live-truck claim.
- Citizen tabs retain saveable state; report drafts and login form entries survive recreation.
- Login respects keyboard and system navigation insets and constrains content width on wider devices.
- Header language and role controls have at least 48 dp height.

Validation: source reviewed. Compilation was attempted using the installed Gradle 9.2 distribution, but native-platform.dll failed to load before project configuration. The project requests Gradle 9.3.1 and does not include wrapper scripts or the wrapper JAR. No APK was rebuilt or emulator verification performed.

Follow-up device checks: phone and tablet widths, large system font, all five languages, rotation while entering forms, tab switching with a report draft, and QR display.
