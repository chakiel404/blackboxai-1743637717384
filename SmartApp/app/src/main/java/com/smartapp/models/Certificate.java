package com.smartapp.models;

import com.google.gson.annotations.SerializedName;

public class Certificate {
    @SerializedName("certificate_id")
    private int certificateId;

    @SerializedName("student_id")
    private int studentId;

    @SerializedName("type")
    private String type; // "quiz" or "assignment"

    @SerializedName("reference_id")
    private int referenceId; // quiz_id or assignment_id

    @SerializedName("title")
    private String title;

    @SerializedName("student_name")
    private String studentName;

    @SerializedName("subject_name")
    private String subjectName;

    @SerializedName("class_name")
    private String className;

    @SerializedName("score")
    private double score;

    @SerializedName("grade")
    private String grade; // A, B, C, D, E

    @SerializedName("completion_date")
    private String completionDate;

    @SerializedName("teacher_name")
    private String teacherName;

    @SerializedName("academic_year")
    private String academicYear;

    @SerializedName("semester")
    private String semester;

    @SerializedName("certificate_number")
    private String certificateNumber;

    @SerializedName("qr_code")
    private String qrCode; // URL to verify certificate authenticity

    public int getCertificateId() {
        return certificateId;
    }

    public void setCertificateId(int certificateId) {
        this.certificateId = certificateId;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(int referenceId) {
        this.referenceId = referenceId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
        this.grade = calculateGrade(score);
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getCompletionDate() {
        return completionDate;
    }

    public void setCompletionDate(String completionDate) {
        this.completionDate = completionDate;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
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

    public String getCertificateNumber() {
        return certificateNumber;
    }

    public void setCertificateNumber(String certificateNumber) {
        this.certificateNumber = certificateNumber;
    }

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    private String calculateGrade(double score) {
        if (score >= 90) return "A";
        if (score >= 80) return "B";
        if (score >= 70) return "C";
        if (score >= 60) return "D";
        return "E";
    }

    public boolean isPassing() {
        return score >= 70; // C or better is passing
    }
}