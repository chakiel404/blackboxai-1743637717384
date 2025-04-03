package com.smartapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.smartapp.R;
import com.smartapp.models.Quiz;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class QuizAdapter extends RecyclerView.Adapter<QuizAdapter.QuizViewHolder> {

    private List<Quiz> quizzes;
    private Context context;
    private OnQuizClickListener listener;
    private SimpleDateFormat dateFormat;
    private SimpleDateFormat timeFormat;

    public interface OnQuizClickListener {
        void onQuizClick(Quiz quiz);
    }

    public QuizAdapter(Context context, List<Quiz> quizzes, OnQuizClickListener listener) {
        this.context = context;
        this.quizzes = quizzes;
        this.listener = listener;
        this.dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        this.timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    }

    @NonNull
    @Override
    public QuizViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_quiz, parent, false);
        return new QuizViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuizViewHolder holder, int position) {
        Quiz quiz = quizzes.get(position);
        
        holder.titleText.setText(quiz.getTitle());
        holder.subjectText.setText(quiz.getSubject());
        holder.descriptionText.setText(quiz.getDescription());
        holder.durationText.setText(quiz.getDuration() + " minutes");

        // Format and set times
        String startDate = dateFormat.format(quiz.getStartTime());
        String startTime = timeFormat.format(quiz.getStartTime());
        String endDate = dateFormat.format(quiz.getEndTime());
        String endTime = timeFormat.format(quiz.getEndTime());

        holder.startTimeText.setText("Start: " + startDate + " " + startTime);
        holder.endTimeText.setText("End: " + endDate + " " + endTime);

        // Set status chip
        setupStatusChip(holder.statusChip, quiz);

        // Show score if quiz is completed
        if (quiz.isCompleted()) {
            holder.scoreText.setVisibility(View.VISIBLE);
            holder.scoreText.setText("Score: " + quiz.getScore() + "%");
        } else {
            holder.scoreText.setVisibility(View.GONE);
        }

        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onQuizClick(quiz);
            }
        });
    }

    private void setupStatusChip(Chip chip, Quiz quiz) {
        Date now = new Date();
        
        if (quiz.isCompleted()) {
            chip.setText("Completed");
            chip.setChipBackgroundColorResource(R.color.success);
        } else if (!quiz.isActive()) {
            chip.setText("Not Active");
            chip.setChipBackgroundColorResource(R.color.secondary_text);
        } else if (now.before(quiz.getStartTime())) {
            chip.setText("Upcoming");
            chip.setChipBackgroundColorResource(R.color.primary);
        } else if (now.after(quiz.getEndTime())) {
            chip.setText("Expired");
            chip.setChipBackgroundColorResource(R.color.error);
        } else {
            chip.setText("Available");
            chip.setChipBackgroundColorResource(R.color.primary);
        }
        
        chip.setTextColor(ContextCompat.getColor(context, R.color.white));
    }

    @Override
    public int getItemCount() {
        return quizzes != null ? quizzes.size() : 0;
    }

    public void updateData(List<Quiz> newQuizzes) {
        this.quizzes = newQuizzes;
        notifyDataSetChanged();
    }

    static class QuizViewHolder extends RecyclerView.ViewHolder {
        TextView titleText;
        Chip statusChip;
        TextView subjectText;
        TextView durationText;
        TextView descriptionText;
        TextView startTimeText;
        TextView endTimeText;
        TextView scoreText;

        QuizViewHolder(View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.titleText);
            statusChip = itemView.findViewById(R.id.statusChip);
            subjectText = itemView.findViewById(R.id.subjectText);
            durationText = itemView.findViewById(R.id.durationText);
            descriptionText = itemView.findViewById(R.id.descriptionText);
            startTimeText = itemView.findViewById(R.id.startTimeText);
            endTimeText = itemView.findViewById(R.id.endTimeText);
            scoreText = itemView.findViewById(R.id.scoreText);
        }
    }
}