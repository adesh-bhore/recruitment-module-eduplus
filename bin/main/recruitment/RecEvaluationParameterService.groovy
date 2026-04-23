package recruitment

import grails.gorm.transactions.Transactional

@Transactional
class RecEvaluationParameterService {
    
    // ═══════════════════════════════════════════════════════════════
    // Phase 1: Evaluation Parameter Management APIs
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Get initial data for parameter input form
     * Used by: GET /recEvaluationParameter/getInitialData
     */
    def getInitialData(hm, request, data) {
        def uid = hm.remove("uid")
        def itemId = hm.remove("itemId")
        
        if (!uid) {
            hm.msg = "User not authenticated"
            hm.flag = false
            return
        }
        
        Login login = Login.findByUsername(uid)
        if (!login) {
            hm.msg = "Login not found"
            hm.flag = false
            return
        }
        
        Instructor instructor = Instructor.findByUid(login.username)
        if (!instructor) {
            hm.msg = "Instructor not found"
            hm.flag = false
            return
        }
        
        Organization org = instructor.organization
        
        // Get current academic year for Recruitment module
        def aay = getCurrentAcademicYear(org)
        if (!aay) {
            hm.msg = "Application Academic Year Not Set for Recruitment Module"
            hm.flag = false
            return
        }
        
        def academicyear = aay.academicyear
        
        // Get academic year list
        def aylist = AcademicYear.findAllByIsactive(true, [sort: 'sort_order'])
        
        // Check if user is management
        def ismanagement = isManagement(login, instructor)
        
        // Get organization list based on management status
        def organizationlist = []
        if (ismanagement) {
            organizationlist = OrganizationOrgGroupMapping.createCriteria().list() {
                projections {
                    distinct('organization')
                }
                'in'('organizationgroup', org?.organizationgroup)
            }
            organizationlist?.sort { it?.sort_order }
        } else {
            organizationlist = [instructor?.organization]
        }
        
        // Get recruitment versions for organization and academic year
        def recversionlist = RecVersion.findAllByOrganizationAndAcademicyear(org, academicyear)
        
        // Get current recruitment version
        RecVersion recversion = RecVersion.findByOrganizationAndIscurrentforbackendprocessingAndAcademicyear(org, true, academicyear)
        if (!recversion) {
            recversion = RecVersion.findByOrganizationAndAcademicyear(org, academicyear)
        }
        
        // Get expert groups for current version
        def recdeptexpertgroup = RecDeptExpertGroup.findAllByOrganizationAndRecversion(org, recversion)
        
        // Get parameters list for current version
        def parameterlist = RecEvaluationParameter.findAllByOranizationAndRecversion(org, recversion)
        
        // Get expert types for organization
        def recexperttypelist = RecExpertType.findAllByOranization(org)
        
        // If editing, get edit data
        def editData = null
        if (itemId) {
            editData = RecEvaluationParameter.get(itemId)
            if (editData) {
                // Override academic year if editing
                academicyear = editData?.recversion?.academicyear
                recversionlist = RecVersion.findAllByOrganizationAndAcademicyear(org, academicyear)
            }
        }
        
        // Build response
        hm.recversion = recversion ? [
            id: recversion.id,
            version_number: recversion.version_number,
            version_date: recversion.version_date,
            iscurrent: recversion.iscurrent
        ] : null
        
        hm.recexperttypelist = recexperttypelist.collect { [
            id: it.id,
            type: it.type
        ] }
        
        hm.recdeptexpertgroup = recdeptexpertgroup.collect { [
            id: it.id,
            groupno: it.groupno,
            groupname: it.groupname,
            cutoff: it.cutoff
        ] }
        
        hm.parameterlist = parameterlist.collect { [
            id: it.id,
            parameter: it.parameter,
            parameter_number: it.parameter_number,
            maxmarks: it.maxmarks,
            recexperttype: [id: it.recexperttype?.id, type: it.recexperttype?.type],
            recdeptexpertgroup: [id: it.recdeptexpertgroup?.id, groupname: it.recdeptexpertgroup?.groupname]
        ] }
        
        hm.editData = editData ? [
            id: editData.id,
            parameter: editData.parameter,
            parameter_number: editData.parameter_number,
            maxmarks: editData.maxmarks,
            recexperttype_id: editData.recexperttype?.id,
            recdeptexpertgroup_id: editData.recdeptexpertgroup?.id
        ] : null
        
        hm.organizationlist = organizationlist.collect { [
            id: it.id,
            name: it.organization_name
        ] }
        
        hm.academicyear = academicyear ? [id: academicyear.id, year: academicyear.ay] : null
        
        hm.aylist = aylist.collect { [id: it.id, year: it.ay] }
        
        hm.recversionlist = recversionlist.collect { [
            id: it.id,
            version_number: it.version_number
        ] }
        
        hm.ismanagement = ismanagement
        hm.msg = "Initial data fetched successfully"
        hm.flag = true
    }
    
    /**
     * Get recruitment versions for organization and academic year
     * Used by: GET /recEvaluationParameter/getRecVersions
     */
    def getRecVersions(hm, request, data) {
        def uid = hm.remove("uid")
        def organizationId = hm.remove("organizationId")
        def academicYearId = hm.remove("academicYearId")
        
        if (!uid) {
            hm.msg = "User not authenticated"
            hm.flag = false
            return
        }
        
        if (!organizationId || !academicYearId) {
            hm.msg = "Organization ID and Academic Year ID are required"
            hm.flag = false
            return
        }
        
        Organization organization = Organization.get(organizationId)
        AcademicYear academicYear = AcademicYear.get(academicYearId)
        
        if (!organization) {
            hm.msg = "Organization not found"
            hm.flag = false
            return
        }
        
        if (!academicYear) {
            hm.msg = "Academic Year not found"
            hm.flag = false
            return
        }
        
        def recversionlist = RecVersion.findAllByOrganizationAndAcademicyear(organization, academicYear)
        
        hm.recversionlist = recversionlist.collect { [
            id: it.id,
            version_number: it.version_number,
            version_date: it.version_date,
            iscurrent: it.iscurrent,
            from_date: it.from_date,
            to_date: it.to_date
        ] }
        
        hm.msg = "Recruitment versions fetched successfully"
        hm.flag = true
    }
    
    /**
     * Get expert groups for organization and recruitment version
     * Used by: GET /recEvaluationParameter/getExpertGroups
     */
    def getExpertGroups(hm, request, data) {
        def uid = hm.remove("uid")
        def organizationId = hm.remove("organizationId")
        def recversionId = hm.remove("recversionId")
        def itemId = hm.remove("itemId")
        
        if (!uid) {
            hm.msg = "User not authenticated"
            hm.flag = false
            return
        }
        
        if (!organizationId || !recversionId) {
            hm.msg = "Organization ID and Recruitment Version ID are required"
            hm.flag = false
            return
        }
        
        Organization organization = Organization.get(organizationId)
        RecVersion recversion = RecVersion.get(recversionId)
        
        if (!organization) {
            hm.msg = "Organization not found"
            hm.flag = false
            return
        }
        
        if (!recversion) {
            hm.msg = "Recruitment Version not found"
            hm.flag = false
            return
        }
        
        def recdeptexpertgroup = RecDeptExpertGroup.findAllByOrganizationAndRecversion(organization, recversion)
        
        hm.recdeptexpertgroup = recdeptexpertgroup.collect { [
            id: it.id,
            groupno: it.groupno,
            groupname: it.groupname,
            cutoff: it.cutoff,
            round2cutoff: it.round2cutoff
        ] }
        
        // If editing, get edit data
        def editData = null
        if (itemId) {
            editData = RecEvaluationParameter.get(itemId)
            if (editData) {
                hm.editData = [
                    id: editData.id,
                    parameter: editData.parameter,
                    parameter_number: editData.parameter_number,
                    maxmarks: editData.maxmarks,
                    recexperttype_id: editData.recexperttype?.id,
                    recdeptexpertgroup_id: editData.recdeptexpertgroup?.id
                ]
            }
        }
        
        hm.msg = "Expert groups fetched successfully"
        hm.flag = true
    }
    
    /**
     * Get evaluation parameters list for organization and recruitment version
     * Used by: GET /recEvaluationParameter/getParameters
     */
    def getParameters(hm, request, data) {
        def uid = hm.remove("uid")
        def organizationId = hm.remove("organizationId")
        def recversionId = hm.remove("recversionId")
        
        if (!uid) {
            hm.msg = "User not authenticated"
            hm.flag = false
            return
        }
        
        if (!organizationId || !recversionId) {
            hm.msg = "Organization ID and Recruitment Version ID are required"
            hm.flag = false
            return
        }
        
        Organization organization = Organization.get(organizationId)
        RecVersion recversion = RecVersion.get(recversionId)
        
        if (!organization) {
            hm.msg = "Organization not found"
            hm.flag = false
            return
        }
        
        if (!recversion) {
            hm.msg = "Recruitment Version not found"
            hm.flag = false
            return
        }
        
        def parameterlist = RecEvaluationParameter.findAllByOranizationAndRecversion(organization, recversion)
        
        hm.parameterlist = parameterlist.collect { [
            id: it.id,
            parameter: it.parameter,
            parameter_number: it.parameter_number,
            maxmarks: it.maxmarks,
            recexperttype: [
                id: it.recexperttype?.id,
                type: it.recexperttype?.type
            ],
            recdeptexpertgroup: [
                id: it.recdeptexpertgroup?.id,
                groupno: it.recdeptexpertgroup?.groupno,
                groupname: it.recdeptexpertgroup?.groupname
            ]
        ] }
        
        hm.msg = "Parameters fetched successfully"
        hm.flag = true
    }
    
    /**
     * Create or update evaluation parameter
     * Used by: POST /recEvaluationParameter/saveParameter
     */
    def saveParameter(hm, request, data) {
        def uid = hm.remove("uid")
        def parameterId = data.id
        def organizationId = data.organizationId
        def recversionId = data.recversionId
        def expertGroupIds = data.expertGroupIds
        def experttypeId = data.experttypeId
        def parameter = data.parameter
        def parameter_number = data.parameter_number
        def maxmarks = data.maxmarks
        
        if (!uid) {
            hm.msg = "User not authenticated"
            hm.flag = false
            return
        }
        
        if (!parameter || !parameter_number || !maxmarks || !experttypeId) {
            hm.msg = "Required fields missing: parameter, parameter_number, maxmarks, experttypeId"
            hm.flag = false
            return
        }
        
        Login login = Login.findByUsername(uid)
        if (!login) {
            hm.msg = "Login not found"
            hm.flag = false
            return
        }
        
        Instructor instructor = Instructor.findByUid(login.username)
        if (!instructor) {
            hm.msg = "Instructor not found"
            hm.flag = false
            return
        }
        
        RecExpertType recexperttype = RecExpertType.get(experttypeId)
        if (!recexperttype) {
            hm.msg = "Expert Type not found"
            hm.flag = false
            return
        }
        
        // Update existing parameter
        if (parameterId) {
            RecEvaluationParameter recevaluationparameter = RecEvaluationParameter.get(parameterId)
            
            if (!recevaluationparameter) {
                hm.msg = "Parameter not found"
                hm.flag = false
                return
            }
            
            recevaluationparameter.parameter = parameter
            recevaluationparameter.parameter_number = parameter_number.toInteger()
            recevaluationparameter.maxmarks = maxmarks.toDouble()
            recevaluationparameter.recexperttype = recexperttype
            
            // Update expert group if provided
            if (expertGroupIds) {
                def expertGroupId = expertGroupIds instanceof List ? expertGroupIds[0] : expertGroupIds
                RecDeptExpertGroup recdeptexpertgroup = RecDeptExpertGroup.get(expertGroupId)
                if (recdeptexpertgroup) {
                    recevaluationparameter.recdeptexpertgroup = recdeptexpertgroup
                }
            }
            
            recevaluationparameter.username = uid
            recevaluationparameter.updation_date = new Date()
            recevaluationparameter.updation_ip_address = request.getRemoteAddr()
            recevaluationparameter.save(flush: true, failOnError: true)
            
            hm.parameterId = recevaluationparameter.id
            hm.msg = "Evaluation parameter updated successfully"
            hm.flag = true
            return
        }
        
        // Create new parameters
        if (!organizationId || !recversionId || !expertGroupIds) {
            hm.msg = "Required fields missing for creation: organizationId, recversionId, expertGroupIds"
            hm.flag = false
            return
        }
        
        Organization organization = Organization.get(organizationId)
        RecVersion recversion = RecVersion.get(recversionId)
        
        if (!organization) {
            hm.msg = "Organization not found"
            hm.flag = false
            return
        }
        
        if (!recversion) {
            hm.msg = "Recruitment Version not found"
            hm.flag = false
            return
        }
        
        // Handle multiple expert groups
        def departmentlist = []
        if (expertGroupIds instanceof List) {
            for (expertGroupId in expertGroupIds) {
                RecDeptExpertGroup recdeptexpertgroup = RecDeptExpertGroup.get(expertGroupId)
                if (recdeptexpertgroup) {
                    departmentlist.add(recdeptexpertgroup)
                }
            }
        } else {
            RecDeptExpertGroup recdeptexpertgroup = RecDeptExpertGroup.get(expertGroupIds)
            if (recdeptexpertgroup) {
                departmentlist.add(recdeptexpertgroup)
            }
        }
        
        if (departmentlist.isEmpty()) {
            hm.msg = "No valid expert groups found"
            hm.flag = false
            return
        }
        
        // Create parameter for each expert group
        def createdCount = 0
        for (department in departmentlist) {
            RecEvaluationParameter recevaluationparameter = new RecEvaluationParameter()
            recevaluationparameter.recdeptexpertgroup = department
            recevaluationparameter.oranization = organization
            recevaluationparameter.recversion = recversion
            recevaluationparameter.parameter = parameter
            recevaluationparameter.parameter_number = parameter_number.toInteger()
            recevaluationparameter.maxmarks = maxmarks.toDouble()
            recevaluationparameter.recexperttype = recexperttype
            recevaluationparameter.username = uid
            recevaluationparameter.creation_date = new Date()
            recevaluationparameter.updation_date = new Date()
            recevaluationparameter.creation_ip_address = request.getRemoteAddr()
            recevaluationparameter.updation_ip_address = request.getRemoteAddr()
            recevaluationparameter.save(flush: true, failOnError: true)
            createdCount++
        }
        
        hm.parametersCreated = createdCount
        hm.msg = "Evaluation parameters created successfully"
        hm.flag = true
    }
    
    /**
     * Delete evaluation parameter
     * Used by: DELETE /recEvaluationParameter/deleteParameter/:id
     */
    def deleteParameter(hm, request, data) {
        def uid = hm.remove("uid")
        def parameterId = hm.remove("parameterId")
        
        if (!uid) {
            hm.msg = "User not authenticated"
            hm.flag = false
            return
        }
        
        if (!parameterId) {
            hm.msg = "Parameter ID is required"
            hm.flag = false
            return
        }
        
        Login login = Login.findByUsername(uid)
        if (!login) {
            hm.msg = "Login not found"
            hm.flag = false
            return
        }
        
        Instructor instructor = Instructor.findByUid(login.username)
        if (!instructor) {
            hm.msg = "Instructor not found"
            hm.flag = false
            return
        }
        
        RecEvaluationParameter recevaluationparameter = RecEvaluationParameter.get(parameterId)
        
        if (!recevaluationparameter) {
            hm.msg = "Parameter not found"
            hm.flag = false
            return
        }
        
        recevaluationparameter.delete(flush: true, failOnError: true)
        
        hm.parameterId = parameterId
        hm.msg = "Evaluation parameter deleted successfully"
        hm.flag = true
    }
    
    // ═══════════════════════════════════════════════════════════════
    // Helper Methods (Inlined from InformationService)
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Get current academic year for organization and module
     */
    private def getCurrentAcademicYear(Organization org) {
        // Find ApplicationType with application_type = "ERP"
        def applicationType = ApplicationType.findByApplication_type("ERP")
        if (!applicationType) {
            return null
        }
        
        // Find RoleType with type = "Recruitment" for this organization and application type
        def roleType = RoleType.findByOrganizationAndApplicationtypeAndType(org, applicationType, "Recruitment")
        if (!roleType) {
            return null
        }
        
        // Find active ApplicationAcademicYear for this organization and roletype
        def aay = ApplicationAcademicYear.findByOrganizationAndRoletypeAndIsActive(org, roleType, true)
        return aay
    }
    
    /**
     * Check if user is management
     * Checks if login has a role with "MANAGEMENT" in the role name
     */
    private boolean isManagement(Login login, Instructor instructor) {
        if (!login || !login.roles) {
            return false
        }
        
        // Check if any of the user's roles contains "MANAGEMENT"
        def hasManagementRole = login.roles.any { role ->
            role.role?.toUpperCase()?.contains("MANAGEMENT")
        }
        
        return hasManagementRole
    }
}
