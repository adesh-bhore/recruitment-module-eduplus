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

    def processRequest(serviceMethod) {
        println("Processing request for: ${serviceMethod}")

        HashMap hm = new HashMap()
        hm.putAll(commonData(request))
        hm.put("msg", "Failed!!!")
        hm.put("flag", false)
        RecInterviewScheduleService service = new RecInterviewScheduleService()
        service."${serviceMethod}"(hm, request, request.JSON)
        render hm as JSON
    }

    def processRequestWithoutParams(String methodName) {
        println("In $methodName")
        HashMap hm = new HashMap()
        hm.putAll(commonData(request))
        hm.put("msg", "Failed!!!")
        hm.put("flag", false)
        RecInterviewScheduleService service = new RecInterviewScheduleService()
        service."$methodName"(hm, request)
        render hm as JSON
    }
    
    def processRequestWithEmail(serviceMethod) {
        println("Processing request for: ${serviceMethod}")

        HashMap hm = new HashMap()
        hm.putAll(commonData(request))
        hm.put("msg", "Failed!!!")
        hm.put("flag", false)
        RecInterviewScheduleService service = new RecInterviewScheduleService()
        service."${serviceMethod}"(hm, request, request.JSON, sendMailService)
        render hm as JSON
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
        processRequestWithoutParams("getInterviewScheduleList")
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
        processRequest("getRecVersion")
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
        processRequest("getDept")
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
        processRequest("getPost")
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
        processRequest("getInterviewList")
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
        processRequest("saveInterviewSchedule")
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
        processRequest("editInterviewSchedule")
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
        processRequest("deleteSched")
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
        processRequestWithEmail("sendInterviewCallLetters")
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
        processRequest("previewCallLetters")
    }

    // ── Branch APIs ──────────────────────────────────────────────────

    /**
     * API Endpoint: Get all branches with form data (programs + versions)
     * URL: /recInterviewSchedule/getRecBranchList
     * Method: GET
     * Headers: EPC-UID
     */
    def getRecBranchList() {
        processRequestWithoutParams("getRecBranchList")
    }

    /**
     * API Endpoint: Save new recruitment branch
     * URL: /recInterviewSchedule/saverecBranch
     * Method: POST
     * Headers: EPC-UID
     * Body: { "recver": 1, "program": 1, "recbranch": "CSE", "recbranchabbr": "CSE" }
     */
    def saverecBranch() {
        processRequest("saveRecBranch")
    }

    /**
     * API Endpoint: Edit existing recruitment branch
     * URL: /recInterviewSchedule/editRecBranch
     * Method: POST
     * Headers: EPC-UID
     * Body: { "editId": 1, "program": 1, "recbranch": "Updated Name", "recbranchabbr": "UPD" }
     */
    def editRecBranch() {
        processRequest("editRecBranch")
    }

    /**
     * API Endpoint: Delete recruitment branch
     * URL: /recInterviewSchedule/deleterecBranch
     * Method: POST
     * Headers: EPC-UID
     * Body: { "brcid": 1 }
     */
    def deleterecBranch() {
        processRequest("deleteRecBranch")
    }

    /**
     * API Endpoint: Toggle isActive on a recruitment branch
     * URL: /recInterviewSchedule/isActiveBranch
     * Method: POST
     * Headers: EPC-UID
     * Body: { "ActiveId": 1 }
     */
    def isActiveBranch() {
        processRequest("toggleBranchActive")
    }

    // ── Post Management APIs ─────────────────────────────────────────

    /**
     * API Endpoint: Get all posts with form data (designations + versions)
     * URL: /recInterviewSchedule/getRecPostList
     * Method: GET
     * Headers: EPC-UID
     */
    def getRecPostList() {
        processRequestWithoutParams("getRecPostList")
    }

    /**
     * API Endpoint: Save new recruitment post
     * URL: /recInterviewSchedule/saverecPost
     * Method: POST
     * Headers: EPC-UID
     * Body: { "recver": 1, "designation": 1 }
     */
    def saverecPost() {
        processRequest("saveRecPost")
    }

    /**
     * API Endpoint: Edit existing recruitment post
     * URL: /recInterviewSchedule/editRecPost
     * Method: POST
     * Headers: EPC-UID
     * Body: { "editId": 1, "designation": 2 }
     */
    def editRecPost() {
        processRequest("editRecPost")
    }

    /**
     * API Endpoint: Delete recruitment post
     * URL: /recInterviewSchedule/deleterecPost
     * Method: POST
     * Headers: EPC-UID
     * Body: { "brcid": 1 }
     */
    def deleterecPost() {
        processRequest("deleteRecPost")
    }

    /**
     * API Endpoint: Toggle isActive on a recruitment post
     * URL: /recInterviewSchedule/isActivePost
     * Method: POST
     * Headers: EPC-UID
     * Body: { "ActiveId": 1 }
     */
    def isActivePost() {
        processRequest("togglePostActive")
    }
}
