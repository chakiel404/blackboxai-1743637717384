package com.smartapp.activities;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.smartapp.R;
import com.smartapp.models.Material;
import com.smartapp.network.APIClient;
import com.smartapp.network.APIService;

import java.text.SimpleDateFormat;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MaterialDetailActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 1;

    private ImageView fileTypeIcon;
    private TextView titleText;
    private TextView subjectText;
    private TextView uploadedByText;
    private TextView uploadDateText;
    private TextView fileSizeText;
    private TextView descriptionText;
    private View progressBar;
    private FloatingActionButton fabDownload;

    private APIService apiService;
    private Material material;
    private SimpleDateFormat dateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_material_detail);

        // Initialize API service
        apiService = APIClient.getClient().create(APIService.class);
        dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

        // Set up toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Initialize views
        initializeViews();

        // Get material ID from intent
        int materialId = getIntent().getIntExtra("materialId", -1);
        if (materialId != -1) {
            loadMaterialDetails(materialId);
        } else {
            finish();
            return;
        }

        // Set up download FAB
        fabDownload.setOnClickListener(v -> {
            if (material != null) {
                if (checkPermission()) {
                    startDownload();
                } else {
                    requestPermission();
                }
            }
        });
    }

    private void initializeViews() {
        fileTypeIcon = findViewById(R.id.fileTypeIcon);
        titleText = findViewById(R.id.titleText);
        subjectText = findViewById(R.id.subjectText);
        uploadedByText = findViewById(R.id.uploadedByText);
        uploadDateText = findViewById(R.id.uploadDateText);
        fileSizeText = findViewById(R.id.fileSizeText);
        descriptionText = findViewById(R.id.descriptionText);
        progressBar = findViewById(R.id.progressBar);
        fabDownload = findViewById(R.id.fabDownload);
    }

    private void loadMaterialDetails(int materialId) {
        showLoading();
        
        apiService.getMaterial(materialId).enqueue(new Callback<Material>() {
            @Override
            public void onResponse(Call<Material> call, Response<Material> response) {
                hideLoading();
                
                if (response.isSuccessful() && response.body() != null) {
                    material = response.body();
                    displayMaterialDetails();
                } else {
                    showError("Failed to load material details");
                    finish();
                }
            }

            @Override
            public void onFailure(Call<Material> call, Throwable t) {
                hideLoading();
                showError("Network error. Please try again.");
                finish();
            }
        });
    }

    private void displayMaterialDetails() {
        // Set toolbar title
        getSupportActionBar().setTitle(material.getTitle());

        // Set file type icon based on file type
        setFileTypeIcon(material.getFileType());

        // Set text fields
        titleText.setText(material.getTitle());
        subjectText.setText(material.getSubject());
        uploadedByText.setText(material.getUploadedBy());
        uploadDateText.setText(dateFormat.format(material.getUploadDate()));
        fileSizeText.setText(material.getFormattedFileSize());
        descriptionText.setText(material.getDescription());
    }

    private void setFileTypeIcon(String fileType) {
        if (fileType == null) return;

        int iconRes;
        switch (fileType.toLowerCase()) {
            case "pdf":
                iconRes = android.R.drawable.ic_menu_agenda; // Replace with your PDF icon
                break;
            case "docx":
            case "doc":
                iconRes = android.R.drawable.ic_menu_edit; // Replace with your DOC icon
                break;
            default:
                iconRes = android.R.drawable.ic_menu_help; // Default icon
        }
        fileTypeIcon.setImageResource(iconRes);
    }

    private boolean checkPermission() {
        return ContextCompat.checkSelfPermission(this, 
            Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this,
            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
            PERMISSION_REQUEST_CODE);
    }

    private void startDownload() {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(material.getFileUrl()));
        request.setTitle(material.getTitle());
        request.setDescription("Downloading material...");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, material.getTitle());

        DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        if (manager != null) {
            manager.enqueue(request);
            Toast.makeText(this, "Download started", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Download failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        fabDownload.setVisibility(View.GONE);
    }

    private void hideLoading() {
        progressBar.setVisibility(View.GONE);
        fabDownload.setVisibility(View.VISIBLE);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startDownload();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}