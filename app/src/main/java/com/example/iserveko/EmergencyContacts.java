package com.example.iserveko;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Edit the emergency directory here. */
public final class EmergencyContacts {
    private static final List<EmergencyContact> CONTACTS = Collections.unmodifiableList(Arrays.asList(
            new EmergencyContact("National Emergency Hotline", "911", "911"),
            new EmergencyContact("Lipa CDRRMO (Lipa Rescue)", "RESCUE",
                    "0915-463-5005", "(043) 756-0127", "(043) 757-5164"),
            new EmergencyContact("Lipa Bureau of Fire Protection", "BFP",
                    "0927-575-8065", "(043) 757-4618"),
            new EmergencyContact("Lipa Philippine National Police", "PNP",
                    "0977-744-9692", "(043) 702-3832"),
            new EmergencyContact("Lipa Traffic Management Division", "TMD",
                    "(043) 702-6454", "(043) 404-9152"),
            new EmergencyContact("Batangas II Electric Cooperative Inc.", "POWER",
                    "0917-504-9254", "0998-892-0145", "(043) 756-6337"),
            new EmergencyContact("Metro Lipa Water District", "MLWD",
                    "(043) 756-1611", "(043) 756-1118"),
            new EmergencyContact("Red Cross Lipa", "MEDICAL",
                    "0998-957-0443", "(043) 740-0768")
    ));

    private EmergencyContacts() { }
    public static List<EmergencyContact> getAll() { return CONTACTS; }
}