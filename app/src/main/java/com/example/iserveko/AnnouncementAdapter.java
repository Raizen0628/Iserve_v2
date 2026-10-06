package com.example.iserveko;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class AnnouncementAdapter extends RecyclerView.Adapter<AnnouncementAdapter.ViewHolder> {

    public interface OnAnnouncementClickListener {
        void onEditClick(Announcement announcement);
        void onDeleteClick(Announcement announcement);
        void onItemClick(Announcement announcement);
    }

    private final List<Announcement> originalList;
    private final List<Announcement> filteredList;
    private final boolean isOfficialMode;
    private final OnAnnouncementClickListener listener;

    public AnnouncementAdapter(List<Announcement> list, boolean isOfficialMode, OnAnnouncementClickListener listener) {
        this.originalList = new ArrayList<>(list);
        this.filteredList = new ArrayList<>(list);
        this.isOfficialMode = isOfficialMode;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_announcement, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Announcement item = filteredList.get(position);

        holder.txtTitle.setText(item.getTitle());
        holder.txtDate.setText(item.getDate());
        holder.txtDescription.setText(item.getDescription());

        if (item.isPinned()) {
            holder.txtPinnedBadge.setVisibility(View.VISIBLE);
        } else {
            holder.txtPinnedBadge.setVisibility(View.GONE);
        }

        if (isOfficialMode) {
            holder.actionsContainer.setVisibility(View.VISIBLE);
            holder.btnEdit.setOnClickListener(v -> {
                if (listener != null) listener.onEditClick(item);
            });
            holder.btnDelete.setOnClickListener(v -> {
                if (listener != null) listener.onDeleteClick(item);
            });
        } else {
            holder.actionsContainer.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public void filter(String query) {
        filteredList.clear();
        if (query == null || query.trim().isEmpty()) {
            filteredList.addAll(originalList);
        } else {
            String lowerQuery = query.toLowerCase().trim();
            for (Announcement item : originalList) {
                if (item.getTitle().toLowerCase().contains(lowerQuery) ||
                        item.getDescription().toLowerCase().contains(lowerQuery)) {
                    filteredList.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void updateList(List<Announcement> newList) {
        originalList.clear();
        originalList.addAll(newList);
        filteredList.clear();
        filteredList.addAll(newList);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitle, txtDate, txtDescription, txtPinnedBadge;
        View actionsContainer;
        ImageView btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtDate = itemView.findViewById(R.id.txtDate);
            txtDescription = itemView.findViewById(R.id.txtDescription);
            txtPinnedBadge = itemView.findViewById(R.id.txtPinnedBadge);
            actionsContainer = itemView.findViewById(R.id.actionsContainer);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
