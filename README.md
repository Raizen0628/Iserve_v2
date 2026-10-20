# 🏛️ IServeKo (iServeKo)
> *Securing & Serving our local community*  
> *"Mas mabilis na serbisyo, mas maunlad na barangay."*

---

## 📱 About the Project
**IServeKo** is a comprehensive Android mobile application designed to bridge the gap between local residents and barangay officials. It streamlines community governance, service requests, equipment borrowing, emergency assistance, and announcements into a modern, user-friendly digital platform built with Material 3 design principles.

---

## ✨ Key Features

### 👥 1. Role-Based Access Control
* **Resident Portal:** Access essential barangay services, submit requests, view calendar schedules, and track personal notifications.
* **Barangay Official Portal:** Manage and review document/equipment requests, post announcements, and oversee community operations.

### 📝 2. Digital Document Requests
* Apply online for various barangay documents:
  * Barangay Clearance
  * Certificate of Residency
  * Certificate of Indigency
  * Barangay Business Clearance
  * Certificate of Good Moral Character
  * First-Time Job Seeker Certification
* Track request status (*Pending*, *Approved*, *Ready for Pickup*, *Declined*) with official remarks.

### 🪑 3. Equipment Borrowing (Tables & Chairs)
* Real-time inventory stock counter for tables and chairs.
* **Privacy Protected:** Borrow requests are strictly private—only the resident who made the request and authorized officials can view them.

### 📅 4. Meeting Requests & Event Calendar
* **Meeting Scheduler:** Residents can request scheduled meetings with barangay officials by specifying date, time, and agenda/purpose.
* **Unified Event Calendar:** Automatically displays approved meetings, equipment borrowing schedules, and barangay announcements in chronological order.

### 📢 5. Announcement Bulletin
* Stay up-to-date with official community advisories, clean-up drives, health center schedules, and emergency notices.
* Officials can post, pin, edit, and delete announcements.

### 🚨 6. Emergency & Community Services
* Quick dialer access for Emergency Hotlines (911), Barangay Tanod, Ambulance/Medical Services, Barangay Health Center, and the Barangay Hall.

---

## 🛠️ Tech Stack & Architecture
* **Language:** Java
* **UI Framework:** Android Views, Material Design 3 (M3), CoordinatorLayout, RecyclerView
* **Local Database:** SQLite (`DatabaseHelper`)
* **Remote Database Support:** MS SQL Server (SSMS) via JDBC / connection utilities (`connect_sql`)
* **Networking:** Volley Library

---

## 📂 Project Structure
```text
com.example.iserveko/
├── MainActivity.java                # Authentication & Login handler
├── ResidentDashboardActivity.java   # Resident main dashboard
├── OfficialDashboardActivity.java   # Barangay Official main dashboard
├── BorrowResidentActivity.java      # Table & Chair equipment borrowing
├── BorrowOfficialActivity.java      # Official management for equipment
├── DocumentRequestActivity.java     # Digital document applications
├── DocumentOfficialActivity.java    # Official review for document requests
├── MeetingRequestActivity.java      # Meeting scheduling & requests
├── CalendarActivity.java            # Unified event & schedule calendar
├── AnnouncementResidentActivity.java# Community bulletin
├── AnnouncementOfficialActivity.java# Official announcement management
├── NotificationsActivity.java       # Personal status notifications
├── profile.java                     # User profile & password management
├── Service.java                     # Emergency & hotline services
└── DatabaseHelper.java              # Local SQLite database & schema management
```

---

## 🚀 Getting Started

1. **Clone the Repository:**
   ```bash
   git clone https://github.com/your-username/IServeKo.git
   ```
2. **Open in Android Studio:**
   - Open Android Studio and select *Open an Existing Project*.
   - Navigate to the cloned `Iserve_v2` directory.
3. **Build & Run:**
   - Sync project with Gradle files.
   - Run the app on an Android Emulator or physical device (API level 24+ recommended).

---

## 📄 License
This project is developed for local community service and academic/institutional purposes.
