# Implementation Plan: Announcement Pages (Resident & Barangay Official Views)

This plan outlines the steps to build the Announcement feature for both Residents and Barangay Officials in the iServeKo app, including the request to update the bottom navigation bar announcement icon to a horn/megaphone symbol.

## Proposed Changes

### Vector Graphics

#### [MODIFY] [ic_announcements.xml](file:///C:/Users/raiji/AndroidStudioProjects/IServeKo/app/src/main/res/drawable/ic_announcements.xml)
- Replace the current bell vector path with an announcement horn/bullhorn icon path.

### UI Layouts

#### [NEW] [activity_announcement_resident.xml](file:///C:/Users/raiji/AndroidStudioProjects/IServeKo/app/src/main/res/layout/activity_announcement_resident.xml)
- Create layout with a header labeled "Announcements".
- Add an elegant Material 3 Search Bar component.
- Add a dedicated full-width "Pinned Announcement" card at the top.
- Add a scrollable list (or structured layout) of typical neighborhood announcement cards containing an icon, title, date, and brief excerpt description.
- Include the standard `BottomNavigationView` with Home, Announcements, Notifications, and Profile tabs.

#### [NEW] [activity_announcement_official.xml](file:///C:/Users/raiji/AndroidStudioProjects/IServeKo/app/src/main/res/layout/activity_announcement_official.xml)
- Create layout with a header labeled "Manage Announcements".
- Add a large, prominent full-width "+ Create Announcement" Button or Extended FAB card.
- Add announcement list items styled symmetrically to the resident page but including quick-action icons for **Edit** and **Delete**.
- Include the matching bottom navigation bar.

### Backend Activities

#### [NEW] [AnnouncementResidentActivity.java](file:///C:/Users/raiji/AndroidStudioProjects/IServeKo/app/src/main/java/com/example/iserveko/AnnouncementResidentActivity.java)
- Main activity class for the resident view managing content display, layout view bindings, and enabling navigation bar items.

#### [NEW] [AnnouncementOfficialActivity.java](file:///C:/Users/raiji/AndroidStudioProjects/IServeKo/app/src/main/java/com/example/iserveko/AnnouncementOfficialActivity.java)
- Main activity class for the official management view enabling administrative actions.

### Integration & Setup

#### [MODIFY] [AndroidManifest.xml](file:///C:/Users/raiji/AndroidStudioProjects/IServeKo/app/src/main/AndroidManifest.xml)
- Register `AnnouncementResidentActivity` and `AnnouncementOfficialActivity`.

#### [MODIFY] [ResidentDashboardActivity.java](file:///C:/Users/raiji/AndroidStudioProjects/IServeKo/app/src/main/java/com/example/iserveko/ResidentDashboardActivity.java)
- Wire up the bottom navigation listener and the announcement dashboard card to launch `AnnouncementResidentActivity`.

#### [MODIFY] [OfficialDashboardActivity.java](file:///C:/Users/raiji/AndroidStudioProjects/IServeKo/app/src/main/java/com/example/iserveko/OfficialDashboardActivity.java)
- Wire up the bottom navigation listener and the "Manage Announcements" dashboard card to launch `AnnouncementOfficialActivity`.

## Verification Plan

### Automated Tests
- Gradle project compile/build checks (`gradle_build` task).

### Manual Verification
- Deploy to device/emulator to visually inspect matching header alignment, scroll layout properties, search field input capability, edit/delete touch anchors, and verification of the brand new navigation horn logo.
