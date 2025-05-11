-- Update subscription plan prices to meet Razorpay minimum requirements
UPDATE subscription_plans 
SET monthly_price = 199.00, annual_price = 1999.00 
WHERE name = 'BASIC';

UPDATE subscription_plans 
SET monthly_price = 299.00, annual_price = 2999.00 
WHERE name = 'STANDARD';

UPDATE subscription_plans 
SET monthly_price = 399.00, annual_price = 3999.00 
WHERE name = 'PREMIUM';
