# Setting Up Brevo Email Templates for OTP Verification

This guide explains how to set up email templates in Brevo for OTP verification in the Online Library Management System.

## Prerequisites

1. A Brevo account (formerly Sendinblue)
2. API key with appropriate permissions

## Template Setup Instructions

### 1. Registration OTP Template

1. Log in to your Brevo account
2. Navigate to "Email Templates" in the left sidebar
3. Click "Create a new template"
4. Choose "Drag & Drop Editor" or "HTML Editor" based on your preference
5. Set the template name to "Registration OTP"
6. Design your template with the following content:

**Subject**: Your OTP for Account Registration

**Body**:
```html
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Registration OTP</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            line-height: 1.6;
            color: #333;
        }
        .container {
            max-width: 600px;
            margin: 0 auto;
            padding: 20px;
            border: 1px solid #ddd;
            border-radius: 5px;
        }
        .header {
            text-align: center;
            padding-bottom: 10px;
            border-bottom: 1px solid #eee;
        }
        .content {
            padding: 20px 0;
        }
        .otp-code {
            font-size: 24px;
            font-weight: bold;
            text-align: center;
            padding: 10px;
            margin: 20px 0;
            background-color: #f5f5f5;
            border-radius: 5px;
        }
        .footer {
            text-align: center;
            font-size: 12px;
            color: #777;
            border-top: 1px solid #eee;
            padding-top: 10px;
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h2>Online Library - Account Registration</h2>
        </div>
        <div class="content">
            <p>Hello,</p>
            <p>Thank you for registering with the Online Library. To complete your registration, please use the following One-Time Password (OTP):</p>
            <div class="otp-code">{{ params.otp }}</div>
            <p>This OTP is valid for 10 minutes. Please do not share it with anyone.</p>
            <p>If you did not request this registration, please ignore this email.</p>
        </div>
        <div class="footer">
            <p>&copy; 2025 Online Library. All rights reserved.</p>
            <p>This is an automated message, please do not reply.</p>
        </div>
    </div>
</body>
</html>
```

7. Save the template
8. Note the template ID (visible in the URL or template details)
9. Update the `app.brevo.registration-template-id` property in `application.properties` with this ID

### 2. Password Reset OTP Template

1. Log in to your Brevo account
2. Navigate to "Email Templates" in the left sidebar
3. Click "Create a new template"
4. Choose "Drag & Drop Editor" or "HTML Editor" based on your preference
5. Set the template name to "Password Reset OTP"
6. Design your template with the following content:

**Subject**: Your OTP for Password Reset

**Body**:
```html
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Password Reset OTP</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            line-height: 1.6;
            color: #333;
        }
        .container {
            max-width: 600px;
            margin: 0 auto;
            padding: 20px;
            border: 1px solid #ddd;
            border-radius: 5px;
        }
        .header {
            text-align: center;
            padding-bottom: 10px;
            border-bottom: 1px solid #eee;
        }
        .content {
            padding: 20px 0;
        }
        .otp-code {
            font-size: 24px;
            font-weight: bold;
            text-align: center;
            padding: 10px;
            margin: 20px 0;
            background-color: #f5f5f5;
            border-radius: 5px;
        }
        .footer {
            text-align: center;
            font-size: 12px;
            color: #777;
            border-top: 1px solid #eee;
            padding-top: 10px;
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h2>Online Library - Password Reset</h2>
        </div>
        <div class="content">
            <p>Hello,</p>
            <p>We received a request to reset your password for the Online Library. To proceed with the password reset, please use the following One-Time Password (OTP):</p>
            <div class="otp-code">{{ params.otp }}</div>
            <p>This OTP is valid for 10 minutes. Please do not share it with anyone.</p>
            <p>If you did not request a password reset, please ignore this email or contact support if you have concerns.</p>
        </div>
        <div class="footer">
            <p>&copy; 2025 Online Library. All rights reserved.</p>
            <p>This is an automated message, please do not reply.</p>
        </div>
    </div>
</body>
</html>
```

7. Save the template
8. Note the template ID (visible in the URL or template details)
9. Update the `app.brevo.password-reset-template-id` property in `application.properties` with this ID

## Testing the Templates

1. Make sure the `app.email.debug-mode` property is set to `false` in `application.properties`
2. Restart the application
3. Test the registration flow by signing up with a valid email address
4. Test the password reset flow by using the "Forgot Password" feature

## Troubleshooting

If emails are not being sent:

1. Check the application logs for any errors
2. Verify that the API key is correct and has the necessary permissions
3. Ensure the template IDs are correctly set in the application.properties file
4. Check your Brevo account for any sending limits or restrictions
5. Temporarily enable debug mode to see the email content in the logs
