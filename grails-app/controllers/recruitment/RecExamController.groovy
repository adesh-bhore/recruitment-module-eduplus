package recruitment

import grails.converters.JSON

class RecExamController {
    
    // Inject RecExamService using Grails dependency injection
    def recExamService
    
    /**
     * Common exception handler
     */
    private def handleException(Exception e) {
        println("Exception in RecExamController: ${e.message}")
        e.printStackTrace()
        HashMap hashMap = new HashMap()
        hashMap.put("error_msg", e.message)
        hashMap.put("flag", false)
        render hashMap as JSON
        return
    }
    
    /**
     * Common request processing with parameters from request body
     */
    private void processRequestWithParams(String methodName) {
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
            recExamService."${methodName}"(hm, request, requestData)
            
            render hm as JSON
            
        } catch (Exception e) {
            handleException(e)
        }
    }
    
    /**
     * Common request processing without body parameters (GET requests)
     */
    private void processRequestWithoutParams(String methodName) {
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
            recExamService."${methodName}"(hm, request, data)
            
            render hm as JSON
            
        } catch (Exception e) {
            handleException(e)
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
        processRequestWithoutParams("generateSecretCodes")
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
        processRequestWithoutParams("getExamApplicants")
    }
    
    /**
     * API 3: Allocate Questions
     * POST /recExam/allocateQuestions
     * Headers: EPC-UID
     * 
     * Allocate questions to applicants based on weightage
     */
    def allocateQuestions() {
        processRequestWithoutParams("allocateQuestions")
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
        processRequestWithoutParams("getGroups")
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
        processRequestWithoutParams("getSchedule")
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
        processRequestWithParams("setSchedule")
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
        processRequestWithParams("setScheduleForAll")
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
        processRequestWithParams("extendTime")
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
        processRequestWithParams("stopExam")
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
        processRequestWithParams("startExam")
    }
    
    // ═══════════════════════════════════════════════════════════════
    // Phase 3: Supervision APIs
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * API 11: Get Current Exams
     * GET /recExam/getCurrentExams
     * Headers: EPC-UID
     * 
     * Get all exams scheduled for today (supervisor dashboard)
     */
    def getCurrentExams() {
        processRequestWithoutParams("getCurrentExams")
    }
    
    /**
     * API 12: Get Supervisors
     * GET /recExam/getSupervisors
     * Headers: EPC-UID
     * 
     * Get list of all instructors and current supervisors
     */
    def getSupervisors() {
        processRequestWithoutParams("getSupervisors")
    }
    
    /**
     * API 13: Appoint Supervisor
     * POST /recExam/appointSupervisor
     * Headers: EPC-UID
     * Body: { instructorId }
     * 
     * Assign supervisor role to an instructor
     */
    def appointSupervisor() {
        processRequestWithParams("appointSupervisor")
    }
    
    // ═══════════════════════════════════════════════════════════════
    // Phase 4: Results & Selection APIs
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * API 14: Get Expert Groups
     * GET /recExam/getExpertGroups
     * Headers: EPC-UID
     * 
     * Get all expert groups for result viewing and selection
     */
    def getExpertGroups() {
        processRequestWithoutParams("getExpertGroups")
    }
    
    /**
     * API 15: Get Results By Group
     * GET /recExam/getResultsByGroup
     * Headers: EPC-UID
     * Query Params: expertGroupId (required)
     * 
     * View exam results by expert group (sorted by score)
     */
    def getResultsByGroup() {
        processRequestWithoutParams("getResultsByGroup")
    }
    
    /**
     * API 16: Save Selected Applications
     * POST /recExam/saveSelectedApplications
     * Headers: EPC-UID
     * Body: { expertGroupId, cutoff, applications: [{applicationId, marks}] }
     * 
     * Apply cutoff and save selected/rejected candidates
     */
    def saveSelectedApplications() {
        processRequestWithParams("saveSelectedApplications")
    }
}
