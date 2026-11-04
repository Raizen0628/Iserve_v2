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

public class ComplaintAdapter extends RecyclerView.Adapter<ComplaintAdapter.ComplaintViewHolder> {

    public interface OnComplaintClickListener {
        void onComplaintClick(Complaint complaint);
    }

    private final boolean isOfficial;
    private final OnComplaintClickListener listener;
    private final List<Complaint> allItems = new ArrayList<>();
    private final List<Complaint> visibleItems = new ArrayList<>();
    private String statusFilter = "All";

    public ComplaintAdapter(boolean isOfficial, OnComplaintClickListener listener) {
        this.isOfficial = isOfficial;
        this.listener = listener;
    }

    public void submitList(List<Complaint> items) {
        allItems.clear();
        if (items != null) {
            allItems.addAll(items);
        }
        applyFilter();
    }

    public void filterByStatus(String status) {
        this.statusFilter = (status == null || status.trim().isEmpty()) ? "All" : status.trim();
        applyFilter();
    }

    private void applyFilter() {
        visibleItems.clear();
        for (Complaint item : allItems) {
            if ("All".equalsIgnoreCase(statusFilter) || statusFilter.equalsIgnoreCase(item.getStatus())) {
                visibleItems.add(item);
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ComplaintViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_complaint, parent, false);
        return new ComplaintViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ComplaintViewHolder holder, int position) {
        Complaint complaint = visibleItems.get(position);

        holder.category.setText(complaint.getCategory());
        StatusColorUtil.applyBadge(holder.status, complaint.getStatus());
        holder.subject.setText(complaint.getSubject());
        holder.description.setText(complaint.getDescription());

        // Resident Info (Show for officials)
        if (isOfficial) {
            holder.residentInfo.setVisibility(View.VISIBLE);
            String name = complaint.getResidentName();
            String email = complaint.getResidentEmail();
            if (name.isEmpty()) {
                holder.residentInfo.setText("Resident: " + email);
            } else {
                holder.residentInfo.setText("Resident: " + name + " (" + email + ")");
            }
        } else {
            holder.residentInfo.setVisibility(View.GONE);
        }

        // Location
        String location = complaint.getLocation();
        if (location != null && !location.trim().isEmpty()) {
            holder.location.setVisibility(View.VISIBLE);
            holder.location.setText("📍 Location: " + location.trim());
        } else {
            holder.location.setVisibility(View.GONE);
        }

        // Submitted Date
        String submitted = complaint.getSubmittedAt();
        if (submitted != null && !submitted.trim().isEmpty()) {
            holder.submittedAt.setVisibility(View.VISIBLE);
            holder.submittedAt.setText("Submitted: " + submitted);
        } else {
            holder.submittedAt.setVisibility(View.GONE);
        }

        // Official Remarks
        String remarks = complaint.getOfficialRemarks();
        if (remarks != null && !remarks.trim().isEmpty()) {
            holder.remarksLayout.setVisibility(View.VISIBLE);
            holder.remarks.setText(remarks.trim());
        } else {
            holder.remarksLayout.setVisibility(View.GONE);
        }

        // Official Actions
        if (isOfficial) {
            holder.officialActionsLayout.setVisibility(View.VISIBLE);
            holder.btnReview.setOnClickListener(v -> {
                if (listener != null) listener.onComplaintClick(complaint);
            });
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) listener.onComplaintClick(complaint);
            });
        } else {
            holder.officialActionsLayout.setVisibility(View.GONE);
            holder.itemView.setOnClickListener(null);
        }
    }

    @Override
    public int getItemCount() {
        return visibleItems.size();
    }

    public static class ComplaintViewHolder extends RecyclerView.ViewHolder {
        final TextView category, status, subject, residentInfo, description, location, submittedAt, remarks;
        final View remarksLayout, officialActionsLayout;
        final Button btnReview;

        ComplaintViewHolder(@NonNull View itemView) {
            super(itemView);
            category = itemView.findViewById(R.id.txtComplaintCategory);
            status = itemView.findViewById(R.id.txtComplaintStatus);
            subject = itemView.findViewById(R.id.txtComplaintSubject);
            residentInfo = itemView.findViewById(R.id.txtComplaintResidentInfo);
            description = itemView.findViewById(R.id.txtComplaintDescription);
            location = itemView.findViewById(R.id.txtComplaintLocation);
            submittedAt = itemView.findViewById(R.id.txtComplaintSubmittedAt);
            remarks = itemView.findViewById(R.id.txtComplaintRemarks);
            remarksLayout = itemView.findViewById(R.id.layoutComplaintRemarks);
            officialActionsLayout = itemView.findViewById(R.id.layoutComplaintOfficialActions);
            btnReview = itemView.findViewById(R.id.btnReviewComplaint);
        }
    }
}
