package recruitment

import grails.gorm.transactions.Transactional
import common.SendMailService

@Transactional
class RecApplicationLoginService {

    SendMailService sendMailService

    /**
     * Phase 1: Login and Registration APIs
     */

    /**
     * API 1: Login - Authenticate user with email and password
     * @param username - User email
     * @param password - User password
     * @return Map with login result
     */
    def processerplogin(String username, String password) {
        try {
            // Validate input
            if (!username || !password) {
                return [flag: false, msg: "Username and password are required"]
            }

            // Check if user exists
            def recLogin = RecLogin.findByUsername(username?.trim()?.toLowerCase())
            if (!recLogin) {
                return [flag: false, msg: "User not registered. Please sign up first", notreg: true]
            }

            // Check if account is blocked
            if (recLogin.isblocked) {
                return [flag: false, msg: "Your account has been blocked. Please contact administrator"]
            }

            // Verify password (plain text comparison for compatibility)
            if (recLogin.password != password) {
                return [flag: false, msg: "Invalid password", passnot: true]
            }

            // Get applicant details (may not exist yet if profile not completed)
            def recApplicant = RecApplicant.findByUsername(username?.trim()?.toLowerCase())
            
            // Successful login
            def result = [
                flag: true,
                msg: "Login successful",
                username: recLogin.username
            ]
            
            // Add applicant details if profile exists
            if (recApplicant) {
                result.fullname = recApplicant.fullname
                result.email = recApplicant.email
                result.applicantId = recApplicant.id
            } else {
                result.fullname = ""
                result.email = recLogin.username
                result.applicantId = null
                result.profileCompleted = false
            }
            
            return result

        } catch (Exception e) {
            log.error("Error in processerplogin: ${e.message}", e)
            return [flag: false, msg: "Error during login: ${e.message}"]
        }
    }

    /**
     * API 2: Send OTP - Generate and send OTP for registration
     * @param username - User email
     * @return Map with OTP send result
     */
    def sendotp(String username) {
        try {
            // Validate email
            if (!username || !username.contains("@")) {
                return [flag: false, msg: "Valid email address is required"]
            }

            def email = username?.trim()?.toLowerCase()

            // Check if user already registered
            def existingLogin = RecLogin.findByUsername(email)
            if (existingLogin) {
                return [flag: false, msg: "User already registered. Please login"]
            }

            // Generate 6-digit OTP
            def otpValue = String.format("%06d", new Random().nextInt(999999))

            // Delete any existing OTP for this email
            Otp.findAllByEmail(email)*.delete(flush: true)

            // Save new OTP
            def otp = new Otp(
                email: email,
                otp: otpValue,
                otpgenerationtime: new Date()
            )
            
            if (!otp.save(flush: true)) {
                return [flag: false, msg: "Failed to generate OTP"]
            }

            // Send OTP via email
            def emailSent = sendOtpEmail(email, otpValue, "Registration")
            
            if (!emailSent) {
                return [flag: false, msg: "OTP generated but email sending failed. Please try again"]
            }

            return [
                flag: true,
                msg: "OTP sent successfully to your email",
                email: email
            ]

        } catch (Exception e) {
            log.error("Error in sendotp: ${e.message}", e)
            return [flag: false, msg: "Error sending OTP: ${e.message}"]
        }
    }

    /**
     * API 3: Verify OTP - Check if OTP is valid
     * @param email - User email
     * @param otpValue - OTP entered by user
     * @return Map with verification result
     */
    def checkotp(String email, String otpValue) {
        try {
            // Validate input
            if (!email || !otpValue) {
                return [flag: false, msg: "Email and OTP are required"]
            }

            def emailLower = email?.trim()?.toLowerCase()

            // Find OTP record
            def otp = Otp.findByEmail(emailLower)
            if (!otp) {
                return [flag: false, msg: "No OTP found for this email. Please request a new OTP"]
            }

            // Check OTP expiration (15 minutes)
            def currentTime = new Date()
            def timeDiff = (currentTime.time - otp.otpgenerationtime.time) / 1000 / 60 // in minutes
            
            if (timeDiff > 15) {
                return [flag: false, msg: "OTP has expired. Please request a new OTP"]
            }

            // Verify OTP
            if (otp.otp != otpValue?.trim()) {
                return [flag: false, msg: "Invalid OTP. Please try again"]
            }

            return [
                flag: true,
                msg: "OTP verified successfully",
                email: emailLower
            ]

        } catch (Exception e) {
            log.error("Error in checkotp: ${e.message}", e)
            return [flag: false, msg: "Error verifying OTP: ${e.message}"]
        }
    }

    /**
     * API 4: Resend OTP - Resend OTP for registration
     * @param email - User email
     * @return Map with resend result
     */
    def resendotp(String email) {
        try {
            // Validate email
            if (!email) {
                return [flag: false, msg: "Email is required"]
            }

            def emailLower = email?.trim()?.toLowerCase()

            // Check if OTP exists
            def existingOtp = Otp.findByEmail(emailLower)
            if (!existingOtp) {
                return [flag: false, msg: "No OTP request found. Please start registration again"]
            }

            // Generate new OTP
            def otpValue = String.format("%06d", new Random().nextInt(999999))

            // Update OTP
            existingOtp.otp = otpValue
            existingOtp.otpgenerationtime = new Date()
            
            if (!existingOtp.save(flush: true)) {
                return [flag: false, msg: "Failed to generate new OTP"]
            }

            // Send OTP via email
            def emailSent = sendOtpEmail(emailLower, otpValue, "Registration")
            
            if (!emailSent) {
                return [flag: false, msg: "OTP generated but email sending failed. Please try again"]
            }

            return [
                flag: true,
                msg: "OTP resent successfully to your email"
            ]

        } catch (Exception e) {
            log.error("Error in resendotp: ${e.message}", e)
            return [flag: false, msg: "Error resending OTP: ${e.message}"]
        }
    }

    /**
     * API 5: Complete Registration - Save user credentials after OTP verification
     * @param username - User email
     * @param password - User password
     * @param confirmPassword - Confirm password
     * @return Map with registration result
     */
    def savelogin(String username, String password, String confirmPassword) {
        try {
            // Validate input
            if (!username || !password || !confirmPassword) {
                return [flag: false, msg: "All fields are required"]
            }

            // Validate password match
            if (password != confirmPassword) {
                return [flag: false, msg: "Passwords do not match"]
            }

            // Validate password length
            if (password.length() < 8) {
                return [flag: false, msg: "Password must be at least 8 characters long"]
            }

            def email = username?.trim()?.toLowerCase()

            // Check if user already exists
            def existingLogin = RecLogin.findByUsername(email)
            if (existingLogin) {
                return [flag: false, msg: "User already registered. Please login"]
            }

            // Verify OTP was validated (check if OTP exists)
            def otp = Otp.findByEmail(email)
            if (!otp) {
                return [flag: false, msg: "Please verify OTP first"]
            }

            // Get client IP
            def ipAddress = "0.0.0.0" // Will be set from controller

            // Create RecLogin
            def recLogin = new RecLogin(
                username: email,
                password: password, // Plain text for compatibility
                isblocked: false,
                creation_date: new Date(),
                updation_date: new Date(),
                creation_ip_address: ipAddress,
                updation_ip_address: ipAddress
            )

            if (!recLogin.save(flush: true)) {
                return [flag: false, msg: "Failed to create login account", errors: recLogin.errors]
            }

            // Delete OTP after successful registration
            otp.delete(flush: true)

            return [
                flag: true,
                msg: "Registration completed successfully. Please login",
                username: email
            ]

        } catch (Exception e) {
            log.error("Error in savelogin: ${e.message}", e)
            return [flag: false, msg: "Error during registration: ${e.message}"]
        }
    }

    /**
     * Helper method to send OTP email
     */
    private boolean sendOtpEmail(String email, String otp, String purpose) {
        try {
            // Get email configuration
            def emailConfig = ApiConfiguration.findByName("SMTP")
            if (!emailConfig) {
                log.error("Email configuration not found")
                return false
            }

            def subject = "OTP for ${purpose} - Recruitment Portal"
            def body = """
                <html>
                <body>
                    <h3>Recruitment Portal - ${purpose}</h3>
                    <p>Dear User,</p>
                    <p>Your OTP for ${purpose} is: <strong>${otp}</strong></p>
                    <p>This OTP is valid for 15 minutes.</p>
                    <p>Please do not share this OTP with anyone.</p>
                    <br>
                    <p>Thanks & Regards,<br>Recruitment Portal Team</p>
                </body>
                </html>
            """

            def result = sendMailService.sendmailwithcss(
                emailConfig.username,
                emailConfig.secret_key,
                email,
                subject,
                body,
                "",
                ""
            )

            return result == 1

        } catch (Exception e) {
            log.error("Error sending OTP email: ${e.message}", e)
            return false
        }
    }


    /**
     * Phase 2: Password Reset APIs
     */

    /**
     * API 6: Send OTP for Password Reset
     * @param email - User email
     * @return Map with OTP send result
     */
    def sendotppasswordchange(String email) {
        try {
            // Validate email
            if (!email || !email.contains("@")) {
                return [flag: false, msg: "Valid email address is required"]
            }

            def emailLower = email?.trim()?.toLowerCase()

            // Check if user exists and is not blocked
            def recLogin = RecLogin.findByUsernameAndIsblocked(emailLower, false)
            if (!recLogin) {
                return [flag: false, msg: "User not found or account is blocked", notuser: true]
            }

            // Generate 6-digit OTP
            def otpValue = String.format("%06d", new Random().nextInt(999999))

            // Delete any existing OTP for this email
            Otp.findAllByEmail(emailLower)*.delete(flush: true)

            // Save new OTP
            def otp = new Otp(
                email: emailLower,
                otp: otpValue,
                otpgenerationtime: new Date()
            )
            
            if (!otp.save(flush: true)) {
                return [flag: false, msg: "Failed to generate OTP"]
            }

            // Send OTP via email
            def emailSent = sendOtpEmail(emailLower, otpValue, "Password Reset")
            
            if (!emailSent) {
                return [flag: false, msg: "OTP generated but email sending failed. Please try again"]
            }

            return [
                flag: true,
                msg: "OTP sent successfully to your email",
                email: emailLower
            ]

        } catch (Exception e) {
            log.error("Error in sendotppasswordchange: ${e.message}", e)
            return [flag: false, msg: "Error sending OTP: ${e.message}"]
        }
    }

    /**
     * API 7: Verify OTP for Password Reset
     * @param email - User email
     * @param otpValue - OTP entered by user
     * @return Map with verification result
     */
    def checkotppasswordchange(String email, String otpValue) {
        try {
            // Validate input
            if (!email || !otpValue) {
                return [flag: false, msg: "Email and OTP are required"]
            }

            def emailLower = email?.trim()?.toLowerCase()

            // Find OTP record
            def otp = Otp.findByEmail(emailLower)
            if (!otp) {
                return [flag: false, msg: "No OTP found for this email. Please request a new OTP"]
            }

            // Check OTP expiration (15 minutes)
            def currentTime = new Date()
            def timeDiff = (currentTime.time - otp.otpgenerationtime.time) / 1000 / 60 // in minutes
            
            if (timeDiff > 15) {
                return [flag: false, msg: "OTP has expired. Please request a new OTP"]
            }

            // Verify OTP
            if (otp.otp != otpValue?.trim()) {
                return [flag: false, msg: "Invalid OTP. Please try again"]
            }

            return [
                flag: true,
                msg: "OTP verified successfully. You can now reset your password",
                email: emailLower
            ]

        } catch (Exception e) {
            log.error("Error in checkotppasswordchange: ${e.message}", e)
            return [flag: false, msg: "Error verifying OTP: ${e.message}"]
        }
    }

    /**
     * API 8: Resend OTP for Password Reset
     * @param email - User email
     * @return Map with resend result
     */
    def resendotppasswordchange(String email) {
        try {
            // Validate email
            if (!email) {
                return [flag: false, msg: "Email is required"]
            }

            def emailLower = email?.trim()?.toLowerCase()

            // Check if user exists
            def recLogin = RecLogin.findByUsernameAndIsblocked(emailLower, false)
            if (!recLogin) {
                return [flag: false, msg: "User not found or account is blocked"]
            }

            // Check if OTP exists
            def existingOtp = Otp.findByEmail(emailLower)
            if (!existingOtp) {
                return [flag: false, msg: "No OTP request found. Please start password reset again"]
            }

            // Generate new OTP
            def otpValue = String.format("%06d", new Random().nextInt(999999))

            // Update OTP
            existingOtp.otp = otpValue
            existingOtp.otpgenerationtime = new Date()
            
            if (!existingOtp.save(flush: true)) {
                return [flag: false, msg: "Failed to generate new OTP"]
            }

            // Send OTP via email
            def emailSent = sendOtpEmail(emailLower, otpValue, "Password Reset")
            
            if (!emailSent) {
                return [flag: false, msg: "OTP generated but email sending failed. Please try again"]
            }

            return [
                flag: true,
                msg: "OTP resent successfully to your email"
            ]

        } catch (Exception e) {
            log.error("Error in resendotppasswordchange: ${e.message}", e)
            return [flag: false, msg: "Error resending OTP: ${e.message}"]
        }
    }

    /**
     * API 9: Reset Password (After OTP Verification)
     * @param username - User email
     * @param password - New password
     * @param confirmPassword - Confirm new password
     * @return Map with password reset result
     */
    def changePassword(String username, String password, String confirmPassword) {
        try {
            // Validate input
            if (!username || !password || !confirmPassword) {
                return [flag: false, msg: "All fields are required"]
            }

            // Validate password match
            if (password != confirmPassword) {
                return [flag: false, msg: "Passwords do not match"]
            }

            // Validate password length
            if (password.length() < 8) {
                return [flag: false, msg: "Password must be at least 8 characters long"]
            }

            def email = username?.trim()?.toLowerCase()

            // Check if user exists
            def recLogin = RecLogin.findByUsername(email)
            if (!recLogin) {
                return [flag: false, msg: "User not found"]
            }

            // Verify OTP was validated (check if OTP exists)
            def otp = Otp.findByEmail(email)
            if (!otp) {
                return [flag: false, msg: "Please verify OTP first"]
            }

            // Update password
            recLogin.password = password
            recLogin.updation_date = new Date()
            recLogin.updation_ip_address = "0.0.0.0" // Will be set from controller
            
            if (!recLogin.save(flush: true)) {
                return [flag: false, msg: "Failed to update password", errors: recLogin.errors]
            }

            // Delete OTP after successful password change
            otp.delete(flush: true)

            return [
                flag: true,
                msg: "Password changed successfully. Please login with your new password",
                username: email
            ]

        } catch (Exception e) {
            log.error("Error in changePassword: ${e.message}", e)
            return [flag: false, msg: "Error changing password: ${e.message}"]
        }
    }

    /**
     * Phase 3: Change Password for Logged-in User
     */

    /**
     * API 10: Change Password (For Logged-in User)
     * @param username - User email (from session)
     * @param newPassword - New password
     * @param confirmPassword - Confirm new password
     * @return Map with password change result
     */
    def savechangepassword(String username, String newPassword, String confirmPassword) {
        try {
            // Validate input
            if (!username || !newPassword || !confirmPassword) {
                return [flag: false, msg: "All fields are required"]
            }

            // Validate password match
            if (newPassword != confirmPassword) {
                return [flag: false, msg: "New and confirm passwords do not match"]
            }

            // Validate password length
            if (newPassword.length() < 8) {
                return [flag: false, msg: "Password must be at least 8 characters long"]
            }

            def email = username?.trim()?.toLowerCase()

            // Check if user exists
            def recLogin = RecLogin.findByUsername(email)
            if (!recLogin) {
                return [flag: false, msg: "User not found"]
            }

            // Update password
            recLogin.password = newPassword
            recLogin.updation_date = new Date()
            recLogin.updation_ip_address = "0.0.0.0" // Will be set from controller
            
            if (!recLogin.save(flush: true)) {
                return [flag: false, msg: "Failed to update password", errors: recLogin.errors]
            }

            return [
                flag: true,
                msg: "Password changed successfully",
                username: email
            ]

        } catch (Exception e) {
            log.error("Error in savechangepassword: ${e.message}", e)
            return [flag: false, msg: "Error changing password: ${e.message}"]
        }
    }
}
