package com.example.iserveko;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DocumentRequestActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;

    private Spinner spinnerDocumentType;
    private TextView txtOfficialRequirements;

    private EditText inputFullName, inputEmail, inputAddress, inputContactNumber, inputPurpose;

    // Dynamic Specific Field Layouts
    private LinearLayout layoutClearanceFields, layoutResidencyFields, layoutIndigencyFields,
            layoutBusinessFields, layoutGoodMoralFields, layoutFirstTimeJobSeekerFields;

    // Specific Field Views
    private EditText inputClearanceCivilStatus, inputClearanceYearsResidency;
    private EditText inputResidencyYears, inputResidencyHouseholdHead;
    private EditText inputIndigencyIncome, inputIndigencyDependents, inputIndigencyReason;
    private EditText inputBusinessName, inputBusinessType, inputBusinessAddress;
    private EditText inputGoodMoralYears, inputGoodMoralReference;
    private EditText inputJobSeekerEducation;
    private CheckBox chkFirstTimeUndertaking;

    private String residentEmail = "resident@iserveko.com";

    private static final String[] DOCUMENT_TYPES = {
            "Barangay Clearance",
            "Certificate of Residency",
            "Certificate of Indigency",
            "Barangay Business Clearance",
            "Certificate of Good Moral Character",
            "First-Time Job Seeker Certification"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_document_request);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        databaseHelper = new DatabaseHelper(this);

        if (getIntent().hasExtra("EMAIL")) {
            residentEmail = getIntent().getStringExtra("EMAIL");
        } else {
            residentEmail = getSharedPreferences("UserSession", MODE_PRIVATE)
                    .getString("logged_in_email", "resident@iserveko.com");
        }

        initViews();
        setupSpinner();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnSubmitDocumentRequest).setOnClickListener(v -> submitRequest());

        loadUserProfile();
        loadMyDocumentRequests();
    }



    private RecyclerView recyclerViewMyDocRequests;
    private DocumentAdapter adapter;

    private void initViews() {
        spinnerDocumentType = findViewById(R.id.spinnerDocumentType);
        txtOfficialRequirements = findViewById(R.id.txtOfficialRequirements);

        inputFullName = findViewById(R.id.inputFullName);
        inputEmail = findViewById(R.id.inputEmail);
        inputAddress = findViewById(R.id.inputAddress);
        inputContactNumber = findViewById(R.id.inputContactNumber);
        inputPurpose = findViewById(R.id.inputPurpose);

        recyclerViewMyDocRequests = findViewById(R.id.recyclerViewMyDocRequests);
        if (recyclerViewMyDocRequests != null) {
            recyclerViewMyDocRequests.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
        }

        layoutClearanceFields = findViewById(R.id.layoutClearanceFields);
        layoutResidencyFields = findViewById(R.id.layoutResidencyFields);
        layoutIndigencyFields = findViewById(R.id.layoutIndigencyFields);
        layoutBusinessFields = findViewById(R.id.layoutBusinessFields);
        layoutGoodMoralFields = findViewById(R.id.layoutGoodMoralFields);
        layoutFirstTimeJobSeekerFields = findViewById(R.id.layoutFirstTimeJobSeekerFields);

        inputClearanceCivilStatus = findViewById(R.id.inputClearanceCivilStatus);
        inputClearanceYearsResidency = findViewById(R.id.inputClearanceYearsResidency);

        inputResidencyYears = findViewById(R.id.inputResidencyYears);
        inputResidencyHouseholdHead = findViewById(R.id.inputResidencyHouseholdHead);

        inputIndigencyIncome = findViewById(R.id.inputIndigencyIncome);
        inputIndigencyDependents = findViewById(R.id.inputIndigencyDependents);
        inputIndigencyReason = findViewById(R.id.inputIndigencyReason);

        inputBusinessName = findViewById(R.id.inputBusinessName);
        inputBusinessType = findViewById(R.id.inputBusinessType);
        inputBusinessAddress = findViewById(R.id.inputBusinessAddress);

        inputGoodMoralYears = findViewById(R.id.inputGoodMoralYears);
        inputGoodMoralReference = findViewById(R.id.inputGoodMoralReference);

        inputJobSeekerEducation = findViewById(R.id.inputJobSeekerEducation);
        chkFirstTimeUndertaking = findViewById(R.id.chkFirstTimeUndertaking);
    }

    private void setupSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, DOCUMENT_TYPES);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDocumentType.setAdapter(adapter);

        // Pre-select document type if passed via Intent
        if (getIntent().hasExtra("DOC_TYPE")) {
            String preSelectedType = getIntent().getStringExtra("DOC_TYPE");
            for (int i = 0; i < DOCUMENT_TYPES.length; i++) {
                if (DOCUMENT_TYPES[i].equalsIgnoreCase(preSelectedType)) {
                    spinnerDocumentType.setSelection(i);
                    break;
                }
            }
        }

        spinnerDocumentType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateFormForDocumentType(DOCUMENT_TYPES[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void updateFormForDocumentType(String docType) {
        // Hide all dynamic layouts first
        layoutClearanceFields.setVisibility(View.GONE);
        layoutResidencyFields.setVisibility(View.GONE);
        layoutIndigencyFields.setVisibility(View.GONE);
        layoutBusinessFields.setVisibility(View.GONE);
        layoutGoodMoralFields.setVisibility(View.GONE);
        layoutFirstTimeJobSeekerFields.setVisibility(View.GONE);

        switch (docType) {
            case "Barangay Clearance":
                layoutClearanceFields.setVisibility(View.VISIBLE);
                txtOfficialRequirements.setText("1. Valid Government-Issued ID\n2. Proof of Barangay Residency / Purok Clearance\n3. Cedula (Community Tax Certificate)\n4. Processing Fee: PHP 50.00");
                break;

            case "Certificate of Residency":
                layoutResidencyFields.setVisibility(View.VISIBLE);
                txtOfficialRequirements.setText("1. Valid Government ID / Student ID\n2. Barangay Records Verification\n3. Processing Fee: FREE");
                break;

            case "Certificate of Indigency":
                layoutIndigencyFields.setVisibility(View.VISIBLE);
                txtOfficialRequirements.setText("1. Valid Government ID / Student ID\n2. DSWD / Case Study Recommendation (if applicable)\n3. Barangay Verification\n4. Processing Fee: FREE");
                break;

            case "Barangay Business Clearance":
                layoutBusinessFields.setVisibility(View.VISIBLE);
                txtOfficialRequirements.setText("1. DTI / SEC Registration Certificate\n2. Tax Declaration or Lease Contract for Location\n3. Barangay Inspection Approval\n4. Processing Fee: PHP 150.00");
                break;

            case "Certificate of Good Moral Character":
                layoutGoodMoralFields.setVisibility(View.VISIBLE);
                txtOfficialRequirements.setText("1. Valid Government ID\n2. Barangay Police / Tanod Record Clearance\n3. Minimum 6 Months Residency\n4. Processing Fee: PHP 30.00");
                break;

            case "First-Time Job Seeker Certification":
                layoutFirstTimeJobSeekerFields.setVisibility(View.VISIBLE);
                txtOfficialRequirements.setText("1. Valid ID / Birth Certificate\n2. School Diploma or Transcript of Records\n3. Signed Undertaking under RA 11261\n4. Processing Fee: FREE (RA 11261 First-Time Job Seekers Act)");
                break;
        }
    }

    private void loadUserProfile() {
        if (residentEmail == null || residentEmail.isEmpty()) return;

        inputEmail.setText(residentEmail);

        String userQuery = "SELECT first_name, middle_name, last_name, email, address, mobile FROM users WHERE email = '" + residentEmail.replace("'", "''") + "'";
        connect_sql.executeQueryAsync(userQuery, new connect_sql.QueryCallback() {
            @Override
            public void onSuccess(ResultSet rs) {
                try {
                    if (rs != null && rs.next()) {
                        String fn = rs.getString("first_name");
                        String mn = rs.getString("middle_name");
                        String ln = rs.getString("last_name");
                        String addr = rs.getString("address");
                        String mob = rs.getString("mobile");

                        StringBuilder nameBuilder = new StringBuilder();
                        if (fn != null && !fn.trim().isEmpty()) nameBuilder.append(fn.trim());
                        if (mn != null && !mn.trim().isEmpty()) nameBuilder.append(" ").append(mn.trim());
                        if (ln != null && !ln.trim().isEmpty()) nameBuilder.append(" ").append(ln.trim());

                        inputFullName.setText(nameBuilder.toString());
                        if (addr != null) inputAddress.setText(addr);
                        if (mob != null) inputContactNumber.setText(mob);
                        return;
                    }
                } catch (SQLException e) {
                    Log.e("DOC_PROFILE_ERR", e.getMessage(), e);
                }
                loadLocalUserProfile();
            }

            @Override
            public void onError(Exception e) {
                loadLocalUserProfile();
            }
        });
    }

    private void loadLocalUserProfile() {
        String[] prof = databaseHelper.getUserProfile(residentEmail);
        if (prof != null) {
            if (!prof[0].isEmpty()) inputFullName.setText(prof[0]);
            if (!prof[1].isEmpty()) inputAddress.setText(prof[1]);
            if (!prof[2].isEmpty()) inputContactNumber.setText(prof[2]);
        }
    }

    private void submitRequest() {
        String docType = spinnerDocumentType.getSelectedItem().toString();
        String fullName = inputFullName.getText().toString().trim();
        String email = inputEmail.getText().toString().trim();
        String address = inputAddress.getText().toString().trim();
        String contact = inputContactNumber.getText().toString().trim();
        String purpose = inputPurpose.getText().toString().trim();

        if (fullName.isEmpty() || email.isEmpty() || address.isEmpty() || contact.isEmpty() || purpose.isEmpty()) {
            Toast.makeText(this, "Please fill in all common required fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder specificBuilder = new StringBuilder();

        switch (docType) {
            case "Barangay Clearance":
                String civil = inputClearanceCivilStatus.getText().toString().trim();
                String years = inputClearanceYearsResidency.getText().toString().trim();
                specificBuilder.append("Civil Status: ").append(civil.isEmpty() ? "N/A" : civil)
                        .append(", Years Residency: ").append(years.isEmpty() ? "N/A" : years);
                break;

            case "Certificate of Residency":
                String resYears = inputResidencyYears.getText().toString().trim();
                String head = inputResidencyHouseholdHead.getText().toString().trim();
                specificBuilder.append("Years Living in Barangay: ").append(resYears.isEmpty() ? "N/A" : resYears)
                        .append(", Household Head: ").append(head.isEmpty() ? "N/A" : head);
                break;

            case "Certificate of Indigency":
                String inc = inputIndigencyIncome.getText().toString().trim();
                String dep = inputIndigencyDependents.getText().toString().trim();
                String reason = inputIndigencyReason.getText().toString().trim();
                specificBuilder.append("Monthly Income: ").append(inc.isEmpty() ? "N/A" : inc)
                        .append(", Dependents: ").append(dep.isEmpty() ? "N/A" : dep)
                        .append(", Reason: ").append(reason.isEmpty() ? "N/A" : reason);
                break;

            case "Barangay Business Clearance":
                String busName = inputBusinessName.getText().toString().trim();
                String busType = inputBusinessType.getText().toString().trim();
                String busAddr = inputBusinessAddress.getText().toString().trim();

                if (busName.isEmpty() || busType.isEmpty()) {
                    Toast.makeText(this, "Please enter Business Name and Business Type.", Toast.LENGTH_SHORT).show();
                    return;
                }
                specificBuilder.append("Business Name: ").append(busName)
                        .append(", Type: ").append(busType)
                        .append(", Business Address: ").append(busAddr.isEmpty() ? address : busAddr);
                break;

            case "Certificate of Good Moral Character":
                String gmYears = inputGoodMoralYears.getText().toString().trim();
                String ref = inputGoodMoralReference.getText().toString().trim();
                specificBuilder.append("Years Resident: ").append(gmYears.isEmpty() ? "N/A" : gmYears)
                        .append(", Character Reference: ").append(ref.isEmpty() ? "N/A" : ref);
                break;

            case "First-Time Job Seeker Certification":
                if (!chkFirstTimeUndertaking.isChecked()) {
                    Toast.makeText(this, "Please check the RA 11261 undertaking checkbox to proceed.", Toast.LENGTH_LONG).show();
                    return;
                }
                String edu = inputJobSeekerEducation.getText().toString().trim();
                specificBuilder.append("Education: ").append(edu.isEmpty() ? "N/A" : edu)
                        .append(", Undertaking Acknowledged: Yes (RA 11261)");
                break;
        }

        String specificDetails = specificBuilder.toString();
        String reqDate = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(new Date());

        // Local SQLite save
        databaseHelper.addDocumentRequest(email, fullName, docType, address, contact, purpose, specificDetails, reqDate);

        // MSSQL save
        submitDocumentRequestToMSSQL(email, fullName, docType, address, contact, purpose, specificDetails, reqDate);
    }

    private void submitDocumentRequestToMSSQL(String email, String name, String docType, String address, String contact, String purpose, String specificDetails, String reqDate) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                Connection con = connect_sql.getConnection();
                if (con != null) {
                    Statement stmt = con.createStatement();

                    // Ensure table exists
                    String checkTable = "IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'document_requests') "
                            + "CREATE TABLE document_requests ("
                            + "id INT IDENTITY(1,1) PRIMARY KEY, "
                            + "resident_email VARCHAR(100) NOT NULL, "
                            + "full_name VARCHAR(100) NOT NULL, "
                            + "document_type VARCHAR(100) NOT NULL, "
                            + "address VARCHAR(255) NOT NULL, "
                            + "contact_number VARCHAR(50) NOT NULL, "
                            + "purpose VARCHAR(255) NOT NULL, "
                            + "specific_details VARCHAR(500) NULL, "
                            + "status VARCHAR(20) NOT NULL DEFAULT 'Pending', "
                            + "request_date VARCHAR(50) NOT NULL, "
                            + "remarks VARCHAR(255) NULL);";
                    stmt.executeUpdate(checkTable);

                    String sql = "INSERT INTO document_requests (resident_email, full_name, document_type, address, contact_number, purpose, specific_details, status, request_date, remarks) VALUES ('"
                            + email.replace("'", "''") + "', '"
                            + name.replace("'", "''") + "', '"
                            + docType.replace("'", "''") + "', '"
                            + address.replace("'", "''") + "', '"
                            + contact.replace("'", "''") + "', '"
                            + purpose.replace("'", "''") + "', '"
                            + specificDetails.replace("'", "''") + "', 'Pending', '"
                            + reqDate + "', '');";

                    stmt.executeUpdate(sql);
                }
            } catch (Exception e) {
                Log.e("DOC_REQ_MSSQL_ERR", e.getMessage(), e);
            }
            handler.post(() -> {
                Toast.makeText(DocumentRequestActivity.this, "Document request submitted successfully!", Toast.LENGTH_LONG).show();
                loadMyDocumentRequests();
            });
        });
    }

    private void loadMyDocumentRequests() {
        if (residentEmail == null || residentEmail.isEmpty()) return;

        String query = "SELECT * FROM document_requests WHERE resident_email = '" + residentEmail.replace("'", "''") + "' ORDER BY id DESC";
        connect_sql.executeQueryAsync(query, new connect_sql.QueryCallback() {
            @Override
            public void onSuccess(ResultSet rs) {
                try {
                    List<DocumentRequest> list = new ArrayList<>();
                    while (rs != null && rs.next()) {
                        int id = rs.getInt("id");
                        String email = rs.getString("resident_email");
                        String name = rs.getString("full_name");
                        String docType = rs.getString("document_type");
                        String addr = rs.getString("address");
                        String contact = rs.getString("contact_number");
                        String purpose = rs.getString("purpose");
                        String specific = rs.getString("specific_details");
                        String status = rs.getString("status");
                        String reqDate = rs.getString("request_date");
                        String remarks = rs.getString("remarks");

                        list.add(new DocumentRequest(id, email, name, docType, addr, contact, purpose, specific, status, reqDate, remarks));
                    }
                    if (!list.isEmpty()) {
                        adapter = new DocumentAdapter(list, false, null);
                        if (recyclerViewMyDocRequests != null) {
                            recyclerViewMyDocRequests.setAdapter(adapter);
                        }
                        return;
                    }
                } catch (SQLException e) {
                    Log.e("DOC_REQ_LOAD_MY_ERR", e.getMessage(), e);
                }
                bindLocalMyDocumentRequests();
            }

            @Override
            public void onError(Exception e) {
                bindLocalMyDocumentRequests();
            }
        });
    }

    private void bindLocalMyDocumentRequests() {
        List<DocumentRequest> list = databaseHelper.getDocumentRequestsForResident(residentEmail);
        adapter = new DocumentAdapter(list, false, null);
        if (recyclerViewMyDocRequests != null) {
            recyclerViewMyDocRequests.setAdapter(adapter);
        }
    }
}
