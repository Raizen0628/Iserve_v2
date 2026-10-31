package com.example.iserveko;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/** Renders the 42 selectable day cells in the calendar month grid. */
public class CalendarDayAdapter extends RecyclerView.Adapter<CalendarDayAdapter.DayHolder> {
    public interface OnDaySelectedListener { void onSelected(Calendar calendar); }

    public static class Day {
        final Calendar date;
        final boolean inDisplayedMonth;
        final boolean today;
        final boolean hasAnnouncement;
        final boolean hasMeeting;
        boolean selected;

        Day(Calendar date, boolean inDisplayedMonth, boolean today, boolean hasAnnouncement,
            boolean hasMeeting, boolean selected) {
            this.date = (Calendar) date.clone();
            this.inDisplayedMonth = inDisplayedMonth;
            this.today = today;
            this.hasAnnouncement = hasAnnouncement;
            this.hasMeeting = hasMeeting;
            this.selected = selected;
        }
    }

    private final List<Day> days = new ArrayList<>();
    private final OnDaySelectedListener listener;

    public CalendarDayAdapter(OnDaySelectedListener listener) { this.listener = listener; }

    public void setDays(List<Day> values) {
        days.clear();
        days.addAll(values);
        notifyDataSetChanged();
    }

    @NonNull @Override public DayHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DayHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_calendar_day, parent, false));
    }

    @Override public void onBindViewHolder(@NonNull DayHolder holder, int position) {
        Day day = days.get(position);
        holder.number.setText(String.valueOf(day.date.get(Calendar.DAY_OF_MONTH)));
        holder.number.setAlpha(day.inDisplayedMonth ? 1f : 0.38f);
        int primary = ContextCompat.getColor(holder.itemView.getContext(), R.color.primary);
        int surface = ContextCompat.getColor(holder.itemView.getContext(), R.color.surface);
        int onSurface = ContextCompat.getColor(holder.itemView.getContext(), R.color.onSurface);
        GradientDrawable background = new GradientDrawable();
        background.setShape(GradientDrawable.OVAL);
        if (day.selected) {
            background.setColor(primary);
            holder.number.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.onPrimary));
        } else {
            background.setColor(surface);
            holder.number.setTextColor(onSurface);
            if (day.today) background.setStroke(dp(holder, 1), primary);
        }
        holder.number.setBackground(background);
        holder.announcementDot.setVisibility(day.hasAnnouncement ? View.VISIBLE : View.INVISIBLE);
        holder.meetingDot.setVisibility(day.hasMeeting ? View.VISIBLE : View.INVISIBLE);
        holder.itemView.setOnClickListener(v -> listener.onSelected((Calendar) day.date.clone()));
    }

    private int dp(DayHolder holder, int value) {
        return Math.round(value * holder.itemView.getResources().getDisplayMetrics().density);
    }

    @Override public int getItemCount() { return days.size(); }

    static class DayHolder extends RecyclerView.ViewHolder {
        final TextView number, announcementDot, meetingDot;
        DayHolder(View view) {
            super(view);
            number = view.findViewById(R.id.calendarDayNumber);
            announcementDot = view.findViewById(R.id.calendarAnnouncementDot);
            meetingDot = view.findViewById(R.id.calendarMeetingDot);
        }
    }
}
