#!/bin/bash

# Print colorful messages
print_message() {
    echo -e "\e[1;34m$1\e[0m"
}

print_error() {
    echo -e "\e[1;31m$1\e[0m"
}

print_success() {
    echo -e "\e[1;32m$1\e[0m"
}

# Check if running with sudo/root
if [ "$EUID" -ne 0 ]; then 
    print_error "Please run this script with sudo or as root"
    exit 1
fi

print_message "Starting SmartApp Web API setup..."

# Create necessary directories if they don't exist
print_message "Creating directories..."
mkdir -p uploads/materials
mkdir -p uploads/assignments
chmod 755 uploads
chmod 755 uploads/materials
chmod 755 uploads/assignments

# Set proper permissions for web server
print_message "Setting file permissions..."
chown -R www-data:www-data .
find . -type d -exec chmod 755 {} \;
find . -type f -exec chmod 644 {} \;

# Make setup scripts executable
chmod +x setup.sh
chmod +x database/init.php

# Create log file and set permissions
touch log.txt
chmod 666 log.txt
chown www-data:www-data log.txt

# Check if PHP is installed
if ! command -v php &> /dev/null; then
    print_error "PHP is not installed. Please install PHP and try again."
    exit 1
fi

# Check if MySQL/MariaDB is installed
if ! command -v mysql &> /dev/null; then
    print_error "MySQL/MariaDB is not installed. Please install MySQL/MariaDB and try again."
    exit 1
fi

# Initialize database
print_message "Initializing database..."
if php database/init.php; then
    print_success "Database initialized successfully!"
else
    print_error "Failed to initialize database. Please check the error messages above."
    exit 1
fi

# Create .htaccess if it doesn't exist
if [ ! -f .htaccess ]; then
    print_message "Creating .htaccess file..."
    cp .htaccess.example .htaccess 2>/dev/null || :
fi

# Check PHP extensions
print_message "Checking PHP extensions..."
required_extensions=("pdo" "pdo_mysql" "json" "fileinfo" "mbstring")
missing_extensions=()

for ext in "${required_extensions[@]}"; do
    if ! php -m | grep -q "^$ext$"; then
        missing_extensions+=("$ext")
    fi
done

if [ ${#missing_extensions[@]} -ne 0 ]; then
    print_error "Missing PHP extensions: ${missing_extensions[*]}"
    print_error "Please install the missing extensions and try again."
    exit 1
fi

# Check web server configuration
print_message "Checking web server..."
if command -v apache2 &> /dev/null; then
    print_message "Apache found. Checking configuration..."
    if apache2ctl -t &> /dev/null; then
        print_success "Apache configuration is valid"
    else
        print_error "Apache configuration test failed"
    fi
elif command -v nginx &> /dev/null; then
    print_message "Nginx found. Checking configuration..."
    if nginx -t &> /dev/null; then
        print_success "Nginx configuration is valid"
    else
        print_error "Nginx configuration test failed"
    fi
else
    print_message "No supported web server found. Please configure your web server manually."
fi

# Final setup message
print_success "\nSetup completed successfully!"
print_message "\nDefault login credentials:"
echo "Admin:"
echo "Email: admin@smartapp.com"
echo "Password: admin123"
echo ""
echo "Teacher:"
echo "Email: john.doe@smartapp.com"
echo "Password: teacher123"
echo ""
echo "Student:"
echo "Email: student1@smartapp.com"
echo "Password: student123"
echo ""

print_message "Please ensure your web server is configured correctly and the database credentials in config.php are set properly."
print_message "For development, you can use PHP's built-in server: php -S localhost:8000"

# Optional: Start PHP development server
read -p "Would you like to start the PHP development server now? (y/n) " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    print_message "Starting PHP development server on port 8000..."
    php -S localhost:8000
fi