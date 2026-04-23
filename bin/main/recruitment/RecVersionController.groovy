package recruitment


import grails.rest.*
import grails.converters.*

class RecVersionController {
	static responseFormats = ['json', 'xml']
    
    // Inject RecVersionService using Grails dependency injection
    def recVersionService
    def recVersionService_2

    /**
     * Common exception handler
     */
    private def handleException(Exception e) {
        println("Exception in RecVersionController: ${e.message}")
        e.printStackTrace()
        HashMap hashMap = new HashMap()
        hashMap.put("error_msg", e.message)
        hashMap.put("flag", false)
        render hashMap as JSON
        return
    }

    /**
     * Process request with JSON body parameters
     */
    private def processRequest(String serviceMethod) {
        try {
            println("Processing request for: ${serviceMethod}")
            HashMap hm = new HashMap()
            hm.putAll(commonData(request))
            hm.put("msg", "Failed!!!")
            hm.put("flag", false)
            recVersionService."${serviceMethod}"(hm, request, request.JSON)
            render hm as JSON
        } catch (Exception e) {
            handleException(e)
        }
    }
    
    /**
     * Process request without body parameters (GET requests)
     */
    private def processRequestWithoutParams(String methodName) {
        try {
            println("In ${methodName}")
            HashMap hm = new HashMap()
            hm.putAll(commonData(request))
            hm.put("msg", "Failed!!!")
            hm.put("flag", false)
            recVersionService."${methodName}"(hm, request)
            render hm as JSON
        } catch (Exception e) {
            handleException(e)
        }
    }
    
    def commonData(request){
        def loginId = request.getHeader("EPC-UID")
        Login login = Login.findByUsernameAndIsloginblocked(loginId,false)
        Instructor instructor = Instructor.findByUid(login?.username)
        def hm = [:]
        hm.inst = instructor
        hm.org = instructor?.organization
        return hm
    }
    
    ///////////////////////////////////////////////////////////////////////
    
    def getTest() {
        processRequest("getTest")
    }
    
    /**
     * API Endpoint: Get filter data for statistics report
     * URL: /recVersion/statisticsreportfilter
     * Method: GET
     * Headers: EPC-UID (username)
     * Returns: JSON with academic years, organizations, versions, and management flag
     */
    def statisticsreportfilter() {
        processRequestWithoutParams("getStatisticsFilterData")
    }

    /**
     * API Endpoint: Get recruitment versions by academic year and organization
     * URL: /recVersion/getRecVersionDetails
     * Method: POST
     * Headers: EPC-UID (username)
     * Body: { "ay": academicYearId, "org": organizationId (optional) }
     * Returns: JSON with list of recruitment versions
     */
    def getRecVersionDetails() {
        processRequest("getRecVersionDetails")
    }

    /**
     * API Endpoint: Get statistics summary report (Branch x Post matrix)
     * URL: /recVersion/statisticssummaryreport
     * Method: POST
     * Headers: EPC-UID (username)
     * Body: { "ay": academicYearId, "recversion": recVersionId, "org": organizationId (optional) }
     * Returns: JSON with statistics matrix showing application counts by branch and post
     */
    def statisticssummaryreport() {
        processRequest("getStatisticsSummaryReport")
    }

    /**
     * API Endpoint: Get total post counts (distinct or all applications)
     * URL: /recVersion/gettotalpostcounts
     * Method: POST
     * Headers: EPC-UID (username)
     * Body: { "ay": academicYearId, "recversion": recVersionId, "recpostid": postId, "type": "distinct|all", "org": organizationId (optional) }
     * Returns: JSON with count of applications for the specified post
     */
    def gettotalpostcounts() {
        processRequest("getTotalPostCounts")
    }

    /**
     * API Endpoint: Get total department/branch counts (distinct or all applications)
     * URL: /recVersion/gettotaldeptcounts
     * Method: POST
     * Headers: EPC-UID (username)
     * Body: { "ay": academicYearId, "recversion": recVersionId, "recbranchid": branchId, "type": "distinct|all", "org": organizationId (optional) }
     * Returns: JSON with count of applications for the specified branch
     */
    def gettotaldeptcounts() {
        processRequest("getTotalDeptCounts")
    }

    /**
     * API Endpoint: Get candidate list with optional filtering by branch and/or post
     * URL: /recVersion/getcandidatelist
     * Method: POST
     * Headers: EPC-UID (username)
     * Body: { "ay": academicYearId, "recversion": recVersionId, "recbranchid": branchId (optional), "recpostid": postId (optional), "org": organizationId (optional) }
     * Returns: JSON with list of candidates with their details, addresses, and document counts
     */
    def getcandidatelist() {
        try {
            println("Processing request for getcandidatelist")
            HashMap hm = new HashMap()
            hm.putAll(commonData(request))
            hm.put("msg", "Failed!!!")
            hm.put("flag", false)
            recVersionService_2.getCandidateList(hm, request, request.JSON)
            render hm as JSON
        } catch (Exception e) {
            handleException(e)
        }
    }

    /**
     * API Endpoint: Download selected documents as ZIP file
     * URL: /recVersion/downloadSelecteddocuments
     * Method: POST
     * Headers: EPC-UID (username)
     * Body: { "ay": academicYearId, "recversion": recVersionId, "applicationids": [id1, id2, ...], "dept": deptName (optional), "post": postName (optional), "org": organizationId (optional) }
     * Returns: ZIP file with all documents from selected applications
     */
    def downloadSelecteddocuments() {
        try {
            println("Processing request for downloadSelecteddocuments")
            HashMap hm = new HashMap()
            hm.putAll(commonData(request))
            hm.put("msg", "Failed!!!")
            hm.put("flag", false)
            recVersionService_2.downloadSelectedDocuments(hm, request, response, request.JSON)
            
            // If it's a file download, don't render JSON
            if (!hm.isFileDownload) {
                render hm as JSON
            }
            // Otherwise, the ZIP file has already been written to response.outputStream
        } catch (Exception e) {
            handleException(e)
        }
    }
}
