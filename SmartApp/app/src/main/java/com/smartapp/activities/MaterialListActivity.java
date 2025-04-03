package com.smartapp.activities;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.smartapp.R;
import com.smartapp.adapters.MaterialAdapter;
import com.smartapp.models.Material;
import com.smartapp.network.APIClient;
import com.smartapp.network.APIService;
import com.smartapp.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MaterialListActivity extends AppCompatActivity implements MaterialAdapter.OnMaterialClickListener {

    private static final int PERMISSION_REQUEST_CODE = 1;

    private RecyclerView recyclerView;
    private MaterialAdapter adapter;
    private SwipeRefreshLayout swipeRefresh;
    private View progressBar;
    private View emptyView;
    private FloatingActionButton fabAddMaterial;

    private APIService apiService;
    private SessionManager sessionManager;
    private List<Material> materials = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_material_list);

        // Initialize network and session components
        apiService = APIClient.getClient().create(APIService.class);
        sessionManager = new SessionManager(this);

        // Set up toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Initialize views
        recyclerView = findViewById(R.id.materialsRecyclerView);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        progressBar = findViewById(R.id.progressBar);
        emptyView = findViewById(R.id.emptyView);
        fabAddMaterial = findViewById(R.id.fabAddMaterial);

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MaterialAdapter(this, materials, this);
        recyclerView.setAdapter(adapter);

        // Show FAB only for teachers
        if ("TEACHER".equals(sessionManager.getUserRole())) {
            fabAddMaterial.setVisibility(View.VISIBLE);
            fabAddMaterial.setOnClickListener(v -> 
                startActivity(new Intent(this, UploadMaterialActivity.class)));
        }

        // Set up SwipeRefreshLayout
        swipeRefresh.setOnRefreshListener(this::loadMaterials);

        // Initial load
        loadMaterials();
    }

    private void loadMaterials() {
        showLoading();
        
        apiService.getMaterials().enqueue(new Callback<List<Material>>() {
            @Override
            public void onResponse(Call<List<Material>> call, Response<List<Material>> response) {
                hideLoading();
                
                if (response.isSuccessful() && response.body() != null) {
                    materials = response.body();
                    adapter.updateData(materials);
                    updateEmptyView();
                } else {
                    showError("Failed to load materials");
                }
            }

            @Override
            public void onFailure(Call<List<Material>> call, Throwable t) {
                hideLoading();
                showError("Network error. Please try again.");
            }
        });
    }

    @Override
    public void onMaterialClick(Material material) {
        // Open material detail activity
        Intent intent = new Intent(this, MaterialDetailActivity.class);
        intent.putExtra("materialId", material.getId());
        startActivity(intent);
    }

    @Override
    public void onDownloadClick(Material material) {
        if (checkPermission()) {
            startDownload(material);
        } else {
            requestPermission();
        }
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

    private void startDownload(Material material) {
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
        swipeRefresh.setRefreshing(false);
    }

    private void hideLoading() {
        progressBar.setVisibility(View.GONE);
        swipeRefresh.setRefreshing(false);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void updateEmptyView() {
        emptyView.setVisibility(materials.isEmpty() ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(materials.isEmpty() ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}