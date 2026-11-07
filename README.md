# IServeKo

An Android mobile app for barangay services. IServeKo connects residents and barangay officials in one place, so document requests, complaints, equipment borrowing, meetings and announcements can be handled from a phone instead of in person or on paper.

## Features

### For residents
- **Account registration and login** with a saved session and profile management
- **Document requests** for Barangay Clearance, Certificates and Certificate of Good Moral Character, with status tracking
- **Complaints and reports** with a category, description and location, plus the official's response
- **Equipment borrowing** of barangay tables and chairs, with live availability
- **Meeting requests** with barangay officials
- **Announcements and calendar** with pinned announcements and event dates
- **Notifications** for request, borrowing and meeting updates
- **Emergency contacts** with one-tap calling (hotline, police, BFP Lipa, poison control)

### For barangay officials
- **Dashboard** with barangay service statistics
- **Announcement management**: post, edit, delete and pin
- **Document request management**: review, approve or reject, with remarks
- **Complaint and report management**: filter by status, update status and respond
- **Borrowing and inventory management**: approve requests, mark items returned or ready for pickup, update stock
- **Meeting management**: approve or decline requests
- **Resident directory**: view and manage resident profiles

## Tech stack

| Item | Details |
| --- | --- |
| Platform | Android 7.0 (API 24) and above |
| Language and UI | Java/Kotlin, Material Design |
| Database | Microsoft SQL Server (cloud-hosted) |
| Database driver | jTDS JDBC |
| User roles | Resident, Official |
| Permissions | Internet only |

## Getting started

### Requirements
- Android Studio (latest stable version)
- An Android device or emulator running Android 7.0 or higher
- A Microsoft SQL Server database that the app can reach

### Setup
1. Clone the repository:
   ```bash
   git clone https://github.com/YOUR-USERNAME/iserveko.git
   ```
2. Open the project in Android Studio and let Gradle sync.
3. Create the database tables by running the script in `database/schema.sql` on your SQL Server.
4. Add your own database host, name, username and password to your local configuration (see below). **Never commit these values.**
5. Run the app on a device or emulator.

### Database configuration
Keep your credentials out of the source code. A common approach is to put them in `local.properties`, which Android Studio already excludes from git:

```properties
DB_HOST=your-server-host
DB_NAME=your-database-name
DB_USER=your-username
DB_PASSWORD=your-password
```

Then read them through `BuildConfig` fields in `app/build.gradle`.

## Database tables

| Table | Purpose |
| --- | --- |
| `users` | Resident and official accounts and profiles |
| `announcements` | Barangay announcements, with pinning and event dates |
| `document_requests` | Clearance and certificate requests |
| `Complaints` | Resident complaints |
| `Reports` | Resident reports |
| `borrow_inventory` | Available tables and chairs |
| `borrow_requests` | Equipment borrowing requests |
| `meeting_requests` | Meeting requests with officials |

## Project structure

```
iserveko/
├── app/                  Android app source code
├── database/
│   └── schema.sql        Table definitions (no real data)
└── README.md
```

## Known limitations and planned improvements

- Passwords should be hashed instead of stored as plain text
- Photo attachments for complaints are not yet supported
- Password reset and email verification are not yet available
- Official accounts should require approval before activation

## Security notes

- Do not commit real resident data, database backups or credentials to this repository.
- Use test data only when sharing or demonstrating the app.

## License

Add your license here (for example, MIT), or state that the project is for academic use only.

## Developers

Rainner Gallo 
Roy Matthew L. Perilla  
Christan Angelo Parcon
Gabriel Jimenez
Patrick Llanes

