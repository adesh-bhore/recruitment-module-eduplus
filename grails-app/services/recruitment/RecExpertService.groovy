package recruitment

import grails.gorm.transactions.Transactional

@Transactional
class RecExpertService {
    
    // ═══════════════════════════════════════════════════════════════
    // Phase 1: Expert CRUD APIs
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Get initial data for expert management form
     * Used by: GET /recExpert/getInitialData
     */
    def getInitialData(hm, request, data) {
        def uid = hm.remove("uid")
        
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
        
        Organization organization = instructor.organization
        
        // Get ApplicationType
        ApplicationType at = ApplicationType.findByApplication_type("ERP")
        if (!at) {
            hm.msg = "Application Type 'ERP' not found"
            hm.flag = false
            return
        }
        
        // Check if user is management
        def is_management = isManagement(login, organization, at)
        
        // Get RoleType for Registration module (may not exist)
        RoleType rt = RoleType.findByApplicationtypeAndTypeAndOrganization(at, "Registration", organization)
        
        // Get active ApplicationAcademicYear (may not exist)
        ApplicationAcademicYear aay = null
        AcademicYear currentAcademicYear = null
        
        if (rt) {
            aay = ApplicationAcademicYear.findByRoletypeAndIsActiveAndOrganization(rt, true, organization)
            if (aay) {
                currentAcademicYear = aay.academicyear
            }
        }
        
        // If no aay found, try to get any active academic year
        if (!currentAcademicYear) {
            currentAcademicYear = AcademicYear.findByIsactive(true, [sort: 'sort_order', order: 'desc'])
        }
        
        // Get academic year list
        def aylist = AcademicYear.findAllByIsactive(true, [sort: 'ay'])
        
        // Get recruitment versions
        def recversionlist = []
        RecVersion recversion = null
        
        if (currentAcademicYear) {
            recversionlist = RecVersion.findAllByOrganizationAndAcademicyear(organization, currentAcademicYear)
            
            // Get current recruitment version
            recversion = RecVersion.findByOrganizationAndAcademicyearAndIscurrent(organization, currentAcademicYear, true)
            if (!recversion && recversionlist) {
                recversion = recversionlist[0]
            }
        }
        
        // If still no recversion, try to get any current version for backend processing
        if (!recversion) {
            recversion = RecVersion.findByOrganizationAndIscurrentforbackendprocessing(organization, true)
        }
        
        // If still no recversion, get any version for this organization
        if (!recversion) {
            recversion = RecVersion.findByOrganization(organization)
        }
        
        if (!recversion) {
            hm.msg = "No recruitment version found for organization"
            hm.flag = false
            return
        }
        
        // Get expert types
        def recexperttypelist = RecExpertType.findAllByOranization(organization)
        if (!recexperttypelist) {
            hm.msg = "Please add Expert Type in system"
            hm.flag = false
            return
        }
        
        // Get department expert groups
        def recdeptexpertgrouplist = RecDeptExpertGroup.findAllByOrganizationAndRecversion(organization, recversion)
        if (!recdeptexpertgrouplist) {
            hm.msg = "Please add Department Groups in system"
            hm.flag = false
            return
        }
        
        // Get organization list based on management status
        def orglist = []
        if (is_management) {
            OrganizationGroup organizationGroup = organization.organizationgroup
            if (organizationGroup) {
                orglist = Organization.findAllByIsactiveAndOrganizationgroup(true, organizationGroup)
            } else {
                orglist = [organization]
            }
        } else {
            orglist = [organization]
        }
        
        // Get existing experts
        def recexpertlist = RecExpert.findAllByOranization(organization)
        
        // Build response
        hm.aylist = aylist.collect { [id: it.id, year: it.ay] }
        
        hm.recversionlist = recversionlist.collect { [
            id: it.id,
            version_number: it.version_number
        ] }
        
        hm.recversion = [
            id: recversion.id,
            version_number: recversion.version_number
        ]
        
        hm.aay = aay ? [
            id: aay.id,
            academicyear: [id: aay.academicyear.id, year: aay.academicyear.ay]
        ] : (currentAcademicYear ? [
            id: null,
            academicyear: [id: currentAcademicYear.id, year: currentAcademicYear.ay]
        ] : null)
        
        hm.orglist = orglist.collect { [
            id: it.id,
            name: it.organization_name
        ] }
        
        hm.org = [
            id: organization.id,
            name: organization.organization_name
        ]
        
        hm.is_management = is_management
        
        hm.recdeptexpertgrouplist = recdeptexpertgrouplist.collect { [
            id: it.id,
            groupno: it.groupno,
            groupname: it.groupname,
            cutoff: it.cutoff
        ] }
        
        hm.recexpertlist = recexpertlist.collect { [
            id: it.id,
            expertno: it.expertno,
            expname: it.expname,
            loginname: it.loginname,
            password: it.password,
            isblocked: it.isblocked,
            recdeptexpertgroup: [
                id: it.recdeptexpertgroup?.id,
                groupname: it.recdeptexpertgroup?.groupname
            ],
            recexperttype: [
                id: it.recexperttype?.id,
                type: it.recexperttype?.type
            ]
        ] }
        
        hm.recexperttypelist = recexperttypelist.collect { [
            id: it.id,
            type: it.type
        ] }
        
        hm.msg = "Initial data fetched successfully"
        hm.flag = true
    }
    
    /**
     * Get filtered data based on organization
     * Used by: GET /recExpert/getFilters
     */
    def getFilters(hm, request, data) {
        def uid = hm.remove("uid")
        def organizationId = hm.remove("organizationId")
        
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
        
        // Use provided organization or instructor's organization
        Organization organization = organizationId ? Organization.get(organizationId) : instructor.organization
        if (!organization) {
            hm.msg = "Organization not found"
            hm.flag = false
            return
        }
        
        // Get ApplicationType
        ApplicationType at = ApplicationType.findByApplication_type("ERP")
        if (!at) {
            hm.msg = "Application Type 'ERP' not found"
            hm.flag = false
            return
        }
        
        // Check if user is management
        def is_management = isManagement(login, instructor.organization, at)
        
        // Get RoleType for Registration module (may not exist)
        RoleType rt = RoleType.findByApplicationtypeAndTypeAndOrganization(at, "Registration", organization)
        
        // Get active ApplicationAcademicYear (may not exist)
        ApplicationAcademicYear aay = null
        AcademicYear currentAcademicYear = null
        
        if (rt) {
            aay = ApplicationAcademicYear.findByRoletypeAndIsActiveAndOrganization(rt, true, organization)
            if (aay) {
                currentAcademicYear = aay.academicyear
            }
        }
        
        // If no aay found, try to get any active academic year
        if (!currentAcademicYear) {
            currentAcademicYear = AcademicYear.findByIsactive(true, [sort: 'sort_order', order: 'desc'])
        }
        

        // Get academic year list
        def aylist = AcademicYear.findAllByIsactive(true, [sort: 'ay'])
        
        // Get recruitment versions
        def recversionlist = []
        RecVersion recversion = null
        
        if (currentAcademicYear) {
            recversionlist = RecVersion.findAllByOrganizationAndAcademicyear(organization, currentAcademicYear)
            
            // Get current recruitment version
            recversion = RecVersion.findByOrganizationAndAcademicyearAndIscurrent(organization, currentAcademicYear, true)
            if (!recversion && recversionlist) {
                recversion = recversionlist[0]
            }
        }
        
        // If still no recversion, try to get any current version for backend processing
        if (!recversion) {
            recversion = RecVersion.findByOrganizationAndIscurrentforbackendprocessing(organization, true)
        }
        
        // If still no recversion, get any version for this organization
        if (!recversion) {
            recversion = RecVersion.findByOrganization(organization)
        }
        
        if (!recversion) {
            hm.msg = "No recruitment version found for organization"
            hm.flag = false
            return
        }
        
        // Get expert types
        def recexperttypelist = RecExpertType.findAllByOranization(organization)
        if (!recexperttypelist) {
            hm.msg = "Please add Expert Type in system"
            hm.flag = false
            return
        }
        
        // Get department expert groups
        def recdeptexpertgrouplist = RecDeptExpertGroup.findAllByOrganizationAndRecversion(organization, recversion)
        if (!recdeptexpertgrouplist) {
            hm.msg = "Please add Department Groups in system"
            hm.flag = false
            return
        }
        
        // Get organization list based on management status
        def orglist = []
        if (is_management) {
            OrganizationGroup organizationGroup = instructor.organization.organizationgroup
            if (organizationGroup) {
                orglist = Organization.findAllByIsactiveAndOrganizationgroup(true, organizationGroup)
            } else {
                orglist = [instructor.organization]
            }
        } else {
            orglist = [instructor.organization]
        }
        
        // Get existing experts
        def recexpertlist = RecExpert.findAllByOranization(organization)
        
        // Build response (same as getInitialData)
        hm.aylist = aylist.collect { [id: it.id, year: it.ay] }
        hm.recversionlist = recversionlist.collect { [id: it.id, version_number: it.version_number] }
        hm.recversion = [id: recversion.id, version_number: recversion.version_number]
        hm.aay = aay ? [id: aay.id, academicyear: [id: aay.academicyear.id, year: aay.academicyear.ay]] : (currentAcademicYear ? [id: null, academicyear: [id: currentAcademicYear.id, year: currentAcademicYear.ay]] : null)
        hm.orglist = orglist.collect { [id: it.id, name: it.organization_name] }
        hm.org = [id: organization.id, name: organization.organization_name]
        hm.is_management = is_management
        hm.recdeptexpertgrouplist = recdeptexpertgrouplist.collect { [id: it.id, groupno: it.groupno, groupname: it.groupname, cutoff: it.cutoff] }
        hm.recexpertlist = recexpertlist.collect { [
            id: it.id,
            expertno: it.expertno,
            expname: it.expname,
            loginname: it.loginname,
            password: it.password,
            isblocked: it.isblocked,
            recdeptexpertgroup: [id: it.recdeptexpertgroup?.id, groupname: it.recdeptexpertgroup?.groupname],
            recexperttype: [id: it.recexperttype?.id, type: it.recexperttype?.type]
        ] }
        hm.recexperttypelist = recexperttypelist.collect { [id: it.id, type: it.type] }
        
        hm.msg = "Filtered data fetched successfully"
        hm.flag = true
    }
    
    /**
     * Create new expert
     * Used by: POST /recExpert/saveExpert
     */
    def saveExpert(hm, request, data) {
        def uid = hm.remove("uid")
        def expertname = data.expertname
        def deptExpertGroupId = data.deptExpertGroupId
        def expertTypeId = data.expertTypeId
        
        if (!uid) {
            hm.msg = "User not authenticated"
            hm.flag = false
            return
        }
        
        if (!expertname || !deptExpertGroupId || !expertTypeId) {
            hm.msg = "Required fields missing: expertname, deptExpertGroupId, expertTypeId"
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
        
        Organization organization = instructor.organization
        
        // Get current recruitment version
        RecVersion recversion = RecVersion.findByOrganizationAndIscurrentforbackendprocessing(organization, true)
        if (!recversion) {
            hm.msg = "No current recruitment version found for backend processing"
            hm.flag = false
            return
        }
        
        // Get department expert group
        RecDeptExpertGroup recdeptexpertgroup = RecDeptExpertGroup.get(deptExpertGroupId)
        if (!recdeptexpertgroup) {
            hm.msg = "Department Expert Group not found"
            hm.flag = false
            return
        }
        
        // Get expert type
        RecExpertType recexperttype = RecExpertType.get(expertTypeId)
        if (!recexperttype) {
            hm.msg = "Expert Type not found"
            hm.flag = false
            return
        }
        
        // Generate expert number
        def lastExpert = RecExpert.list(sort: 'expertno', order: 'desc', max: 1)[0]
        def expertno = lastExpert ? lastExpert.expertno + 1 : 1
        
        // Generate login name
        String loginname = "${organization.id}${recversion.id}${recdeptexpertgroup.id}${expertno}"
        
        // Generate random 6-digit password
        Random random = new Random()
        String password = ""
        for (int i = 1; i <= 6; i++) {
            int min = (i == 1) ? 1 : 0
            int max = 9
            int n = random.nextInt(max) + min
            password += n
        }
        
        // Create expert
        RecExpert recexpert = new RecExpert()
        recexpert.expertno = expertno
        recexpert.expname = expertname
        recexpert.loginname = loginname
        recexpert.password = password
        recexpert.isblocked = false
        recexpert.username = uid
        recexpert.creation_date = new Date()
        recexpert.updation_date = new Date()
        recexpert.creation_ip_address = request.getRemoteAddr()
        recexpert.updation_ip_address = request.getRemoteAddr()
        recexpert.oranization = organization
        recexpert.recversion = recversion
        recexpert.recdeptexpertgroup = recdeptexpertgroup
        recexpert.recexperttype = recexperttype
        recexpert.save(flush: true, failOnError: true)
        
        hm.expertId = recexpert.id
        hm.expertno = expertno
        hm.loginname = loginname
        hm.password = password
        hm.msg = "Expert created successfully"
        hm.flag = true
    }
    
    /**
     * Delete expert
     * Used by: DELETE /recExpert/deleteExpert/:id
     */
    def deleteExpert(hm, request, data) {
        def uid = hm.remove("uid")
        def expertId = hm.remove("expertId")
        
        if (!uid) {
            hm.msg = "User not authenticated"
            hm.flag = false
            return
        }
        
        if (!expertId) {
            hm.msg = "Expert ID is required"
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
        
        RecExpert expert = RecExpert.get(expertId)
        if (!expert) {
            hm.msg = "Expert not found"
            hm.flag = false
            return
        }
        
        expert.delete(flush: true, failOnError: true)
        
        hm.expertId = expertId
        hm.msg = "Expert deleted successfully"
        hm.flag = true
    }
    
    /**
     * Block expert
     * Used by: POST /recExpert/blockExpert/:id
     */
    def blockExpert(hm, request, data) {
        def uid = hm.remove("uid")
        def expertId = hm.remove("expertId")
        
        if (!uid) {
            hm.msg = "User not authenticated"
            hm.flag = false
            return
        }
        
        if (!expertId) {
            hm.msg = "Expert ID is required"
            hm.flag = false
            return
        }
        
        RecExpert expert = RecExpert.get(expertId)
        if (!expert) {
            hm.msg = "Expert not found"
            hm.flag = false
            return
        }
        
        expert.isblocked = true
        expert.save(flush: true, failOnError: true)
        
        hm.expertId = expertId
        hm.isblocked = true
        hm.msg = "Expert blocked successfully"
        hm.flag = true
    }
    
    /**
     * Unblock expert
     * Used by: POST /recExpert/unblockExpert/:id
     */
    def unblockExpert(hm, request, data) {
        def uid = hm.remove("uid")
        def expertId = hm.remove("expertId")
        
        if (!uid) {
            hm.msg = "User not authenticated"
            hm.flag = false
            return
        }
        
        if (!expertId) {
            hm.msg = "Expert ID is required"
            hm.flag = false
            return
        }
        
        RecExpert expert = RecExpert.get(expertId)
        if (!expert) {
            hm.msg = "Expert not found"
            hm.flag = false
            return
        }
        
        expert.isblocked = false
        expert.save(flush: true, failOnError: true)
        
        hm.expertId = expertId
        hm.isblocked = false
        hm.msg = "Expert unblocked successfully"
        hm.flag = true
    }
    
    // ═══════════════════════════════════════════════════════════════
    // Phase 2: Expert Authentication APIs
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Authenticate expert
     * Used by: POST /recExpert/login
     */
    def loginExpert(hm, request, data) {
        def loginname = data.loginname
        def password = data.password
        
        if (!loginname || !password) {
            hm.msg = "Login name and password are required"
            hm.flag = false
            return
        }
        
        // Find expert by loginname, password, and not blocked
        RecExpert recexpert = RecExpert.findByLoginnameAndPasswordAndIsblocked(loginname, password, false)
        
        if (!recexpert) {
            hm.msg = "Invalid username or password, or account is blocked"
            hm.flag = false
            return
        }
        
        // Build expert response with all details
        hm.expert = [
            id: recexpert.id,
            expertno: recexpert.expertno,
            expname: recexpert.expname,
            loginname: recexpert.loginname,
            isblocked: recexpert.isblocked,
            organization: [
                id: recexpert.oranization?.id,
                name: recexpert.oranization?.organization_name
            ],
            recversion: [
                id: recexpert.recversion?.id,
                version_number: recexpert.recversion?.version_number
            ],
            recdeptexpertgroup: [
                id: recexpert.recdeptexpertgroup?.id,
                groupno: recexpert.recdeptexpertgroup?.groupno,
                groupname: recexpert.recdeptexpertgroup?.groupname
            ],
            recexperttype: [
                id: recexpert.recexperttype?.id,
                type: recexpert.recexperttype?.type
            ]
        ]
        
        hm.msg = "Login successful"
        hm.flag = true
    }
    
    // ═══════════════════════════════════════════════════════════════
    // Helper Methods
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Check if user is management for Recruitment module
     */
    private boolean isManagement(Login login, Organization organization, ApplicationType applicationType) {
        if (!login || !login.roles) {
            return false
        }
        
        // Find RoleType for Recruitment
        RoleType recruitmentRoleType = RoleType.findByApplicationtypeAndTypeAndOrganization(applicationType, "Recruitment", organization)
        if (!recruitmentRoleType) {
            return false
        }
        
        // Check if user has Management role for Recruitment module
        def hasManagementRole = login.roles.any { role ->
            role.role == 'Management' && role.roletype?.id == recruitmentRoleType.id
        }
        
        return hasManagementRole
    }
}
