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
    
    // ═══════════════════════════════════════════════════════════════
    // Phase 2: Scheduling & Control APIs
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * API 4: Get Groups
     * GET /recExam/getGroups
     * Headers: EPC-UID
     * 
     * Get all department groups for scheduling
     */
    def getGroups() {
        processRequestWithoutParams("getGroups", getService())
    }
    
    /**
     * API 5: Get Schedule
     * GET /recExam/getSchedule
     * Headers: EPC-UID
     * Query Params: groupId (required)
     * 
     * Get exam schedule for a specific group
     */
    def getSchedule() {
        processRequestWithoutParams("getSchedule", getService())
    }
    
    /**
     * API 6: Set Schedule
     * POST /recExam/setSchedule
     * Headers: EPC-UID
     * Body: { secretId, startTime, endTime }
     * 
     * Set exam schedule for individual candidate
     */
    def setSchedule() {
        processRequestWithParams("setSchedule", getService())
    }
    
    /**
     * API 7: Set Schedule For All
     * POST /recExam/setScheduleForAll
     * Headers: EPC-UID
     * Body: { groupId, startTime, endTime }
     * 
     * Set same schedule for all candidates in a group
     */
    def setScheduleForAll() {
        processRequestWithParams("setScheduleForAll", getService())
    }
    
    /**
     * API 8: Extend Time
     * POST /recExam/extendTime
     * Headers: EPC-UID
     * Body: { secretId, extraMinutes }
     * 
     * Extend exam time for a candidate
     */
    def extendTime() {
        processRequestWithParams("extendTime", getService())
    }
    
    /**
     * API 9: Stop Exam
     * POST /recExam/stopExam
     * Headers: EPC-UID
     * Body: { secretId }
     * 
     * Stop exam and calculate result
     */
    def stopExam() {
        processRequestWithParams("stopExam", getService())
    }
    
    /**
     * API 10: Start Exam
     * POST /recExam/startExam
     * Headers: EPC-UID
     * Body: { secretId }
     * 
     * Start or restart exam for a candidate
     */
    def startExam() {
        processRequestWithParams("startExam", getService())
    }
}
