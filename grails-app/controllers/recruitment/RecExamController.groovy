package recruitment

import grails.converters.JSON

class RecExamController {
    
    // Helper method to get service instance
    private RecExamService getService() {
        return new RecExamService()
    }
    
    /**
     * Common request processing with parameters from request body
     */
    private void processRequestWithParams(String methodName, service) {
        try {
            def requestData = request.JSON
            def uid = request.getHeader("EPC-UID")
            
            if (!uid) {
                render([flag: false, msg: "User not authenticated"] as JSON)
                return
            }
            
            HashMap hm = new HashMap()
            hm.uid = uid
            
            // Add all request parameters to hashmap
            requestData.each { key, value ->
                hm[key] = value
            }
            
            // Call service method
            service."${methodName}"(hm, request, requestData)
            
            render hm as JSON
            
        } catch (Exception e) {
            println("Error in processRequestWithParams: ${e.message}")
            render([flag: false, msg: "Error processing request: ${e.message}"] as JSON)
        }
    }
    
    /**
     * Common request processing without body parameters (GET requests)
     */
    private void processRequestWithoutParams(String methodName, service) {
        try {
            def uid = request.getHeader("EPC-UID")
            
            if (!uid) {
                render([flag: false, msg: "User not authenticated"] as JSON)
                return
            }
            
            HashMap hm = new HashMap()
            hm.uid = uid
            
            // Create data map from query parameters
            def data = [:]
            params.each { key, value ->
                if (key != 'controller' && key != 'action') {
                    hm[key] = value
                    data[key] = value
                }
            }
            
            // Call service method with data parameter
            service."${methodName}"(hm, request, data)
            
            render hm as JSON
            
        } catch (Exception e) {
            println("Error in processRequestWithoutParams: ${e.message}")
            render([flag: false, msg: "Error processing request: ${e.message}"] as JSON)
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // Phase 1: Core Exam Management APIs
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * API 1: Generate Secret Codes
     * POST /recExam/generateSecretCodes
     * Headers: EPC-UID
     * 
     * Generates secret codes for shortlisted candidates
     */
    def generateSecretCodes() {
        processRequestWithoutParams("generateSecretCodes", getService())
    }
    
    /**
     * API 2: Get Exam Applicants
     * GET /recExam/getExamApplicants
     * Headers: EPC-UID
     * Query Params: groupId (optional)
     * 
     * Get exam applicant data by group or all
     */
    def getExamApplicants() {
        processRequestWithoutParams("getExamApplicants", getService())
    }
    
    /**
     * API 3: Allocate Questions
     * POST /recExam/allocateQuestions
     * Headers: EPC-UID
     * 
     * Allocate questions to applicants based on weightage
     */
    def allocateQuestions() {
        processRequestWithoutParams("allocateQuestions", getService())
    }
}
