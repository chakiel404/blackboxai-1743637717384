package com.smartapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.smartapp.R;
import com.smartapp.models.LoginRequest;
import com.smartapp.models.LoginResponse;
import com.smartapp.network.APIClient;
import com.smartapp.network.APIService;
import com.smartapp.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private Spinner spinnerLoginType;
    private TextInputLayout tilIdentifier;
    private TextInputEditText etIdentifier;
    private TextInputLayout tilPassword;
    private TextInputEditText etPassword;
    private Button btnLogin;
    private TextView tvRegister;

    private APIService apiService;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize views
        spinnerLoginType = findViewById(R.id.spinnerLoginType);
        tilIdentifier = findViewById(R.id.tilIdentifier);
        etIdentifier = findViewById(R.id.etIdentifier);
        tilPassword = findViewById(R.id.tilPassword);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);

        // Initialize API service and session manager
        apiService = APIClient.getClient().create(APIService.class);
        sessionManager = new SessionManager(this);

        // Check if already logged in
        if (sessionManager.isLoggedIn()) {
            navigateToMain();
            finish();
            return;
        }

        // Set up login type spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.login_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLoginType.setAdapter(adapter);

        // Handle login type selection
        spinnerLoginType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateIdentifierField(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Set up click listeners
        btnLogin.setOnClickListener(v -> attemptLogin());
        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegistrationActivity.class));
        });
    }

    private void updateIdentifierField(int position) {
        String[] loginTypes = getResources().getStringArray(R.array.login_types);
        String loginType = loginTypes[position];

        switch (loginType) {
            case "Email":
                tilIdentifier.setHint(getString(R.string.hint_email));
                etIdentifier.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                break;
            case "NISN":
                tilIdentifier.setHint(getString(R.string.hint_nisn));
                etIdentifier.setInputType(InputType.TYPE_CLASS_NUMBER);
                break;
            case "NIP":
                tilIdentifier.setHint(getString(R.string.hint_nip));
                etIdentifier.setInputType(InputType.TYPE_CLASS_NUMBER);
                break;
        }
    }

    private void attemptLogin() {
        // Reset errors
        tilIdentifier.setError(null);
        tilPassword.setError(null);

        // Get values
        String identifier = etIdentifier.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String loginType = getResources().getStringArray(R.array.login_type_values)
                [spinnerLoginType.getSelectedItemPosition()];

        // Validate input
        if (identifier.isEmpty()) {
            tilIdentifier.setError(getString(R.string.error_field_required));
            etIdentifier.requestFocus();
            return;
        }

        // Validate identifier format based on login type
        if (loginType.equals("email") && !android.util.Patterns.EMAIL_ADDRESS.matcher(identifier).matches()) {
            tilIdentifier.setError(getString(R.string.error_invalid_email));
            etIdentifier.requestFocus();
            return;
        } else if (loginType.equals("nisn") && !identifier.matches("\\d{10}")) {
            tilIdentifier.setError(getString(R.string.error_invalid_nisn));
            etIdentifier.requestFocus();
            return;
        } else if (loginType.equals("nip") && !identifier.matches("\\d{18}")) {
            tilIdentifier.setError(getString(R.string.error_invalid_nip));
            etIdentifier.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            tilPassword.setError(getString(R.string.error_field_required));
            etPassword.requestFocus();
            return;
        }

        // Show loading state
        setLoading(true);

        // Create login request
        LoginRequest loginRequest = new LoginRequest(identifier, password, loginType);

        // Make API call
        apiService.login(loginRequest).enqueue(new Callback<LoginResponse>() {
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

                    Toast.makeText(LoginActivity.this, 
                        getString(R.string.success_login), 
                        Toast.LENGTH_SHORT).show();

                    navigateToMain();
                    finish();
                } else {
                    try {
                        String errorMessage = response.errorBody() != null ? 
                            response.errorBody().string() : 
                            getString(R.string.error_login_failed);
                        Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(LoginActivity.this, 
                            getString(R.string.error_login_failed), 
                            Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                setLoading(false);
                Toast.makeText(LoginActivity.this, 
                    getString(R.string.error_network), 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setLoading(boolean isLoading) {
        btnLogin.setEnabled(!isLoading);
        btnLogin.setText(isLoading ? R.string.loading : R.string.btn_login);
        spinnerLoginType.setEnabled(!isLoading);
        etIdentifier.setEnabled(!isLoading);
        etPassword.setEnabled(!isLoading);
        tvRegister.setEnabled(!isLoading);
    }

    private void navigateToMain() {
        // For now, navigate to MaterialListActivity
        // TODO: Implement proper navigation based on user role
        Intent intent = new Intent(this, MaterialListActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}