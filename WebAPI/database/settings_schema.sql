-- System settings table
CREATE TABLE system_settings (
    setting_id INT PRIMARY KEY AUTO_INCREMENT,
    setting_key VARCHAR(50) NOT NULL UNIQUE,
    setting_value TEXT NOT NULL,
    description TEXT,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by INT,
    FOREIGN KEY (updated_by) REFERENCES users(user_id)
);

-- Academic periods table
CREATE TABLE academic_periods (
    period_id INT PRIMARY KEY AUTO_INCREMENT,
    academic_year VARCHAR(9) NOT NULL,
    semester VARCHAR(10) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    is_active BOOLEAN DEFAULT false,
    status ENUM('upcoming', 'active', 'completed') NOT NULL DEFAULT 'upcoming',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by INT,
    updated_by INT,
    FOREIGN KEY (created_by) REFERENCES users(user_id),
    FOREIGN KEY (updated_by) REFERENCES users(user_id),
    UNIQUE KEY unique_academic_period (academic_year, semester)
);

-- Insert default settings
INSERT INTO system_settings (setting_key, setting_value, description) VALUES
('current_academic_year', '2023/2024', 'Current academic year'),
('current_semester', 'Ganjil', 'Current semester'),
('grade_passing_score', '70', 'Minimum score to pass a subject'),
('certificate_enabled', 'true', 'Enable/disable certificate generation'),
('ranking_update_interval', 'daily', 'How often to update rankings'),
('max_quiz_attempts', '3', 'Maximum attempts allowed for quizzes'),
('assignment_late_policy', 'accept', 'Policy for late assignment submissions');

-- Stored procedure to activate academic period
DELIMITER //
CREATE PROCEDURE activate_academic_period(
    IN p_academic_year VARCHAR(9),
    IN p_semester VARCHAR(10),
    IN p_admin_id INT
)
BEGIN
    DECLARE current_period_id INT;
    
    -- Start transaction
    START TRANSACTION;
    
    -- Deactivate current period
    UPDATE academic_periods 
    SET is_active = false,
        status = 'completed',
        updated_by = p_admin_id,
        updated_at = NOW()
    WHERE is_active = true;
    
    -- Activate new period
    UPDATE academic_periods 
    SET is_active = true,
        status = 'active',
        updated_by = p_admin_id,
        updated_at = NOW()
    WHERE academic_year = p_academic_year 
    AND semester = p_semester;
    
    -- Update system settings
    UPDATE system_settings 
    SET setting_value = p_academic_year,
        updated_by = p_admin_id,
        updated_at = NOW()
    WHERE setting_key = 'current_academic_year';
    
    UPDATE system_settings 
    SET setting_value = p_semester,
        updated_by = p_admin_id,
        updated_at = NOW()
    WHERE setting_key = 'current_semester';
    
    -- Commit transaction
    COMMIT;
END //
DELIMITER ;

-- Trigger to update status based on dates
DELIMITER //
CREATE TRIGGER update_period_status
BEFORE UPDATE ON academic_periods
FOR EACH ROW
BEGIN
    IF NEW.is_active = true THEN
        SET NEW.status = 'active';
    ELSE
        IF CURRENT_DATE() < NEW.start_date THEN
            SET NEW.status = 'upcoming';
        ELSEIF CURRENT_DATE() > NEW.end_date THEN
            SET NEW.status = 'completed';
        END IF;
    END IF;
END //
DELIMITER ;

-- Function to get current academic period
DELIMITER //
CREATE FUNCTION get_current_period()
RETURNS JSON
DETERMINISTIC
BEGIN
    DECLARE result JSON;
    
    SELECT JSON_OBJECT(
        'academic_year', academic_year,
        'semester', semester,
        'start_date', start_date,
        'end_date', end_date,
        'status', status
    ) INTO result
    FROM academic_periods
    WHERE is_active = true
    LIMIT 1;
    
    RETURN result;
END //
DELIMITER ;