package com.example.iserveko;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

/** Compatibility route to the current SQL Server-backed meeting screens. */
public class MeetingRequestActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean isOfficial = SessionManager.isOfficial(this, false);
        Intent intent = new Intent(this, isOfficial
                ? MeetingOfficialActivity.class
                : MeetingResidentActivity.class);
        String email = SessionManager.getEmail(this, "");
        if (!email.isEmpty()) intent.putExtra("EMAIL", email);
        startActivity(intent);
        finish();
    }
}
