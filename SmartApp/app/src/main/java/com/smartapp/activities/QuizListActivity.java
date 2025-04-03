package com.smartapp.activities;

import android.content.Intent;
import android.os.Bundle;
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
import com.smartapp.adapters.QuizAdapter;
import com.smartapp.models.Quiz;
import com.smartapp.network.APIClient;
import com.smartapp.network.APIService;
import com.smartapp.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QuizListActivity extends AppCompatActivity implements QuizAdapter.OnQuizClickListener {

    private RecyclerView recyclerView;
    private QuizAdapter adapter;
    private SwipeRefreshLayout swipeRefresh;
    private View progressBar;
    private View emptyView;
    private FloatingActionButton fabAddQuiz;
    private Spinner filterSpinner;

    private APIService apiService;
    private SessionManager sessionManager;
    private List<Quiz> quizzes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_list);

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
            fabAddQuiz.setVisibility(View.VISIBLE);
            fabAddQuiz.setOnClickListener(v -> 
                startActivity(new Intent(this, CreateQuizActivity.class)));
        }

        // Set up SwipeRefreshLayout
        swipeRefresh.setOnRefreshListener(this::loadQuizzes);

        // Initial load
        loadQuizzes();
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.quizRecyclerView);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        progressBar = findViewById(R.id.progressBar);
        emptyView = findViewById(R.id.emptyView);
        fabAddQuiz = findViewById(R.id.fabAddQuiz);
        filterSpinner = findViewById(R.id.filterSpinner);
    }

    private void setupFilterSpinner() {
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this,
            R.array.quiz_filters, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterSpinner.setAdapter(spinnerAdapter);
        filterSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                filterQuizzes(position);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new QuizAdapter(this, quizzes, this);
        recyclerView.setAdapter(adapter);
    }

    private void loadQuizzes() {
        showLoading();
        
        apiService.getQuizzes().enqueue(new Callback<List<Quiz>>() {
            @Override
            public void onResponse(Call<List<Quiz>> call, Response<List<Quiz>> response) {
                hideLoading();
                
                if (response.isSuccessful() && response.body() != null) {
                    quizzes = response.body();
                    filterQuizzes(filterSpinner.getSelectedItemPosition());
                } else {
                    showError("Failed to load quizzes");
                }
            }

            @Override
            public void onFailure(Call<List<Quiz>> call, Throwable t) {
                hideLoading();
                showError("Network error. Please try again.");
            }
        });
    }

    private void filterQuizzes(int filterPosition) {
        List<Quiz> filteredQuizzes = new ArrayList<>();
        
        switch (filterPosition) {
            case 0: // All
                filteredQuizzes = quizzes;
                break;
            case 1: // Available
                for (Quiz quiz : quizzes) {
                    if (quiz.isAvailable()) {
                        filteredQuizzes.add(quiz);
                    }
                }
                break;
            case 2: // Completed
                for (Quiz quiz : quizzes) {
                    if (quiz.isCompleted()) {
                        filteredQuizzes.add(quiz);
                    }
                }
                break;
            case 3: // Upcoming
                for (Quiz quiz : quizzes) {
                    if (quiz.isActive() && !quiz.isAvailable() && !quiz.isCompleted()) {
                        filteredQuizzes.add(quiz);
                    }
                }
                break;
        }
        
        adapter.updateData(filteredQuizzes);
        updateEmptyView(filteredQuizzes.isEmpty());
    }

    @Override
    public void onQuizClick(Quiz quiz) {
        if (!quiz.isActive()) {
            showError("This quiz is not active");
            return;
        }

        if (quiz.isCompleted()) {
            // Show quiz results
            Intent intent = new Intent(this, QuizResultActivity.class);
            intent.putExtra("quizId", quiz.getId());
            startActivity(intent);
        } else if (quiz.isAvailable()) {
            // Start quiz
            Intent intent = new Intent(this, QuizActivity.class);
            intent.putExtra("quizId", quiz.getId());
            startActivity(intent);
        } else {
            showError("This quiz is not yet available");
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