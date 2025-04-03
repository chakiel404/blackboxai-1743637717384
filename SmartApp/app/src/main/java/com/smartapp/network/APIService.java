package com.smartapp.network;

import com.smartapp.models.LoginRequest;
import com.smartapp.models.LoginResponse;
import com.smartapp.models.RegistrationRequest;
import com.smartapp.models.Material;
import com.smartapp.models.Quiz;
import com.smartapp.models.Assignment;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface APIService {
    // Authentication
    @POST("auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("auth/register")
    Call<LoginResponse> register(@Body RegistrationRequest request);

    // Materials
    @GET("materials")
    Call<List<Material>> getMaterials(@Header("Authorization") String token);

    @GET("materials/{id}")
    Call<Material> getMaterial(@Header("Authorization") String token, @Path("id") int id);

    @Multipart
    @POST("materials")
    Call<Material> uploadMaterial(
        @Header("Authorization") String token,
        @Part("title") RequestBody title,
        @Part("description") RequestBody description,
        @Part("subject_id") RequestBody subjectId,
        @Part MultipartBody.Part file
    );

    @DELETE("materials/{id}")
    Call<Void> deleteMaterial(@Header("Authorization") String token, @Path("id") int id);

    // Quizzes
    @GET("quizzes")
    Call<List<Quiz>> getQuizzes(@Header("Authorization") String token);

    @GET("quizzes/{id}")
    Call<Quiz> getQuiz(@Header("Authorization") String token, @Path("id") int id);

    @POST("quizzes")
    Call<Quiz> createQuiz(@Header("Authorization") String token, @Body Quiz quiz);

    @POST("quizzes/{id}/submit")
    Call<Quiz> submitQuiz(@Header("Authorization") String token, @Path("id") int id, @Body Map<String, String> answers);

    // Assignments
    @GET("assignments")
    Call<List<Assignment>> getAssignments(@Header("Authorization") String token);

    @GET("assignments/{id}")
    Call<Assignment> getAssignment(@Header("Authorization") String token, @Path("id") int id);

    @Multipart
    @POST("assignments")
    Call<Assignment> createAssignment(
        @Header("Authorization") String token,
        @Part("title") RequestBody title,
        @Part("description") RequestBody description,
        @Part("subject_id") RequestBody subjectId,
        @Part("due_date") RequestBody dueDate,
        @Part MultipartBody.Part file
    );

    @Multipart
    @POST("assignments/{id}/submit")
    Call<Assignment> submitAssignment(
        @Header("Authorization") String token,
        @Path("id") int id,
        @Part MultipartBody.Part file
    );

    @PUT("assignments/{id}/grade")
    Call<Assignment> gradeAssignment(
        @Header("Authorization") String token,
        @Path("id") int id,
        @Body Map<String, Object> gradeData
    );

    // Subjects
    @GET("subjects")
    Call<List<Subject>> getSubjects(@Header("Authorization") String token);

    @GET("subjects/{id}")
    Call<Subject> getSubject(@Header("Authorization") String token, @Path("id") int id);

    @POST("subjects")
    Call<Subject> createSubject(@Header("Authorization") String token, @Body Subject subject);

    @PUT("subjects/{id}")
    Call<Subject> updateSubject(@Header("Authorization") String token, @Path("id") int id, @Body Subject subject);

    @DELETE("subjects/{id}")
    Call<Void> deleteSubject(@Header("Authorization") String token, @Path("id") int id);

    // Profile Management
    @GET("profile/student")
    Call<Student> getStudentProfile(@Header("Authorization") String token);

    @GET("profile/teacher") 
    Call<Teacher> getTeacherProfile(@Header("Authorization") String token);

    @PUT("profile/student")
    Call<Student> updateStudentProfile(@Header("Authorization") String token, @Body Student student);

    @PUT("profile/teacher")
    Call<Teacher> updateTeacherProfile(@Header("Authorization") String token, @Body Teacher teacher);

    // Admin Profile Management
    @GET("profile/admin")
    Call<Admin> getAdminProfile(@Header("Authorization") String token);

    @PUT("profile/admin")
    Call<Admin> updateAdminProfile(@Header("Authorization") String token, @Body Admin admin);

    // Teacher Subject Management
    @GET("teachers/{teacherId}/subjects")
    Call<List<TeacherSubject>> getTeacherSubjects(
        @Header("Authorization") String token,
        @Path("teacherId") int teacherId
    );

    @POST("teachers/{teacherId}/subjects")
    Call<TeacherSubject> assignSubjectToTeacher(
        @Header("Authorization") String token,
        @Path("teacherId") int teacherId,
        @Body TeacherSubject teacherSubject
    );

    @DELETE("teachers/{teacherId}/subjects/{subjectId}")
    Call<Void> removeTeacherSubject(
        @Header("Authorization") String token,
        @Path("teacherId") int teacherId,
        @Path("subjectId") int subjectId
    );

    // Class Management
    @GET("classes")
    Call<List<SchoolClass>> getClasses(@Header("Authorization") String token);

    @POST("classes")
    Call<SchoolClass> createClass(@Header("Authorization") String token, @Body SchoolClass schoolClass);

    @PUT("classes/{id}")
    Call<SchoolClass> updateClass(@Header("Authorization") String token, @Path("id") int id, @Body SchoolClass schoolClass);

    @DELETE("classes/{id}")
    Call<Void> deleteClass(@Header("Authorization") String token, @Path("id") int id);

    // Schedule Management
    @GET("schedules")
    Call<List<Schedule>> getSchedules(@Header("Authorization") String token);

    @GET("schedules/class/{classId}")
    Call<List<Schedule>> getClassSchedules(
        @Header("Authorization") String token,
        @Path("classId") int classId
    );

    @GET("schedules/teacher/{teacherId}")
    Call<List<Schedule>> getTeacherSchedules(
        @Header("Authorization") String token,
        @Path("teacherId") int teacherId
    );

    @POST("schedules")
    Call<Schedule> createSchedule(@Header("Authorization") String token, @Body Schedule schedule);

    @PUT("schedules/{id}")
    Call<Schedule> updateSchedule(@Header("Authorization") String token, @Path("id") int id, @Body Schedule schedule);

    @DELETE("schedules/{id}")
    Call<Void> deleteSchedule(@Header("Authorization") String token, @Path("id") int id);

    // Student-specific endpoints
    @GET("students/schedule")
    Call<List<Schedule>> getStudentSchedule(
        @Header("Authorization") String token,
        @Query("academic_year") String academicYear,
        @Query("semester") String semester
    );

    @GET("students/materials")
    Call<List<Material>> getStudentMaterials(
        @Header("Authorization") String token,
        @Query("subject_id") Integer subjectId,
        @Query("class_id") Integer classId
    );

    @GET("students/assignments")
    Call<List<Assignment>> getStudentAssignments(
        @Header("Authorization") String token,
        @Query("subject_id") Integer subjectId,
        @Query("class_id") Integer classId
    );

    // Quiz endpoints
    @GET("students/quizzes")
    Call<List<Quiz>> getStudentQuizzes(
        @Header("Authorization") String token,
        @Query("subject_id") Integer subjectId,
        @Query("class_id") Integer classId
    );

    @POST("students/quizzes/{quizId}/submit")
    Call<Map<String, Object>> submitQuiz(
        @Header("Authorization") String token,
        @Path("quizId") int quizId,
        @Body Map<String, Object> answers
    );

    @GET("teachers/quizzes")
    Call<List<Quiz>> getTeacherQuizzes(
        @Header("Authorization") String token,
        @Query("subject_id") Integer subjectId,
        @Query("class_id") Integer classId
    );

    @POST("teachers/quizzes")
    Call<Quiz> createQuiz(
        @Header("Authorization") String token,
        @Body Quiz quiz
    );

    @PUT("teachers/quizzes/{quizId}")
    Call<Quiz> updateQuiz(
        @Header("Authorization") String token,
        @Path("quizId") int quizId,
        @Body Quiz quiz
    );

    @DELETE("teachers/quizzes/{quizId}")
    Call<Void> deleteQuiz(
        @Header("Authorization") String token,
        @Path("quizId") int quizId
    );

    // Assignment endpoints with multiple submission types support
    @GET("students/assignments")
    Call<List<Assignment>> getStudentAssignments(
        @Header("Authorization") String token,
        @Query("subject_id") Integer subjectId,
        @Query("class_id") Integer classId
    );

    // For file type assignments
    @Multipart
    @POST("students/assignments/{assignmentId}/submit-file")
    Call<AssignmentSubmission> submitFileAssignment(
        @Header("Authorization") String token,
        @Path("assignmentId") int assignmentId,
        @Part("submission_text") RequestBody submissionText,
        @Part List<MultipartBody.Part> files
    );

    // For link type assignments
    @POST("students/assignments/{assignmentId}/submit-link")
    Call<AssignmentSubmission> submitLinkAssignment(
        @Header("Authorization") String token,
        @Path("assignmentId") int assignmentId,
        @Body Map<String, String> linkSubmission // Contains link URL and optional notes
    );

    // For manual (questions) type assignments
    @POST("students/assignments/{assignmentId}/submit-answers")
    Call<AssignmentSubmission> submitManualAssignment(
        @Header("Authorization") String token,
        @Path("assignmentId") int assignmentId,
        @Body List<Map<String, Object>> answers // Contains question answers (multiple choice/essay)
    );

    @GET("teachers/assignments")
    Call<List<Assignment>> getTeacherAssignments(
        @Header("Authorization") String token,
        @Query("subject_id") Integer subjectId,
        @Query("class_id") Integer classId
    );

    // Create file type assignment
    @Multipart
    @POST("teachers/assignments/file")
    Call<Assignment> createFileAssignment(
        @Header("Authorization") String token,
        @Part("assignment_data") RequestBody assignmentData,
        @Part List<MultipartBody.Part> attachments
    );

    // Create link type assignment
    @POST("teachers/assignments/link")
    Call<Assignment> createLinkAssignment(
        @Header("Authorization") String token,
        @Body Assignment assignment // Contains link URL and assignment details
    );

    // Create manual questions assignment
    @POST("teachers/assignments/manual")
    Call<Assignment> createManualAssignment(
        @Header("Authorization") String token,
        @Body Assignment assignment // Contains questions (multiple choice/essay)
    );

    @PUT("teachers/assignments/{assignmentId}/grade")
    Call<AssignmentSubmission> gradeAssignment(
        @Header("Authorization") String token,
        @Path("assignmentId") int assignmentId,
        @Path("submissionId") int submissionId,
        @Body Map<String, Object> gradeData
    );

    // Certificate endpoints
    @GET("students/certificates")
    Call<List<Certificate>> getStudentCertificates(
        @Header("Authorization") String token,
        @Query("type") String type, // "quiz" or "assignment"
        @Query("subject_id") Integer subjectId,
        @Query("academic_year") String academicYear,
        @Query("semester") String semester
    );

    @GET("certificates/{certificateId}/download")
    @Streaming
    Call<ResponseBody> downloadCertificate(
        @Header("Authorization") String token,
        @Path("certificateId") int certificateId
    );

    @GET("certificates/verify/{code}")
    Call<Map<String, Object>> verifyCertificate(
        @Path("code") String verificationCode
    );

    // Ranking endpoints
    @GET("students/rankings/subject")
    Call<List<Map<String, Object>>> getStudentSubjectRankings(
        @Header("Authorization") String token,
        @Query("subject_id") Integer subjectId,
        @Query("class_id") Integer classId,
        @Query("academic_year") String academicYear,
        @Query("semester") String semester
    );

    @GET("students/rankings/class")
    Call<List<Map<String, Object>>> getStudentClassRankings(
        @Header("Authorization") String token,
        @Query("class_id") Integer classId,
        @Query("academic_year") String academicYear,
        @Query("semester") String semester
    );

    @GET("students/achievements")
    Call<List<Map<String, Object>>> getStudentAchievements(
        @Header("Authorization") String token,
        @Query("subject_id") Integer subjectId,
        @Query("class_id") Integer classId
    );

    // Teacher ranking endpoints
    @GET("teachers/rankings/subject/{subjectId}")
    Call<List<Map<String, Object>>> getSubjectRankings(
        @Header("Authorization") String token,
        @Path("subjectId") int subjectId,
        @Query("class_id") Integer classId,
        @Query("academic_year") String academicYear,
        @Query("semester") String semester
    );

    @GET("teachers/rankings/class/{classId}")
    Call<List<Map<String, Object>>> getClassRankings(
        @Header("Authorization") String token,
        @Path("classId") int classId,
        @Query("academic_year") String academicYear,
        @Query("semester") String semester
    );

    @GET("students/grades")
    Call<Map<String, Object>> getStudentGrades(
        @Header("Authorization") String token,
        @Query("subject_id") Integer subjectId,
        @Query("academic_year") String academicYear,
        @Query("semester") String semester
    );

    @GET("students/subjects")
    Call<List<Subject>> getStudentSubjects(
        @Header("Authorization") String token,
        @Query("academic_year") String academicYear,
        @Query("semester") String semester
    );

    // Teacher-specific endpoints
    @GET("teachers/schedule")
    Call<List<Schedule>> getTeacherSchedule(
        @Header("Authorization") String token,
        @Query("academic_year") String academicYear,
        @Query("semester") String semester
    );

    @GET("teachers/materials")
    Call<List<Material>> getTeacherMaterials(
        @Header("Authorization") String token,
        @Query("subject_id") Integer subjectId,
        @Query("class_id") Integer classId
    );

    @GET("teachers/assignments")
    Call<List<Assignment>> getTeacherAssignments(
        @Header("Authorization") String token,
        @Query("subject_id") Integer subjectId,
        @Query("class_id") Integer classId
    );

    @GET("teachers/quizzes")
    Call<List<Quiz>> getTeacherQuizzes(
        @Header("Authorization") String token,
        @Query("subject_id") Integer subjectId,
        @Query("class_id") Integer classId
    );

    @GET("teachers/{teacherId}/subjects")
    Call<List<TeacherSubject>> getTeacherSubjects(
        @Header("Authorization") String token,
        @Path("teacherId") int teacherId,
        @Query("academic_year") String academicYear,
        @Query("semester") String semester
    );

    @GET("teachers/{teacherId}/classes")
    Call<List<SchoolClass>> getTeacherClasses(
        @Header("Authorization") String token,
        @Path("teacherId") int teacherId,
        @Query("academic_year") String academicYear,
        @Query("semester") String semester
    );

    // System Settings endpoints
    @GET("system/settings")
    Call<SystemSettings> getSystemSettings(@Header("Authorization") String token);

    // Admin-only endpoints
    @GET("admin/academic-periods")
    Call<List<SystemSettings.AcademicPeriod>> getAcademicPeriods(@Header("Authorization") String token);

    @POST("admin/academic-periods")
    Call<SystemSettings.AcademicPeriod> createAcademicPeriod(
        @Header("Authorization") String token,
        @Body SystemSettings.AcademicPeriod period
    );

    @PUT("admin/academic-periods/{periodId}")
    Call<SystemSettings.AcademicPeriod> updateAcademicPeriod(
        @Header("Authorization") String token,
        @Path("periodId") int periodId,
        @Body SystemSettings.AcademicPeriod period
    );

    @POST("admin/academic-periods/{periodId}/activate")
    Call<SystemSettings.AcademicPeriod> activateAcademicPeriod(
        @Header("Authorization") String token,
        @Path("periodId") int periodId
    );

    @PUT("admin/settings")
    Call<SystemSettings.Settings> updateSystemSettings(
        @Header("Authorization") String token,
        @Body SystemSettings.Settings settings
    );

    @GET("admin/settings/history")
    Call<List<Map<String, Object>>> getSettingsHistory(
        @Header("Authorization") String token,
        @Query("setting_key") String settingKey
    );

    @GET("admin/users")
    Call<List<Map<String, Object>>> getAllUsers(@Header("Authorization") String token);

    @PUT("admin/users/{userId}/status")
    Call<Void> updateUserStatus(
        @Header("Authorization") String token,
        @Path("userId") int userId,
        @Body Map<String, Object> status
    );
}