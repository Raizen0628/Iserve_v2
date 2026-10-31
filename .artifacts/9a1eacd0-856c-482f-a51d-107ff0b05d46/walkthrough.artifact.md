# Walkthrough: Emergency & Community Services Page

I have implemented the "Emergency & Community Services" page for the iServeKo app, featuring a modern design with clickable cards that trigger phone calls for essential services.

## Changes Made

### 1. UI Implementation
- **[activity_service.xml](file:///C:/Users/raiji/AndroidStudioProjects/IServeKo/app/src/main/res/layout/activity_service.xml)**: Created a modern, clean layout using:
    - `CoordinatorLayout` and `AppBarLayout` for a professional header.
    - Teal-colored back button and header title "Services".
    - `NestedScrollView` containing a list of `MaterialCardView` items.
    - Each card uses specific tinted backgrounds and emojis in circular containers to match the dashboard's visual language.

### 2. Logic & Functionality
- **[Service.java](file:///C:/Users/raiji/AndroidStudioProjects/IServeKo/app/src/main/java/com/example/iserveko/Service.java)**:
    - Emergency contacts now come from the central `EmergencyContacts` directory. Cards open the dialer, open SMS, or copy a number with a long press.
    - Configured the back button to close the activity.
    - Handled `BottomNavigationView` navigation logic to return to the dashboard.
- **[ResidentDashboardActivity.java](file:///C:/Users/raiji/AndroidStudioProjects/IServeKo/app/src/main/java/com/example/iserveko/ResidentDashboardActivity.java)**: Connected the "Emergency Hotline" dashboard card and the bottom navigation "Services" item to the new page.
- **[OfficialDashboardActivity.java](file:///C:/Users/raiji/AndroidStudioProjects/IServeKo/app/src/main/java/com/example/iserveko/OfficialDashboardActivity.java)**: Connected the bottom navigation "Services" item to the new page.

### 3. Emergency Contact Directory
Mobile contact data is centralized in `EmergencyContacts.java`. Contacts without a verified mobile number display a placeholder. The national emergency short code remains a separate exception.

## Verification
- Verified that all service cards are present and styled correctly according to the requested modern/teal theme.
- Verified that each card triggers the system dialer with the correct phone number.
- Verified navigation from both resident and official dashboards.
