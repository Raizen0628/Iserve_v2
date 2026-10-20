package com.example.iserveko;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private List<NotificationItem> notificationList;

    public NotificationAdapter(List<NotificationItem> notificationList) {
        this.notificationList = notificationList;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        NotificationItem item = notificationList.get(position);

        if ("DOCUMENT".equalsIgnoreCase(item.getType())) {
            holder.txtNotifIcon.setText("📄");
        } else if ("MEETING".equalsIgnoreCase(item.getType())) {
            holder.txtNotifIcon.setText("📅");
        } else {
            holder.txtNotifIcon.setText("🪑");
        }

        holder.txtNotifTitle.setText(item.getTitle());
        holder.txtNotifDate.setText("Requested on: " + item.getDate());
        holder.txtNotifDetails.setText(item.getDetails());

        String status = item.getStatus() != null ? item.getStatus() : "Pending";
        holder.txtNotifStatusBadge.setText(status.toUpperCase());

        int badgeColor;
        switch (status.toLowerCase()) {
            case "approved":
                badgeColor = Color.parseColor("#2E7D32"); // Green
                break;
            case "ready for pickup":
                badgeColor = Color.parseColor("#1565C0"); // Blue
                break;
            case "declined":
                badgeColor = Color.parseColor("#C62828"); // Red
                break;
            case "pending":
            default:
                badgeColor = Color.parseColor("#F57F17"); // Amber
                break;
        }

        GradientDrawable shape = new GradientDrawable();
        shape.setCornerRadius(20);
        shape.setColor(badgeColor);
        holder.txtNotifStatusBadge.setBackground(shape);

        if (item.getRemarks() != null && !item.getRemarks().trim().isEmpty()) {
            holder.txtNotifRemarks.setVisibility(View.VISIBLE);
            holder.txtNotifRemarks.setText("Remarks: " + item.getRemarks());
        } else {
            holder.txtNotifRemarks.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return notificationList != null ? notificationList.size() : 0;
    }

    public static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView txtNotifIcon, txtNotifTitle, txtNotifDate, txtNotifStatusBadge, txtNotifDetails, txtNotifRemarks;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNotifIcon = itemView.findViewById(R.id.txtNotifIcon);
            txtNotifTitle = itemView.findViewById(R.id.txtNotifTitle);
            txtNotifDate = itemView.findViewById(R.id.txtNotifDate);
            txtNotifStatusBadge = itemView.findViewById(R.id.txtNotifStatusBadge);
            txtNotifDetails = itemView.findViewById(R.id.txtNotifDetails);
            txtNotifRemarks = itemView.findViewById(R.id.txtNotifRemarks);
        }
    }
}
