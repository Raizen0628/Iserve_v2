package com.example.iserveko;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ResidentSignupActivity extends AppCompatActivity {

    private static final String BASE_URL = "http://192.168.1.2/iserveko/";
    private static final String ROLE = "Resident";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resident_signup);

        TextView backButton    = findViewById(R.id.btnBack);
        EditText firstName     = findViewById(R.id.first_name_input);
        EditText middleName    = findViewById(R.id.middle_name_input);
        EditText lastName      = findViewById(R.id.last_name_input);
        EditText emailInput    = findViewById(R.id.email_input);
        EditText addressInput  = findViewById(R.id.address_input);
        EditText dobInput      = findViewById(R.id.dob_input);
        EditText passwordInput = findViewById(R.id.password_input);
        EditText confirmInput  = findViewById(R.id.confirm_password_input);
        Button registerButton  = findViewById(R.id.btnRegister);

        backButton.setOnClickListener(v -> finish());

        registerButton.setOnClickListener(v -> {
            String first   = firstName.getText().toString().trim();
            String middle  = middleName.getText().toString().trim();
            String last    = lastName.getText().toString().trim();
            String email   = emailInput.getText().toString().trim();
            String address = addressInput.getText().toString().trim();
            String dob     = dobInput.getText().toString().trim();
            String pass    = passwordInput.getText().toString();
            String confirm = confirmInput.getText().toString();

            if (first.isEmpty() || last.isEmpty() || email.isEmpty()
                    || address.isEmpty() || dob.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            } else if (!pass.equals(confirm)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            } else {
                registerUser(first, middle, last, email, address, dob, pass);
            }
        });
    }

    private void registerUser(String first, String middle, String last, String email,
                              String address, String dob, String password) {
        StringRequest request = new StringRequest(Request.Method.POST, BASE_URL + "register.php",
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        Toast.makeText(this, json.getString("message"), Toast.LENGTH_LONG).show();

                        if (json.getBoolean("success")) {
                            startActivity(new Intent(this, MainActivity.class));
                            finish();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(this, "Invalid server response", Toast.LENGTH_LONG).show();
                    }
                },
                error -> Toast.makeText(this, "Connection error: " + error.getMessage(), Toast.LENGTH_LONG).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("first_name", first);
                params.put("middle_name", middle);
                params.put("last_name", last);
                params.put("email", email);
                params.put("address", address);
                params.put("dob", dob);
                params.put("password", password);
                params.put("role", ROLE);
                return params;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }
}