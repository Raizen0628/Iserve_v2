package com.example.iserveko;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
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

    private TextView txtProfileName, txtProfileEmail, txtProfileAddress, txtProfileMobile;
    private EditText inputBio;
    private String residentEmail = "resident@iserveko.com";
    private boolean isOfficial = false;
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

        if (getIntent().hasExtra("EMAIL")) {
            residentEmail = getIntent().getStringExtra("EMAIL");
        } else {
            residentEmail = SessionManager.getEmail(this, "resident@iserveko.com");
        }
        isOfficial = getIntent().getBooleanExtra("IS_OFFICIAL", 
                SessionManager.isOfficial(this, false));

        txtProfileName = findViewById(R.id.txtProfileName);
        txtProfileEmail = findViewById(R.id.txtProfileEmail);
        txtProfileAddress = findViewById(R.id.txtProfileAddress);
        txtProfileMobile = findViewById(R.id.txtProfileMobile);
        txtProfileName.setOnClickListener(v -> showEditNameDialog());
        txtProfileAddress.setOnClickListener(v -> showEditProfileValueDialog(true));
        txtProfileMobile.setOnClickListener(v -> showEditProfileValueDialog(false));
        inputBio = findViewById(R.id.inputBio);
        String savedDisplayName = SessionManager.getDisplayName(this, "").trim();
        txtProfileName.setText(savedDisplayName);
        txtProfileEmail.setText(residentEmail);
        String savedAddress = SessionManager.getAddress(this, "");
        String savedMobile = SessionManager.getMobile(this, "");
        txtProfileAddress.setText(profileValueOrFallback(savedAddress));
        txtProfileMobile.setText(profileValueOrFallback(savedMobile));
        Log.d("PROFILE_SESSION", "Read session fields: address=" + savedAddress
                + ", mobile=" + savedMobile);

        findViewById(R.id.btnSaveBio).setOnClickListener(v -> saveBio());

        findViewById(R.id.btnChangePassword).setOnClickListener(v -> showChangePasswordDialog());

        findViewById(R.id.btnLogOut).setOnClickListener(v -> {
            SessionManager.clearSession(this);
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

    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserProfile();
    }

    private void loadUserProfile() {
        if (residentEmail == null || residentEmail.trim().isEmpty()) return;
        profileExecutor.execute(() -> {
            ProfileData result = loadCloudProfile();
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
            Log.e("PROFILE_LOAD", "Could not load the profile from SQL Server for " + residentEmail, e);
            return null;
        }
    }

    private void bindProfile(ProfileData profile) {
        if (profile != null) {
            String name = profile.name.isEmpty()
                    ? SessionManager.getDisplayName(this, "") : profile.name;
            String email = profile.email.isEmpty() ? residentEmail : profile.email;
            String address = profileValueOrFallback(profile.address);
            String mobile = profileValueOrFallback(profile.mobile);
            txtProfileName.setText(name);
            txtProfileEmail.setText(email);
            txtProfileAddress.setText(address);
            txtProfileMobile.setText(mobile);
            Log.d("PROFILE_LOAD", "SQL row for " + email + ": name=" + name
                    + ", address=" + profile.address + ", mobile=" + profile.mobile);
            SessionManager.saveUserSession(
                    this,
                    SessionManager.getUserId(this, ""),
                    name,
                    email,
                    profile.address,
                    profile.mobile,
                    isOfficial);
            android.util.Log.d("PROFILE_LOAD",
                    "Loaded profile for " + email + ", name=" + name);
        }

        String remoteBio = profile == null ? "" : safe(profile.bio);
        inputBio.setText(remoteBio.isEmpty() ? createGeneratedBio(profile) : remoteBio);
    }

    private void showEditProfileValueDialog(boolean addressField) {
        EditText valueInput = new EditText(this);
        valueInput.setSingleLine(true);
        valueInput.setInputType(addressField
                ? android.text.InputType.TYPE_CLASS_TEXT
                | android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
                : android.text.InputType.TYPE_CLASS_PHONE);
        TextView target = addressField ? txtProfileAddress : txtProfileMobile;
        String currentValue = target.getText().toString();
        valueInput.setText("Not set".equals(currentValue) ? "" : currentValue);

        new AlertDialog.Builder(this)
                .setTitle(addressField ? "Edit address" : "Edit mobile")
                .setView(valueInput)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton("Save", (dialog, which) ->
                        saveProfileValue(addressField, valueInput.getText().toString().trim()))
                .show();
    }

    private void saveProfileValue(boolean addressField, String value) {
        String column = addressField ? "address" : "mobile";
        profileExecutor.execute(() -> {
            boolean saved = false;
            try (Connection connection = connect_sql.getConnection();
                 PreparedStatement statement = connection == null ? null
                         : connection.prepareStatement(
                                 "UPDATE users SET " + column + " = ? WHERE email = ?")) {
                if (statement != null) {
                    statement.setString(1, value);
                    statement.setString(2, residentEmail);
                    saved = statement.executeUpdate() == 1;
                }
            } catch (Exception e) {
                Log.e("PROFILE_CONTACT_SAVE", "SQL Server " + column + " update failed.", e);
            }

            boolean savedOnServer = saved;
            mainHandler.post(() -> {
                if (isFinishing() || isDestroyed()) return;
                if (!savedOnServer) {
                    Toast.makeText(this, "Could not save the change.", Toast.LENGTH_SHORT).show();
                    return;
                }
                String address = addressField ? value : txtProfileAddress.getText().toString();
                String mobile = addressField ? txtProfileMobile.getText().toString() : value;
                if ("Not set".equals(address)) address = "";
                if ("Not set".equals(mobile)) mobile = "";
                (addressField ? txtProfileAddress : txtProfileMobile)
                        .setText(profileValueOrFallback(value));
                SessionManager.saveUserSession(
                        this,
                        SessionManager.getUserId(this, ""),
                        txtProfileName.getText().toString(),
                        residentEmail,
                        address,
                        mobile,
                        isOfficial);
                Toast.makeText(this, "Profile details saved.", Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void showEditNameDialog() {
        EditText nameInput = new EditText(this);
        nameInput.setSingleLine(true);
        nameInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT
                | android.text.InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        nameInput.setText(txtProfileName.getText());

        new AlertDialog.Builder(this)
                .setTitle("Edit name")
                .setView(nameInput)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton("Save", (dialog, which) ->
                        saveProfileName(nameInput.getText().toString().trim()))
                .show();
    }

    private void saveProfileName(String fullName) {
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length < 2 || fullName.trim().isEmpty()) {
            Toast.makeText(this, "Enter first and last name.", Toast.LENGTH_SHORT).show();
            return;
        }
        String firstName = parts[0];
        String lastName = parts[parts.length - 1];
        StringBuilder middleName = new StringBuilder();
        for (int index = 1; index < parts.length - 1; index++) {
            if (middleName.length() > 0) middleName.append(' ');
            middleName.append(parts[index]);
        }

        profileExecutor.execute(() -> {
            boolean saved = false;
            try (Connection connection = connect_sql.getConnection();
                 PreparedStatement statement = connection == null ? null
                         : connection.prepareStatement(
                                 "UPDATE users SET first_name = ?, middle_name = ?, last_name = ? WHERE email = ?")) {
                if (statement != null) {
                    statement.setString(1, firstName);
                    statement.setString(2, middleName.toString());
                    statement.setString(3, lastName);
                    statement.setString(4, residentEmail);
                    saved = statement.executeUpdate() == 1;
                }
            } catch (Exception e) {
                Log.e("PROFILE_NAME_SAVE", "SQL Server name update failed.", e);
            }

            boolean savedOnServer = saved;
            mainHandler.post(() -> {
                if (isFinishing() || isDestroyed()) return;
                if (savedOnServer) {
                    txtProfileName.setText(fullName);
                    SessionManager.saveUserSession(
                            this,
                            SessionManager.getUserId(this, ""),
                            fullName,
                            residentEmail,
                            txtProfileAddress.getText().toString(),
                            txtProfileMobile.getText().toString(),
                            isOfficial);
                    Toast.makeText(this, "Name saved.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Could not save the name.", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void saveBio() {
        String bio = inputBio.getText() == null ? "" : inputBio.getText().toString().trim();
        View saveButton = findViewById(R.id.btnSaveBio);
        saveButton.setEnabled(false);
        profileExecutor.execute(() -> {
            boolean cloudSaved = false;
            try (Connection connection = connect_sql.getConnection();
                 PreparedStatement statement = connection == null ? null
                         : connection.prepareStatement("UPDATE users SET bio = ? WHERE email = ?")) {
                if (statement != null) {
                    statement.setString(1, bio);
                    statement.setString(2, residentEmail);
                    cloudSaved = statement.executeUpdate() == 1;
                }
            } catch (Exception e) {
                Log.w("PROFILE_BIO_SAVE", "SQL Server bio update failed.", e);
            }

            final boolean savedOnServer = cloudSaved;
            mainHandler.post(() -> {
                if (isFinishing() || isDestroyed()) return;
                saveButton.setEnabled(true);
                Toast.makeText(this, savedOnServer
                        ? R.string.profile_bio_saved : R.string.profile_bio_save_error, Toast.LENGTH_SHORT).show();
                if (savedOnServer) inputBio.setText(bio);
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

    private String profileValueOrFallback(String value) {
        return value == null || value.trim().isEmpty() ? "Not set" : value.trim();
    }

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

    /** Holds user fields loaded from SQL Server. */
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
        int pad = Math.round(20 * getResources().getDisplayMetrics().density);
        layout.setPadding(pad, pad / 2, pad, 0);

        EditText newPassInput = new EditText(this);
        newPassInput.setHint("New Password");
        newPassInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(newPassInput);

        EditText confirmPassInput = new EditText(this);
        confirmPassInput.setHint("Confirm New Password");
        confirmPassInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(confirmPassInput);

        new AlertDialog.Builder(this)
                .setTitle("Change Password")
                .setView(layout)
                .setPositiveButton("Update", (dialog, which) -> {
                    String newPass = newPassInput.getText().toString();
                    String confirmPass = confirmPassInput.getText().toString();

                    if (newPass.isEmpty()) {
                        Toast.makeText(this, "Please enter a new password", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (newPass.length() < 8) {
                        Toast.makeText(this, "Password must be at least 8 characters long", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (!newPass.equals(confirmPass)) {
                        Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    updatePasswordOnServerAndLocal(newPass);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updatePasswordOnServerAndLocal(String newPassword) {
        profileExecutor.execute(() -> {
            boolean serverUpdated = false;
            try (Connection con = connect_sql.getConnection()) {
                if (con != null) {
                    try (PreparedStatement stmt = con.prepareStatement(
                            "UPDATE users SET password = ? WHERE email = ?")) {
                        stmt.setString(1, newPassword);
                        stmt.setString(2, residentEmail);
                        serverUpdated = stmt.executeUpdate() > 0;
                    }
                }
            } catch (Exception e) {
                Log.e("PASSWORD_UPDATE", "Failed to update password on SQL Server", e);
            }

            boolean localUpdated = false;
            try (DatabaseHelper dbHelper = new DatabaseHelper(this)) {
                localUpdated = dbHelper.updatePassword(residentEmail, newPassword);
            } catch (Exception ignored) {}

            final boolean success = serverUpdated || localUpdated;

            mainHandler.post(() -> {
                if (isFinishing() || isDestroyed()) return;
                if (success) {
                    Toast.makeText(this, "Password updated successfully!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Failed to update password. Please check connection.", Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        profileExecutor.shutdown();
    }

}

