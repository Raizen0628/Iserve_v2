package com.example.iserveko;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class DocumentAdapter extends RecyclerView.Adapter<DocumentAdapter.DocumentViewHolder> {

    private List<DocumentRequest> originalList;
    private List<DocumentRequest> filteredList;
    private final boolean isOfficial;
    private final OnDocumentActionListener listener;

    public interface OnDocumentActionListener {
        void onApprove(DocumentRequest request);
        void onDecline(DocumentRequest request);
        void onReadyForPickup(DocumentRequest request);
    }

    public DocumentAdapter(List<DocumentRequest> list, boolean isOfficial, OnDocumentActionListener listener) {
        this.originalList = list != null ? list : new ArrayList<>();
        this.filteredList = new ArrayList<>(this.originalList);
        this.isOfficial = isOfficial;
        this.listener = listener;
    }

    public void filterByStatus(String status) {
        filteredList.clear();
        if (status == null || status.isEmpty() || "All".equalsIgnoreCase(status)) {
            filteredList.addAll(originalList);
        } else {
            for (DocumentRequest item : originalList) {
                if (status.equalsIgnoreCase(item.getStatus())) {
                    filteredList.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DocumentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_document_request, parent, false);
        return new DocumentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DocumentViewHolder holder, int position) {
        DocumentRequest request = filteredList.get(position);

        holder.txtDocType.setText("📄 " + request.getDocumentType());
        holder.txtResidentName.setText("👤 Resident: " + request.getFullName());
        holder.txtResidentEmail.setText("📧 Email: " + request.getResidentEmail());
        holder.txtPurpose.setText("Purpose: " + request.getPurpose());
        holder.txtSpecificDetails.setText("Details: " + request.getSpecificDetails());
        holder.txtAddressContact.setText("Address: " + request.getAddress() + " | Contact: " + request.getContactNumber());
        holder.txtRequestDate.setText("Requested on: " + request.getRequestDate());

        String status = request.getStatus();
        holder.txtStatusBadge.setText(status.toUpperCase());

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
                badgeColor = Color.parseColor("#F57F17"); // Amber/Yellow
                break;
        }

        GradientDrawable shape = new GradientDrawable();
        shape.setCornerRadius(20);
        shape.setColor(badgeColor);
        holder.txtStatusBadge.setBackground(shape);

        if (request.getRemarks() != null && !request.getRemarks().trim().isEmpty()) {
            holder.txtRemarks.setVisibility(View.VISIBLE);
            holder.txtRemarks.setText("Note: " + request.getRemarks());
        } else {
            holder.txtRemarks.setVisibility(View.GONE);
        }

        if (isOfficial) {
            holder.layoutOfficialActions.setVisibility(View.VISIBLE);
            if ("Pending".equalsIgnoreCase(status)) {
                holder.btnApprove.setVisibility(View.VISIBLE);
                holder.btnDecline.setVisibility(View.VISIBLE);
                holder.btnReadyForPickup.setVisibility(View.VISIBLE);
            } else if ("Approved".equalsIgnoreCase(status)) {
                holder.btnApprove.setVisibility(View.GONE);
                holder.btnDecline.setVisibility(View.VISIBLE);
                holder.btnReadyForPickup.setVisibility(View.VISIBLE);
            } else {
                holder.layoutOfficialActions.setVisibility(View.GONE);
            }

            holder.btnApprove.setOnClickListener(v -> {
                if (listener != null) listener.onApprove(request);
            });
            holder.btnDecline.setOnClickListener(v -> {
                if (listener != null) listener.onDecline(request);
            });
            holder.btnReadyForPickup.setOnClickListener(v -> {
                if (listener != null) listener.onReadyForPickup(request);
            });
        } else {
            holder.layoutOfficialActions.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    static class DocumentViewHolder extends RecyclerView.ViewHolder {
        TextView txtDocType, txtResidentName, txtResidentEmail, txtPurpose, txtSpecificDetails,
                txtAddressContact, txtRequestDate, txtStatusBadge, txtRemarks;
        LinearLayout layoutOfficialActions;
        Button btnApprove, btnDecline, btnReadyForPickup;

        public DocumentViewHolder(@NonNull View itemView) {
            super(itemView);
            txtDocType = itemView.findViewById(R.id.txtDocType);
            txtResidentName = itemView.findViewById(R.id.txtResidentName);
            txtResidentEmail = itemView.findViewById(R.id.txtResidentEmail);
            txtPurpose = itemView.findViewById(R.id.txtPurpose);
            txtSpecificDetails = itemView.findViewById(R.id.txtSpecificDetails);
            txtAddressContact = itemView.findViewById(R.id.txtAddressContact);
            txtRequestDate = itemView.findViewById(R.id.txtRequestDate);
            txtStatusBadge = itemView.findViewById(R.id.txtStatusBadge);
            txtRemarks = itemView.findViewById(R.id.txtRemarks);

            layoutOfficialActions = itemView.findViewById(R.id.layoutOfficialActions);
            btnApprove = itemView.findViewById(R.id.btnApprove);
            btnDecline = itemView.findViewById(R.id.btnDecline);
            btnReadyForPickup = itemView.findViewById(R.id.btnReadyForPickup);
        }
    }
}
