package com.smartapp.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Assignment {
    @SerializedName("assignment_id")
    private int assignmentId;

    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    @SerializedName("subject_id")
    private int subjectId;

    @SerializedName("class_id")
    private int classId;

    @SerializedName("teacher_id")
    private int teacherId;

    @SerializedName("due_date")
    private String dueDate;

    @SerializedName("academic_year")
    private String academicYear;

    @SerializedName("semester")
    private String semester;

    @SerializedName("max_points")
    private int maxPoints;

    public static final String TYPE_FILE = "file";
    public static final String TYPE_LINK = "link";
    public static final String TYPE_MANUAL = "manual";

    @SerializedName("assignment_type")
    private String assignmentType; // "file", "link", or "manual"

    @SerializedName("file_attachment")
    private FileAttachment fileAttachment; // For teacher's file attachment

    @SerializedName("link_url")
    private String linkUrl; // For teacher's link assignment

    @SerializedName("questions")
    private List<Question> questions; // For manual questions (multiple choice/essay)

    @SerializedName("allowed_file_types")
    private List<String> allowedFileTypes; // For student submissions ["pdf", "docx", "jpg", "png"]

    @SerializedName("max_file_size_mb")
    private int maxFileSizeMb;

    @SerializedName("attachment_required")
    private boolean attachmentRequired;

    public static class FileAttachment {
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
    }

    public String getAssignmentType() {
        return assignmentType;
    }

    public void setAssignmentType(String assignmentType) {
        this.assignmentType = assignmentType;
    }

    public FileAttachment getFileAttachment() {
        return fileAttachment;
    }

    public void setFileAttachment(FileAttachment fileAttachment) {
        this.fileAttachment = fileAttachment;
    }

    public String getLinkUrl() {
        return linkUrl;
    }

    public void setLinkUrl(String linkUrl) {
        this.linkUrl = linkUrl;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }

    @SerializedName("is_active")
    private boolean isActive;

    // For student submissions
    @SerializedName("submission")
    private AssignmentSubmission submission;

    public int getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(int assignmentId) {
        this.assignmentId = assignmentId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(int subjectId) {
        this.subjectId = subjectId;
    }

    public int getClassId() {
        return classId;
    }

    public void setClassId(int classId) {
        this.classId = classId;
    }

    public int getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(int teacherId) {
        this.teacherId = teacherId;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

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

    public int getMaxPoints() {
        return maxPoints;
    }

    public void setMaxPoints(int maxPoints) {
        this.maxPoints = maxPoints;
    }

    public List<String> getAllowedFileTypes() {
        return allowedFileTypes;
    }

    public void setAllowedFileTypes(List<String> allowedFileTypes) {
        this.allowedFileTypes = allowedFileTypes;
    }

    public int getMaxFileSizeMb() {
        return maxFileSizeMb;
    }

    public void setMaxFileSizeMb(int maxFileSizeMb) {
        this.maxFileSizeMb = maxFileSizeMb;
    }

    public boolean isAttachmentRequired() {
        return attachmentRequired;
    }

    public void setAttachmentRequired(boolean attachmentRequired) {
        this.attachmentRequired = attachmentRequired;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public AssignmentSubmission getSubmission() {
        return submission;
    }

    public void setSubmission(AssignmentSubmission submission) {
        this.submission = submission;
    }

    public boolean isFileTypeAllowed(String fileExtension) {
        if (allowedFileTypes == null || allowedFileTypes.isEmpty()) {
            return true; // If no restrictions specified
        }
        return allowedFileTypes.contains(fileExtension.toLowerCase());
    }

    public boolean isBeforeDueDate() {
        // TODO: Implement due date checking logic
        return true;
    }
}