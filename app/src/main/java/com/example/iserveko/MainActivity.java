package com.example.iserveko;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import com.android.volley.toolbox.JsonArrayRequest;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    // Emulator: 10.0.2.2 | Physical phone: your PC's IP (run ipconfig)
    private static final String BASE_URL = "http://192.168.1.2/iserveko/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        EditText usernameInput = findViewById(R.id.username_input);
        EditText passwordInput = findViewById(R.id.password_input);
        Button loginButton = findViewById(R.id.button);
        TextView signupText = findViewById(R.id.signupText);

        loginButton.setOnClickListener(v -> {
            String username = usernameInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show();
            } else {
                loginUser(username, password);
            }
        });

        signupText.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, AccountSetupActivity.class)));
    }

    private void loginUser(String username, String password) {
        StringRequest request = new StringRequest(Request.Method.POST, BASE_URL + "login.php",
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);

                        if (json.getBoolean("success")) {
                            JSONObject user = json.getJSONObject("user");
                            String role = user.optString("role", "").trim();

                            Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();

                            if (role.isEmpty() || role.equals("null")) {
                                Intent intent = new Intent(this, AccountSetupActivity.class);
                                intent.putExtra("EMAIL", username);
                                startActivity(intent);
                            } else if (role.equals("Resident")) {
                                startActivity(new Intent(this, ResidentDashboardActivity.class));
                                finish();
                            } else if (role.equals("Barangay Official")) {
                                startActivity(new Intent(this, OfficialDashboardActivity.class));
                                finish();
                            } else {
                                Toast.makeText(this, "Welcome " + role, Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(this, json.getString("message"), Toast.LENGTH_SHORT).show();
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
                params.put("username", username);
                params.put("password", password);
                return params;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }
}