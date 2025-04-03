package com.smartapp.activities;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.smartapp.R;
import com.smartapp.adapters.QuizQuestionAdapter;
import com.smartapp.models.Quiz;
import com.smartapp.network.APIClient;
import com.smartapp.network.APIService;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QuizActivity extends AppCompatActivity implements QuizQuestionAdapter.OnAnswerSelectedListener {

    private TextView timerText;
    private TextView quizTitleText;
    private TextView questionCountText;
    private ViewPager2 questionViewPager;
    private MaterialButton prevButton;
    private MaterialButton nextButton;
    private MaterialButton submitButton;
    private View progressBar;

    private APIService apiService;
    private Quiz quiz;
    private QuizQuestionAdapter adapter;
    private CountDownTimer timer;
    private boolean isQuizSubmitted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        // Initialize API service
        apiService = APIClient.getClient().create(APIService.class);

        // Initialize views
        initializeViews();

        // Get quiz ID from intent
        int quizId = getIntent().getIntExtra("quizId", -1);
        if (quizId != -1) {
            loadQuiz(quizId);
        } else {
            finish();
            return;
        }

        // Set up navigation buttons
        setupNavigation();

        // Set up submit button
        submitButton.setOnClickListener(v -> confirmSubmission());
    }

    private void initializeViews() {
        timerText = findViewById(R.id.timerText);
        quizTitleText = findViewById(R.id.quizTitleText);
        questionCountText = findViewById(R.id.questionCountText);
        questionViewPager = findViewById(R.id.questionViewPager);
        prevButton = findViewById(R.id.prevButton);
        nextButton = findViewById(R.id.nextButton);
        submitButton = findViewById(R.id.submitButton);
        progressBar = findViewById(R.id.progressBar);
    }

    private void loadQuiz(int quizId) {
        showLoading();
        
        apiService.getQuiz(quizId).enqueue(new Callback<Quiz>() {
            @Override
            public void onResponse(Call<Quiz> call, Response<Quiz> response) {
                hideLoading();
                
                if (response.isSuccessful() && response.body() != null) {
                    quiz = response.body();
                    setupQuiz();
                } else {
                    showError("Failed to load quiz");
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

    private void setupQuiz() {
        // Set quiz title and question count
        quizTitleText.setText(quiz.getTitle());
        questionCountText.setText(String.format(Locale.getDefault(),
            "Questions: %d", quiz.getQuestionCount()));

        // Set up ViewPager adapter
        adapter = new QuizQuestionAdapter(quiz.getQuestions(), this);
        questionViewPager.setAdapter(adapter);

        // Start timer
        startTimer(quiz.getDuration() * 60 * 1000); // Convert minutes to milliseconds
    }

    private void setupNavigation() {
        questionViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateNavigationButtons(position);
            }
        });

        prevButton.setOnClickListener(v -> {
            if (questionViewPager.getCurrentItem() > 0) {
                questionViewPager.setCurrentItem(questionViewPager.getCurrentItem() - 1);
            }
        });

        nextButton.setOnClickListener(v -> {
            if (questionViewPager.getCurrentItem() < adapter.getItemCount() - 1) {
                questionViewPager.setCurrentItem(questionViewPager.getCurrentItem() + 1);
            }
        });
    }

    private void updateNavigationButtons(int position) {
        prevButton.setEnabled(position > 0);
        nextButton.setEnabled(position < adapter.getItemCount() - 1);
    }

    private void startTimer(long durationMillis) {
        timer = new CountDownTimer(durationMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished);
                long seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                        TimeUnit.MINUTES.toSeconds(minutes);
                timerText.setText(String.format(Locale.getDefault(),
                    "%02d:%02d", minutes, seconds));
            }

            @Override
            public void onFinish() {
                timerText.setText("00:00");
                submitQuiz();
            }
        }.start();
    }

    @Override
    public void onAnswerSelected(int questionPosition, int selectedOptionIndex) {
        if (!isQuizSubmitted) {
            quiz.getQuestions().get(questionPosition).setSelectedOptionIndex(selectedOptionIndex);
        }
    }

    private void confirmSubmission() {
        if (!quiz.isFullyAnswered()) {
            new MaterialAlertDialogBuilder(this)
                .setTitle("Confirm Submission")
                .setMessage("You haven't answered all questions. Are you sure you want to submit?")
                .setPositiveButton("Submit", (dialog, which) -> submitQuiz())
                .setNegativeButton("Continue", null)
                .show();
        } else {
            submitQuiz();
        }
    }

    private void submitQuiz() {
        if (isQuizSubmitted) return;

        showLoading();
        isQuizSubmitted = true;

        // Cancel timer if it's running
        if (timer != null) {
            timer.cancel();
        }

        // Prepare answers
        Map<String, String> answers = new HashMap<>();
        for (Quiz.Question question : quiz.getQuestions()) {
            answers.put(String.valueOf(question.getId()),
                String.valueOf(question.getSelectedOptionIndex()));
        }

        // Submit to server
        apiService.submitQuiz(quiz.getId(), answers).enqueue(new Callback<Quiz>() {
            @Override
            public void onResponse(Call<Quiz> call, Response<Quiz> response) {
                hideLoading();
                
                if (response.isSuccessful() && response.body() != null) {
                    quiz = response.body();
                    showResults();
                } else {
                    showError("Failed to submit quiz");
                }
            }

            @Override
            public void onFailure(Call<Quiz> call, Throwable t) {
                hideLoading();
                showError("Network error. Please try again.");
            }
        });
    }

    private void showResults() {
        // Update adapter to show correct/incorrect answers
        adapter.setQuizSubmitted(true);

        // Disable navigation and submit buttons
        prevButton.setEnabled(false);
        nextButton.setEnabled(false);
        submitButton.setEnabled(false);

        // Show score dialog
        new MaterialAlertDialogBuilder(this)
            .setTitle("Quiz Completed")
            .setMessage(String.format(Locale.getDefault(),
                "Your score: %d%%", quiz.getScore()))
            .setPositiveButton("OK", (dialog, which) -> finish())
            .setCancelable(false)
            .show();
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

    @Override
    public void onBackPressed() {
        if (!isQuizSubmitted) {
            new MaterialAlertDialogBuilder(this)
                .setTitle("Quit Quiz")
                .setMessage("Are you sure you want to quit? Your progress will be lost.")
                .setPositiveButton("Quit", (dialog, which) -> super.onBackPressed())
                .setNegativeButton("Continue", null)
                .show();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
        }
    }
}