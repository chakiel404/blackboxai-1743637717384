package com.smartapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.smartapp.R;
import com.smartapp.models.Quiz;

import java.util.List;

public class QuizQuestionAdapter extends RecyclerView.Adapter<QuizQuestionAdapter.QuestionViewHolder> {

    private List<Quiz.Question> questions;
    private OnAnswerSelectedListener listener;
    private boolean isQuizSubmitted;

    public interface OnAnswerSelectedListener {
        void onAnswerSelected(int questionPosition, int selectedOptionIndex);
    }

    public QuizQuestionAdapter(List<Quiz.Question> questions, OnAnswerSelectedListener listener) {
        this.questions = questions;
        this.listener = listener;
        this.isQuizSubmitted = false;
    }

    @NonNull
    @Override
    public QuestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_quiz_question, parent, false);
        return new QuestionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuestionViewHolder holder, int position) {
        Quiz.Question question = questions.get(position);
        
        // Set question number and text
        holder.questionNumberText.setText("Question " + (position + 1));
        holder.questionText.setText(question.getQuestionText());

        // Set options
        List<String> options = question.getOptions();
        RadioButton[] optionButtons = {
            holder.option1, holder.option2, holder.option3, holder.option4
        };

        for (int i = 0; i < options.size(); i++) {
            optionButtons[i].setText(options.get(i));
            optionButtons[i].setEnabled(!isQuizSubmitted);
        }

        // Set selected option if any
        Integer selectedOption = question.getSelectedOptionIndex();
        if (selectedOption != null) {
            optionButtons[selectedOption].setChecked(true);
        } else {
            holder.optionsRadioGroup.clearCheck();
        }

        // Handle option selection
        holder.optionsRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            int selectedIndex = -1;
            if (checkedId == R.id.option1) selectedIndex = 0;
            else if (checkedId == R.id.option2) selectedIndex = 1;
            else if (checkedId == R.id.option3) selectedIndex = 2;
            else if (checkedId == R.id.option4) selectedIndex = 3;

            if (selectedIndex != -1 && listener != null) {
                listener.onAnswerSelected(position, selectedIndex);
            }
        });

        // Show feedback if quiz is submitted
        if (isQuizSubmitted) {
            holder.feedbackText.setVisibility(View.VISIBLE);
            if (question.isCorrect()) {
                holder.feedbackText.setText("Correct!");
                holder.feedbackText.setTextColor(holder.itemView.getContext()
                    .getColor(R.color.success));
            } else {
                holder.feedbackText.setText("Incorrect. The correct answer was: " + 
                    options.get(question.getCorrectOptionIndex()));
                holder.feedbackText.setTextColor(holder.itemView.getContext()
                    .getColor(R.color.error));
            }
        } else {
            holder.feedbackText.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return questions != null ? questions.size() : 0;
    }

    public void setQuizSubmitted(boolean submitted) {
        this.isQuizSubmitted = submitted;
        notifyDataSetChanged();
    }

    static class QuestionViewHolder extends RecyclerView.ViewHolder {
        TextView questionNumberText;
        TextView questionText;
        RadioGroup optionsRadioGroup;
        RadioButton option1;
        RadioButton option2;
        RadioButton option3;
        RadioButton option4;
        TextView feedbackText;

        QuestionViewHolder(View itemView) {
            super(itemView);
            questionNumberText = itemView.findViewById(R.id.questionNumberText);
            questionText = itemView.findViewById(R.id.questionText);
            optionsRadioGroup = itemView.findViewById(R.id.optionsRadioGroup);
            option1 = itemView.findViewById(R.id.option1);
            option2 = itemView.findViewById(R.id.option2);
            option3 = itemView.findViewById(R.id.option3);
            option4 = itemView.findViewById(R.id.option4);
            feedbackText = itemView.findViewById(R.id.feedbackText);
        }
    }
}