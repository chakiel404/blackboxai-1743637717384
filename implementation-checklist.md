# Implementation Checklist

## Web API Implementation

### Authentication
- [ ] Login with NISN
- [ ] Login with NIP
- [ ] Login with Email
- [ ] Registration
- [ ] Password Reset

### Student Features
- [ ] View materials by class/subject
- [ ] Download materials (PDF/DOCX)
- [ ] Submit assignments (PDF/DOCX/Image)
- [ ] Take quizzes
- [ ] View grades
- [ ] Edit profile

### Teacher Features
- [ ] Upload materials (PDF/DOCX)
- [ ] Create/manage quizzes
- [ ] Grade assignments
- [ ] View student grades
- [ ] Edit profile
- [ ] Subject assignment restrictions

### Admin Features
- [ ] Class management (CRUD)
- [ ] Teacher management (CRUD)
- [ ] Subject management (CRUD)
- [ ] System settings
- [ ] User management
- [ ] Schedule management
- [ ] Edit profile

## Android Implementation

### Authentication
- [x] Login screen with NISN/NIP/Email
- [x] Registration screen
- [x] Password reset screen

### Student Features
- [x] Materials list view
- [x] Material download
- [x] Assignment submission
- [x] Quiz interface
- [x] Grades view
- [x] Profile editing

### Teacher Features
- [x] Material upload interface
- [x] Quiz creation interface
- [x] Grading interface
- [x] Student grades view
- [x] Profile editing
- [x] Subject-restricted views

### Admin Features
- [x] Class management interface
- [x] Teacher management interface
- [x] Subject management interface
- [x] Settings management
- [x] User management interface
- [x] Schedule management
- [x] Profile editing

## Models
- [x] Student
- [x] Teacher
- [x] Admin
- [x] SchoolClass
- [x] Subject
- [x] TeacherSubject
- [x] Schedule
- [x] Material
- [x] Assignment
- [x] Quiz

## API Endpoints
- [x] Authentication endpoints
- [x] Student endpoints
- [x] Teacher endpoints
- [x] Admin endpoints
- [x] Class management endpoints
- [x] Schedule management endpoints

## Database Tables
- [ ] users
- [ ] students
- [ ] teachers
- [ ] admins
- [ ] classes
- [ ] subjects
- [ ] teacher_subjects
- [ ] schedules
- [ ] materials
- [ ] assignments
- [ ] quizzes
- [ ] grades
- [ ] system_settings

## Security
- [ ] JWT Authentication
- [ ] Role-based access control
- [ ] Input validation
- [ ] File upload security
- [ ] API rate limiting

## Additional Features
- [ ] Email notifications
- [ ] Push notifications
- [ ] File compression
- [ ] Caching
- [ ] Logging