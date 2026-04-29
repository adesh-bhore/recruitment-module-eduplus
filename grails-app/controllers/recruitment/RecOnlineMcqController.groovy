package recruitment

import grails.converters.JSON

class RecOnlineMcqController {
    
    def recOnlineMcqService
    
    // ═══════════════════════════════════════════════════════════════
    // Centralized Exception Handler
    // ═══════════════════════════════════════════════════════════════
    
    private def handleException(Exception e) {
        println("Exception in RecOnlineMcqController: ${e.message}")
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
            
            recOnlineMcqService."${methodName}"(hm, request, params)
            render hm as JSON
        } catch (Exception e) {
            handleException(e)
        }
    }
    
    private def processMultipartRequest(String methodName) {
        try {
            def uid = request.getHeader("EPC-UID")
            
            if (!uid) {
                render([flag: false, msg: "User not authenticated"] as JSON)
                return
            }
            
            HashMap hm = new HashMap()
            hm.put("uid", uid)
            
            // Extract params — skip file entries (MultipartFile) to avoid JSON serialization errors
            params.each { key, value ->
                if (key != 'controller' && key != 'action') {
                    if (value instanceof String || value instanceof Number || value instanceof Boolean) {
                        hm.put(key, value)
                    }
                }
            }
            
            recOnlineMcqService."${methodName}"(hm, request, params)
            render hm as JSON
        } catch (Exception e) {
            handleException(e)
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // Phase 1: Question Bank Management APIs (Admin)
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Get instructor's courses list
     * GET /recOnlineMcq/getCourses
     * 
     * Response:
     * {
     *   "courses": [
     *     {
     *       "id": 1,
     *       "name": "Computer Science",
     *       "code": "CS101",
     *       "description": "..."
     *     }
     *   ],
     *   "msg": "Courses fetched successfully",
     *   "flag": true
     * }
     */
    def getCourses() {
        processRequest("getInstructorCourses")
    }
    
    /**
     * Get all questions for a course with pagination
     * GET /recOnlineMcq/getQuestionBank
     * 
     * Params:
     * - courseId (required): Course ID
     * - groupNo (optional): Department group number for filtering
     * - page (optional): Page number (default: 1)
     * - pageSize (optional): Items per page (default: 50)
     * 
     * Response:
     * {
     *   "course": {
     *     "id": 1,
     *     "name": "Computer Science",
     *     "code": "CS101"
     *   },
     *   "questions": [
     *     {
     *       "que_id": 1,
     *       "qno": 1,
     *       "question_statement": "What is...",
     *       "weightage": 2,
     *       "isapproved": true,
     *       "unitno": 1,
     *       "recdeptgroup": 1,
     *       "recdeptgroupId": 1,
     *       "doc_url": "https://...",
     *       "question_file_name": "image.png"
     *     }
     *   ],
     *   "totalCount": 100,
     *   "page": 1,
     *   "pageSize": 50,
     *   "totalPages": 2,
     *   "deptGroups": [
     *     {
     *       "id": 1,
     *       "groupno": 1,
     *       "name": "Group A"
     *     }
     *   ],
     *   "msg": "Question bank fetched successfully",
     *   "flag": true
     * }
     */
    def getQuestionBank() {
        processRequest("getQuestionBank")
    }
    
    /**
     * Create new MCQ question with options
     * POST /recOnlineMcq/createQuestion
     * Content-Type: multipart/form-data
     * 
     * Params:
     * - courseId (required): Course ID
     * - qno (required): Question number
     * - question_statement (required): Question text
     * - weightage (required): Question weightage/marks
     * - isapproved (optional): Approval status (true/false)
     * - recdeptgroup (required): Department group ID
     * - selectoption (required): Number of options (1-5)
     * - option_statement_1 to option_statement_5: Option texts
     * - correct_answer (required): Correct answer (isanswer1 to isanswer5)
     * - qfile (optional): Question image file
     * - file1 to file5 (optional): Option image files
     * 
     * Response:
     * {
     *   "questionId": 123,
     *   "msg": "Question created successfully",
     *   "flag": true
     * }
     */
    def createQuestion() {
        processMultipartRequest("createQuestion")
    }
    
    /**
     * Update existing question with options
     * PUT /recOnlineMcq/updateQuestion/:id
     * Content-Type: multipart/form-data
     * 
     * Params:
     * - questionId (required): Question ID (from URL)
     * - courseId (optional): Course ID
     * - question_statement (optional): Question text
     * - weightage (optional): Question weightage/marks
     * - isapproved (optional): Approval status (true/false)
     * - recdeptgroup (optional): Department group ID
     * - selectoption (optional): Number of options (1-5)
     * - option_statement_1 to option_statement_5: Option texts
     * - option_id_1 to option_id_5: Existing option IDs
     * - correct_answer (optional): Correct answer (isanswer1 to isanswer5)
     * - qfile (optional): Question image file
     * - file1 to file5 (optional): Option image files
     * 
     * Response:
     * {
     *   "questionId": 123,
     *   "msg": "Question updated successfully",
     *   "flag": true
     * }
     */
    def updateQuestion() {
        processMultipartRequest("updateQuestion")
    }
    
    /**
     * Toggle question approval status
     * PATCH /recOnlineMcq/toggleApproval/:id
     * 
     * Params:
     * - questionId (required): Question ID (from URL)
     * - isapproved (required): Approval status (true/false)
     * 
     * Response:
     * {
     *   "questionId": 123,
     *   "isapproved": true,
     *   "msg": "Question approval status updated successfully",
     *   "flag": true
     * }
     */
    def toggleApproval() {
        processRequest("toggleQuestionApproval")
    }
    
    // ═══════════════════════════════════════════════════════════════
    // Phase 2: Question Details and File Management APIs
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Get full question details with all options for editing
     * GET /recOnlineMcq/getQuestionDetails
     */
    def getQuestionDetails() {
        processRequest("getQuestionDetails")
    }
    
    /**
     * Get only options for a specific question
     * GET /recOnlineMcq/getQuestionOptions
     */
    def getQuestionOptions() {
        processRequest("getQuestionOptions")
    }
    
    /**
     * Delete question image file from AWS
     * DELETE /recOnlineMcq/deleteQuestionFile
     */
    def deleteQuestionFile() {
        processRequest("deleteQuestionFile")
    }
    
    /**
     * Delete option image file from AWS
     * DELETE /recOnlineMcq/deleteOptionFile
     */
    def deleteOptionFile() {
        processRequest("deleteOptionFile")
    }
    
    // ═══════════════════════════════════════════════════════════════
    // Phase 3: Student View and Question Display APIs
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Get all approved questions for a course (student exam view)
     * GET /recOnlineMcq/studentView
     * 
     * Params:
     * - courseId (required): Course ID
     * 
     * Response:
     * {
     *   "course": {
     *     "id": 1,
     *     "name": "Computer Science",
     *     "code": "CS101"
     *   },
     *   "questions": [
     *     {
     *       "que_id": 10001,
     *       "qno": 1,
     *       "question_statement": "What is...",
     *       "weightage": 5,
     *       "unitno": 1,
     *       "question_file_name": "image.png",
     *       "question_file_url": "https://...",
     *       "options": [
     *         {
     *           "id": 1001,
     *           "opno": 1,
     *           "option_statement": "Option A",
     *           "iscorrecetoption": true,
     *           "option_file_name": "opt1.png",
     *           "option_file_url": "https://..."
     *         }
     *       ]
     *     }
     *   ],
     *   "totalQuestions": 50,
     *   "approvedQuestions": 45,
     *   "difficultyLevels": [
     *     {"id": 1, "name": "Easy", "description": "..."}
     *   ],
     *   "msg": "Questions fetched successfully",
     *   "flag": true
     * }
     */
    def studentView() {
        processRequest("getStudentView")
    }
    
    /**
     * Get a specific question with all its options (detailed view)
     * GET /recOnlineMcq/getQuestionWithOptions
     * 
     * Params:
     * - questionId (required): Question ID
     * 
     * Response:
     * {
     *   "question": {
     *     "id": 10001,
     *     "qno": 1,
     *     "question_statement": "What is...",
     *     "weightage": 5,
     *     "isapproved": true,
     *     "unitno": 1,
     *     "question_file_name": "image.png",
     *     "question_file_url": "https://..."
     *   },
     *   "options": [
     *     {
     *       "id": 1001,
     *       "opno": 1,
     *       "option_statement": "Option A",
     *       "iscorrecetoption": true,
     *       "option_file_name": "opt1.png",
     *       "option_file_url": "https://..."
     *     }
     *   ],
     *   "msg": "Question with options fetched successfully",
     *   "flag": true
     * }
     */
    def getQuestionWithOptions() {
        processRequest("getQuestionWithOptions")
    }
}
