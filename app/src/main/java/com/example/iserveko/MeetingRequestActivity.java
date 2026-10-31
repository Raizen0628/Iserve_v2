package com.example.iserveko;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MeetingRequestActivity extends AppCompatActivity {
    private DatabaseHelper databaseHelper;
    private boolean isOfficial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        databaseHelper = new DatabaseHelper(this);
        isOfficial = getIntent().getBooleanExtra("IS_OFFICIAL", false);
        setTitle(isOfficial ? "Meeting Requests" : "Request a Meeting");

        ScrollView scrollView = new ScrollView(this);
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(24, 24, 24, 24);
        scrollView.addView(content);

        TextView title = new TextView(this);
        title.setText(isOfficial ? "Meeting Requests" : "Schedule a Meeting");
        title.setTextSize(24);
        content.addView(title);

        if (isOfficial) {
            showRequests(content);
        } else {
            showRequestForm(content);
        }
        setContentView(scrollView);
    }

    private void showRequestForm(LinearLayout content) {
        EditText dateInput = addInput(content, "Meeting date (for example, Nov 5, 2026)");
        EditText timeInput = addInput(content, "Meeting time");
        EditText purposeInput = addInput(content, "Purpose");

        Button submit = new Button(this);
        submit.setText("Submit request");
        content.addView(submit);
        submit.setOnClickListener(view -> {
            String email = getIntent().getStringExtra("EMAIL");
            String date = dateInput.getText().toString().trim();
            String time = timeInput.getText().toString().trim();
            String purpose = purposeInput.getText().toString().trim();
            if (email == null || email.trim().isEmpty() || date.isEmpty() || time.isEmpty() || purpose.isEmpty()) {
                Toast.makeText(this, "Enter the date, time, and purpose.", Toast.LENGTH_SHORT).show();
                return;
            }
            String[] profile = databaseHelper.getUserProfile(email);
            String requestDate = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(new Date());
            boolean saved = databaseHelper.addMeetingRequest(email, profile[0], date, time, purpose, requestDate);
            Toast.makeText(this, saved ? "Meeting request submitted." : "Could not submit the request.", Toast.LENGTH_SHORT).show();
            if (saved) finish();
        });
    }

    private EditText addInput(LinearLayout content, String hint) {
        EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setHint(hint);
        content.addView(input, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        return input;
    }

    private void showRequests(LinearLayout content) {
        for (MeetingRequest request : databaseHelper.getAllMeetingRequests()) {
            TextView details = new TextView(this);
            details.setText(request.getResidentName() + " (" + request.getResidentEmail() + ")\n"
                    + request.getMeetingDate() + " at " + request.getMeetingTime() + "\n"
                    + request.getPurpose() + "\nStatus: " + request.getStatus());
            details.setTextSize(16);
            details.setPadding(0, 20, 0, 8);
            content.addView(details);

            if ("Pending".equalsIgnoreCase(request.getStatus())) {
                LinearLayout actions = new LinearLayout(this);
                Button approve = new Button(this);
                approve.setText("Approve");
                Button decline = new Button(this);
                decline.setText("Decline");
                actions.addView(approve, new LinearLayout.LayoutParams(0,
                        LinearLayout.LayoutParams.WRAP_CONTENT, 1));
                actions.addView(decline, new LinearLayout.LayoutParams(0,
                        LinearLayout.LayoutParams.WRAP_CONTENT, 1));
                content.addView(actions);
                View.OnClickListener update = view -> {
                    String status = view == approve ? "Approved" : "Declined";
                    if (databaseHelper.updateMeetingRequestStatus(request.getId(), status, "")) {
                        Toast.makeText(this, "Request " + status.toLowerCase(Locale.getDefault()) + ".", Toast.LENGTH_SHORT).show();
                        recreate();
                    }
                };
                approve.setOnClickListener(update);
                decline.setOnClickListener(update);
            }
        }
    }
}
