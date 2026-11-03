package com.example.iserveko;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import java.util.List;

/** Renders agencies with an individual dial action for every number. */
public class EmergencyContactAdapter extends RecyclerView.Adapter<EmergencyContactAdapter.Holder> {
    private final List<EmergencyContact> contacts;
    public EmergencyContactAdapter(List<EmergencyContact> contacts) { this.contacts = contacts; }

    @NonNull @Override public Holder onCreateViewHolder(@NonNull ViewGroup parent, int type) {
        return new Holder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_emergency_contact, parent, false));
    }

    @Override public void onBindViewHolder(@NonNull Holder holder, int position) {
        EmergencyContact contact = contacts.get(position);
        holder.name.setText(contact.name + (contact.category.isEmpty() ? "" : " (" + contact.category + ")"));
        holder.numbers.removeAllViews();
        Context context = holder.itemView.getContext();
        for (String number : contact.numbers) {
            LinearLayout row = new LinearLayout(context);
            row.setGravity(android.view.Gravity.CENTER_VERTICAL);
            row.setOrientation(LinearLayout.HORIZONTAL);
            TextView value = new TextView(context);
            value.setText(number);
            value.setTextSize(16);
            value.setTextColor(context.getColor(android.R.color.holo_blue_dark));
            value.setPaintFlags(value.getPaintFlags() | android.graphics.Paint.UNDERLINE_TEXT_FLAG);
            value.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            Button call = new Button(context);
            call.setText("Call");
            call.setOnClickListener(v -> dial(context, number));
            value.setOnClickListener(v -> dial(context, number));
            row.addView(value);
            row.addView(call);
            holder.numbers.addView(row);
        }
    }

    private void dial(Context context, String number) {
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + number.replaceAll("[^+0-9]", "")));
        try { context.startActivity(intent); }
        catch (ActivityNotFoundException e) {
            Toast.makeText(context, "No dialer app is available.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override public int getItemCount() { return contacts.size(); }

    static class Holder extends RecyclerView.ViewHolder {
        final TextView name;
        final LinearLayout numbers;
        Holder(View view) {
            super(view);
            name = view.findViewById(R.id.contactName);
            numbers = view.findViewById(R.id.contactNumbers);
        }
    }
}
