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

public class BorrowAdapter extends RecyclerView.Adapter<BorrowAdapter.BorrowViewHolder> {

    private List<BorrowRequest> originalList;
    private List<BorrowRequest> filteredList;
    private final boolean isOfficial;
    private final OnBorrowActionListener listener;

    public interface OnBorrowActionListener {
        void onApprove(BorrowRequest request);
        void onDecline(BorrowRequest request);
        void onMarkReturned(BorrowRequest request);
        void onCancel(BorrowRequest request);
        void onReturnEquipment(BorrowRequest request);
    }

    public BorrowAdapter(List<BorrowRequest> list, boolean isOfficial, OnBorrowActionListener listener) {
        this.originalList = list != null ? list : new ArrayList<>();
        this.filteredList = new ArrayList<>(this.originalList);
        this.isOfficial = isOfficial;
        this.listener = listener;
    }

    public void updateList(List<BorrowRequest> newList) {
        this.originalList = newList != null ? newList : new ArrayList<>();
        this.filteredList = new ArrayList<>(this.originalList);
        notifyDataSetChanged();
    }

    public void filterByStatus(String status) {
        filteredList.clear();
        if (status == null || status.isEmpty() || "All".equalsIgnoreCase(status)) {
            filteredList.addAll(originalList);
        } else {
            for (BorrowRequest item : originalList) {
                if (status.equalsIgnoreCase(item.getStatus())) {
                    filteredList.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BorrowViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_borrow_request, parent, false);
        return new BorrowViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BorrowViewHolder holder, int position) {
        BorrowRequest request = filteredList.get(position);

        String nameDisplay = "👤 Resident: " + request.getResidentName();
        String emailDisplay = "📧 Gmail: " + request.getResidentEmail();

        holder.txtBorrowerName.setText(nameDisplay);
        holder.txtBorrowerEmail.setText(emailDisplay);

        String quantitiesStr = "🪑 " + request.getTablesRequested() + " Table(s)  |  🪑 " + request.getChairsRequested() + " Chair(s)";
        holder.txtRequestedQuantities.setText(quantitiesStr);

        holder.txtBorrowDate.setText(request.getBorrowDate());
        holder.txtPurpose.setText("Purpose: " + request.getPurpose());

        // Status Badge styling
        String status = request.getStatus();
        holder.txtStatusBadge.setText(status.toUpperCase());
        int badgeColor;
        switch (status.toLowerCase()) {
            case "approved":
                badgeColor = Color.parseColor("#2E7D32"); // Green
                break;
            case "declined":
                badgeColor = Color.parseColor("#C62828"); // Red
                break;
            case "returned":
                badgeColor = Color.parseColor("#1565C0"); // Blue
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

        // Remarks
        if (request.getRemarks() != null && !request.getRemarks().trim().isEmpty()) {
            holder.txtRemarks.setVisibility(View.VISIBLE);
            holder.txtRemarks.setText("Note: " + request.getRemarks());
        } else {
            holder.txtRemarks.setVisibility(View.GONE);
        }

        // Actions
        if (isOfficial) {
            holder.layoutResidentActions.setVisibility(View.GONE);
            if ("Pending".equalsIgnoreCase(status)) {
                holder.layoutOfficialActions.setVisibility(View.VISIBLE);
                holder.btnApprove.setVisibility(View.VISIBLE);
                holder.btnDecline.setVisibility(View.VISIBLE);
                holder.btnMarkReturned.setVisibility(View.GONE);

                holder.btnApprove.setOnClickListener(v -> {
                    if (listener != null) listener.onApprove(request);
                });
                holder.btnDecline.setOnClickListener(v -> {
                    if (listener != null) listener.onDecline(request);
                });
            } else if ("Approved".equalsIgnoreCase(status)) {
                holder.layoutOfficialActions.setVisibility(View.VISIBLE);
                holder.btnApprove.setVisibility(View.GONE);
                holder.btnDecline.setVisibility(View.GONE);
                holder.btnMarkReturned.setVisibility(View.VISIBLE);

                holder.btnMarkReturned.setOnClickListener(v -> {
                    if (listener != null) listener.onMarkReturned(request);
                });
            } else {
                holder.layoutOfficialActions.setVisibility(View.GONE);
            }
        } else { // Resident view
            holder.layoutOfficialActions.setVisibility(View.GONE);
            if ("Pending".equalsIgnoreCase(status)) {
                holder.layoutResidentActions.setVisibility(View.VISIBLE);
                holder.btnCancelRequest.setVisibility(View.VISIBLE);
                holder.btnResidentReturn.setVisibility(View.GONE);

                holder.btnCancelRequest.setOnClickListener(v -> {
                    if (listener != null) listener.onCancel(request);
                });
            } else if ("Approved".equalsIgnoreCase(status)) {
                holder.layoutResidentActions.setVisibility(View.VISIBLE);
                holder.btnCancelRequest.setVisibility(View.GONE);
                holder.btnResidentReturn.setVisibility(View.VISIBLE);

                holder.btnResidentReturn.setOnClickListener(v -> {
                    if (listener != null) listener.onReturnEquipment(request);
                });
            } else {
                holder.layoutResidentActions.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    static class BorrowViewHolder extends RecyclerView.ViewHolder {
        TextView txtBorrowerName, txtBorrowerEmail, txtRequestedQuantities, txtBorrowDate, txtPurpose, txtStatusBadge, txtRemarks;
        LinearLayout layoutOfficialActions, layoutResidentActions;
        Button btnApprove, btnDecline, btnMarkReturned, btnCancelRequest, btnResidentReturn;

        public BorrowViewHolder(@NonNull View itemView) {
            super(itemView);
            txtBorrowerName = itemView.findViewById(R.id.txtBorrowerName);
            txtBorrowerEmail = itemView.findViewById(R.id.txtBorrowerEmail);
            txtRequestedQuantities = itemView.findViewById(R.id.txtRequestedQuantities);
            txtBorrowDate = itemView.findViewById(R.id.txtBorrowDate);
            txtPurpose = itemView.findViewById(R.id.txtPurpose);
            txtStatusBadge = itemView.findViewById(R.id.txtStatusBadge);
            txtRemarks = itemView.findViewById(R.id.txtRemarks);

            layoutOfficialActions = itemView.findViewById(R.id.layoutOfficialActions);
            layoutResidentActions = itemView.findViewById(R.id.layoutResidentActions);
            btnApprove = itemView.findViewById(R.id.btnApprove);
            btnDecline = itemView.findViewById(R.id.btnDecline);
            btnMarkReturned = itemView.findViewById(R.id.btnMarkReturned);
            btnCancelRequest = itemView.findViewById(R.id.btnCancelRequest);
            btnResidentReturn = itemView.findViewById(R.id.btnResidentReturn);
        }
    }
}
