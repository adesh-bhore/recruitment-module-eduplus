package recruitment

import grails.converters.JSON

class RecApplicationController {
    
    // Helper methods to get service instances
    private RecApplicationService_1 getService1() {
        return new RecApplicationService_1()
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
            
            // Add query parameters to hashmap
            params.each { key, value ->
                if (key != 'controller' && key != 'action') {
                    hm[key] = value
                }
            }
            
            // Call service method
            service."${methodName}"(hm, request)
            
            render hm as JSON
            
        } catch (Exception e) {
            println("Error in processRequestWithoutParams: ${e.message}")
            render([flag: false, msg: "Error processing request: ${e.message}"] as JSON)
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
        processRequestWithoutParams("getActiveRecruitments", getService1())
    }
    
    /**
     * API 2: Get application form data (posts, branches, categories, etc.)
     * GET /recApplication/getApplicationFormData
     * Headers: EPC-UID
     * Query Params: recver (recruitment version ID)
     */
    def getApplicationFormData() {
        processRequestWithoutParams("getApplicationFormData", getService1())
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
        processRequestWithParams("submitApplication", getService1())
    }
    
    /**
     * API 4: Get all applications for logged-in applicant
     * GET /recApplication/getMyApplications
     * Headers: EPC-UID
     */
    def getMyApplications() {
        processRequestWithoutParams("getMyApplications", getService1())
    }
    
    /**
     * API 5: Get detailed application information
     * GET /recApplication/getApplicationDetails
     * Headers: EPC-UID
     * Query Params: applicationId (application ID)
     */
    def getApplicationDetails() {
        processRequestWithoutParams("getApplicationDetails", getService1())
    }
}
