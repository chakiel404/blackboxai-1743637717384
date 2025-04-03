package com.smartapp.activities;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.smartapp.R;
import com.smartapp.adapters.AssignmentAdapter;
import com.smartapp.models.Assignment;
import com.smartapp.network.APIClient;
import com.smartapp.network.APIService;
import com.smartapp.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AssignmentListActivity extends AppCompatActivity implements AssignmentAdapter.OnAssignmentClickListener {

    private RecyclerView recyclerView;
    private AssignmentAdapter adapter;
    private SwipeRefreshLayout swipeRefresh;
    private View progressBar;
    private View emptyView;
    private FloatingActionButton fabAddAssignment;
    private Spinner filterSpinner;

    private APIService apiService;
    private SessionManager sessionManager;
    private List<Assignment> assignments = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assignment_list);

        // Initialize network and session components
        apiService = APIClient.getClient().create(APIService.class);
        sessionManager = new SessionManager(this);

        // Set up toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Initialize views
        initializeViews();
        setupFilterSpinner();
        setupRecyclerView();

        // Show FAB only for teachers
        if ("TEACHER".equals(sessionManager.getUserRole())) {
            fabAddAssignment.setVisibility(View.VISIBLE);
            fabAddAssignment.setOnClickListener(v -> 
                startActivity(new Intent(this, CreateAssignmentActivity.class)));
        }

        // Set up SwipeRefreshLayout
        swipeRefresh.setOnRefreshListener(this::loadAssignments);

        // Initial load
        loadAssignments();
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.assignmentsRecyclerView);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        progressBar = findViewById(R.id.progressBar);
        emptyView = findViewById(R.id.emptyView);
        fabAddAssignment = findViewById(R.id.fabAddAssignment);
        filterSpinner = findViewById(R.id.filterSpinner);
    }

    private void setupFilterSpinner() {
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this,
            R.array.assignment_filters, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterSpinner.setAdapter(spinnerAdapter);
        filterSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                filterAssignments(position);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AssignmentAdapter(this, assignments, this);
        recyclerView.setAdapter(adapter);
    }

    private void loadAssignments() {
        showLoading();
        
        apiService.getAssignments().enqueue(new Callback<List<Assignment>>() {
            @Override
            public void onResponse(Call<List<Assignment>> call, Response<List<Assignment>> response) {
                hideLoading();
                
                if (response.isSuccessful() && response.body() != null) {
                    assignments = response.body();
                    filterAssignments(filterSpinner.getSelectedItemPosition());
                } else {
                    showError("Failed to load assignments");
                }
            }

            @Override
            public void onFailure(Call<List<Assignment>> call, Throwable t) {
                hideLoading();
                showError("Network error. Please try again.");
            }
        });
    }

    private void filterAssignments(int filterPosition) {
        List<Assignment> filteredAssignments = new ArrayList<>();
        
        switch (filterPosition) {
            case 0: // All
                filteredAssignments = assignments;
                break;
            case 1: // Pending
                for (Assignment assignment : assignments) {
                    if (!assignment.isSubmitted() && !assignment.isOverdue()) {
                        filteredAssignments.add(assignment);
                    }
                }
                break;
            case 2: // Submitted
                for (Assignment assignment : assignments) {
                    if (assignment.isSubmitted() && !assignment.isGraded()) {
                        filteredAssignments.add(assignment);
                    }
                }
                break;
            case 3: // Graded
                for (Assignment assignment : assignments) {
                    if (assignment.isGraded()) {
                        filteredAssignments.add(assignment);
                    }
                }
                break;
            case 4: // Overdue
                for (Assignment assignment : assignments) {
                    if (assignment.isOverdue()) {
                        filteredAssignments.add(assignment);
                    }
                }
                break;
        }
        
        adapter.updateData(filteredAssignments);
        updateEmptyView(filteredAssignments.isEmpty());
    }

    @Override
    public void onAssignmentClick(Assignment assignment) {
        Intent intent;
        if ("TEACHER".equals(sessionManager.getUserRole())) {
            // Teachers can grade assignments
            intent = new Intent(this, GradeAssignmentActivity.class);
        } else {
            // Students can view/submit assignments
            intent = new Intent(this, AssignmentDetailActivity.class);
        }
        intent.putExtra("assignmentId", assignment.getId());
        startActivity(intent);
    }

    @Override
    public void onFileClick(Assignment assignment) {
        // Download the assignment file
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

    private void updateEmptyView(boolean isEmpty) {
        emptyView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }
}