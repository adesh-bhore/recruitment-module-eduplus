package recruitment

import grails.converters.JSON

class RecApplicationController {
    
    // Inject services using Grails dependency injection
    def recApplicationService_1
    def recApplicationService_2
    def recApplicationService_3
    def recApplicationService_4
    
    /**
     * Common exception handler
     */
    private def handleException(Exception e) {
        println("Exception in RecApplicationController: ${e.message}")
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
            handleException(e)
        }
    }
    
    /**
     * Helper to prepare request data and return HashMap (for methods that handle response directly)
     */
    private HashMap prepareRequestData() {
        def requestData = request.JSON
        def uid = request.getHeader("EPC-UID")
        
        HashMap hm = new HashMap()
        hm.uid = uid
        hm.flag = true
        
        if (!uid) {
            hm.flag = false
            hm.msg = "User not authenticated"
            return hm
        }
        
        // Add all request parameters to hashmap
        requestData.each { key, value ->
            hm[key] = value
        }
        
        hm.data = requestData
        return hm
    }
    
    /**
     * Helper to prepare query params and return HashMap (for GET methods that handle response directly)
     */
    private HashMap prepareQueryParams() {
        def uid = request.getHeader("EPC-UID")
        
        HashMap hm = new HashMap()
        hm.uid = uid
        hm.flag = true
        
        if (!uid) {
            hm.flag = false
            hm.msg = "User not authenticated"
            return hm
        }
        
        // Create data map from query parameters
        def data = [:]
        params.each { key, value ->
            if (key != 'controller' && key != 'action') {
                hm[key] = value
                data[key] = value
            }
        }
        
        hm.data = data
        return hm
    }
    
    /**
     * Common request processing for multipart form data (file uploads)
     */
    private void processMultipartRequest(String methodName, service) {
        try {
            def uid = request.getHeader("EPC-UID")
            
            if (!uid) {
                render([flag: false, msg: "User not authenticated"] as JSON)
                return
            }
            
            HashMap hm = new HashMap()
            hm.uid = uid
            
            // Add only non-file parameters to hashmap
            params.each { key, value ->
                if (key != 'controller' && key != 'action' && !(value instanceof org.springframework.web.multipart.MultipartFile)) {
                    hm[key] = value
                }
            }
            
            // Call service method with params as data
            service."${methodName}"(hm, request, params)
            
            // Remove any non-serializable objects before rendering
            hm.remove('documentname')
            
            render hm as JSON
            
        } catch (Exception e) {
            handleException(e)
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
            handleException(e)
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // Phase 1: Application Form & Submission APIs
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * API 1: Get active recruitment versions
     * GET /recApplication/getActiveRecruitments
     * Headers: EPC-UID
     */
    def getActiveRecruitments() {
        processRequestWithoutParams("getActiveRecruitments", recApplicationService_1)
    }
    
    /**
     * API 2: Get application form data (posts, branches, categories, etc.)
     * GET /recApplication/getApplicationFormData
     * Headers: EPC-UID
     * Query Params: recver (recruitment version ID)
     */
    def getApplicationFormData() {
        processRequestWithoutParams("getApplicationFormData", recApplicationService_1)
    }
    
    /**
     * API 3: Submit or update application
     * POST /recApplication/submitApplication
     * Headers: EPC-UID
     * Request Body: {
     *   recver: Long,
     *   selectedPostId: String (comma-separated IDs),
     *   branch: [Long] or Long,
     *   fullname: String,
     *   category: Long,
     *   maritalstatus: Long,
     *   salutation: Long,
     *   gender: Long,
     *   minority: Long,
     *   dateofbirth: String (yyyy-MM-dd),
     *   mobilenumber: String,
     *   cast: String,
     *   pancardno: String,
     *   aadharcardno: String,
     *   area_of_specialization: String,
     *   any_other_info_related_to_post: String,
     *   present_salary: String,
     *   ishandicapped: Boolean,
     *   place: String,
     *   padd: String, ptaluka: String, ppin: String, pcountry: Long, pstate: Long, pdist: Long, pcity: Long,
     *   lladd: String, ltaluka: String, lpin: String, lcountry: Long, lstate: Long, ldist: Long, lcity: Long
     * }
     */
    def submitApplication() {
        processRequestWithParams("submitApplication", recApplicationService_1)
    }
    
    /**
     * API 4: Get all applications for logged-in applicant
     * GET /recApplication/getMyApplications
     * Headers: EPC-UID
     */
    def getMyApplications() {
        processRequestWithoutParams("getMyApplications", recApplicationService_1)
    }
    
    /**
     * API 5: Get detailed application information
     * GET /recApplication/getApplicationDetails
     * Headers: EPC-UID
     * Query Params: applicationId (application ID)
     */
    def getApplicationDetails() {
        processRequestWithoutParams("getApplicationDetails", recApplicationService_1)
    }
    
    // ═══════════════════════════════════════════════════════════════
    // Phase 2: Document Management APIs
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * API 6: Get document types required for application
     * GET /recApplication/getDocumentTypes
     * Headers: EPC-UID
     */
    def getDocumentTypes() {
        processRequestWithoutParams("getDocumentTypes", recApplicationService_1)
    }
    
    /**
     * API 7: Upload document to AWS S3
     * POST /recApplication/uploadDocument
     * Headers: EPC-UID
     * Content-Type: multipart/form-data
     * Form Data:
     *   documenttype: Long (document type ID)
     *   documentname: File (the file to upload)
     */
    def uploadDocument() {
        processMultipartRequest("uploadDocument", recApplicationService_1)
    }
    
    /**
     * API 8: Download document from AWS S3
     * GET /recApplication/downloadDocument
     * Headers: EPC-UID
     * Query Params: documentId (document ID)
     */
    def downloadDocument() {
        processRequestWithoutParams("downloadDocument", recApplicationService_1)
    }
    
    /**
     * API 9: Delete document from AWS S3
     * POST /recApplication/deleteDocument
     * Headers: EPC-UID
     * Request Body: { documentId: Long }
     */
    def deleteDocument() {
        processRequestWithParams("deleteDocument", recApplicationService_1)
    }
    
    /**
     * API 10: Get applicant photo
     * GET /recApplication/getApplicantPhoto
     * Headers: EPC-UID
     * Query Params: applicantId (optional, defaults to logged-in user)
     */
    def getApplicantPhoto() {
        processRequestWithoutParams("getApplicantPhoto", recApplicationService_1)
    }
    
    // ═══════════════════════════════════════════════════════════════
    // Phase 3: Application Preview & PDF Data APIs
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * API 11: Get application preview data (for PDF generation or display)
     * GET /recApplication/getApplicationPreview
     * Headers: EPC-UID
     * Query Params: applicationId (application ID)
     */
    def getApplicationPreview() {
        processRequestWithoutParams("getApplicationPreview", recApplicationService_1)
    }
    
    /**
     * API 12: Download document file (get presigned URL for direct download)
     * GET /recApplication/downloadDocumentFile
     * Headers: EPC-UID
     * Query Params: documentId (document ID)
     * 
     * Note: This replaces old downloaddocuments() method
     * Returns presigned AWS S3 URL for direct file download
     */
    def downloadDocumentFile() {
        processRequestWithoutParams("downloadDocumentFile", recApplicationService_1)
    }

    
    // ═══════════════════════════════════════════════════════════════
    // Phase 4: Admin - Application Listing & Filtering APIs
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * API 13: Get authority applications
     * GET /recApplication/getAuthorityApplications
     * Headers: EPC-UID
     */
    def getAuthorityApplications() {
        processRequestWithoutParams("getAuthorityApplications", recApplicationService_2)
    }
    
    /**
     * API 14: Get application summary with filters
     * GET /recApplication/getApplicationSummary
     * Headers: EPC-UID
     * Query Params: authorityType, recver, status, recbranch, recpost, fromdate, todate, page, pageSize
     */
    def getApplicationSummary() {
        processRequestWithoutParams("getApplicationSummary", recApplicationService_2)
    }
    
    /**
     * API 15: Get detailed application list
     * GET /recApplication/getDetailedApplicationList
     * Headers: EPC-UID
     * Query Params: authorityType, recver, status, recbranch, applicationId
     */
    def getDetailedApplicationList() {
        processRequestWithoutParams("getDetailedApplicationList", recApplicationService_2)
    }
    
    /**
     * API 16: Get application counts and statistics
     * GET /recApplication/getApplicationCounts
     * Headers: EPC-UID
     * Query Params: authorityType, recver
     */
    def getApplicationCounts() {
        processRequestWithoutParams("getApplicationCounts", recApplicationService_2)
    }
    
    /**
     * API 17: Get application data for specific application
     * GET /recApplication/getApplicationData
     * Headers: EPC-UID
     * Query Params: applicationId
     */
    def getApplicationData() {
        processRequestWithoutParams("getApplicationData", recApplicationService_2)
    }
    
    /**
     * API 18: Get applicants list with filtering
     * GET /recApplication/getApplicantsList
     * Headers: EPC-UID
     * Query Params: recver, category, searchText, page, pageSize
     */
    def getApplicantsList() {
        processRequestWithoutParams("getApplicantsList", recApplicationService_2)
    }
    
    // ═══════════════════════════════════════════════════════════════
    // Phase 5: Admin - Application Approval & Shortlisting APIs
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * API 19: Process application (approve/reject/shortlist)
     * POST /recApplication/processApplication
     * Headers: EPC-UID
     * Request Body: {
     *   applicationId: Long,
     *   branchId: Long (optional),
     *   authorityTypeId: Long,
     *   action: String ('approve', 'reject', 'shortlist'),
     *   remark: String (optional)
     * }
     */
    def processApplication() {
        processRequestWithParams("processApplication", recApplicationService_2)
    }
    
    /**
     * API 20: Notify shortlisted candidates
     * POST /recApplication/notifyShortlistedCandidates
     * Headers: EPC-UID
     * Request Body: {
     *   applicationIds: [Long],
     *   emailSubject: String,
     *   emailBody: String
     * }
     */
    def notifyShortlistedCandidates() {
        processRequestWithParams("notifyShortlistedCandidates", recApplicationService_2)
    }
    
    /**
     * API 21: Reject application with reason
     * POST /recApplication/rejectApplication
     * Headers: EPC-UID
     * Request Body: {
     *   applicationId: Long,
     *   branchId: Long (optional),
     *   authorityTypeId: Long (optional),
     *   rejectionReason: String
     * }
     */
    def rejectApplication() {
        processRequestWithParams("rejectApplication", recApplicationService_2)
    }
    
    /**
     * API 22: Mark applicant attendance for interview
     * POST /recApplication/markAttendance
     * Headers: EPC-UID
     * Request Body: {
     *   applicationId: Long,
     *   branchId: Long (optional),
     *   isPresent: Boolean,
     *   attendanceRemark: String (optional)
     * }
     */
    def markAttendance() {
        processRequestWithParams("markAttendance", recApplicationService_2)
    }

    // ═══════════════════════════════════════════════════════════════
    // Phase 6: Qualification & Experience Management APIs
    // ═══════════════════════════════════════════════════════════════

    /**
     * API 23: Get Qualification Details
     * GET /recApplication/getQualificationDetails
     * 
     * Query Parameters: {
     *   applicantId: Long
     * }
     */
    def getQualificationDetails() {
        processRequestWithoutParams("getQualificationDetails", recApplicationService_3)
    }

    /**
     * API 24: Update Qualification
     * POST /recApplication/updateQualification
     * 
     * Request Body: {
     *   qualificationId: Long,
     *   degreeId: Long (optional),
     *   degreeNameId: Long (optional),
     *   classId: Long (optional),
     *   degreeStatusId: Long (optional),
     *   yearOfPassing: String (optional),
     *   university: String (optional),
     *   branch: String (optional),
     *   cpiMarks: Double (optional)
     * }
     */
    def updateQualification() {
        processRequestWithParams("updateQualification", recApplicationService_3)
    }

    /**
     * API 25: Delete Qualification
     * POST /recApplication/deleteQualification
     * 
     * Request Body: {
     *   qualificationId: Long
     * }
     */
    def deleteQualification() {
        processRequestWithParams("deleteQualification", recApplicationService_3)
    }

    /**
     * API 26: Get Experience Details
     * GET /recApplication/getExperienceDetails
     * 
     * Query Parameters: {
     *   applicantId: Long
     * }
     */
    def getExperienceDetails() {
        processRequestWithoutParams("getExperienceDetails", recApplicationService_3)
    }

    /**
     * API 27: Get Degrees by Exam
     * GET /recApplication/getDegreesByExam
     * 
     * Query Parameters: {
     *   examId: Long,
     *   organizationId: Long
     * }
     */
    def getDegreesByExam() {
        processRequestWithoutParams("getDegreesByExam", recApplicationService_3)
    }

    // ═══════════════════════════════════════════════════════════════
    // Phase 7: Bulk Operations & Reports APIs
    // ═══════════════════════════════════════════════════════════════

    /**
     * API 28: Bulk Download Documents
     * POST /recApplication/bulkDownloadDocuments
     * 
     * Request Body: {
     *   applicantId: Long,
     *   versionId: Long,
     *   bulkType: String ("All" or "Selected"),
     *   documentIds: Array<Long> (required if bulkType="Selected")
     * }
     * 
     * Response: ZIP file download
     */
    def bulkDownloadDocuments() {
        try {
            def hm = prepareRequestData()
            if (!hm.flag) {
                render hm as JSON
                return
            }
            
            recApplicationService_4.bulkDownloadDocuments(hm, request, response, hm.data)
            
            // If service set flag to false, render error
            if (!hm.flag) {
                render hm as JSON
            }
            // Otherwise response was already streamed by service
        } catch (Exception e) {
            handleException(e)
        }
    }

    /**
     * API 29: Bulk Approve Applications
     * POST /recApplication/bulkApproveApplications
     * 
     * Request Body: {
     *   applicationIds: Array<Long>,
     *   authorityTypeId: Long,
     *   branchId: Long (optional),
     *   remark: String (optional)
     * }
     */
    def bulkApproveApplications() {
        processRequestWithParams("bulkApproveApplications", recApplicationService_4)
    }

    /**
     * API 30: Bulk Reject Applications
     * POST /recApplication/bulkRejectApplications
     * 
     * Request Body: {
     *   applicationIds: Array<Long>,
     *   rejectionReason: String,
     *   authorityTypeId: Long (optional),
     *   branchId: Long (optional)
     * }
     */
    def bulkRejectApplications() {
        processRequestWithParams("bulkRejectApplications", recApplicationService_4)
    }

    /**
     * API 31: Export Applications
     * GET /recApplication/exportApplications
     * 
     * Query Parameters: {
     *   versionId: Long (optional),
     *   authorityType: String (optional),
     *   status: String (optional),
     *   branchId: Long (optional),
     *   format: String (optional, default: "csv")
     * }
     * 
     * Response: CSV file download
     */
    def exportApplications() {
        try {
            def hm = prepareQueryParams()
            if (!hm.flag) {
                render hm as JSON
                return
            }
            
            recApplicationService_4.exportApplications(hm, request, response, hm.data)
            
            // If service set flag to false, render error
            if (!hm.flag) {
                render hm as JSON
            }
            // Otherwise response was already streamed by service
        } catch (Exception e) {
            handleException(e)
        }
    }
}

