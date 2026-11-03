package com.example.iserveko;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

/** Resident entry screen for complaints and reports. */
public class ComplaintsReportActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_complaints_reports);
        findViewById(R.id.complaintsBack).setOnClickListener(v -> finish());
    }
}
