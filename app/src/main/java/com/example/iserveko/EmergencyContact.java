package com.example.iserveko;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** One agency and all of its dialable numbers. */
public final class EmergencyContact {
    public final String name;
    public final String category;
    public final List<String> numbers;

    public EmergencyContact(String name, String category, String... numbers) {
        this.name = name;
        this.category = category;
        this.numbers = Collections.unmodifiableList(Arrays.asList(numbers));
    }
}
