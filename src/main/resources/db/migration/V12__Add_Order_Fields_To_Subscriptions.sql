-- Add order_id and signature columns to subscriptions table
ALTER TABLE subscriptions
ADD COLUMN order_id VARCHAR(100),
ADD COLUMN signature VARCHAR(255);
