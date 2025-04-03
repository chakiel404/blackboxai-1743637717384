package com.smartapp.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.smartapp.R;
import com.smartapp.adapters.QuizQuestionAdapter;
import com.smartapp.models.Quiz;
import com.smartapp.network.APIClient;
import com.smartapp.network.APIService;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QuizResultActivity extends AppCompatActivity {

    private TextView quizTitleText;
    private TextView scoreText;
    private TextView correctAnswersText;
    private TextView timeSpentText;
    private RecyclerView questionsRecyclerView;
    private View progressBar;

    private APIService apiService;
    private SimpleDateFormat dateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_result);

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

        // Get quiz ID from intent
        int quizId = getIntent().getIntExtra("quizId", -1);
        if (quizId != -1) {
            loadQuizResult(quizId);
        } else {
            finish();
            return;
        }
    }

    private void initializeViews() {
        quizTitleText = findViewById(R.id.quizTitleText);
        scoreText = findViewById(R.id.scoreText);
        correctAnswersText = findViewById(R.id.correctAnswersText);
        timeSpentText = findViewById(R.id.timeSpentText);
        questionsRecyclerView = findViewById(R.id.questionsRecyclerView);
        progressBar = findViewById(R.id.progressBar);

        // Set up RecyclerView
        questionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadQuizResult(int quizId) {
        showLoading();
        
        apiService.getQuiz(quizId).enqueue(new Callback<Quiz>() {
            @Override
            public void onResponse(Call<Quiz> call, Response<Quiz> response) {
                hideLoading();
                
                if (response.isSuccessful() && response.body() != null) {
                    displayQuizResult(response.body());
                } else {
                    showError("Failed to load quiz results");
                    finish();
                }
            }

            @Override
            public void onFailure(Call<Quiz> call, Throwable t) {
                hideLoading();
                showError("Network error. Please try again.");
                finish();
            }
        });
    }

    private void displayQuizResult(Quiz quiz) {
        // Set quiz title
        quizTitleText.setText(quiz.getTitle());

        // Set score
        scoreText.setText(String.format(Locale.getDefault(), "%d%%", quiz.getScore()));

        // Set correct answers count
        int correctCount = 0;
        for (Quiz.Question question : quiz.getQuestions()) {
            if (question.isCorrect()) {
                correctCount++;
            }
        }
        correctAnswersText.setText(String.format(Locale.getDefault(),
            "Correct Answers: %d/%d", correctCount, quiz.getQuestionCount()));

        // Set time spent
        long timeSpentMillis = quiz.getEndTime().getTime() - quiz.getStartTime().getTime();
        long minutes = TimeUnit.MILLISECONDS.toMinutes(timeSpentMillis);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(timeSpentMillis) -
                TimeUnit.MINUTES.toSeconds(minutes);
        timeSpentText.setText(String.format(Locale.getDefault(),
            "Time Spent: %02d:%02d", minutes, seconds));

        // Set up questions review
        QuizQuestionAdapter adapter = new QuizQuestionAdapter(quiz.getQuestions(), null);
        adapter.setQuizSubmitted(true); // Show correct/incorrect answers
        questionsRecyclerView.setAdapter(adapter);
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