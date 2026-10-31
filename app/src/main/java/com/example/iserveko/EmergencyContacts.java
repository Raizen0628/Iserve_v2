package com.example.iserveko;

import androidx.annotation.StringRes;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Central directory for emergency contacts, numbers, and missing-mobile placeholders. */
public final class EmergencyContacts {
    private static final List<EmergencyContact> CONTACTS =
            Collections.unmodifiableList(Arrays.asList(
                    new EmergencyContact(
                            R.string.emergency_hotline_title,
                            R.string.emergency_hotline_desc,
                            R.string.emoji_emergency,
                            "911",
                            "911",
                            true),
                    new EmergencyContact(
                            R.string.police_contact_title,
                            R.string.police_contact_desc,
                            R.string.emoji_tanod,
                            "0936-185-5910",
                            "09361855910",
                            false),
                    new EmergencyContact(
                            R.string.fire_contact_title,
                            R.string.fire_contact_desc,
                            R.string.emoji_fire,
                            "0927-575-8095",
                            "09275758095",
                            false),
                    new EmergencyContact(
                            R.string.fire_alternate_title,
                            R.string.fire_alternate_desc,
                            R.string.emoji_fire,
                            "0949-854-7526",
                            "09498547526",
                            false),
                    new EmergencyContact(
                            R.string.hospital_emergency_title,
                            R.string.hospital_emergency_desc,
                            R.string.emoji_ambulance,
                            "0917-1499387",
                            "09171499387",
                            false),
                    new EmergencyContact(
                            R.string.barangay_hall_title,
                            R.string.barangay_hall_desc,
                            R.string.emoji_hall,
                            "0977-625-5928",
                            "09776255928",
                            false),
                    new EmergencyContact(
                            R.string.drrm_title,
                            R.string.drrm_desc,
                            R.string.emoji_drrm,
                            "0915-463-5005",
                            "09154635005",
                            false),
                    missingMobile(
                            R.string.mayor_contact_title,
                            R.string.mayor_contact_desc,
                            R.string.emoji_other_contacts),
                    missingMobile(
                            R.string.batelec_contact_title,
                            R.string.batelec_contact_desc,
                            R.string.emoji_other_contacts),
                    missingMobile(
                            R.string.poison_control_title,
                            R.string.poison_control_desc,
                            R.string.emoji_ambulance),
                    missingMobile(
                            R.string.red_cross_contact_title,
                            R.string.red_cross_contact_desc,
                            R.string.emoji_other_contacts),
                    missingMobile(
                            R.string.water_district_contact_title,
                            R.string.water_district_contact_desc,
                            R.string.emoji_other_contacts)
            ));

    private EmergencyContacts() { }

    private static EmergencyContact missingMobile(
            @StringRes int nameRes,
            @StringRes int descriptionRes,
            @StringRes int iconRes) {
        return new EmergencyContact(
                nameRes, descriptionRes, iconRes, null, null, false);
    }

    public static List<EmergencyContact> getAll() {
        return CONTACTS;
    }
}
