package com.example.iserveko;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class profile extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private TextView txtProfileName, txtProfileEmail, txtProfileAddress, txtProfileMobile;
    private EditText inputBio;
    private String residentEmail = "resident@iserveko.com";
    private boolean isOfficial = false;
    private SharedPreferences profilePrefs;
    private final ExecutorService profileExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        databaseHelper = new DatabaseHelper(this);

        if (getIntent().hasExtra("EMAIL")) {
            residentEmail = getIntent().getStringExtra("EMAIL");
        } else {
            residentEmail = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
                    .getString("logged_in_email", "resident@iserveko.com");
        }
        isOfficial = getIntent().getBooleanExtra("IS_OFFICIAL", 
                getSharedPreferences("UserSession", Context.MODE_PRIVATE).getBoolean("is_official", false));

        txtProfileName = findViewById(R.id.txtProfileName);
        txtProfileEmail = findViewById(R.id.txtProfileEmail);
        txtProfileAddress = findViewById(R.id.txtProfileAddress);
        txtProfileMobile = findViewById(R.id.txtProfileMobile);
        inputBio = findViewById(R.id.inputBio);

        txtProfileEmail.setText(residentEmail);

        profilePrefs = getSharedPreferences("UserProfilePrefs", Context.MODE_PRIVATE);
        findViewById(R.id.btnSaveBio).setOnClickListener(v -> saveBio());

        findViewById(R.id.btnChangePassword).setOnClickListener(v -> showChangePasswordDialog());

        findViewById(R.id.btnLogOut).setOnClickListener(v -> {
            getSharedPreferences("UserSession", Context.MODE_PRIVATE).edit().clear().apply();
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setOnItemSelectedListener(null);
        bottomNavigationView.setSelectedItemId(R.id.nav_profile);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_profile) {
                return true;
            }
            Class<?> targetClass = null;
            if (itemId == R.id.nav_home) {
                targetClass = isOfficial ? OfficialDashboardActivity.class : ResidentDashboardActivity.class;
            } else if (itemId == R.id.nav_announcements) {
                targetClass = isOfficial ? AnnouncementOfficialActivity.class : AnnouncementResidentActivity.class;
            } else if (itemId == R.id.nav_services) {
                targetClass = Service.class;
            } else if (itemId == R.id.nav_notifications) {
                targetClass = NotificationsActivity.class;
            }

            if (targetClass != null) {
                Intent intent = new Intent(this, targetClass);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                intent.putExtra("EMAIL", residentEmail);
                if (isOfficial) intent.putExtra("IS_OFFICIAL", true);
                startActivity(intent);
                return true;
            }
            return false;
        });

        loadUserProfile();
    }

    private void loadUserProfile() {
        if (residentEmail == null || residentEmail.trim().isEmpty()) return;
        profileExecutor.execute(() -> {
            ProfileData profile = loadCloudProfile();
            String[] local = databaseHelper.getUserProfileDetails(residentEmail);
            if (profile == null && local != null) {
                profile = new ProfileData(residentEmail, local[0], local[1], local[2],
                        local[3], local[4], local[5]);
            } else if (profile != null && (profile.bio == null || profile.bio.trim().isEmpty())
                    && local != null && local[5] != null && !local[5].trim().isEmpty()) {
                profile.bio = local[5];
            }

            ProfileData result = profile;
            mainHandler.post(() -> {
                if (isFinishing() || isDestroyed()) return;
                bindProfile(result);
            });
        });
    }

    private ProfileData loadCloudProfile() {
        try (Connection connection = connect_sql.getConnection()) {
            if (connection == null) return null;
            ProfileData profile;
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT first_name, middle_name, last_name, email, address, mobile, role, position FROM users WHERE email = ?")) {
                statement.setString(1, residentEmail);
                try (ResultSet result = statement.executeQuery()) {
                    if (!result.next()) return null;
                    profile = new ProfileData(
                            safe(result.getString("email")),
                            joinName(result.getString("first_name"), result.getString("middle_name"),
                                    result.getString("last_name")),
                            safe(result.getString("address")),
                            safe(result.getString("mobile")),
                            safe(result.getString("role")),
                            safe(result.getString("position")),
                            "");
                }
            }
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT bio FROM users WHERE email = ?")) {
                statement.setString(1, residentEmail);
                try (ResultSet result = statement.executeQuery()) {
                    if (result.next()) profile.bio = safe(result.getString("bio"));
                }
            } catch (Exception missingBioColumn) {
                Log.i("PROFILE_BIO", "Cloud bio is unavailable until the users bio column is added.");
            }
            return profile;
        } catch (Exception e) {
            Log.w("PROFILE_LOAD", "Could not load the cloud profile; using the local profile.");
            return null;
        }
    }

    private void bindProfile(ProfileData profile) {
        if (profile != null) {
            txtProfileName.setText(profile.name.isEmpty() ? residentEmail : profile.name);
            txtProfileEmail.setText(profile.email.isEmpty() ? residentEmail : profile.email);
            if (!profile.address.isEmpty()) txtProfileAddress.setText(profile.address);
            if (!profile.mobile.isEmpty()) txtProfileMobile.setText(profile.mobile);
        }

        String remoteBio = profile == null ? "" : safe(profile.bio);
        String storedBio = profilePrefs.getString(bioKey(), null);
        boolean manuallyEdited = profilePrefs.getBoolean(bioEditedKey(), false)
                || (storedBio != null && !storedBio.trim().isEmpty());
        String value;
        if (manuallyEdited) {
            value = storedBio == null ? "" : storedBio;
            profilePrefs.edit().putBoolean(bioEditedKey(), true).apply();
        } else if (!remoteBio.trim().isEmpty()) {
            value = remoteBio;
        } else {
            value = createGeneratedBio(profile);
        }
        inputBio.setText(value);
    }

    private void saveBio() {
        String bio = inputBio.getText() == null ? "" : inputBio.getText().toString().trim();
        profilePrefs.edit().putString(bioKey(), bio).putBoolean(bioEditedKey(), true).apply();
        profileExecutor.execute(() -> {
            boolean localSaved = databaseHelper.updateUserBio(residentEmail, bio);
            boolean cloudSaved = false;
            try (Connection connection = connect_sql.getConnection();
                 PreparedStatement statement = connection == null ? null
                         : connection.prepareStatement("UPDATE users SET bio = ? WHERE email = ?")) {
                if (statement != null) {
                    statement.setString(1, bio);
                    statement.setString(2, residentEmail);
                    cloudSaved = statement.executeUpdate() > 0;
                }
            } catch (Exception e) {
                Log.w("PROFILE_BIO_SAVE", "Cloud bio save failed; the local bio remains saved.");
            }

            boolean savedOnCloud = cloudSaved;
            mainHandler.post(() -> {
                if (isFinishing() || isDestroyed()) return;
                int message = savedOnCloud ? R.string.profile_bio_saved
                        : localSaved ? R.string.profile_bio_saved_offline : R.string.profile_bio_save_error;
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            });
        });
    }

    private String createGeneratedBio(ProfileData profile) {
        String role = profile == null ? "" : profile.role;
        String address = profile == null ? "" : profile.address;
        String position = profile == null ? "" : profile.position;
        if (role.toLowerCase(java.util.Locale.ROOT).contains("official")) {
            return position.isEmpty() ? getString(R.string.profile_bio_official)
                    : getString(R.string.profile_bio_official_position, position);
        }
        if (!address.isEmpty()) {
            if (address.toLowerCase(java.util.Locale.ROOT).contains("lipa city")) {
                return getString(R.string.profile_bio_resident_address, address);
            }
            return getString(R.string.profile_bio_resident_address_city, address);
        }
        return getString(R.string.profile_bio_resident_default);
    }

    private String bioKey() { return "bio_" + residentEmail; }
    private String bioEditedKey() { return "bio_edited_" + residentEmail; }

    private String joinName(String first, String middle, String last) {
        StringBuilder name = new StringBuilder();
        for (String part : new String[]{first, middle, last}) {
            if (part != null && !part.trim().isEmpty()) {
                if (name.length() > 0) name.append(' ');
                name.append(part.trim());
            }
        }
        return name.toString();
    }

    private String safe(String value) { return value == null ? "" : value.trim(); }

    /** Holds user fields loaded from MSSQL or the local profile cache. */
    private static class ProfileData {
        final String email;
        final String name;
        final String address;
        final String mobile;
        final String role;
        final String position;
        String bio;

        ProfileData(String email, String name, String address, String mobile,
                    String role, String position, String bio) {
            this.email = email;
            this.name = name;
            this.address = address;
            this.mobile = mobile;
            this.role = role;
            this.position = position;
            this.bio = bio;
        }
    }

    private void showChangePasswordDialog() {
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(48, 32, 48, 32);

        final EditText inputNewPass = new EditText(this);
        inputNewPass.setHint("Enter New Password");
        inputNewPass.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD | android.text.InputType.TYPE_CLASS_TEXT);
        layout.addView(inputNewPass);

        final EditText inputConfirmPass = new EditText(this);
        inputConfirmPass.setHint("Confirm New Password");
        inputConfirmPass.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD | android.text.InputType.TYPE_CLASS_TEXT);
        layout.addView(inputConfirmPass);

        new AlertDialog.Builder(this)
                .setTitle("Change Password")
                .setView(layout)
                .setPositiveButton("Update", (dialog, which) -> {
                    String newPass = inputNewPass.getText().toString().trim();
                    String confirmPass = inputConfirmPass.getText().toString().trim();

                    if (newPass.isEmpty() || confirmPass.isEmpty()) {
                        Toast.makeText(this, "Please enter all fields.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (!newPass.equals(confirmPass)) {
                        Toast.makeText(this, "Passwords do not match.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    databaseHelper.updatePassword(residentEmail, newPass);
                    updatePasswordOnMSSQL(residentEmail, newPass);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        profileExecutor.shutdown();
    }

    private void updatePasswordOnMSSQL(String email, String newPass) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                Connection con = connect_sql.getConnection();
                if (con != null) {
                    try (java.sql.PreparedStatement ps = con.prepareStatement("UPDATE users SET password = ? WHERE email = ?")) {
                        ps.setString(1, newPass);
                        ps.setString(2, email);
                        ps.executeUpdate();
                    }
                }
            } catch (Exception e) {
                Log.e("MSSQL_PASS_ERR", e.getMessage(), e);
            }
            handler.post(() -> Toast.makeText(profile.this, "Password updated successfully!", Toast.LENGTH_SHORT).show());
        });
    }

}

