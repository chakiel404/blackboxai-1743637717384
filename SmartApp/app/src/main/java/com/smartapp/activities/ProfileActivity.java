package com.smartapp.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.smartapp.R;
import com.smartapp.models.Student;
import com.smartapp.models.Teacher;
import com.smartapp.network.APIClient;
import com.smartapp.network.APIService;
import com.smartapp.utils.SessionManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.text.TextUtils;

public class ProfileActivity extends AppCompatActivity {
    private TextInputLayout tilEmail;
    private TextInputEditText etEmail;
    private TextInputLayout tilFullName;
    private TextInputEditText etFullName;
    private TextInputLayout tilIdentifier;
    private TextInputEditText etIdentifier;
    private TextInputLayout tilPhone;
    private TextInputEditText etPhone;
    private TextInputLayout tilBirthDate;
    private TextInputEditText etBirthDate;
    private TextInputLayout tilBirthPlace;
    private TextInputEditText etBirthPlace;
    private TextInputLayout tilAddress;
    private TextInputEditText etAddress;
    private Spinner spinnerGender;
    
    // Student-specific fields
    private View studentFields;
    private TextInputLayout tilClass;
    private TextInputEditText etClass;
    private TextInputLayout tilParentName;
    private TextInputEditText etParentName;
    private TextInputLayout tilParentPhone;
    private TextInputEditText etParentPhone;

    // Teacher-specific fields
    private View teacherFields;
    private TextInputLayout tilEducationLevel;
    private TextInputEditText etEducationLevel;
    private TextInputLayout tilMajor;
    private TextInputEditText etMajor;
    private TextInputLayout tilJoinDate;
    private TextInputEditText etJoinDate;

    // Admin-specific fields
    private View adminFields;
    private TextInputLayout tilAdminCode;
    private TextInputEditText etAdminCode;
    private TextInputLayout tilRole;
    private TextInputEditText etRole;

    private MaterialButton btnSave;
    private APIService apiService;
    private SessionManager sessionManager;
    private Calendar calendar;
    private SimpleDateFormat dateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize components
        initializeViews();
        setupDatePickers();
        setupGenderSpinner();

        // Initialize API and session manager
        apiService = APIClient.getClient().create(APIService.class);
        sessionManager = new SessionManager(this);

        // Show/hide role-specific fields and set read-only fields
        if (sessionManager.isStudent()) {
            studentFields.setVisibility(View.VISIBLE);
            teacherFields.setVisibility(View.GONE);
            adminFields.setVisibility(View.GONE);
            tilIdentifier.setHint(getString(R.string.hint_nisn));
            
            // Set student-specific read-only fields
            etIdentifier.setEnabled(false); // NISN
            etIdentifier.setFocusable(false);
            etClass.setEnabled(false);
            etClass.setFocusable(false);
            
            loadStudentProfile();
        } else if (sessionManager.isTeacher()) {
            studentFields.setVisibility(View.GONE);
            teacherFields.setVisibility(View.VISIBLE);
            adminFields.setVisibility(View.GONE);
            tilIdentifier.setHint(getString(R.string.hint_nip));
            
            // Set teacher-specific read-only fields
            etIdentifier.setEnabled(false); // NIP
            etIdentifier.setFocusable(false);
            etEducationLevel.setEnabled(false);
            etEducationLevel.setFocusable(false);
            etMajor.setEnabled(false);
            etMajor.setFocusable(false);
            etJoinDate.setEnabled(false);
            etJoinDate.setFocusable(false);
            
            loadTeacherProfile();
        } else if (sessionManager.isAdmin()) {
            studentFields.setVisibility(View.GONE);
            teacherFields.setVisibility(View.GONE);
            adminFields.setVisibility(View.VISIBLE);
            tilIdentifier.setHint(getString(R.string.hint_admin_code));
            loadAdminProfile();
        }

        // Set common read-only fields for non-admin users
        if (!sessionManager.isAdmin()) {
            etEmail.setEnabled(false);
            etEmail.setFocusable(false);
        }

        // Set up save button
        btnSave.setOnClickListener(v -> saveProfile());
    }

    private void initializeViews() {
        tilEmail = findViewById(R.id.tilEmail);
        etEmail = findViewById(R.id.etEmail);
        tilFullName = findViewById(R.id.tilFullName);
        etFullName = findViewById(R.id.etFullName);
        tilIdentifier = findViewById(R.id.tilIdentifier);
        etIdentifier = findViewById(R.id.etIdentifier);
        tilPhone = findViewById(R.id.tilPhone);
        etPhone = findViewById(R.id.etPhone);
        tilBirthDate = findViewById(R.id.tilBirthDate);
        etBirthDate = findViewById(R.id.etBirthDate);
        tilBirthPlace = findViewById(R.id.tilBirthPlace);
        etBirthPlace = findViewById(R.id.etBirthPlace);
        tilAddress = findViewById(R.id.tilAddress);
        etAddress = findViewById(R.id.etAddress);
        spinnerGender = findViewById(R.id.spinnerGender);

        studentFields = findViewById(R.id.studentFields);
        tilClass = findViewById(R.id.tilClass);
        etClass = findViewById(R.id.etClass);
        tilParentName = findViewById(R.id.tilParentName);
        etParentName = findViewById(R.id.etParentName);
        tilParentPhone = findViewById(R.id.tilParentPhone);
        etParentPhone = findViewById(R.id.etParentPhone);

        teacherFields = findViewById(R.id.teacherFields);
        tilEducationLevel = findViewById(R.id.tilEducationLevel);
        etEducationLevel = findViewById(R.id.etEducationLevel);
        tilMajor = findViewById(R.id.tilMajor);
        etMajor = findViewById(R.id.etMajor);
        tilJoinDate = findViewById(R.id.tilJoinDate);
        etJoinDate = findViewById(R.id.etJoinDate);

        adminFields = findViewById(R.id.adminFields);
        tilAdminCode = findViewById(R.id.tilAdminCode);
        etAdminCode = findViewById(R.id.etAdminCode);
        tilRole = findViewById(R.id.tilRole);
        etRole = findViewById(R.id.etRole);

        btnSave = findViewById(R.id.btnSave);
    }

    private void loadAdminProfile() {
        String token = "Bearer " + sessionManager.getAuthToken();
        showLoading(true);
        apiService.getAdminProfile(token).enqueue(new Callback<Admin>() {
            @Override
            public void onResponse(Call<Admin> call, Response<Admin> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    Admin admin = response.body();
                    populateAdminData(admin);
                } else {
                    showError(getString(R.string.error_loading_profile));
                }
            }

            @Override
            public void onFailure(Call<Admin> call, Throwable t) {
                showLoading(false);
                showError(getString(R.string.error_loading_profile) + ": " + t.getMessage());
            }
        });
    }

    private void populateAdminData(Admin admin) {
        etEmail.setText(admin.getEmail());
        etFullName.setText(admin.getFullName());
        etIdentifier.setText(admin.getAdminCode());
        etPhone.setText(admin.getPhone());
        etBirthDate.setText(admin.getBirthDate());
        etBirthPlace.setText(admin.getBirthPlace());
        etAddress.setText(admin.getAddress());
        etAdminCode.setText(admin.getAdminCode());
        etRole.setText(admin.getRole());

        if ("L".equals(admin.getGender())) {
            spinnerGender.setSelection(0);
        } else if ("P".equals(admin.getGender())) {
            spinnerGender.setSelection(1);
        }
    }

    private void setupDatePickers() {
        calendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

        DatePickerDialog.OnDateSetListener dateSetListener = (view, year, month, day) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, day);
            updateDateField();
        };

        etBirthDate.setOnClickListener(v -> new DatePickerDialog(ProfileActivity.this, dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show());

        if (sessionManager.isTeacher()) {
            etJoinDate.setOnClickListener(v -> new DatePickerDialog(ProfileActivity.this, dateSetListener,
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)).show());
        }
    }

    private void setupGenderSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.genders, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(adapter);
    }

    private void updateDateField() {
        String date = dateFormat.format(calendar.getTime());
        if (etBirthDate.hasFocus()) {
            etBirthDate.setText(date);
        } else if (etJoinDate != null && etJoinDate.hasFocus()) {
            etJoinDate.setText(date);
        }
    }

    private void loadStudentProfile() {
        String token = "Bearer " + sessionManager.getAuthToken();
        showLoading(true);
        apiService.getStudentProfile(token).enqueue(new Callback<Student>() {
            @Override
            public void onResponse(Call<Student> call, Response<Student> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    Student student = response.body();
                    populateStudentData(student);
                } else {
                    showError(getString(R.string.error_loading_profile));
                }
            }

            @Override
            public void onFailure(Call<Student> call, Throwable t) {
                showLoading(false);
                showError(getString(R.string.error_loading_profile) + ": " + t.getMessage());
            }
        });
    }

    private void loadTeacherProfile() {
        String token = "Bearer " + sessionManager.getAuthToken();
        showLoading(true);
        apiService.getTeacherProfile(token).enqueue(new Callback<Teacher>() {
            @Override
            public void onResponse(Call<Teacher> call, Response<Teacher> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    Teacher teacher = response.body();
                    populateTeacherData(teacher);
                } else {
                    showError(getString(R.string.error_loading_profile));
                }
            }

            @Override
            public void onFailure(Call<Teacher> call, Throwable t) {
                showLoading(false);
                showError(getString(R.string.error_loading_profile) + ": " + t.getMessage());
            }
        });
    }

    private void showLoading(boolean show) {
        if (show) {
            btnSave.setEnabled(false);
            btnSave.setText(R.string.loading);
        } else {
            btnSave.setEnabled(true);
            btnSave.setText(R.string.save);
        }
    }

    private void showError(String message) {
        Toast.makeText(ProfileActivity.this, message, Toast.LENGTH_LONG).show();
    }

    private void populateStudentData(Student student) {
        etEmail.setText(student.getEmail());
        etFullName.setText(student.getFullName());
        etIdentifier.setText(student.getNisn());
        etPhone.setText(student.getPhone());
        etBirthDate.setText(student.getBirthDate());
        etBirthPlace.setText(student.getBirthPlace());
        etAddress.setText(student.getAddress());
        etClass.setText(student.getClassName());
        etParentName.setText(student.getParentName());
        etParentPhone.setText(student.getParentPhone());

        if ("L".equals(student.getGender())) {
            spinnerGender.setSelection(0);
        } else if ("P".equals(student.getGender())) {
            spinnerGender.setSelection(1);
        }
    }

    private void populateTeacherData(Teacher teacher) {
        etEmail.setText(teacher.getEmail());
        etFullName.setText(teacher.getFullName());
        etIdentifier.setText(teacher.getNip());
        etPhone.setText(teacher.getPhone());
        etBirthDate.setText(teacher.getBirthDate());
        etBirthPlace.setText(teacher.getBirthPlace());
        etAddress.setText(teacher.getAddress());
        etEducationLevel.setText(teacher.getEducationLevel());
        etMajor.setText(teacher.getMajor());
        etJoinDate.setText(teacher.getJoinDate());

        if ("L".equals(teacher.getGender())) {
            spinnerGender.setSelection(0);
        } else if ("P".equals(teacher.getGender())) {
            spinnerGender.setSelection(1);
        }
    }

    private void saveProfile() {
        if (!validateInput()) {
            return;
        }

        showLoading(true);
        String token = "Bearer " + sessionManager.getAuthToken();
        
        if (sessionManager.isStudent()) {
            updateStudentProfile(token);
        } else if (sessionManager.isTeacher()) {
            updateTeacherProfile(token);
        } else if (sessionManager.isAdmin()) {
            updateAdminProfile(token);
        }
    }

    private void updateAdminProfile(String token) {
        Admin admin = new Admin();
        admin.setEmail(etEmail.getText().toString().trim());
        admin.setFullName(etFullName.getText().toString().trim());
        admin.setAdminCode(etIdentifier.getText().toString().trim());
        admin.setPhone(etPhone.getText().toString().trim());
        admin.setBirthDate(etBirthDate.getText().toString().trim());
        admin.setBirthPlace(etBirthPlace.getText().toString().trim());
        admin.setAddress(etAddress.getText().toString().trim());
        admin.setAdminCode(etAdminCode.getText().toString().trim());
        admin.setRole(etRole.getText().toString().trim());
        admin.setGender(spinnerGender.getSelectedItemPosition() == 0 ? "L" : "P");

        apiService.updateAdminProfile(token, admin).enqueue(new Callback<Admin>() {
            @Override
            public void onResponse(Call<Admin> call, Response<Admin> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    showSuccess();
                } else {
                    showError(getString(R.string.error_update_profile));
                }
            }

            @Override
            public void onFailure(Call<Admin> call, Throwable t) {
                showLoading(false);
                showError(getString(R.string.error_update_profile) + ": " + t.getMessage());
            }
        });
    }

    private void updateStudentProfile(String token) {
        Student student = new Student();
        // Only update editable fields
        student.setFullName(etFullName.getText().toString().trim());
        student.setPhone(etPhone.getText().toString().trim());
        student.setBirthDate(etBirthDate.getText().toString().trim());
        student.setBirthPlace(etBirthPlace.getText().toString().trim());
        student.setAddress(etAddress.getText().toString().trim());
        student.setParentName(etParentName.getText().toString().trim());
        student.setParentPhone(etParentPhone.getText().toString().trim());
        student.setGender(spinnerGender.getSelectedItemPosition() == 0 ? "L" : "P");
        
        // Keep original values for restricted fields
        student.setEmail(etEmail.getText().toString().trim()); // Original email
        student.setNisn(etIdentifier.getText().toString().trim()); // Original NISN
        student.setClassName(etClass.getText().toString().trim()); // Original class

        apiService.updateStudentProfile(token, student).enqueue(new Callback<Student>() {
            @Override
            public void onResponse(Call<Student> call, Response<Student> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    showSuccess();
                } else {
                    showError(getString(R.string.error_update_profile));
                }
            }

            @Override
            public void onFailure(Call<Student> call, Throwable t) {
                showLoading(false);
                showError(getString(R.string.error_update_profile) + ": " + t.getMessage());
            }
        });
    }

    private void updateTeacherProfile(String token) {
        Teacher teacher = new Teacher();
        // Only update editable fields
        teacher.setFullName(etFullName.getText().toString().trim());
        teacher.setPhone(etPhone.getText().toString().trim());
        teacher.setBirthDate(etBirthDate.getText().toString().trim());
        teacher.setBirthPlace(etBirthPlace.getText().toString().trim());
        teacher.setAddress(etAddress.getText().toString().trim());
        teacher.setGender(spinnerGender.getSelectedItemPosition() == 0 ? "L" : "P");
        
        // Keep original values for restricted fields
        teacher.setEmail(etEmail.getText().toString().trim()); // Original email
        teacher.setNip(etIdentifier.getText().toString().trim()); // Original NIP
        teacher.setEducationLevel(etEducationLevel.getText().toString().trim()); // Original education
        teacher.setMajor(etMajor.getText().toString().trim()); // Original major
        teacher.setJoinDate(etJoinDate.getText().toString().trim()); // Original join date

        apiService.updateTeacherProfile(token, teacher).enqueue(new Callback<Teacher>() {
            @Override
            public void onResponse(Call<Teacher> call, Response<Teacher> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    showSuccess();
                } else {
                    showError(getString(R.string.error_update_profile));
                }
            }

            @Override
            public void onFailure(Call<Teacher> call, Throwable t) {
                showLoading(false);
                showError(getString(R.string.error_update_profile) + ": " + t.getMessage());
            }
        });
    }

    private boolean validateInput() {
        boolean isValid = true;
        
        // Clear previous errors
        tilEmail.setError(null);
        tilFullName.setError(null);
        tilPhone.setError(null);
        tilBirthDate.setError(null);
        tilBirthPlace.setError(null);
        tilAddress.setError(null);

        // Validate common fields
        String email = etEmail.getText().toString().trim();
        String fullName = etFullName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String birthDate = etBirthDate.getText().toString().trim();
        String birthPlace = etBirthPlace.getText().toString().trim();
        String address = etAddress.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            tilEmail.setError(getString(R.string.error_field_required));
            isValid = false;
        }

        if (TextUtils.isEmpty(fullName)) {
            tilFullName.setError(getString(R.string.error_field_required));
            isValid = false;
        }

        if (TextUtils.isEmpty(phone)) {
            tilPhone.setError(getString(R.string.error_field_required));
            isValid = false;
        }

        if (TextUtils.isEmpty(birthDate)) {
            tilBirthDate.setError(getString(R.string.error_field_required));
            isValid = false;
        }

        if (TextUtils.isEmpty(birthPlace)) {
            tilBirthPlace.setError(getString(R.string.error_field_required));
            isValid = false;
        }

        if (TextUtils.isEmpty(address)) {
            tilAddress.setError(getString(R.string.error_field_required));
            isValid = false;
        }

        // Validate student-specific fields
        if (sessionManager.isStudent()) {
            String className = etClass.getText().toString().trim();
            String parentName = etParentName.getText().toString().trim();
            String parentPhone = etParentPhone.getText().toString().trim();

            if (TextUtils.isEmpty(className)) {
                tilClass.setError(getString(R.string.error_field_required));
                isValid = false;
            }

            if (TextUtils.isEmpty(parentName)) {
                tilParentName.setError(getString(R.string.error_field_required));
                isValid = false;
            }

            if (TextUtils.isEmpty(parentPhone)) {
                tilParentPhone.setError(getString(R.string.error_field_required));
                isValid = false;
            }
        }

        // Validate teacher-specific fields
        if (sessionManager.isTeacher()) {
            String educationLevel = etEducationLevel.getText().toString().trim();
            String major = etMajor.getText().toString().trim();
            String joinDate = etJoinDate.getText().toString().trim();

            if (TextUtils.isEmpty(educationLevel)) {
                tilEducationLevel.setError(getString(R.string.error_field_required));
                isValid = false;
            }

            if (TextUtils.isEmpty(major)) {
                tilMajor.setError(getString(R.string.error_field_required));
                isValid = false;
            }

            if (TextUtils.isEmpty(joinDate)) {
                tilJoinDate.setError(getString(R.string.error_field_required));
                isValid = false;
            }
        }

        // Validate admin-specific fields
        if (sessionManager.isAdmin()) {
            String adminCode = etAdminCode.getText().toString().trim();
            String role = etRole.getText().toString().trim();

            if (TextUtils.isEmpty(adminCode)) {
                tilAdminCode.setError(getString(R.string.error_field_required));
                isValid = false;
            }

            if (TextUtils.isEmpty(role)) {
                tilRole.setError(getString(R.string.error_field_required));
                isValid = false;
            }
        }

        return isValid;
    }

    private void showSuccess() {
        Toast.makeText(this, getString(R.string.success_update_profile), Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
    }
}