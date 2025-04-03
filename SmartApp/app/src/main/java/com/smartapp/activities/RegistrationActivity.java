package com.smartapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.smartapp.R;
import com.smartapp.models.LoginResponse;
import com.smartapp.models.RegistrationRequest;
import com.smartapp.network.APIClient;
import com.smartapp.network.APIService;
import com.smartapp.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegistrationActivity extends AppCompatActivity {
    private TextInputLayout tilEmail;
    private TextInputEditText etEmail;
    private TextInputLayout tilPassword;
    private TextInputEditText etPassword;
    private TextInputLayout tilConfirmPassword;
    private TextInputEditText etConfirmPassword;
    private TextInputLayout tilFullName;
    private TextInputEditText etFullName;
    private Spinner spinnerRole;
    private TextInputLayout tilIdentifier;
    private TextInputEditText etIdentifier;
    private MaterialButton btnRegister;

    private APIService apiService;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        // Initialize views
        tilEmail = findViewById(R.id.tilEmail);
        etEmail = findViewById(R.id.etEmail);
        tilPassword = findViewById(R.id.tilPassword);
        etPassword = findViewById(R.id.etPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        tilFullName = findViewById(R.id.tilFullName);
        etFullName = findViewById(R.id.etFullName);
        spinnerRole = findViewById(R.id.spinnerRole);
        tilIdentifier = findViewById(R.id.tilIdentifier);
        etIdentifier = findViewById(R.id.etIdentifier);
        btnRegister = findViewById(R.id.btnRegister);

        // Initialize API service and session manager
        apiService = APIClient.getClient().create(APIService.class);
        sessionManager = new SessionManager(this);

        // Set up role spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.user_roles, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(adapter);

        // Handle role selection
        spinnerRole.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateIdentifierField(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Set up click listeners
        btnRegister.setOnClickListener(v -> attemptRegistration());
        findViewById(R.id.tvLogin).setOnClickListener(v -> finish());
    }

    private void updateIdentifierField(int position) {
        String[] roles = getResources().getStringArray(R.array.user_roles);
        String role = roles[position];

        if (role.equals("Student")) {
            tilIdentifier.setHint(getString(R.string.hint_nisn));
            etIdentifier.setInputType(InputType.TYPE_CLASS_NUMBER);
            tilIdentifier.setVisibility(View.VISIBLE);
        } else if (role.equals("Teacher")) {
            tilIdentifier.setHint(getString(R.string.hint_nip));
            etIdentifier.setInputType(InputType.TYPE_CLASS_NUMBER);
            tilIdentifier.setVisibility(View.VISIBLE);
        }
    }

    private void attemptRegistration() {
        // Reset errors
        tilEmail.setError(null);
        tilPassword.setError(null);
        tilConfirmPassword.setError(null);
        tilFullName.setError(null);
        tilIdentifier.setError(null);

        // Get values
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();
        String fullName = etFullName.getText().toString().trim();
        String identifier = etIdentifier.getText().toString().trim();
        String role = getResources().getStringArray(R.array.user_role_values)
                [spinnerRole.getSelectedItemPosition()];

        // Validate input
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError(getString(R.string.error_invalid_email));
            etEmail.requestFocus();
            return;
        }

        if (password.isEmpty() || password.length() < 6) {
            tilPassword.setError(getString(R.string.error_password_short));
            etPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            tilConfirmPassword.setError(getString(R.string.error_passwords_not_match));
            etConfirmPassword.requestFocus();
            return;
        }

        if (fullName.isEmpty()) {
            tilFullName.setError(getString(R.string.error_field_required));
            etFullName.requestFocus();
            return;
        }

        // Validate role-specific identifiers
        if (role.equals("siswa")) {
            if (!identifier.matches("\\d{10}")) {
                tilIdentifier.setError(getString(R.string.error_invalid_nisn));
                etIdentifier.requestFocus();
                return;
            }
        } else if (role.equals("guru")) {
            if (!identifier.matches("\\d{18}")) {
                tilIdentifier.setError(getString(R.string.error_invalid_nip));
                etIdentifier.requestFocus();
                return;
            }
        }

        // Show loading state
        setLoading(true);

        // Create registration request
        RegistrationRequest request = new RegistrationRequest();
        request.setEmail(email);
        request.setPassword(password);
        request.setFullName(fullName);
        request.setRole(role);
        
        if (role.equals("siswa")) {
            request.setNisn(identifier);
        } else if (role.equals("guru")) {
            request.setNip(identifier);
        }

        // Make API call
        apiService.register(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                setLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    
                    // Save user session
                    sessionManager.saveAuthToken(loginResponse.getToken());
                    sessionManager.saveUserDetails(
                        loginResponse.getUser().getId(),
                        loginResponse.getUser().getEmail(),
                        loginResponse.getUser().getFullName(),
                        loginResponse.getUser().getRole(),
                        loginResponse.getUser().getNisn(),
                        loginResponse.getUser().getNip()
                    );

                    Toast.makeText(RegistrationActivity.this, 
                        getString(R.string.success_register), 
                        Toast.LENGTH_SHORT).show();

                    // Navigate to main activity
                    Intent intent = new Intent(RegistrationActivity.this, MaterialListActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    try {
                        String errorMessage = response.errorBody() != null ? 
                            response.errorBody().string() : 
                            "Registration failed";
                        Toast.makeText(RegistrationActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(RegistrationActivity.this, 
                            "Registration failed", 
                            Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                setLoading(false);
                Toast.makeText(RegistrationActivity.this, 
                    getString(R.string.error_network), 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setLoading(boolean isLoading) {
        btnRegister.setEnabled(!isLoading);
        btnRegister.setText(isLoading ? R.string.loading : R.string.btn_register);
        etEmail.setEnabled(!isLoading);
        etPassword.setEnabled(!isLoading);
        etConfirmPassword.setEnabled(!isLoading);
        etFullName.setEnabled(!isLoading);
        spinnerRole.setEnabled(!isLoading);
        etIdentifier.setEnabled(!isLoading);
    }
}