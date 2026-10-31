package com.example.iserveko;

import android.os.Bundle;
import android.view.Gravity;
import android.widget.CalendarView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CalendarActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(24, 32, 24, 24);

        TextView title = new TextView(this);
        title.setText("Events Calendar");
        title.setTextSize(24);
        title.setGravity(Gravity.CENTER);
        root.addView(title, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        TextView selectedDate = new TextView(this);
        selectedDate.setTextSize(16);
        selectedDate.setGravity(Gravity.CENTER);
        selectedDate.setPadding(0, 24, 0, 16);
        root.addView(selectedDate, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        CalendarView calendar = new CalendarView(this);
        calendar.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Date date = new java.util.GregorianCalendar(year, month, dayOfMonth).getTime();
            selectedDate.setText(new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(date));
        });
        root.addView(calendar, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        setContentView(root);
        setTitle("Events Calendar");
    }
}
