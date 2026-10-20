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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class profile extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private TextView txtProfileName, txtProfileEmail, txtProfileAddress, txtProfileMobile;
    private EditText inputBio;
    private String residentEmail = "resident@iserveko.com";
    private boolean isOfficial = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

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

        SharedPreferences prefs = getSharedPreferences("UserProfilePrefs", Context.MODE_PRIVATE);
        inputBio.setText(prefs.getString("bio_" + residentEmail, ""));

        findViewById(R.id.btnSaveBio).setOnClickListener(v -> {
            String bio = inputBio.getText().toString().trim();
            prefs.edit().putString("bio_" + residentEmail, bio).apply();
            Toast.makeText(this, "Bio saved successfully!", Toast.LENGTH_SHORT).show();
        });

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
        if (residentEmail == null || residentEmail.isEmpty()) return;

        String userQuery = "SELECT first_name, middle_name, last_name, email, address, mobile FROM users WHERE email = '" + residentEmail.replace("'", "''") + "'";
        connect_sql.executeQueryAsync(userQuery, new connect_sql.QueryCallback() {
            @Override
            public void onSuccess(ResultSet rs) {
                try {
                    if (rs != null && rs.next()) {
                        String fn = rs.getString("first_name");
                        String mn = rs.getString("middle_name");
                        String ln = rs.getString("last_name");
                        String addr = rs.getString("address");
                        String mob = rs.getString("mobile");

                        StringBuilder nameBuilder = new StringBuilder();
                        if (fn != null && !fn.trim().isEmpty()) nameBuilder.append(fn.trim());
                        if (mn != null && !mn.trim().isEmpty()) nameBuilder.append(" ").append(mn.trim());
                        if (ln != null && !ln.trim().isEmpty()) nameBuilder.append(" ").append(ln.trim());

                        txtProfileName.setText(nameBuilder.toString().isEmpty() ? residentEmail : nameBuilder.toString());
                        if (addr != null && !addr.trim().isEmpty()) txtProfileAddress.setText(addr);
                        if (mob != null && !mob.trim().isEmpty()) txtProfileMobile.setText(mob);
                        return;
                    }
                } catch (SQLException e) {
                    Log.e("PROFILE_ERR", e.getMessage(), e);
                }
                loadLocalProfile();
            }

            @Override
            public void onError(Exception e) {
                loadLocalProfile();
            }
        });
    }

    private void loadLocalProfile() {
        String[] prof = databaseHelper.getUserProfile(residentEmail);
        if (prof != null) {
            if (!prof[0].isEmpty()) txtProfileName.setText(prof[0]);
            if (!prof[1].isEmpty()) txtProfileAddress.setText(prof[1]);
            if (!prof[2].isEmpty()) txtProfileMobile.setText(prof[2]);
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

    private void updatePasswordOnMSSQL(String email, String newPass) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                Connection con = connect_sql.getConnection();
                if (con != null) {
                    Statement stmt = con.createStatement();
                    stmt.executeUpdate("UPDATE users SET password = '" + newPass.replace("'", "''") + "' WHERE email = '" + email.replace("'", "''") + "'");
                }
            } catch (Exception e) {
                Log.e("MSSQL_PASS_ERR", e.getMessage(), e);
            }
            handler.post(() -> Toast.makeText(profile.this, "Password updated successfully!", Toast.LENGTH_SHORT).show());
        });
    }

}

