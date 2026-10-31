package com.example.iserveko;

import androidx.annotation.StringRes;

/** Holds one emergency service contact and its display and dial values. */
public class EmergencyContact {
    @StringRes public final int nameRes;
    @StringRes public final int descriptionRes;
    @StringRes public final int iconRes;
    public final String displayNumber;
    public final String dialNumber;
    public final boolean emergencyShortCode;

    public EmergencyContact(@StringRes int nameRes,
                            @StringRes int descriptionRes,
                            @StringRes int iconRes,
                            String displayNumber,
                            String dialNumber,
                            boolean emergencyShortCode) {
        this.nameRes = nameRes;
        this.descriptionRes = descriptionRes;
        this.iconRes = iconRes;
        this.displayNumber = displayNumber;
        this.dialNumber = dialNumber;
        this.emergencyShortCode = emergencyShortCode;
    }

    public boolean hasNumber() {
        return displayNumber != null && !displayNumber.trim().isEmpty()
                && dialNumber != null && !dialNumber.trim().isEmpty();
    }
}
