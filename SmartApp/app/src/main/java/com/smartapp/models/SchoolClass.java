package com.smartapp.models;

import com.google.gson.annotations.SerializedName;

public class SchoolClass {
    @SerializedName("class_id")
    private int classId;

    @SerializedName("class_name")
    private String className;

    @SerializedName("grade_level")
    private String gradeLevel;

    @SerializedName("academic_year")
    private String academicYear;

    @SerializedName("semester")
    private String semester;

    @SerializedName("homeroom_teacher_id")
    private int homeroomTeacherId;

    @SerializedName("homeroom_teacher")
    private Teacher homeroomTeacher;

    @SerializedName("active")
    private boolean active;

    public int getClassId() {
        return classId;
    }

    public void setClassId(int classId) {
        this.classId = classId;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getGradeLevel() {
        return gradeLevel;
    }

    public void setGradeLevel(String gradeLevel) {
        this.gradeLevel = gradeLevel;
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

    public int getHomeroomTeacherId() {
        return homeroomTeacherId;
    }

    public void setHomeroomTeacherId(int homeroomTeacherId) {
        this.homeroomTeacherId = homeroomTeacherId;
    }

    public Teacher getHomeroomTeacher() {
        return homeroomTeacher;
    }

    public void setHomeroomTeacher(Teacher homeroomTeacher) {
        this.homeroomTeacher = homeroomTeacher;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}