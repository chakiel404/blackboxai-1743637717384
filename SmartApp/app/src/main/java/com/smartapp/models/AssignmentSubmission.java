package com.smartapp.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class AssignmentSubmission {
    @SerializedName("submission_id")
    private int submissionId;

    @SerializedName("assignment_id")
    private int assignmentId;

    @SerializedName("student_id")
    private int studentId;

    public static final String TYPE_FILE = "file";
    public static final String TYPE_LINK = "link";
    public static final String TYPE_MANUAL = "manual";

    @SerializedName("submission_type")
    private String submissionType; // "file", "link", or "manual"

    @SerializedName("submission_text")
    private String submissionText; // For manual answers or additional notes

    @SerializedName("submitted_files")
    private List<SubmittedFile> submittedFiles; // For file submissions

    @SerializedName("submitted_link")
    private String submittedLink; // For link submissions

    @SerializedName("question_answers")
    private List<QuestionAnswer> questionAnswers; // For manual question answers

    public static class QuestionAnswer {
        @SerializedName("question_id")
        private int questionId;

        @SerializedName("selected_option_id")
        private Integer selectedOptionId; // For multiple choice

        @SerializedName("essay_answer")
        private String essayAnswer; // For essay questions

        public int getQuestionId() {
            return questionId;
        }

        public void setQuestionId(int questionId) {
            this.questionId = questionId;
        }

        public Integer getSelectedOptionId() {
            return selectedOptionId;
        }

        public void setSelectedOptionId(Integer selectedOptionId) {
            this.selectedOptionId = selectedOptionId;
        }

        public String getEssayAnswer() {
            return essayAnswer;
        }

        public void setEssayAnswer(String essayAnswer) {
            this.essayAnswer = essayAnswer;
        }
    }

    public String getSubmissionType() {
        return submissionType;
    }

    public void setSubmissionType(String submissionType) {
        this.submissionType = submissionType;
    }

    public String getSubmittedLink() {
        return submittedLink;
    }

    public void setSubmittedLink(String submittedLink) {
        this.submittedLink = submittedLink;
    }

    public List<QuestionAnswer> getQuestionAnswers() {
        return questionAnswers;
    }

    public void setQuestionAnswers(List<QuestionAnswer> questionAnswers) {
        this.questionAnswers = questionAnswers;
    }

    @SerializedName("submission_date")
    private String submissionDate;

    @SerializedName("points_earned")
    private Integer pointsEarned;

    @SerializedName("teacher_feedback")
    private String teacherFeedback;

    @SerializedName("graded_date")
    private String gradedDate;

    @SerializedName("graded_by")
    private int gradedBy;

    @SerializedName("status")
    private String status; // "submitted", "graded", "late", etc.

    public static class SubmittedFile {
        @SerializedName("file_id")
        private int fileId;

        @SerializedName("file_name")
        private String fileName;

        @SerializedName("file_path")
        private String filePath;

        @SerializedName("file_size")
        private long fileSize;

        @SerializedName("mime_type")
        private String mimeType;

        @SerializedName("upload_date")
        private String uploadDate;

        public int getFileId() {
            return fileId;
        }

        public void setFileId(int fileId) {
            this.fileId = fileId;
        }

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public String getFilePath() {
            return filePath;
        }

        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }

        public long getFileSize() {
            return fileSize;
        }

        public void setFileSize(long fileSize) {
            this.fileSize = fileSize;
        }

        public String getMimeType() {
            return mimeType;
        }

        public void setMimeType(String mimeType) {
            this.mimeType = mimeType;
        }

        public String getUploadDate() {
            return uploadDate;
        }

        public void setUploadDate(String uploadDate) {
            this.uploadDate = uploadDate;
        }
    }

    public int getSubmissionId() {
        return submissionId;
    }

    public void setSubmissionId(int submissionId) {
        this.submissionId = submissionId;
    }

    public int getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(int assignmentId) {
        this.assignmentId = assignmentId;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public String getSubmissionText() {
        return submissionText;
    }

    public void setSubmissionText(String submissionText) {
        this.submissionText = submissionText;
    }

    public List<SubmittedFile> getSubmittedFiles() {
        return submittedFiles;
    }

    public void setSubmittedFiles(List<SubmittedFile> submittedFiles) {
        this.submittedFiles = submittedFiles;
    }

    public String getSubmissionDate() {
        return submissionDate;
    }

    public void setSubmissionDate(String submissionDate) {
        this.submissionDate = submissionDate;
    }

    public Integer getPointsEarned() {
        return pointsEarned;
    }

    public void setPointsEarned(Integer pointsEarned) {
        this.pointsEarned = pointsEarned;
    }

    public String getTeacherFeedback() {
        return teacherFeedback;
    }

    public void setTeacherFeedback(String teacherFeedback) {
        this.teacherFeedback = teacherFeedback;
    }

    public String getGradedDate() {
        return gradedDate;
    }

    public void setGradedDate(String gradedDate) {
        this.gradedDate = gradedDate;
    }

    public int getGradedBy() {
        return gradedBy;
    }

    public void setGradedBy(int gradedBy) {
        this.gradedBy = gradedBy;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isGraded() {
        return pointsEarned != null && gradedDate != null;
    }

    public boolean hasFiles() {
        return submittedFiles != null && !submittedFiles.isEmpty();
    }
}