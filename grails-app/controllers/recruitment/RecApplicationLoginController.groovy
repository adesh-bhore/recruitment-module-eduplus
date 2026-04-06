package recruitment

import grails.converters.JSON

class RecApplicationLoginController {

    // Inject RecApplicationLoginService using Grails dependency injection
    def recApplicationLoginService

    /**
     * Common exception handler
     */
    private def handleException(Exception e) {
        println("Exception in RecApplicationLoginController: ${e.message}")
        e.printStackTrace()
        HashMap hashMap = new HashMap()
        hashMap.put("error_msg", e.message)
        hashMap.put("flag", false)
        render hashMap as JSON
        return
    }

    /**
     * Phase 1: Login and Registration APIs
     */

    /**
     * API 1: Login
     * POST /recApplicationLogin/processerplogin
     * Request Body: { "username": "email@example.com", "password": "password123" }
     */
    def processerplogin() {
        try {
            def requestData = request.JSON
            
            def username = requestData?.username?.toString() ?: params.username?.toString()
            def password = requestData?.password?.toString() ?: params.password?.toString()

            def result = recApplicationLoginService.processerplogin(username, password)
            
            render result as JSON

        } catch (Exception e) {
            handleException(e)
        }
    }

    /**
     * API 2: Send OTP for Registration
     * POST /recApplicationLogin/sendotp
     * Request Body: { "username": "email@example.com" }
     */
    def sendotp() {
        try {
            def requestData = request.JSON
            
            def username = requestData?.username?.toString() ?: params.username?.toString()

            def result = recApplicationLoginService.sendotp(username)
            
            render result as JSON

        } catch (Exception e) {
            handleException(e)
        }
    }

    /**
     * API 3: Verify OTP
     * POST /recApplicationLogin/checkotp
     * Request Body: { "email": "email@example.com", "otp": "123456" }
     * OR: { "username": "email@example.com", "otp": "123456" }
     */
    def checkotp() {
        try {
            def requestData = request.JSON
            
            // Accept both 'email' and 'username' parameters
            def email = requestData?.email?.toString() ?: requestData?.username?.toString() ?: params.email?.toString() ?: params.username?.toString()
            def otp = requestData?.otp?.toString() ?: params.otp?.toString()

            def result = recApplicationLoginService.checkotp(email, otp)
            
            render result as JSON

        } catch (Exception e) {
            handleException(e)
        }
    }

    /**
     * API 4: Resend OTP
     * POST /recApplicationLogin/resendotp
     * Request Body: { "email": "email@example.com" }
     */
    def resendotp() {
        try {
            def requestData = request.JSON
            
            def email = requestData?.email?.toString() ?: params.email?.toString() ?: params.username?.toString()

            def result = recApplicationLoginService.resendotp(email)
            
            render result as JSON

        } catch (Exception e) {
            handleException(e)
        }
    }

    /**
     * API 5: Complete Registration
     * POST /recApplicationLogin/savelogin
     * Request Body: { "username": "email@example.com", "pwd": "password123", "cpwd": "password123" }
     */
    def savelogin() {
        try {
            def requestData = request.JSON
            
            def username = requestData?.username?.toString() ?: params.username?.toString()
            def password = requestData?.pwd?.toString() ?: params.pwd?.toString()
            def confirmPassword = requestData?.cpwd?.toString() ?: params.cpwd?.toString()

            // Get client IP address
            def ipAddress = request.getRemoteAddr()

            def result = recApplicationLoginService.savelogin(username, password, confirmPassword)
            
            // Add IP address to result if needed for logging
            if (result.flag) {
                result.ipAddress = ipAddress
            }
            
            render result as JSON

        } catch (Exception e) {
            handleException(e)
        }
    }


    /**
     * Phase 2: Password Reset APIs
     */

    /**
     * API 6: Send OTP for Password Reset
     * POST /recApplicationLogin/sendotppasswordchange
     * Request Body: { "email": "email@example.com" }
     */
    def sendotppasswordchange() {
        try {
            def requestData = request.JSON
            
            def email = requestData?.email?.toString() ?: params.email?.toString()

            def result = recApplicationLoginService.sendotppasswordchange(email)
            
            render result as JSON

        } catch (Exception e) {
            handleException(e)
        }
    }

    /**
     * API 7: Verify OTP for Password Reset
     * POST /recApplicationLogin/checkotppasswordchange
     * Request Body: { "email": "email@example.com", "otp": "123456" }
     * OR: { "username": "email@example.com", "otp": "123456" }
     */
    def checkotppasswordchange() {
        try {
            def requestData = request.JSON
            
            def email = requestData?.email?.toString() ?: requestData?.username?.toString() ?: params.email?.toString() ?: params.username?.toString()
            def otp = requestData?.otp?.toString() ?: params.otp?.toString()

            def result = recApplicationLoginService.checkotppasswordchange(email, otp)
            
            render result as JSON

        } catch (Exception e) {
            handleException(e)
        }
    }

    /**
     * API 8: Resend OTP for Password Reset
     * POST /recApplicationLogin/resendotppasswordchange
     * Request Body: { "email": "email@example.com" }
     */
    def resendotppasswordchange() {
        try {
            def requestData = request.JSON
            
            def email = requestData?.email?.toString() ?: params.email?.toString() ?: params.username?.toString()

            def result = recApplicationLoginService.resendotppasswordchange(email)
            
            render result as JSON

        } catch (Exception e) {
            handleException(e)
        }
    }

    /**
     * API 9: Reset Password After OTP Verification
     * POST /recApplicationLogin/changePassword
     * Request Body: { "username": "email@example.com", "pwd": "newpassword123", "cpwd": "newpassword123" }
     */
    def changePassword() {
        try {
            def requestData = request.JSON
            
            def username = requestData?.username?.toString() ?: params.username?.toString()
            def password = requestData?.pwd?.toString() ?: params.pwd?.toString()
            def confirmPassword = requestData?.cpwd?.toString() ?: params.cpwd?.toString()

            // Get client IP address
            def ipAddress = request.getRemoteAddr()

            def result = recApplicationLoginService.changePassword(username, password, confirmPassword)
            
            // Add IP address to result if needed for logging
            if (result.flag) {
                result.ipAddress = ipAddress
            }
            
            render result as JSON

        } catch (Exception e) {
            handleException(e)
        }
    }

    /**
     * Phase 3: Change Password for Logged-in User
     */

    /**
     * API 10: Change Password (Logged-in User)
     * POST /recApplicationLogin/savechangepassword
     * Request Body: { "username": "email@example.com", "newpassword": "newpass123", "confirmpassword": "newpass123" }
     */
    def savechangepassword() {
        try {
            def requestData = request.JSON
            
            def username = requestData?.username?.toString() ?: params.username?.toString()
            def newPassword = requestData?.newpassword?.toString() ?: params.newpassword?.toString()
            def confirmPassword = requestData?.confirmpassword?.toString() ?: params.confirmpassword?.toString()

            // Get client IP address
            def ipAddress = request.getRemoteAddr()

            def result = recApplicationLoginService.savechangepassword(username, newPassword, confirmPassword)
            
            // Add IP address to result if needed for logging
            if (result.flag) {
                result.ipAddress = ipAddress
            }
            
            render result as JSON

        } catch (Exception e) {
            handleException(e)
        }
    }
}