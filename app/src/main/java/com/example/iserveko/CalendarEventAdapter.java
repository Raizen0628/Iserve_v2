package com.example.iserveko;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/** Displays selected-day events and opens their detail dialog when tapped. */
public class CalendarEventAdapter extends RecyclerView.Adapter<CalendarEventAdapter.EventHolder> {
    public interface Listener { void onEventClick(CalendarEvent event); }

    private final List<CalendarEvent> events = new ArrayList<>();
    private final Listener listener;

    public CalendarEventAdapter(Listener listener) { this.listener = listener; }

    public void setEvents(List<CalendarEvent> values) {
        events.clear();
        events.addAll(values);
        notifyDataSetChanged();
    }

    @NonNull @Override public EventHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new EventHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_calendar_event, parent, false));
    }

    @Override public void onBindViewHolder(@NonNull EventHolder holder, int position) {
        CalendarEvent event = events.get(position);
        holder.title.setText(event.getTitle());
        holder.time.setText(event.getTime() == null || event.getTime().trim().isEmpty()
                ? holder.itemView.getContext().getString(R.string.calendar_all_day)
                : event.getTime());
        holder.type.setText(event.getType());
        int color = ContextCompat.getColor(holder.itemView.getContext(),
                CalendarEvent.TYPE_MEETING.equals(event.getType())
                        ? R.color.calendar_meeting : R.color.calendar_announcement);
        holder.stripe.setBackgroundColor(color);
        holder.type.setTextColor(color);
        holder.type.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                ContextCompat.getColor(holder.itemView.getContext(),
                        CalendarEvent.TYPE_MEETING.equals(event.getType())
                                ? R.color.calendar_meeting_container : R.color.calendar_announcement_container)));
        holder.itemView.setOnClickListener(v -> listener.onEventClick(event));
    }

    @Override public int getItemCount() { return events.size(); }

    static class EventHolder extends RecyclerView.ViewHolder {
        final TextView title, time, type;
        final View stripe;
        EventHolder(View view) {
            super(view);
            stripe = view.findViewById(R.id.calendarEventStripe);
            title = view.findViewById(R.id.calendarEventTitle);
            time = view.findViewById(R.id.calendarEventTime);
            type = view.findViewById(R.id.calendarEventType);
        }
    }
}
