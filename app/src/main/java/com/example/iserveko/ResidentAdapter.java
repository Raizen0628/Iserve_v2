package com.example.iserveko;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ResidentAdapter extends RecyclerView.Adapter<ResidentAdapter.ResidentViewHolder> {

    private List<ResidentUser> residentList;
    private final List<ResidentUser> originalList;

    public ResidentAdapter(List<ResidentUser> residentList) {
        this.residentList = residentList != null ? residentList : new ArrayList<>();
        this.originalList = new ArrayList<>(this.residentList);
    }

    @NonNull
    @Override
    public ResidentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_resident_directory, parent, false);
        return new ResidentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ResidentViewHolder holder, int position) {
        ResidentUser resident = residentList.get(position);

        String fullName = resident.getFullName();
        holder.txtResidentName.setText(fullName.isEmpty() ? "Unknown Resident" : fullName);

        String email = resident.getEmail();
        holder.txtResidentEmail.setText(email.isEmpty() ? "📧 N/A" : "📧 " + email);

        String address = resident.getAddress();
        holder.txtResidentAddress.setText(address.isEmpty() ? "🏠 Address: N/A" : "🏠 Address: " + address);

        String mobile = resident.getMobile();
        holder.txtResidentMobile.setText(mobile.isEmpty() ? "📞 Mobile: N/A" : "📞 Mobile: " + mobile);

        String role = resident.getRole();
        holder.txtRoleBadge.setText(role.toUpperCase());
    }

    @Override
    public int getItemCount() {
        return residentList.size();
    }

    public void filter(String query) {
        if (query == null || query.trim().isEmpty()) {
            residentList = new ArrayList<>(originalList);
        } else {
            String lower = query.toLowerCase().trim();
            List<ResidentUser> filtered = new ArrayList<>();
            for (ResidentUser user : originalList) {
                if (user.getFullName().toLowerCase().contains(lower)
                        || user.getEmail().toLowerCase().contains(lower)
                        || user.getAddress().toLowerCase().contains(lower)
                        || user.getMobile().toLowerCase().contains(lower)) {
                    filtered.add(user);
                }
            }
            residentList = filtered;
        }
        notifyDataSetChanged();
    }

    public void updateList(List<ResidentUser> newList) {
        this.residentList = newList != null ? newList : new ArrayList<>();
        this.originalList.clear();
        this.originalList.addAll(this.residentList);
        notifyDataSetChanged();
    }

    static class ResidentViewHolder extends RecyclerView.ViewHolder {
        TextView txtResidentName, txtResidentEmail, txtResidentAddress, txtResidentMobile, txtRoleBadge;

        public ResidentViewHolder(@NonNull View itemView) {
            super(itemView);
            txtResidentName = itemView.findViewById(R.id.txtResidentName);
            txtResidentEmail = itemView.findViewById(R.id.txtResidentEmail);
            txtResidentAddress = itemView.findViewById(R.id.txtResidentAddress);
            txtResidentMobile = itemView.findViewById(R.id.txtResidentMobile);
            txtRoleBadge = itemView.findViewById(R.id.txtRoleBadge);
        }
    }
}
