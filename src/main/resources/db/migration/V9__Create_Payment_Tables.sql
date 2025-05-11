-- Create payments table if it doesn't exist
CREATE TABLE IF NOT EXISTS payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    loan_id BIGINT,
    amount DECIMAL(10, 2) NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    transaction_id VARCHAR(100),
    receipt_url VARCHAR(500),
    payment_date DATETIME NOT NULL,
    status VARCHAR(20) NOT NULL,
    description TEXT,
    receipt_number VARCHAR(50),
    verified BOOLEAN DEFAULT FALSE,
    verified_by VARCHAR(100),
    verification_date DATETIME,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (loan_id) REFERENCES loans(id) ON DELETE SET NULL
);

-- Create subscription_payments table for linking payments to subscriptions
CREATE TABLE IF NOT EXISTS subscription_payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    subscription_id BIGINT NOT NULL,
    payment_id BIGINT NOT NULL,
    FOREIGN KEY (subscription_id) REFERENCES subscriptions(id),
    FOREIGN KEY (payment_id) REFERENCES payments(id)
);

-- Create payment_methods table for supported payment methods
CREATE TABLE IF NOT EXISTS payment_methods (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    display_name VARCHAR(100) NOT NULL,
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    icon_url VARCHAR(500),
    processing_fee DECIMAL(5, 2) DEFAULT 0.00,
    min_amount DECIMAL(10, 2) DEFAULT 0.00,
    max_amount DECIMAL(10, 2),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Insert default payment methods
INSERT INTO payment_methods (name, display_name, description, is_active, processing_fee)
VALUES 
('RAZORPAY', 'Credit/Debit Card', 'Pay using credit or debit card through Razorpay', TRUE, 2.00),
('UPI', 'UPI Payment', 'Pay using UPI apps like Google Pay, PhonePe, Paytm', TRUE, 0.00),
('MANUAL', 'Manual Payment', 'Pay at the library counter', TRUE, 0.00),
('NETBANKING', 'Net Banking', 'Pay using your bank account', TRUE, 1.50);
