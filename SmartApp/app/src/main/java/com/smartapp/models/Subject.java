package com.smartapp.models;

import com.google.gson.annotations.SerializedName;

public class Subject {
    @SerializedName("id")
    private int id;

    @SerializedName("name")
    private String name;

    @SerializedName("description")
    private String description;

    @SerializedName("stats")
    private Stats stats;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("updated_at")
    private String updatedAt;

    public static class Stats {
        @SerializedName("materials")
        private int materialCount;

        @SerializedName("quizzes")
        private int quizCount;

        @SerializedName("assignments")
        private int assignmentCount;

        public int getMaterialCount() {
            return materialCount;
        }

        public void setMaterialCount(int materialCount) {
            this.materialCount = materialCount;
        }

        public int getQuizCount() {
            return quizCount;
        }

        public void setQuizCount(int quizCount) {
            this.quizCount = quizCount;
        }

        public int getAssignmentCount() {
            return assignmentCount;
        }

        public void setAssignmentCount(int assignmentCount) {
            this.assignmentCount = assignmentCount;
        }
    }

    // Constructors
    public Subject() {
    }

    public Subject(String name, String description) {
        this.name = name;
        this.description = description;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Stats getStats() {
        return stats;
    }

    public void setStats(Stats stats) {
        this.stats = stats;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}