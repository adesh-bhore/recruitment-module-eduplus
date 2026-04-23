package recruitment

import grails.converters.JSON

class RecExpertController {
    
    def recExpertService
    def recExpertService2
    def recExpertService3
    
    // ═══════════════════════════════════════════════════════════════
    // Centralized Exception Handler
    // ═══════════════════════════════════════════════════════════════
    
    private def handleException(Exception e) {
        println("Exception in RecExpertController: ${e.message}")
        e.printStackTrace()
        HashMap hashMap = new HashMap()
        hashMap.put("error_msg", e.message)
        hashMap.put("flag", false)
        render hashMap as JSON
        return
    }
    
    // ═══════════════════════════════════════════════════════════════
    // Helper Methods
    // ═══════════════════════════════════════════════════════════════
    
    private def processRequest(String methodName) {
        try {
            def uid = request.getHeader("EPC-UID")
            
            if (!uid) {
                render([flag: false, msg: "User not authenticated"] as JSON)
                return
            }
            
            HashMap hm = new HashMap()
            hm.put("uid", uid)
            
            // Extract params
            params.each { key, value ->
                if (key != 'controller' && key != 'action') {
                    hm.put(key, value)
                }
            }
            
            recExpertService."${methodName}"(hm, request, params)
            render hm as JSON
        } catch (Exception e) {
            handleException(e)
        }
    }
    
    private def processJsonRequest(String methodName) {
        try {
            def uid = request.getHeader("EPC-UID")
            
            if (!uid) {
                render([flag: false, msg: "User not authenticated"] as JSON)
                return
            }
            
            HashMap hm = new HashMap()
            hm.put("uid", uid)
            
            // Extract params
            params.each { key, value ->
                if (key != 'controller' && key != 'action') {
                    hm.put(key, value)
                }
            }
            
            // Parse JSON body if present
            def jsonData = request.JSON
            
            recExpertService."${methodName}"(hm, request, jsonData ?: params)
            render hm as JSON
        } catch (Exception e) {
            handleException(e)
        }
    }
    
    private def processRequest2(String methodName) {
        try {
            def uid = request.getHeader("EPC-UID")
            
            if (!uid) {
                render([flag: false, msg: "User not authenticated"] as JSON)
                return
            }
            
            HashMap hm = new HashMap()
            hm.put("uid", uid)
            
            // Extract params
            params.each { key, value ->
                if (key != 'controller' && key != 'action') {
                    hm.put(key, value)
                }
            }
            
            recExpertService2."${methodName}"(hm, request, params)
            render hm as JSON
        } catch (Exception e) {
            handleException(e)
        }
    }
    
    private def processJsonRequest2(String methodName) {
        try {
            def uid = request.getHeader("EPC-UID")
            
            if (!uid) {
                render([flag: false, msg: "User not authenticated"] as JSON)
                return
            }
            
            HashMap hm = new HashMap()
            hm.put("uid", uid)
            
            // Parse JSON body
            def jsonData = request.JSON
            
            recExpertService2."${methodName}"(hm, request, jsonData)
            render hm as JSON
        } catch (Exception e) {
            handleException(e)
        }
    }
    
    private def processRequest3(String methodName) {
        try {
            def uid = request.getHeader("EPC-UID")
            
            if (!uid) {
                render([flag: false, msg: "User not authenticated"] as JSON)
                return
            }
            
            HashMap hm = new HashMap()
            hm.put("uid", uid)
            
            // Extract params
            params.each { key, value ->
                if (key != 'controller' && key != 'action') {
                    hm.put(key, value)
                }
            }
            
            recExpertService3."${methodName}"(hm, request, params)
            render hm as JSON
        } catch (Exception e) {
            handleException(e)
        }
    }
    
    private def processJsonRequest3(String methodName) {
        try {
            def uid = request.getHeader("EPC-UID")
            
            if (!uid) {
                render([flag: false, msg: "User not authenticated"] as JSON)
                return
            }
            
            HashMap hm = new HashMap()
            hm.put("uid", uid)
            
            // Parse JSON body
            def jsonData = request.JSON
            
            recExpertService3."${methodName}"(hm, request, jsonData)
            render hm as JSON
        } catch (Exception e) {
            handleException(e)
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // Phase 1: Expert CRUD APIs
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Get initial data for expert management form
     * GET /recExpert/getInitialData
     * 
     * Response:
     * {
     *   "aylist": [{"id": 1, "year": "2024-2025"}],
     *   "recversionlist": [{"id": 1, "version_number": 1}],
     *   "recversion": {"id": 1, "version_number": 1},
     *   "aay": {"id": 1, "academicyear": {"id": 1, "year": "2024-2025"}},
     *   "orglist": [{"id": 1, "name": "VIT University"}],
     *   "org": {"id": 1, "name": "VIT University"},
     *   "is_management": true,
     *   "recdeptexpertgrouplist": [
     *     {"id": 1, "groupno": 1, "groupname": "COMP-IT-MCA", "cutoff": 60.0}
     *   ],
     *   "recexpertlist": [
     *     {
     *       "id": 1,
     *       "expertno": 1,
     *       "expname": "Dr. John Doe",
     *       "loginname": "11111",
     *       "password": "123456",
     *       "isblocked": false,
     *       "recdeptexpertgroup": {"id": 1, "groupname": "COMP-IT-MCA"},
     *       "recexperttype": {"id": 1, "type": "Technical Expert"}
     *     }
     *   ],
     *   "recexperttypelist": [{"id": 1, "type": "Technical Expert"}],
     *   "msg": "Initial data fetched successfully",
     *   "flag": true
     * }
     */
    def getInitialData() {
        processRequest("getInitialData")
    }
    
    /**
     * Get filtered data based on organization
     * GET /recExpert/getFilters
     * 
     * Params:
     * - organizationId (optional): Organization ID to filter by
     * 
     * Response: Same as getInitialData
     */
    def getFilters() {
        processRequest("getFilters")
    }
    
    /**
     * Create new expert
     * POST /recExpert/saveExpert
     * Content-Type: application/json
     * 
     * Body:
     * {
     *   "expertname": "Dr. John Doe",
     *   "deptExpertGroupId": 1,
     *   "expertTypeId": 1
     * }
     * 
     * Response:
     * {
     *   "expertId": 123,
     *   "expertno": 5,
     *   "loginname": "11115",
     *   "password": "456789",
     *   "msg": "Expert created successfully",
     *   "flag": true
     * }
     */
    def saveExpert() {
        processJsonRequest("saveExpert")
    }
    
    /**
     * Delete expert
     * DELETE /recExpert/deleteExpert/:id
     * 
     * Params:
     * - expertId (required): Expert ID (from URL)
     * 
     * Response:
     * {
     *   "expertId": 123,
     *   "msg": "Expert deleted successfully",
     *   "flag": true
     * }
     */
    def deleteExpert() {
        processRequest("deleteExpert")
    }
    
    /**
     * Block expert (set isblocked = true)
     * POST /recExpert/blockExpert/:id
     * 
     * Params:
     * - expertId (required): Expert ID (from URL)
     * 
     * Response:
     * {
     *   "expertId": 123,
     *   "isblocked": true,
     *   "msg": "Expert blocked successfully",
     *   "flag": true
     * }
     */
    def blockExpert() {
        processRequest("blockExpert")
    }
    
    /**
     * Unblock expert (set isblocked = false)
     * POST /recExpert/unblockExpert/:id
     * 
     * Params:
     * - expertId (required): Expert ID (from URL)
     * 
     * Response:
     * {
     *   "expertId": 123,
     *   "isblocked": false,
     *   "msg": "Expert unblocked successfully",
     *   "flag": true
     * }
     */
    def unblockExpert() {
        processRequest("unblockExpert")
    }
    
    // ═══════════════════════════════════════════════════════════════
    // Phase 2: Expert Authentication APIs
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Authenticate expert
     * POST /recExpert/login
     * Content-Type: application/json
     * 
     * Body:
     * {
     *   "loginname": "11111",
     *   "password": "123456"
     * }
     * 
     * Response:
     * {
     *   "expert": {
     *     "id": 1,
     *     "expertno": 1,
     *     "expname": "Dr. John Doe",
     *     "loginname": "11111",
     *     "isblocked": false,
     *     "organization": {
     *       "id": 1,
     *       "name": "VIT University"
     *     },
     *     "recversion": {
     *       "id": 1,
     *       "version_number": 1
     *     },
     *     "recdeptexpertgroup": {
     *       "id": 1,
     *       "groupno": 1,
     *       "groupname": "COMP-IT-MCA"
     *     },
     *     "recexperttype": {
     *       "id": 1,
     *       "type": "Technical Expert"
     *     }
     *   },
     *   "msg": "Login successful",
     *   "flag": true
     * }
     */
    def login() {
        try {
            HashMap hm = new HashMap()
            
            // Parse JSON body
            def jsonData = request.JSON
            
            recExpertService.loginExpert(hm, request, jsonData)
            render hm as JSON
        } catch (Exception e) {
            handleException(e)
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // Phase 3: Marks Evaluation APIs
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Get applicants list assigned to expert for marks evaluation
     * GET /recExpert/getApplicantsForMarks
     * 
     * Headers:
     * - EPC-UID: Expert's loginname (e.g., "11111")
     * 
     * Response:
     * {
     *   "applicants": [
     *     {
     *       "id": 1,
     *       "applicationId": "120240101001",
     *       "fullname": "John Doe",
     *       "email": "john@example.com",
     *       "hasMarks": true,
     *       "totalMarks": 85.5,
     *       "maxTotalMarks": 100.0
     *     }
     *   ],
     *   "evaluationParameters": [
     *     {
     *       "id": 1,
     *       "parameter": "Technical Knowledge",
     *       "parameter_number": 1,
     *       "maxmarks": 50.0
     *     }
     *   ],
     *   "maxTotalMarks": 100.0,
     *   "msg": "Applicants fetched successfully",
     *   "flag": true
     * }
     */
    def getApplicantsForMarks() {
        try {
            def expertLoginname = request.getHeader("EPC-UID")
            
            if (!expertLoginname) {
                render([flag: false, msg: "Expert not authenticated"] as JSON)
                return
            }
            
            HashMap hm = new HashMap()
            hm.put("expertLoginname", expertLoginname)
            
            recExpertService2.getApplicantsForMarks(hm, request, params)
            render hm as JSON
        } catch (Exception e) {
            handleException(e)
        }
    }
    
    /**
     * Get evaluation form for specific applicant
     * GET /recExpert/getEvaluationForm
     * 
     * Headers:
     * - EPC-UID: Expert's loginname (e.g., "11111")
     * 
     * Params:
     * - applicationId (required): Application ID
     * 
     * Response:
     * {
     *   "application": {
     *     "id": 1,
     *     "applicationId": "120240101001",
     *     "applicant": {
     *       "id": 1,
     *       "fullname": "John Doe",
     *       "email": "john@example.com"
     *     }
     *   },
     *   "evaluationParameters": [
     *     {
     *       "id": 1,
     *       "parameter": "Technical Knowledge",
     *       "parameter_number": 1,
     *       "maxmarks": 50.0,
     *       "obtainedMarks": 45.0
     *     }
     *   ],
     *   "msg": "Evaluation form fetched successfully",
     *   "flag": true
     * }
     */
    def getEvaluationForm() {
        try {
            def expertLoginname = request.getHeader("EPC-UID")
            
            if (!expertLoginname) {
                render([flag: false, msg: "Expert not authenticated"] as JSON)
                return
            }
            
            HashMap hm = new HashMap()
            hm.put("expertLoginname", expertLoginname)
            
            // Extract params
            params.each { key, value ->
                if (key != 'controller' && key != 'action') {
                    hm.put(key, value)
                }
            }
            
            recExpertService2.getEvaluationForm(hm, request, params)
            render hm as JSON
        } catch (Exception e) {
            handleException(e)
        }
    }
    
    /**
     * Submit marks for applicant
     * POST /recExpert/submitMarks
     * Content-Type: application/json
     * 
     * Headers:
     * - EPC-UID: Expert's loginname (e.g., "11111")
     * 
     * Body:
     * {
     *   "applicationId": 1,
     *   "marks": [
     *     {"parameterId": 1, "obtainedMarks": 45.0},
     *     {"parameterId": 2, "obtainedMarks": 40.5}
     *   ]
     * }
     * 
     * Response:
     * {
     *   "applicationId": 1,
     *   "totalMarks": 85.5,
     *   "parameterWiseAverages": [
     *     {"parameterId": 1, "average": 46.5},
     *     {"parameterId": 2, "average": 42.0}
     *   ],
     *   "totalAverage": 88.5,
     *   "msg": "Marks submitted successfully",
     *   "flag": true
     * }
     */
    def submitMarks() {
        try {
            def expertLoginname = request.getHeader("EPC-UID")
            
            if (!expertLoginname) {
                render([flag: false, msg: "Expert not authenticated"] as JSON)
                return
            }
            
            HashMap hm = new HashMap()
            hm.put("expertLoginname", expertLoginname)
            
            // Parse JSON body
            def jsonData = request.JSON
            
            recExpertService2.submitMarks(hm, request, jsonData)
            render hm as JSON
        } catch (Exception e) {
            handleException(e)
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // Phase 4: Dashboard & Reports APIs
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Get expert groups dashboard for instructors
     * GET /recExpert/getExpertGroupsDashboard
     * 
     * Headers:
     * - EPC-UID: Instructor's email (e.g., "roshan1@edupluscampus.com")
     * 
     * Response:
     * {
     *   "expertGroups": [
     *     {
     *       "id": 1,
     *       "groupno": 1,
     *       "groupname": "COMP-IT-MCA",
     *       "cutoff": 60.0
     *     }
     *   ],
     *   "recversion": {
     *     "id": 1,
     *     "version_number": 1,
     *     "academicyear": "2024-2025"
     *   },
     *   "organization": {
     *     "id": 1,
     *     "name": "VIT University"
     *   },
     *   "msg": "Expert groups fetched successfully",
     *   "flag": true
     * }
     */
    def getExpertGroupsDashboard() {
        processRequest2("getExpertGroupsDashboard")
    }
    
    /**
     * Get applicant marks report for specific expert group
     * GET /recExpert/getApplicantMarksReport
     * 
     * Headers:
     * - EPC-UID: Instructor's email (e.g., "roshan1@edupluscampus.com")
     * 
     * Params:
     * - expertGroupId (required): Expert group ID
     * 
     * Response:
     * {
     *   "applicants": [
     *     {
     *       "applicationId": "120240101001",
     *       "applicantId": 1,
     *       "fullname": "John Doe",
     *       "email": "john@example.com",
     *       "posts": ["Assistant Professor - Computer Science"],
     *       "parameterMarks": [
     *         {
     *           "parameterId": 1,
     *           "parameter": "Technical Knowledge",
     *           "maxmarks": 50.0,
     *           "obtainedMarks": 46.5
     *         }
     *       ],
     *       "totalMarks": 91.75,
     *       "maxTotalMarks": 100.0,
     *       "percentage": 91.75
     *     }
     *   ],
     *   "evaluationParameters": [
     *     {
     *       "id": 1,
     *       "parameter": "Technical Knowledge",
     *       "parameter_number": 1,
     *       "maxmarks": 50.0
     *     }
     *   ],
     *   "expertGroup": {
     *     "id": 1,
     *     "groupno": 1,
     *     "groupname": "COMP-IT-MCA",
     *     "cutoff": 60.0
     *   },
     *   "maxTotalMarks": 100.0,
     *   "totalApplicants": 2,
     *   "evaluatedApplicants": 1,
     *   "pendingApplicants": 1,
     *   "msg": "Applicant marks fetched successfully",
     *   "flag": true
     * }
     */
    def getApplicantMarksReport() {
        processRequest2("getApplicantMarksReport")
    }
    
    // ═══════════════════════════════════════════════════════════════
    // Phase 5A: RecDeptExpertGroup Management APIs
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Get initial data for expert group management form
     * GET /recExpert/getExpertGroupInitialData
     * 
     * Headers:
     * - EPC-UID: Instructor's email
     * 
     * Response:
     * {
     *   "expertGroups": [...],
     *   "departmentList": [...],
     *   "programList": [...],
     *   "recDeptGroup": [...],
     *   "aylist": [...],
     *   "recversionlist": [...],
     *   "orglist": [...],
     *   "msg": "Initial data fetched successfully",
     *   "flag": true
     * }
     */
    def getExpertGroupInitialData() {
        processRequest3("getExpertGroupInitialData")
    }
    
    /**
     * Get filtered expert groups
     * GET /recExpert/getExpertGroupFilters
     * 
     * Headers:
     * - EPC-UID: Instructor's email
     * 
     * Query Params:
     * - organizationId (optional)
     * - academicYearId (optional)
     * - recversionId (optional)
     */
    def getExpertGroupFilters() {
        processRequest3("getExpertGroupFilters")
    }
    
    /**
     * Create or update expert group
     * POST /recExpert/saveExpertGroup
     * Content-Type: application/json
     * 
     * Headers:
     * - EPC-UID: Instructor's email
     * 
     * Body:
     * {
     *   "groupno": 1,
     *   "groupname": "COMP-IT-MCA",
     *   "cutoff": 60.0,
     *   "round2cutoff": 55.0,
     *   "departmentIds": [1, 2, 3],
     *   "programIds": [1, 2],
     *   "recDeptGroupId": 1,
     *   "recversionId": 1,
     *   "expertGroupId": null
     * }
     */
    def saveExpertGroup() {
        processJsonRequest3("saveExpertGroup")
    }
    
    /**
     * Delete expert group
     * DELETE /recExpert/deleteExpertGroup/:id
     * 
     * Headers:
     * - EPC-UID: Instructor's email
     * 
     * Params:
     * - expertGroupId (required): Expert group ID
     */
    def deleteExpertGroup() {
        processRequest3("deleteExpertGroup")
    }
    
    /**
     * Get expert groups report
     * GET /recExpert/getExpertGroupReport
     * 
     * Headers:
     * - EPC-UID: Instructor's email
     * 
     * Query Params:
     * - organizationId (optional)
     * - academicYearId (optional)
     */
    def getExpertGroupReport() {
        processRequest3("getExpertGroupReport")
    }
    
    // ═══════════════════════════════════════════════════════════════
    // Phase 5B: RecExpertType Management APIs
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Get list of expert types
     * GET /recExpert/getExpertTypeList
     * 
     * Headers:
     * - EPC-UID: Instructor's email
     */
    def getExpertTypeList() {
        processRequest3("getExpertTypeList")
    }
    
    /**
     * Create or update expert type
     * POST /recExpert/saveExpertType
     * Content-Type: application/json
     * 
     * Headers:
     * - EPC-UID: Instructor's email
     * 
     * Body:
     * {
     *   "type": "Technical Expert",
     *   "expertTypeId": null
     * }
     */
    def saveExpertType() {
        processJsonRequest3("saveExpertType")
    }
    
    /**
     * Delete expert type
     * DELETE /recExpert/deleteExpertType/:id
     * 
     * Headers:
     * - EPC-UID: Instructor's email
     * 
     * Params:
     * - expertTypeId (required): Expert type ID
     */
    def deleteExpertType() {
        processRequest3("deleteExpertType")
    }
}
