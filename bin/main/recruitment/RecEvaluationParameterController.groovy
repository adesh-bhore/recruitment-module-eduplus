package recruitment

import grails.converters.JSON

class RecEvaluationParameterController {
    
    def recEvaluationParameterService
    
    // ═══════════════════════════════════════════════════════════════
    // Centralized Exception Handler
    // ═══════════════════════════════════════════════════════════════
    
    private def handleException(Exception e) {
        println("Exception in RecEvaluationParameterController: ${e.message}")
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
            
            recEvaluationParameterService."${methodName}"(hm, request, params)
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
            
            recEvaluationParameterService."${methodName}"(hm, request, jsonData ?: params)
            render hm as JSON
        } catch (Exception e) {
            handleException(e)
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // Phase 1: Evaluation Parameter Management APIs
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Get initial data for parameter input form
     * GET /recEvaluationParameter/getInitialData
     * 
     * Params:
     * - itemId (optional): Parameter ID for editing
     * 
     * Response:
     * {
     *   "recversion": {
     *     "id": 1,
     *     "version_number": 1,
     *     "version_date": "2024-01-01",
     *     "iscurrent": true
     *   },
     *   "recexperttypelist": [
     *     {"id": 1, "type": "Technical Expert"}
     *   ],
     *   "recdeptexpertgroup": [
     *     {"id": 1, "groupno": 1, "groupname": "COMP-IT-MCA", "cutoff": 60.0}
     *   ],
     *   "parameterlist": [
     *     {
     *       "id": 1,
     *       "parameter": "Technical Skills",
     *       "parameter_number": 1,
     *       "maxmarks": 100.0,
     *       "recexperttype": {"id": 1, "type": "Technical Expert"},
     *       "recdeptexpertgroup": {"id": 1, "groupname": "COMP-IT-MCA"}
     *     }
     *   ],
     *   "editData": null,
     *   "organizationlist": [{"id": 1, "name": "VIT University"}],
     *   "academicyear": {"id": 1, "year": "2024-2025"},
     *   "aylist": [{"id": 1, "year": "2024-2025"}],
     *   "recversionlist": [{"id": 1, "version_number": 1}],
     *   "ismanagement": true,
     *   "msg": "Initial data fetched successfully",
     *   "flag": true
     * }
     */
    def getInitialData() {
        processRequest("getInitialData")
    }
    
    /**
     * Get recruitment versions for organization and academic year
     * GET /recEvaluationParameter/getRecVersions
     * 
     * Params:
     * - organizationId (required): Organization ID
     * - academicYearId (required): Academic Year ID
     * 
     * Response:
     * {
     *   "recversionlist": [
     *     {
     *       "id": 1,
     *       "version_number": 1,
     *       "version_date": "2024-01-01",
     *       "iscurrent": true,
     *       "from_date": "2024-01-01",
     *       "to_date": "2024-12-31"
     *     }
     *   ],
     *   "msg": "Recruitment versions fetched successfully",
     *   "flag": true
     * }
     */
    def getRecVersions() {
        processRequest("getRecVersions")
    }
    
    /**
     * Get expert groups for organization and recruitment version
     * GET /recEvaluationParameter/getExpertGroups
     * 
     * Params:
     * - organizationId (required): Organization ID
     * - recversionId (required): Recruitment Version ID
     * - itemId (optional): Parameter ID for editing
     * 
     * Response:
     * {
     *   "recdeptexpertgroup": [
     *     {
     *       "id": 1,
     *       "groupno": 1,
     *       "groupname": "COMP-IT-MCA",
     *       "cutoff": 60.0,
     *       "round2cutoff": 50.0
     *     }
     *   ],
     *   "editData": {
     *     "id": 123,
     *     "parameter": "Technical Skills",
     *     "parameter_number": 1,
     *     "maxmarks": 100.0
     *   },
     *   "msg": "Expert groups fetched successfully",
     *   "flag": true
     * }
     */
    def getExpertGroups() {
        processRequest("getExpertGroups")
    }
    
    /**
     * Get evaluation parameters list for organization and recruitment version
     * GET /recEvaluationParameter/getParameters
     * 
     * Params:
     * - organizationId (required): Organization ID
     * - recversionId (required): Recruitment Version ID
     * 
     * Response:
     * {
     *   "parameterlist": [
     *     {
     *       "id": 1,
     *       "parameter": "Technical Skills",
     *       "parameter_number": 1,
     *       "maxmarks": 100.0,
     *       "recexperttype": {
     *         "id": 1,
     *         "type": "Technical Expert"
     *       },
     *       "recdeptexpertgroup": {
     *         "id": 1,
     *         "groupno": 1,
     *         "groupname": "COMP-IT-MCA"
     *       }
     *     }
     *   ],
     *   "msg": "Parameters fetched successfully",
     *   "flag": true
     * }
     */
    def getParameters() {
        processRequest("getParameters")
    }
    
    /**
     * Create or update evaluation parameter
     * POST /recEvaluationParameter/saveParameter
     * Content-Type: application/json
     * 
     * Body:
     * {
     *   "id": null,
     *   "organizationId": 1,
     *   "recversionId": 1,
     *   "expertGroupIds": [1, 2],
     *   "experttypeId": 1,
     *   "parameter": "Technical Skills",
     *   "parameter_number": 1,
     *   "maxmarks": 100.0
     * }
     * 
     * Response (Create):
     * {
     *   "parametersCreated": 2,
     *   "msg": "Evaluation parameters created successfully",
     *   "flag": true
     * }
     * 
     * Response (Update):
     * {
     *   "parameterId": 123,
     *   "msg": "Evaluation parameter updated successfully",
     *   "flag": true
     * }
     */
    def saveParameter() {
        processJsonRequest("saveParameter")
    }
    
    /**
     * Delete evaluation parameter
     * DELETE /recEvaluationParameter/deleteParameter/:id
     * 
     * Params:
     * - parameterId (required): Parameter ID (from URL)
     * 
     * Response:
     * {
     *   "parameterId": 123,
     *   "msg": "Evaluation parameter deleted successfully",
     *   "flag": true
     * }
     */
    def deleteParameter() {
        processRequest("deleteParameter")
    }
}
