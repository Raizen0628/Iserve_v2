package com.example.iserveko;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Displays meeting requests and exposes official review actions. */
public class MeetingAdapter extends RecyclerView.Adapter<MeetingAdapter.MeetingViewHolder> {
    public interface Listener {
        void onApprove(MeetingRequest request);
        void onDecline(MeetingRequest request);
    }

    private final boolean official;
    private final Listener listener;
    private final List<MeetingRequest> allItems = new ArrayList<>();
    private final List<MeetingRequest> visibleItems = new ArrayList<>();
    private String statusFilter = "All";

    public MeetingAdapter(boolean official, Listener listener) {
        this.official = official;
        this.listener = listener;
    }

    public void submitList(List<MeetingRequest> items) {
        allItems.clear();
        if (items != null) allItems.addAll(items);
        applyFilter();
    }

    public void filterByStatus(String status) {
        statusFilter = status == null ? "All" : status;
        applyFilter();
    }

    private void applyFilter() {
        visibleItems.clear();
        for (MeetingRequest item : allItems) {
            if ("All".equalsIgnoreCase(statusFilter) || statusFilter.equalsIgnoreCase(item.getStatus())) {
                visibleItems.add(item);
            }
        }
        notifyDataSetChanged();
    }

    @NonNull @Override public MeetingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_meeting_request, parent, false);
        return new MeetingViewHolder(view);
    }

    @Override public void onBindViewHolder(@NonNull MeetingViewHolder holder, int position) {
        MeetingRequest request = visibleItems.get(position);
        holder.name.setText(request.getResidentName());
        holder.email.setText(request.getResidentEmail());
        holder.dateTime.setText(holder.itemView.getContext().getString(
                R.string.meeting_date_time_format, request.getMeetingDate(), request.getMeetingTime()));
        holder.purpose.setText(request.getPurpose());
        holder.remarks.setText(request.getRemarks() == null || request.getRemarks().trim().isEmpty()
                ? "" : holder.itemView.getContext().getString(R.string.meeting_remarks_format, request.getRemarks()));
        holder.remarks.setVisibility(request.getRemarks() == null || request.getRemarks().trim().isEmpty()
                ? View.GONE : View.VISIBLE);
        StatusColorUtil.applyBadge(holder.status, request.getStatus());
        boolean pending = "Pending".equalsIgnoreCase(request.getStatus());
        holder.actions.setVisibility(official && pending ? View.VISIBLE : View.GONE);
        holder.approve.setOnClickListener(v -> listener.onApprove(request));
        holder.decline.setOnClickListener(v -> listener.onDecline(request));
    }

    @Override public int getItemCount() { return visibleItems.size(); }

    static class MeetingViewHolder extends RecyclerView.ViewHolder {
        final TextView name, email, dateTime, purpose, status, remarks;
        final View actions;
        final Button approve, decline;

        MeetingViewHolder(@NonNull View view) {
            super(view);
            name = view.findViewById(R.id.meetingResidentName);
            email = view.findViewById(R.id.meetingResidentEmail);
            dateTime = view.findViewById(R.id.meetingDateTime);
            purpose = view.findViewById(R.id.meetingPurpose);
            status = view.findViewById(R.id.meetingStatus);
            remarks = view.findViewById(R.id.meetingRemarks);
            actions = view.findViewById(R.id.meetingActions);
            approve = view.findViewById(R.id.meetingApprove);
            decline = view.findViewById(R.id.meetingDecline);
        }
    }
}
