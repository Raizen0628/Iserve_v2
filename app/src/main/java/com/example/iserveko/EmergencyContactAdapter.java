package com.example.iserveko;

import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.color.MaterialColors;

import java.util.List;

/** Renders the emergency contact cards and their dial, SMS, and copy actions. */
public class EmergencyContactAdapter
        extends RecyclerView.Adapter<EmergencyContactAdapter.ContactViewHolder> {

    private final List<EmergencyContact> contacts;

    public EmergencyContactAdapter(List<EmergencyContact> contacts) {
        this.contacts = contacts;
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_emergency_contact, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        EmergencyContact contact = contacts.get(position);
        Context context = holder.itemView.getContext();

        holder.name.setText(contact.nameRes);
        holder.description.setText(contact.descriptionRes);
        holder.icon.setText(contact.iconRes);
        holder.icon.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);

        String contactName = context.getString(contact.nameRes);
        if (contact.hasNumber()) {
            holder.number.setText(contact.displayNumber);
            holder.number.setContentDescription(contact.emergencyShortCode
                    ? context.getString(R.string.emergency_short_code_accessibility,
                            contact.displayNumber)
                    : context.getString(R.string.emergency_number_accessibility,
                            contact.displayNumber));
            holder.number.setPaintFlags(
                    holder.number.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            holder.number.setClickable(true);
            holder.number.setFocusable(true);
            holder.number.setOnClickListener(v -> openDialer(context, contact));
            holder.number.setOnLongClickListener(v -> copyNumber(context, contact));
            holder.card.setContentDescription(context.getString(
                    R.string.emergency_contact_card_calls, contactName));
            holder.card.setClickable(true);
            holder.card.setFocusable(true);
            holder.card.setOnClickListener(v -> openDialer(context, contact));
            holder.callButton.setEnabled(true);
            holder.messageButton.setEnabled(true);
            holder.callButton.setAlpha(1f);
            holder.messageButton.setAlpha(1f);
            holder.callButton.setContentDescription(context.getString(
                    R.string.emergency_call_accessibility, contactName));
            holder.messageButton.setContentDescription(context.getString(
                    R.string.emergency_message_accessibility, contactName));
            holder.callButton.setOnClickListener(v -> openDialer(context, contact));
            holder.messageButton.setOnClickListener(v -> openMessages(context, contact));
        } else {
            holder.number.setText(R.string.emergency_mobile_number_needed);
            holder.number.setContentDescription(
                    context.getString(R.string.emergency_mobile_number_needed));
            holder.number.setTextColor(MaterialColors.getColor(
                    holder.number, com.google.android.material.R.attr.colorOnSurfaceVariant));
            holder.number.setPaintFlags(
                    holder.number.getPaintFlags() & ~Paint.UNDERLINE_TEXT_FLAG);
            holder.number.setCompoundDrawablesRelative(null, null, null, null);
            holder.number.setClickable(false);
            holder.number.setFocusable(false);
            holder.number.setOnClickListener(null);
            holder.number.setOnLongClickListener(null);
            holder.card.setContentDescription(context.getString(
                    R.string.emergency_contact_card_missing, contactName));
            holder.card.setClickable(false);
            holder.card.setFocusable(false);
            holder.card.setOnClickListener(null);
            holder.callButton.setEnabled(false);
            holder.messageButton.setEnabled(false);
            holder.callButton.setAlpha(0.5f);
            holder.messageButton.setAlpha(0.5f);
            holder.callButton.setContentDescription(context.getString(
                    R.string.emergency_call_accessibility, contactName));
            holder.messageButton.setContentDescription(context.getString(
                    R.string.emergency_message_accessibility, contactName));
            holder.callButton.setOnClickListener(null);
            holder.messageButton.setOnClickListener(null);
        }
    }

    private void openDialer(Context context, EmergencyContact contact) {
        if (!contact.hasNumber()) return;
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + contact.dialNumber));
        try {
            if (intent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(intent);
            } else {
                Toast.makeText(context, R.string.emergency_dialer_unavailable, Toast.LENGTH_SHORT).show();
            }
        } catch (ActivityNotFoundException exception) {
            Toast.makeText(context, R.string.emergency_dialer_unavailable, Toast.LENGTH_SHORT).show();
        }
    }

    private void openMessages(Context context, EmergencyContact contact) {
        if (!contact.hasNumber()) return;
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + contact.dialNumber));
        try {
            if (intent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(intent);
            } else {
                Toast.makeText(context, R.string.emergency_messages_unavailable, Toast.LENGTH_SHORT).show();
            }
        } catch (ActivityNotFoundException exception) {
            Toast.makeText(context, R.string.emergency_messages_unavailable, Toast.LENGTH_SHORT).show();
        }
    }

    private boolean copyNumber(Context context, EmergencyContact contact) {
        ClipboardManager clipboard =
                (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard == null) return false;

        clipboard.setPrimaryClip(ClipData.newPlainText(
                context.getString(contact.nameRes), contact.displayNumber));
        Toast.makeText(context, R.string.emergency_number_copied, Toast.LENGTH_SHORT).show();
        return true;
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    public static class ContactViewHolder extends RecyclerView.ViewHolder {
        final MaterialCardView card;
        final TextView icon;
        final TextView name;
        final TextView description;
        final TextView number;
        final MaterialButton callButton;
        final MaterialButton messageButton;

        ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            card = (MaterialCardView) itemView;
            icon = itemView.findViewById(R.id.contactIcon);
            name = itemView.findViewById(R.id.contactName);
            description = itemView.findViewById(R.id.contactDescription);
            number = itemView.findViewById(R.id.contactNumber);
            callButton = itemView.findViewById(R.id.contactCallButton);
            messageButton = itemView.findViewById(R.id.contactMessageButton);
        }
    }
}
