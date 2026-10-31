package com.example.iserveko;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

/** Applies consistent, high-contrast status badges across request lists. */
public final class StatusColorUtil {

    private StatusColorUtil() { }

    public static void applyBadge(TextView badge, String status) {
        String normalized = status == null ? "pending" : status.trim().toLowerCase(java.util.Locale.ROOT);
        int backgroundColor;
        @ColorRes int foregroundResource;
        switch (normalized) {
            case "approved":
                backgroundColor = ContextCompat.getColor(badge.getContext(), R.color.status_approved_bg);
                foregroundResource = R.color.status_approved_fg;
                break;
            case "ready for pickup":
            case "returned":
                backgroundColor = ContextCompat.getColor(badge.getContext(), R.color.status_ready_bg);
                foregroundResource = R.color.status_ready_fg;
                break;
            case "declined":
                backgroundColor = ContextCompat.getColor(badge.getContext(), R.color.status_declined_bg);
                foregroundResource = R.color.status_declined_fg;
                break;
            case "pending":
            default:
                backgroundColor = ContextCompat.getColor(badge.getContext(), R.color.status_pending_bg);
                foregroundResource = R.color.status_pending_fg;
                break;
        }

        String label = status == null || status.trim().isEmpty() ? "Pending" : status.trim();
        badge.setText(label.toUpperCase(java.util.Locale.ROOT));
        badge.setTextColor(ContextCompat.getColor(badge.getContext(), foregroundResource));
        badge.setBackgroundTintList(ColorStateList.valueOf(backgroundColor));

        Drawable dot = ContextCompat.getDrawable(badge.getContext(), R.drawable.bg_status_dot);
        if (dot != null) {
            Drawable tintedDot = DrawableCompat.wrap(dot.mutate());
            DrawableCompat.setTint(tintedDot, ContextCompat.getColor(badge.getContext(), foregroundResource));
            int size = Math.round(8 * badge.getResources().getDisplayMetrics().density);
            tintedDot.setBounds(0, 0, size, size);
            badge.setCompoundDrawablesRelative(tintedDot, null, null, null);
            badge.setCompoundDrawablePadding(Math.round(6 * badge.getResources().getDisplayMetrics().density));
        }
    }
}
