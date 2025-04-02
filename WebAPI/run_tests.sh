#!/bin/bash

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Print colorful messages
print_message() {
    echo -e "${BLUE}$1${NC}"
}

print_error() {
    echo -e "${RED}$1${NC}"
}

print_success() {
    echo -e "${GREEN}$1${NC}"
}

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

# Check if config.php exists
if [ ! -f "config/config.php" ]; then
    print_error "config.php not found. Please copy config.example.php to config.php and configure it."
    exit 1
fi

# Function to check if port 8000 is in use
check_port() {
    if lsof -Pi :8000 -sTCP:LISTEN -t >/dev/null ; then
        return 0
    else
        return 1
    fi
}

# Function to kill process using port 8000
kill_port_process() {
    local pid=$(lsof -Pi :8000 -sTCP:LISTEN -t)
    if [ ! -z "$pid" ]; then
        kill $pid
        print_message "Killed process using port 8000"
    fi
}

# Start development server
start_server() {
    if check_port; then
        print_message "Port 8000 is in use. Attempting to kill the process..."
        kill_port_process
        sleep 2
    fi
    
    print_message "Starting PHP development server..."
    php -S localhost:8000 &
    SERVER_PID=$!
    sleep 2
    
    if ! check_port; then
        print_error "Failed to start PHP development server"
        exit 1
    fi
    
    print_success "PHP development server started on port 8000"
}

# Stop development server
stop_server() {
    if [ ! -z "$SERVER_PID" ]; then
        kill $SERVER_PID
        print_message "Stopped PHP development server"
    fi
    
    if check_port; then
        kill_port_process
    fi
}

# Cleanup function
cleanup() {
    print_message "\nCleaning up..."
    stop_server
    exit 0
}

# Set up cleanup on script exit
trap cleanup EXIT

# Main execution
print_message "Starting API tests..."
print_message "Initializing test environment..."

# Initialize database with test data
print_message "\nInitializing database..."
if php database/init.php; then
    print_success "Database initialized successfully"
else
    print_error "Failed to initialize database"
    exit 1
fi

# Start the development server
start_server

# Run the tests
print_message "\nRunning API tests..."
if php test_api.php; then
    print_success "\nAll tests completed"
else
    print_error "\nSome tests failed"
fi

# The cleanup function will be called automatically on exit