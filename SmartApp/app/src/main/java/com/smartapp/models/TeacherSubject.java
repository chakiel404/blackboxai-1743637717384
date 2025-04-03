package com.smartapp.models;

import com.google.gson.annotations.SerializedName;

public class TeacherSubject {
    @SerializedName("teacher_subject_id")
    private int teacherSubjectId;

    @SerializedName("teacher_id")
    private int teacherId;

    @SerializedName("subject_id")
    private int subjectId;

    @SerializedName("class_id")
    private int classId;

    @SerializedName("subject")
    private Subject subject;

    @SerializedName("class_name")
    private String className;

    @SerializedName("academic_year")
    private String academicYear;

    @SerializedName("semester")
    private String semester;

    public int getTeacherSubjectId() {
        return teacherSubjectId;
    }

    public void setTeacherSubjectId(int teacherSubjectId) {
        this.teacherSubjectId = teacherSubjectId;
    }

    public int getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(int teacherId) {
        this.teacherId = teacherId;
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

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
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
}