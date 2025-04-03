package com.smartapp.activities;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.smartapp.R;
import com.smartapp.models.Assignment;
import com.smartapp.network.APIClient;
import com.smartapp.network.APIService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AssignmentDetailActivity extends AppCompatActivity {

    private TextView titleText;
    private Chip statusChip;
    private TextView subjectText;
    private TextView dueDateText;
    private TextView descriptionText;
    private TextView fileNameText;
    private TextView fileSizeText;
    private MaterialButton downloadButton;
    private TextView submissionStatusText;
    private MaterialButton uploadButton;
    private View gradeCard;
    private TextView gradeText;
    private TextView feedbackText;
    private View progressBar;

    private APIService apiService;
    private Assignment assignment;
    private SimpleDateFormat dateFormat;

    private ActivityResultLauncher<Intent> filePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assignment_detail);

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

        // Set up file picker launcher
        setupFilePicker();

        // Get assignment ID from intent
        int assignmentId = getIntent().getIntExtra("assignmentId", -1);
        if (assignmentId != -1) {
            loadAssignment(assignmentId);
        } else {
            finish();
            return;
        }

        // Set click listeners
        downloadButton.setOnClickListener(v -> downloadAssignmentFile());
        uploadButton.setOnClickListener(v -> openFilePicker());
    }

    private void initializeViews() {
        titleText = findViewById(R.id.titleText);
        statusChip = findViewById(R.id.statusChip);
        subjectText = findViewById(R.id.subjectText);
        dueDateText = findViewById(R.id.dueDateText);
        descriptionText = findViewById(R.id.descriptionText);
        fileNameText = findViewById(R.id.fileNameText);
        fileSizeText = findViewById(R.id.fileSizeText);
        downloadButton = findViewById(R.id.downloadButton);
        submissionStatusText = findViewById(R.id.submissionStatusText);
        uploadButton = findViewById(R.id.uploadButton);
        gradeCard = findViewById(R.id.gradeCard);
        gradeText = findViewById(R.id.gradeText);
        feedbackText = findViewById(R.id.feedbackText);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupFilePicker() {
        filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri fileUri = result.getData().getData();
                    if (fileUri != null) {
                        uploadFile(fileUri);
                    }
                }
            }
        );
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
        getSupportActionBar().setTitle(assignment.getTitle());

        // Set basic info
        titleText.setText(assignment.getTitle());
        subjectText.setText(assignment.getSubject());
        dueDateText.setText("Due: " + dateFormat.format(assignment.getDueDate()));
        descriptionText.setText(assignment.getDescription());

        // Set status chip
        setupStatusChip();

        // Set file info
        fileNameText.setText(assignment.getFileName());
        fileSizeText.setText(assignment.getFormattedFileSize());

        // Set submission status
        updateSubmissionStatus();

        // Show grade if available
        if (assignment.isGraded()) {
            gradeCard.setVisibility(View.VISIBLE);
            gradeText.setText(String.format(Locale.getDefault(), "%d%%", assignment.getGrade()));
            feedbackText.setText(assignment.getFeedback());
        } else {
            gradeCard.setVisibility(View.GONE);
        }
    }

    private void setupStatusChip() {
        int bgColor;
        String status;
        
        if (assignment.isGraded()) {
            status = "Graded";
            bgColor = R.color.success;
        } else if (assignment.isSubmitted()) {
            status = "Submitted";
            bgColor = R.color.primary;
        } else if (assignment.isOverdue()) {
            status = "Overdue";
            bgColor = R.color.error;
        } else {
            status = "Pending";
            bgColor = R.color.secondary_text;
        }

        statusChip.setText(status);
        statusChip.setChipBackgroundColorResource(bgColor);
    }

    private void updateSubmissionStatus() {
        if (assignment.isSubmitted()) {
            submissionStatusText.setText("Submitted on: " + 
                dateFormat.format(assignment.getSubmissionDate()));
            uploadButton.setText("Update Submission");
        } else {
            submissionStatusText.setText("Not submitted yet");
            uploadButton.setText("Upload Submission");
        }
    }

    private void downloadAssignmentFile() {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(assignment.getFileUrl()));
        request.setTitle(assignment.getFileName());
        request.setDescription("Downloading assignment file...");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, assignment.getFileName());

        DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        if (manager != null) {
            manager.enqueue(request);
            Toast.makeText(this, "Download started", Toast.LENGTH_SHORT).show();
        } else {
            showError("Download failed");
        }
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        filePickerLauncher.launch(intent);
    }

    private void uploadFile(Uri fileUri) {
        try {
            String fileName = getFileName(fileUri);
            File file = createTempFile(fileUri);

            RequestBody requestFile = RequestBody.create(MediaType.parse(getContentResolver()
                .getType(fileUri)), file);
            MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", 
                fileName, requestFile);

            showLoading();
            apiService.uploadAssignment(assignment.getId(), filePart).enqueue(new Callback<Assignment>() {
                @Override
                public void onResponse(Call<Assignment> call, Response<Assignment> response) {
                    hideLoading();
                    if (response.isSuccessful() && response.body() != null) {
                        assignment = response.body();
                        displayAssignment();
                        Toast.makeText(AssignmentDetailActivity.this, 
                            "Assignment submitted successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        showError("Failed to submit assignment");
                    }
                }

                @Override
                public void onFailure(Call<Assignment> call, Throwable t) {
                    hideLoading();
                    showError("Network error. Please try again.");
                }
            });
        } catch (Exception e) {
            showError("Failed to prepare file for upload");
        }
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private File createTempFile(Uri uri) throws Exception {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        File tempFile = File.createTempFile("upload", null, getCacheDir());
        tempFile.deleteOnExit();

        FileOutputStream outputStream = new FileOutputStream(tempFile);
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        outputStream.close();
        inputStream.close();
        return tempFile;
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