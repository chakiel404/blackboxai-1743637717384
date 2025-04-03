package com.smartapp.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Question {
    public static final String TYPE_MULTIPLE_CHOICE = "multiple_choice";
    public static final String TYPE_ESSAY = "essay";

    @SerializedName("question_id")
    private int questionId;

    @SerializedName("quiz_id")
    private int quizId;

    @SerializedName("question_text")
    private String questionText;

    @SerializedName("question_type")
    private String questionType; // "multiple_choice" or "essay"

    @SerializedName("points")
    private int points;

    @SerializedName("options")
    private List<QuestionOption> options; // For multiple choice questions

    @SerializedName("correct_option_id")
    private Integer correctOptionId; // For multiple choice questions

    @SerializedName("answer_key")
    private String answerKey; // For essay questions

    @SerializedName("order_number")
    private int orderNumber;

    public int getQuestionId() {
        return questionId;
    }

    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }

    public int getQuizId() {
        return quizId;
    }

    public void setQuizId(int quizId) {
        this.quizId = quizId;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String getQuestionType() {
        return questionType;
    }

    public void setQuestionType(String questionType) {
        this.questionType = questionType;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public List<QuestionOption> getOptions() {
        return options;
    }

    public void setOptions(List<QuestionOption> options) {
        this.options = options;
    }

    public Integer getCorrectOptionId() {
        return correctOptionId;
    }

    public void setCorrectOptionId(Integer correctOptionId) {
        this.correctOptionId = correctOptionId;
    }

    public String getAnswerKey() {
        return answerKey;
    }

    public void setAnswerKey(String answerKey) {
        this.answerKey = answerKey;
    }

    public int getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(int orderNumber) {
        this.orderNumber = orderNumber;
    }

    public boolean isMultipleChoice() {
        return TYPE_MULTIPLE_CHOICE.equals(questionType);
    }

    public boolean isEssay() {
        return TYPE_ESSAY.equals(questionType);
    }
}