package com.smartapp.models;

import com.google.gson.annotations.SerializedName;

public class QuestionOption {
    @SerializedName("option_id")
    private int optionId;

    @SerializedName("question_id")
    private int questionId;

    @SerializedName("option_text")
    private String optionText;

    @SerializedName("order_number")
    private int orderNumber;

    public int getOptionId() {
        return optionId;
    }

    public void setOptionId(int optionId) {
        this.optionId = optionId;
    }

    public int getQuestionId() {
        return questionId;
    }

    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }

    public String getOptionText() {
        return optionText;
    }

    public void setOptionText(String optionText) {
        this.optionText = optionText;
    }

    public int getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(int orderNumber) {
        this.orderNumber = orderNumber;
    }
}