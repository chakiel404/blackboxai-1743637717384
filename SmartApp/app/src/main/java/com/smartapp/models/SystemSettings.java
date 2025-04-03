package com.smartapp.models;

import com.google.gson.annotations.SerializedName;

public class SystemSettings {
    @SerializedName("academic_period")
    private AcademicPeriod academicPeriod;

    @SerializedName("settings")
    private Settings settings;

    public static class AcademicPeriod {
        @SerializedName("academic_year")
        private String academicYear;

        @SerializedName("semester")
        private String semester;

        @SerializedName("start_date")
        private String startDate;

        @SerializedName("end_date")
        private String endDate;

        @SerializedName("is_active")
        private boolean isActive;

        @SerializedName("status")
        private String status; // "upcoming", "active", "completed"

        public String getAcademicYear() {
            return academicYear;
        }

        public void setAcademicYear(String academicYear) {
            this.academicYear = academicYear;
        }

        public String getSemester() {
            return semester;
        }

        public void setSemester(String semester) {
            this.semester = semester;
        }

        public String getStartDate() {
            return startDate;
        }

        public void setStartDate(String startDate) {
            this.startDate = startDate;
        }

        public String getEndDate() {
            return endDate;
        }

        public void setEndDate(String endDate) {
            this.endDate = endDate;
        }

        public boolean isActive() {
            return isActive;
        }

        public void setActive(boolean active) {
            isActive = active;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public boolean isUpcoming() {
            return "upcoming".equals(status);
        }

        public boolean isCompleted() {
            return "completed".equals(status);
        }
    }

    public static class Settings {
        @SerializedName("grade_passing_score")
        private int gradePassingScore;

        @SerializedName("certificate_enabled")
        private boolean certificateEnabled;

        @SerializedName("ranking_update_interval")
        private String rankingUpdateInterval;

        @SerializedName("max_quiz_attempts")
        private int maxQuizAttempts;

        @SerializedName("assignment_late_policy")
        private String assignmentLatePolicy;

        public int getGradePassingScore() {
            return gradePassingScore;
        }

        public void setGradePassingScore(int gradePassingScore) {
            this.gradePassingScore = gradePassingScore;
        }

        public boolean isCertificateEnabled() {
            return certificateEnabled;
        }

        public void setCertificateEnabled(boolean certificateEnabled) {
            this.certificateEnabled = certificateEnabled;
        }

        public String getRankingUpdateInterval() {
            return rankingUpdateInterval;
        }

        public void setRankingUpdateInterval(String rankingUpdateInterval) {
            this.rankingUpdateInterval = rankingUpdateInterval;
        }

        public int getMaxQuizAttempts() {
            return maxQuizAttempts;
        }

        public void setMaxQuizAttempts(int maxQuizAttempts) {
            this.maxQuizAttempts = maxQuizAttempts;
        }

        public String getAssignmentLatePolicy() {
            return assignmentLatePolicy;
        }

        public void setAssignmentLatePolicy(String assignmentLatePolicy) {
            this.assignmentLatePolicy = assignmentLatePolicy;
        }
    }

    public AcademicPeriod getAcademicPeriod() {
        return academicPeriod;
    }

    public void setAcademicPeriod(AcademicPeriod academicPeriod) {
        this.academicPeriod = academicPeriod;
    }

    public Settings getSettings() {
        return settings;
    }

    public void setSettings(Settings settings) {
        this.settings = settings;
    }
}