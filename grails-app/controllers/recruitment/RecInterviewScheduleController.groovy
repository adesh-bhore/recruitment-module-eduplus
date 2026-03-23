package recruitment

import grails.rest.*
import grails.converters.*

class RecInterviewScheduleController {
    static responseFormats = ['json', 'xml']
    
    def sendMailService

    def handleException(Exception e) {
        HashMap hashMap = new HashMap()
        hashMap.put("error_msg", e.message)
        render hashMap as JSON
        return
    }

    def processRequest(serviceMethod, serviceInstance) {
        println("Processing request for: ${serviceMethod}")

        HashMap hm = new HashMap()
        hm.putAll(commonData(request))
        hm.put("msg", "Failed!!!")
        hm.put("flag", false)
        serviceInstance."${serviceMethod}"(hm, request, request.JSON)
        render hm as JSON
    }

    def processRequestWithoutParams(String methodName, serviceInstance) {
        println("In $methodName")
        HashMap hm = new HashMap()
        hm.putAll(commonData(request))
        hm.put("msg", "Failed!!!")
        hm.put("flag", false)
        serviceInstance."$methodName"(hm, request)
        render hm as JSON
    }
    
    def processRequestWithEmail(serviceMethod, serviceInstance) {
        println("Processing request for: ${serviceMethod}")

        HashMap hm = new HashMap()
        hm.putAll(commonData(request))
        hm.put("msg", "Failed!!!")
        hm.put("flag", false)
        serviceInstance."${serviceMethod}"(hm, request, request.JSON, sendMailService)
        render hm as JSON
    }
    
    // Helper method to get service instances
    private def getServiceNew() {
        return new RecInterviewScheduleService_NEW()
    }
    
    private def getService2() {
        return new RecInterviewScheduleService_2()
    }
    
    private def getService3() {
        return new RecInterviewScheduleService_3()
    }

    def commonData(request) {
        def loginId = request.getHeader("EPC-UID")
        println("DEBUG: EPC-UID header value: ${loginId}")
        
        Login login = Login.findByUsernameAndIsloginblocked(loginId, false)
        println("DEBUG: Login found: ${login?.id}, username: ${login?.username}")
        
        Instructor instructor = Instructor.findByUid(login?.username)
        println("DEBUG: Instructor found: ${instructor?.id}, uid: ${instructor?.uid}, org: ${instructor?.organization?.id}")
        
        def hm = [:]
        hm.inst = instructor
        hm.org = instructor?.organization
        return hm
    }

    ///////////////////////////////////////////////////////////////////////

    /**
     * API Endpoint: Get interview schedule list with filter data
     * URL: /recInterviewSchedule/getinterviewschedule
     * Method: GET
     * Headers: EPC-UID (username)
     * Returns: JSON with interview schedules, academic years, organizations, versions, departments, posts
     */
    def getinterviewschedule() {
        processRequestWithoutParams("getInterviewScheduleList", getServiceNew())
    }

    /**
     * API Endpoint: Get recruitment versions by organization and academic year
     * URL: /recInterviewSchedule/getRecVersion
     * Method: POST
     * Headers: EPC-UID (username)
     * Body: { "org": organizationId (optional), "ay": academicYearId }
     * Returns: JSON with list of recruitment versions
     */
    def getRecVersion() {
        processRequest("getRecVersion", getServiceNew())
    }

    /**
     * API Endpoint: Get departments by organization and recruitment version
     * URL: /recInterviewSchedule/getDept
     * Method: POST
     * Headers: EPC-UID (username)
     * Body: { "org": organizationId (optional), "recver": recVersionId }
     * Returns: JSON with list of departments
     */
    def getDept() {
        processRequest("getDept", getServiceNew())
    }

    /**
     * API Endpoint: Get posts by organization and recruitment version
     * URL: /recInterviewSchedule/getPost
     * Method: POST
     * Headers: EPC-UID (username)
     * Body: { "org": organizationId (optional), "recver": recVersionId }
     * Returns: JSON with list of posts
     */
    def getPost() {
        processRequest("getPost", getServiceNew())
    }

    /**
     * API Endpoint: Get interview list by organization and recruitment version
     * URL: /recInterviewSchedule/getInterviewList
     * Method: POST
     * Headers: EPC-UID (username)
     * Body: { "org": organizationId (optional), "recver": recVersionId }
     * Returns: JSON with list of interview schedules
     */
    def getInterviewList() {
        processRequest("getInterviewList", getServiceNew())
    }

    /**
     * API Endpoint: Save new interview schedule
     * URL: /recInterviewSchedule/saveinterviewschedule
     * Method: POST
     * Headers: EPC-UID (username)
     * Body: { 
     *   "org": organizationId (optional), 
     *   "recversion": recVersionId, 
     *   "department": departmentId, 
     *   "post": postId,
     *   "interview_date": "2024-12-25",
     *   "interview_venue": "Main Building, Room 101",
     *   "interview_time": "10:00 AM"
     * }
     * Returns: JSON with saved schedule details
     */
    def saveinterviewschedule() {
        processRequest("saveInterviewSchedule", getServiceNew())
    }

    /**
     * API Endpoint: Update existing interview schedule
     * URL: /recInterviewSchedule/editinterviewschedule
     * Method: POST
     * Headers: EPC-UID (username)
     * Body: { 
     *   "recid": scheduleId,
     *   "interview_date": "2024-12-25",
     *   "interview_venue": "Main Building, Room 101",
     *   "interview_time": "10:00 AM"
     * }
     * Returns: JSON with updated schedule details
     */
    def editinterviewschedule() {
        processRequest("editInterviewSchedule", getServiceNew())
    }

    /**
     * API Endpoint: Delete interview schedule
     * URL: /recInterviewSchedule/deletesched
     * Method: POST
     * Headers: EPC-UID (username)
     * Body: { "recshcid": scheduleId }
     * Returns: JSON with success/failure message
     */
    def deletesched() {
        processRequest("deleteSched", getServiceNew())
    }

    /**
     * API Endpoint: Send interview call letters via email
     * URL: /recInterviewSchedule/sendmail
     * Method: POST
     * Headers: EPC-UID (username)
     * Body: { "version": recVersionId }
     * Returns: JSON with email sending statistics (emailsSent, emailsFailed, failedEmails)
     */
    def sendmail() {
        processRequestWithEmail("sendInterviewCallLetters", getServiceNew())
    }

    /**
     * API Endpoint: Preview interview call letters
     * URL: /recInterviewSchedule/preview_callletter
     * Method: POST
     * Headers: EPC-UID (username)
     * Body: { "version": recVersionId }
     * Returns: JSON with list of candidates and their interview details
     */
    def preview_callletter() {
        processRequest("previewCallLetters", getServiceNew())
    }

    // ── Branch APIs ──────────────────────────────────────────────────

    /**
     * API Endpoint: Get all branches with form data (programs + versions)
     * URL: /recInterviewSchedule/getRecBranchList
     * Method: GET
     * Headers: EPC-UID
     */
    def getRecBranchList() {
        processRequestWithoutParams("getRecBranchList", getService2())
    }

    /**
     * API Endpoint: Save new recruitment branch
     * URL: /recInterviewSchedule/saverecBranch
     * Method: POST
     * Headers: EPC-UID
     * Body: { "recver": 1, "program": 1, "recbranch": "CSE", "recbranchabbr": "CSE" }
     */
    def saverecBranch() {
        processRequest("saveRecBranch", getService2())
    }

    /**
     * API Endpoint: Edit existing recruitment branch
     * URL: /recInterviewSchedule/editRecBranch
     * Method: POST
     * Headers: EPC-UID
     * Body: { "editId": 1, "program": 1, "recbranch": "Updated Name", "recbranchabbr": "UPD" }
     */
    def editRecBranch() {
        processRequest("editRecBranch", getService2())
    }

    /**
     * API Endpoint: Delete recruitment branch
     * URL: /recInterviewSchedule/deleterecBranch
     * Method: POST
     * Headers: EPC-UID
     * Body: { "brcid": 1 }
     */
    def deleterecBranch() {
        processRequest("deleteRecBranch", getService2())
    }

    /**
     * API Endpoint: Toggle isActive on a recruitment branch
     * URL: /recInterviewSchedule/isActiveBranch
     * Method: POST
     * Headers: EPC-UID
     * Body: { "ActiveId": 1 }
     */
    def isActiveBranch() {
        processRequest("toggleBranchActive", getService2())
    }

    // ── Post Management APIs ─────────────────────────────────────────

    /**
     * API Endpoint: Get all posts with form data (designations + versions)
     * URL: /recInterviewSchedule/getRecPostList
     * Method: GET
     * Headers: EPC-UID
     */
    def getRecPostList() {
        processRequestWithoutParams("getRecPostList", getService2())
    }

    /**
     * API Endpoint: Save new recruitment post
     * URL: /recInterviewSchedule/saverecPost
     * Method: POST
     * Headers: EPC-UID
     * Body: { "recver": 1, "designation": 1 }
     */
    def saverecPost() {
        processRequest("saveRecPost", getService2())
    }

    /**
     * API Endpoint: Edit existing recruitment post
     * URL: /recInterviewSchedule/editRecPost
     * Method: POST
     * Headers: EPC-UID
     * Body: { "editId": 1, "designation": 2 }
     */
    def editRecPost() {
        processRequest("editRecPost", getService2())
    }

    /**
     * API Endpoint: Delete recruitment post
     * URL: /recInterviewSchedule/deleterecPost
     * Method: POST
     * Headers: EPC-UID
     * Body: { "brcid": 1 }
     */
    def deleterecPost() {
        processRequest("deleteRecPost", getService2())
    }

    /**
     * API Endpoint: Toggle isActive on a recruitment post
     * URL: /recInterviewSchedule/isActivePost
     * Method: POST
     * Headers: EPC-UID
     * Body: { "ActiveId": 1 }
     */
    def isActivePost() {
        processRequest("togglePostActive", getService2())
    }

    // ── Post Assignment APIs ─────────────────────────────────────────

    /**
     * API Endpoint: Get all faculty posts and instructors for assignment
     * URL: /recInterviewSchedule/getAssignPostList
     * Method: GET
     * Headers: EPC-UID
     */
    def getAssignPostList() {
        processRequestWithoutParams("getAssignPostList", getService2())
    }

    /**
     * API Endpoint: Assign faculty post to instructor
     * URL: /recInterviewSchedule/saveassignPost
     * Method: POST
     * Headers: EPC-UID
     * Body: { "inst": 1, "post": 1 }
     */
    def saveassignPost() {
        processRequest("saveAssignPost", getService2())
    }

    /**
     * API Endpoint: Remove faculty post assignment from instructor
     * URL: /recInterviewSchedule/deleteassignPost
     * Method: POST
     * Headers: EPC-UID
     * Body: { "instid": 1, "postid": 1 }
     */
    def deleteassignPost() {
        processRequest("deleteAssignPost", getService2())
    }

    // ── Document Type Management APIs ────────────────────────────────

    /**
     * API Endpoint: Get all document types
     * URL: /recInterviewSchedule/getRecDocumentTypeList
     * Method: GET
     * Headers: EPC-UID
     */
    def getRecDocumentTypeList() {
        processRequestWithoutParams("getRecDocumentTypeList", getService3())
    }

    /**
     * API Endpoint: Save new document type
     * URL: /recInterviewSchedule/saveRecDocumentType
     * Method: POST
     * Headers: EPC-UID
     * Body: { "type": "Aadhar Card", "size": "2MB", "extension": "pdf,jpg", "info": "Upload Aadhar", "resolution": "300dpi", "isactive": true, "iscompulsory": true }
     */
    def saveRecDocumentType() {
        processRequest("saveRecDocumentType", getService3())
    }

    /**
     * API Endpoint: Edit existing document type
     * URL: /recInterviewSchedule/editRecDocumentType
     * Method: POST
     * Headers: EPC-UID
     * Body: { "editId": 1, "type": "Updated Name", "size": "5MB", "extension": "pdf", "info": "Updated info", "resolution": "600dpi", "isactive": true, "iscompulsory": false }
     */
    def editRecDocumentType() {
        processRequest("editRecDocumentType", getService3())
    }

    /**
     * API Endpoint: Delete document type
     * URL: /recInterviewSchedule/deleteRecDocumentType
     * Method: POST
     * Headers: EPC-UID
     * Body: { "deleteId": 1 }
     */
    def deleteRecDocumentType() {
        processRequest("deleteRecDocumentType", getService3())
    }

    /**
     * API Endpoint: Toggle isActive on a document type
     * URL: /recInterviewSchedule/isActiveDocumentType
     * Method: POST
     * Headers: EPC-UID
     * Body: { "ActiveId": 1 }
     */
    def isActiveDocumentType() {
        processRequest("toggleDocumentTypeActive", getService3())
    }

    // ── Document Viewing APIs ────────────────────────────────────────

    /**
     * API Endpoint: Get list of applications with documents
     * URL: /recInterviewSchedule/recdocumentList
     * Method: GET
     * Headers: EPC-UID
     */
    def recdocumentList() {
        processRequestWithoutParams("getRecDocumentList", getService3())
    }

    /**
     * API Endpoint: Get documents for a specific applicant
     * URL: /recInterviewSchedule/getdoc
     * Method: POST
     * Headers: EPC-UID
     * Body: { "recapcntid": 1 }
     */
    def getdoc() {
        processRequest("getApplicantDocuments", getService3())
    }

    /**
     * API Endpoint: Get document statistics
     * URL: /recInterviewSchedule/getDocumentStatistics
     * Method: GET
     * Headers: EPC-UID
     */
    def getDocumentStatistics() {
        processRequestWithoutParams("getDocumentStatistics", getService3())
    }
}
