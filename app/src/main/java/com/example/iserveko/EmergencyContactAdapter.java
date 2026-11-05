package com.example.iserveko;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.List;

/** Renders emergency agencies with styled direct dial action rows. */
public class EmergencyContactAdapter extends RecyclerView.Adapter<EmergencyContactAdapter.Holder> {
    private final List<EmergencyContact> contacts;

    public EmergencyContactAdapter(List<EmergencyContact> contacts) {
        this.contacts = contacts;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int type) {
        return new Holder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_emergency_contact, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        EmergencyContact contact = contacts.get(position);
        Context context = holder.itemView.getContext();

        holder.name.setText(contact.name);

        String categoryText = contact.category;
        if (categoryText == null || categoryText.trim().isEmpty()) {
            String lowerName = contact.name.toLowerCase();
            if (lowerName.contains("rescue") || lowerName.contains("cdrrmo")) categoryText = "RESCUE";
            else if (lowerName.contains("electric") || lowerName.contains("batelec")) categoryText = "POWER";
            else if (lowerName.contains("water") || lowerName.contains("mlwd")) categoryText = "WATER";
            else if (lowerName.contains("red cross")) categoryText = "MEDICAL";
        }

        if (categoryText != null && !categoryText.trim().isEmpty()) {
            holder.categoryBadge.setVisibility(View.VISIBLE);
            holder.categoryBadge.setText(categoryText.toUpperCase().trim());
        } else {
            holder.categoryBadge.setVisibility(View.GONE);
        }

        holder.numbers.removeAllViews();

        int densityPx = (int) (context.getResources().getDisplayMetrics().density * 8);

        for (int i = 0; i < contact.numbers.size(); i++) {
            String number = contact.numbers.get(i);

            LinearLayout row = new LinearLayout(context);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(0, densityPx / 2, 0, densityPx / 2);

            ImageView phoneIcon = new ImageView(context);
            phoneIcon.setImageResource(R.drawable.ic_emergency_call);
            phoneIcon.setColorFilter(ContextCompat.getColor(context, R.color.dash_red));
            int iconSize = (int) (context.getResources().getDisplayMetrics().density * 18);
            LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(iconSize, iconSize);
            iconParams.setMarginEnd(densityPx);
            phoneIcon.setLayoutParams(iconParams);

            TextView numberText = new TextView(context);
            numberText.setText(number);
            numberText.setTextSize(15);
            numberText.setTextColor(ContextCompat.getColor(context, R.color.black));
            numberText.setTypeface(null, android.graphics.Typeface.BOLD);
            LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            numberText.setLayoutParams(textParams);

            MaterialButton callBtn = new MaterialButton(context, null, com.google.android.material.R.attr.materialButtonStyle);
            callBtn.setText("Call");
            callBtn.setCornerRadius((int) (context.getResources().getDisplayMetrics().density * 18));
            callBtn.setBackgroundColor(ContextCompat.getColor(context, R.color.dash_red));
            callBtn.setTextColor(ContextCompat.getColor(context, R.color.white));
            callBtn.setTextSize(12);
            callBtn.setPadding((int) (context.getResources().getDisplayMetrics().density * 12), 0,
                    (int) (context.getResources().getDisplayMetrics().density * 12), 0);
            callBtn.setMinHeight(0);
            callBtn.setMinimumHeight(0);
            callBtn.setInsetTop(0);
            callBtn.setInsetBottom(0);

            int btnHeight = (int) (context.getResources().getDisplayMetrics().density * 36);
            LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, btnHeight);
            callBtn.setLayoutParams(btnParams);

            View.OnClickListener dialListener = v -> dial(context, number);
            callBtn.setOnClickListener(dialListener);
            row.setOnClickListener(dialListener);

            row.addView(phoneIcon);
            row.addView(numberText);
            row.addView(callBtn);

            holder.numbers.addView(row);
        }
    }

    private void dial(Context context, String number) {
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + number.replaceAll("[^+0-9]", "")));
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, "No dialer app is available.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView categoryBadge;
        final LinearLayout numbers;

        Holder(View view) {
            super(view);
            name = view.findViewById(R.id.contactName);
            categoryBadge = view.findViewById(R.id.contactCategory);
            numbers = view.findViewById(R.id.contactNumbers);
        }
    }
}