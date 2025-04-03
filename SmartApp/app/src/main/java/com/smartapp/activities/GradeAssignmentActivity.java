package com.smartapp.activities;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.smartapp.R;
import com.smartapp.models.Assignment;
import com.smartapp.network.APIClient;
import com.smartapp.network.APIService;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GradeAssignmentActivity extends AppCompatActivity {

    private TextView titleText;
    private Chip statusChip;
    private TextView studentNameText;
    private TextView submissionDateText;
    private TextView fileNameText;
    private TextView fileSizeText;
    private MaterialButton downloadButton;
    private TextInputLayout gradeLayout;
    private TextInputEditText gradeInput;
    private TextInputLayout feedbackLayout;
    private TextInputEditText feedbackInput;
    private MaterialButton submitButton;
    private View progressBar;

    private APIService apiService;
    private Assignment assignment;
    private SimpleDateFormat dateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grade_assignment);

        // Initialize API service and date formatter
        apiService = APIClient.getClient().create(APIService.class);
        dateFormat = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());

        // Set up toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Initialize views
        initializeViews();

        // Get assignment ID from intent
        int assignmentId = getIntent().getIntExtra("assignmentId", -1);
        if (assignmentId != -1) {
            loadAssignment(assignmentId);
        } else {
            finish();
            return;
        }

        // Set click listeners
        downloadButton.setOnClickListener(v -> downloadSubmission());
        submitButton.setOnClickListener(v -> submitGrade());
    }

    private void initializeViews() {
        titleText = findViewById(R.id.titleText);
        statusChip = findViewById(R.id.statusChip);
        studentNameText = findViewById(R.id.studentNameText);
        submissionDateText = findViewById(R.id.submissionDateText);
        fileNameText = findViewById(R.id.fileNameText);
        fileSizeText = findViewById(R.id.fileSizeText);
        downloadButton = findViewById(R.id.downloadButton);
        gradeLayout = findViewById(R.id.gradeLayout);
        gradeInput = findViewById(R.id.gradeInput);
        feedbackLayout = findViewById(R.id.feedbackLayout);
        feedbackInput = findViewById(R.id.feedbackInput);
        submitButton = findViewById(R.id.submitButton);
        progressBar = findViewById(R.id.progressBar);
    }

    private void loadAssignment(int assignmentId) {
        showLoading();
        
        apiService.getAssignment(assignmentId).enqueue(new Callback<Assignment>() {
            @Override
            public void onResponse(Call<Assignment> call, Response<Assignment> response) {
                hideLoading();
                
                if (response.isSuccessful() && response.body() != null) {
                    assignment = response.body();
                    displayAssignment();
                } else {
                    showError("Failed to load assignment");
                    finish();
                }
            }

            @Override
            public void onFailure(Call<Assignment> call, Throwable t) {
                hideLoading();
                showError("Network error. Please try again.");
                finish();
            }
        });
    }

    private void displayAssignment() {
        // Set toolbar title
        getSupportActionBar().setTitle("Grade Assignment");

        // Set basic info
        titleText.setText(assignment.getTitle());
        studentNameText.setText("Student: " + assignment.getAssignedBy()); // Assuming this field contains student name
        submissionDateText.setText("Submitted: " + dateFormat.format(assignment.getSubmissionDate()));

        // Set status chip
        setupStatusChip();

        // Set file info
        fileNameText.setText(assignment.getSubmittedFileName());
        fileSizeText.setText(assignment.getFormattedFileSize());

        // Set existing grade and feedback if available
        if (assignment.isGraded()) {
            gradeInput.setText(String.valueOf(assignment.getGrade()));
            feedbackInput.setText(assignment.getFeedback());
        }
    }

    private void setupStatusChip() {
        int bgColor;
        String status;
        
        if (assignment.isGraded()) {
            status = "Graded";
            bgColor = R.color.success;
        } else {
            status = "Submitted";
            bgColor = R.color.primary;
        }

        statusChip.setText(status);
        statusChip.setChipBackgroundColorResource(bgColor);
    }

    private void downloadSubmission() {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(assignment.getSubmittedFileUrl()));
        request.setTitle(assignment.getSubmittedFileName());
        request.setDescription("Downloading submitted file...");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, 
            assignment.getSubmittedFileName());

        DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        if (manager != null) {
            manager.enqueue(request);
            Toast.makeText(this, "Download started", Toast.LENGTH_SHORT).show();
        } else {
            showError("Download failed");
        }
    }

    private void submitGrade() {
        // Validate inputs
        String gradeStr = gradeInput.getText().toString().trim();
        String feedback = feedbackInput.getText().toString().trim();

        if (TextUtils.isEmpty(gradeStr)) {
            gradeLayout.setError("Grade is required");
            return;
        }

        int grade = Integer.parseInt(gradeStr);
        if (grade < 0 || grade > 100) {
            gradeLayout.setError("Grade must be between 0 and 100");
            return;
        }

        if (TextUtils.isEmpty(feedback)) {
            feedbackLayout.setError("Feedback is required");
            return;
        }

        // Clear errors
        gradeLayout.setError(null);
        feedbackLayout.setError(null);

        // Prepare grade data
        Map<String, Object> gradeData = new HashMap<>();
        gradeData.put("grade", grade);
        gradeData.put("feedback", feedback);

        // Submit grade
        showLoading();
        apiService.gradeAssignment(assignment.getId(), gradeData).enqueue(new Callback<Assignment>() {
            @Override
            public void onResponse(Call<Assignment> call, Response<Assignment> response) {
                hideLoading();
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(GradeAssignmentActivity.this, 
                        "Assignment graded successfully", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    showError("Failed to submit grade");
                }
            }

            @Override
            public void onFailure(Call<Assignment> call, Throwable t) {
                hideLoading();
                showError("Network error. Please try again.");
            }
        });
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideLoading() {
        progressBar.setVisibility(View.GONE);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}