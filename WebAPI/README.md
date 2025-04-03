# SmartApp Web API

A RESTful API backend for the SmartApp Learning Management System, built with PHP and MySQL.

## Features

- User authentication and authorization (Admin, Teacher, Student roles)
- Subject management
- Learning materials management with file uploads
- Quiz creation and submission
- Assignment management with file uploads and grading
- Admin dashboard for system management
- RESTful API endpoints with JSON responses
- Secure file handling and validation
- Cross-Origin Resource Sharing (CORS) support

## Requirements

- PHP 7.4 or higher
- MySQL 5.7 or higher
- Apache/Nginx web server
- PHP Extensions:
  - PDO
  - PDO_MySQL
  - JSON
  - FileInfo
  - MBString

## Quick Start

1. Clone the repository:
```bash
git clone https://github.com/yourusername/smartapp-api.git
cd smartapp-api
```

2. Configure your database credentials:
```bash
cp config/config.example.php config/config.php
# Edit config.php with your database credentials
```

3. Run the setup script:
```bash
sudo chmod +x setup.sh
sudo ./setup.sh
```

4. Start the development server:
```bash
php -S localhost:8000
```

## Default Login Credentials

### Admin
- Email: admin@smartapp.com
- Password: admin123

### Teacher
- Email: john.doe@smartapp.com
- Password: teacher123

### Student
- Email: student1@smartapp.com
- Password: student123

## API Endpoints

### Authentication
- `POST /api/auth/login` - User login
- `POST /api/auth/register` - User registration

### Subjects
- `GET /api/subjects` - List all subjects
- `GET /api/subjects/{id}` - Get subject details
- `POST /api/subjects` - Create new subject (Admin only)
- `PUT /api/subjects/{id}` - Update subject (Admin only)
- `DELETE /api/subjects/{id}` - Delete subject (Admin only)

### Materials
- `GET /api/materials` - List all materials
- `GET /api/materials/{id}` - Get material details
- `POST /api/materials` - Upload new material (Teacher/Admin)
- `DELETE /api/materials/{id}` - Delete material (Teacher/Admin)

### Quizzes
- `GET /api/quizzes` - List all quizzes
- `GET /api/quizzes/{id}` - Get quiz details
- `POST /api/quizzes` - Create new quiz (Teacher/Admin)
- `POST /api/quizzes/{id}/submit` - Submit quiz answers (Student)
- `GET /api/quizzes/{id}/submissions` - View quiz submissions (Teacher/Admin)

### Assignments
- `GET /api/assignments` - List all assignments
- `GET /api/assignments/{id}` - Get assignment details
- `POST /api/assignments` - Create new assignment (Teacher/Admin)
- `POST /api/assignments/{id}/submit` - Submit assignment (Student)
- `PUT /api/assignments/{id}/grade` - Grade assignment (Teacher/Admin)
- `GET /api/assignments/{id}/submissions` - View assignment submissions (Teacher/Admin)

## Authentication

All API requests except login and register require authentication using JWT tokens. Include the token in the Authorization header:

```
Authorization: Bearer <your-token-here>
```

## File Upload Limits

- Maximum file size: 10MB
- Allowed file types: pdf, doc, docx, ppt, pptx, xls, xlsx, txt, jpg, jpeg, png

## Response Format

All API responses are in JSON format:

```json
{
    "success": true,
    "data": {
        // Response data here
    }
}
```

Error responses:
```json
{
    "success": false,
    "error": "Error message here"
}
```

## Admin Dashboard

Access the admin dashboard at `/admin`:
- Manage subjects, materials, quizzes, and assignments
- View system statistics
- Manage user accounts
- Configure system settings

## Development

1. Initialize the database with test data:
```bash
php database/init.php
```

2. Start the development server:
```bash
php -S localhost:8000
```

3. Access the API documentation at `http://localhost:8000`

## Security Features

- JWT token authentication
- Password hashing using bcrypt
- Input validation and sanitization
- File upload validation
- CORS protection
- XSS protection
- SQL injection prevention using prepared statements
- Rate limiting
- Session security
- Secure file handling

## Directory Structure

```
WebAPI/
├── admin/              # Admin dashboard files
├── api/               # API endpoint handlers
├── config/            # Configuration files
├── core/              # Core classes and functions
├── database/          # Database scripts
├── uploads/           # File upload directory
│   ├── materials/     # Learning materials
│   └── assignments/   # Assignment submissions
├── .htaccess         # Apache configuration
├── index.php         # API documentation
├── setup.sh          # Setup script
└── README.md         # This file
```

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For support, email support@smartapp.com or create an issue in the repository.