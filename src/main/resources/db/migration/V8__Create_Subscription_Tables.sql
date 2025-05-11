-- Create subscription_plans table
CREATE TABLE IF NOT EXISTS subscription_plans (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    monthly_price DECIMAL(10, 2) NOT NULL,
    annual_price DECIMAL(10, 2),
    max_books INT NOT NULL,
    loan_duration INT NOT NULL,
    grace_period INT NOT NULL,
    reservation_priority INT NOT NULL,
    fine_discount DECIMAL(5, 2) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    features TEXT
);

-- Create subscriptions table
CREATE TABLE IF NOT EXISTS subscriptions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    plan_type VARCHAR(50) NOT NULL,
    start_date DATETIME NOT NULL,
    end_date DATETIME NOT NULL,
    auto_renew BOOLEAN DEFAULT FALSE,
    payment_id VARCHAR(100),
    status VARCHAR(20) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    max_books INT NOT NULL,
    loan_duration INT NOT NULL,
    grace_period INT NOT NULL,
    reservation_priority INT NOT NULL,
    fine_discount DECIMAL(5, 2) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Insert default subscription plans
INSERT INTO subscription_plans (name, description, monthly_price, annual_price, max_books, loan_duration, grace_period, reservation_priority, fine_discount, features)
VALUES 
('BASIC', 'Basic subscription with standard features', 0.00, 0.00, 3, 14, 0, 0, 0.00, '["Access to basic catalog","Borrow up to 3 books","Standard 14-day loan period"]'),
('STANDARD', 'Enhanced subscription with additional benefits', 199.00, 1999.00, 5, 21, 3, 1, 10.00, '["Access to full catalog","Borrow up to 5 books","Extended 21-day loan period","3-day grace period for returns","10% discount on fines","Priority reservations"]'),
('PREMIUM', 'Premium subscription with all benefits', 399.00, 3999.00, 10, 30, 7, 2, 25.00, '["Access to full catalog including rare books","Borrow up to 10 books","Extended 30-day loan period","7-day grace period for returns","25% discount on fines","Highest priority reservations","Exclusive access to new releases","Free home delivery and pickup"]');
