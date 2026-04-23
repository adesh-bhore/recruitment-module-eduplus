package recruitment

import grails.gorm.transactions.Transactional

@Transactional
class RecExpertService3 {
    
    // ═══════════════════════════════════════════════════════════════
    // Phase 5A: RecDeptExpertGroup Management APIs
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Get initial data for expert group management form
     * Used by: GET /recExpert/getExpertGroupInitialData
     */
    def getExpertGroupInitialData(hm, request, data) {
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
        
        // Get RoleType for Registration module
        RoleType rt = RoleType.findByApplicationtypeAndTypeAndOrganization(at, "Registration", organization)
        
        // Get active ApplicationAcademicYear
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
        
        // Get RecDeptGroup list
        def recDeptGroup = RecDeptGroup.findAllByOrganization(organization)
        
        // Get departments and programs from RecBranch
        def departmentList = getRecDepartment(organization, recversion)
        def programList = getRecProgram(organization, recversion)
        
        // Get existing expert groups
        def recDeptExpertGroup = RecDeptExpertGroup.findAllByOrganizationAndRecversion(organization, recversion)
        
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
        
        hm.recDeptGroup = recDeptGroup.collect { [
            id: it.id,
            groupno: it.groupno
        ] }
        
        hm.departmentList = departmentList
        hm.programList = programList
        
        hm.expertGroups = recDeptExpertGroup.collect { group ->
            [
                id: group.id,
                groupno: group.groupno,
                groupname: group.groupname,
                cutoff: group.cutoff,
                round2cutoff: group.round2cutoff,
                recdeptgroup: group.recdeptgroup ? [
                    id: group.recdeptgroup.id,
                    groupno: group.recdeptgroup.groupno
                ] : null,
                departments: group.department?.collect { [id: it.id, name: it.name] } ?: [],
                programs: group.program?.collect { [id: it.id, name: it.name] } ?: []
            ]
        }
        
        hm.msg = "Initial data fetched successfully"
        hm.flag = true
    }
    
    /**
     * Get filtered expert groups based on organization, academic year, version
     * Used by: GET /recExpert/getExpertGroupFilters
     */
    def getExpertGroupFilters(hm, request, data) {
        def uid = hm.remove("uid")
        def organizationId = hm.remove("organizationId")
        def academicYearId = hm.remove("academicYearId")
        def recversionId = hm.remove("recversionId")
        
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
        
        // Get RoleType for Registration module
        RoleType rt = RoleType.findByApplicationtypeAndTypeAndOrganization(at, "Registration", organization)
        
        // Get active ApplicationAcademicYear
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
        
        // Use provided academic year or current
        AcademicYear ay = academicYearId ? AcademicYear.get(academicYearId) : currentAcademicYear
        
        // Get academic year list
        def aylist = AcademicYear.findAllByIsactive(true, [sort: 'ay'])
        
        // Get recruitment versions
        def recversionlist = []
        RecVersion recversion = null
        
        if (ay) {
            recversionlist = RecVersion.findAllByOrganizationAndAcademicyear(organization, ay)
            
            // Get current recruitment version
            recversion = RecVersion.findByOrganizationAndAcademicyearAndIscurrent(organization, ay, true)
            if (!recversion && recversionlist) {
                recversion = recversionlist[0]
            }
        }
        
        // Use provided recversion if specified
        if (recversionId) {
            recversion = RecVersion.get(recversionId)
        }
        
        // If still no recversion, try to get any current version for backend processing
        if (!recversion) {
            recversion = RecVersion.findByOrganizationAndIscurrentforbackendprocessing(organization, true)
        }
        
        if (!recversion) {
            hm.msg = "No recruitment version found"
            hm.flag = false
            return
        }
        
        // Get RecDeptGroup list
        def recDeptGroup = RecDeptGroup.findAllByOrganization(organization)
        
        // Get departments and programs from RecBranch
        def departmentList = getRecDepartment(organization, recversion)
        def programList = getRecProgram(organization, recversion)
        
        // Get existing expert groups
        def recDeptExpertGroup = RecDeptExpertGroup.findAllByOrganizationAndRecversion(organization, recversion)
        
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
        
        // Build response (same structure as getInitialData)
        hm.aylist = aylist.collect { [id: it.id, year: it.ay] }
        hm.recversionlist = recversionlist.collect { [id: it.id, version_number: it.version_number] }
        hm.recversion = [id: recversion.id, version_number: recversion.version_number]
        hm.aay = aay ? [id: aay.id, academicyear: [id: aay.academicyear.id, year: aay.academicyear.ay]] : (ay ? [id: null, academicyear: [id: ay.id, year: ay.ay]] : null)
        hm.orglist = orglist.collect { [id: it.id, name: it.organization_name] }
        hm.org = [id: organization.id, name: organization.organization_name]
        hm.is_management = is_management
        hm.recDeptGroup = recDeptGroup.collect { [id: it.id, groupno: it.groupno] }
        hm.departmentList = departmentList
        hm.programList = programList
        hm.expertGroups = recDeptExpertGroup.collect { group ->
            [
                id: group.id,
                groupno: group.groupno,
                groupname: group.groupname,
                cutoff: group.cutoff,
                round2cutoff: group.round2cutoff,
                recdeptgroup: group.recdeptgroup ? [id: group.recdeptgroup.id, groupno: group.recdeptgroup.groupno] : null,
                departments: group.department?.collect { [id: it.id, name: it.name] } ?: [],
                programs: group.program?.collect { [id: it.id, name: it.name] } ?: []
            ]
        }
        
        hm.msg = "Filtered data fetched successfully"
        hm.flag = true
    }
    
    /**
     * Create or update expert group
     * Used by: POST /recExpert/saveExpertGroup
     */
    def saveExpertGroup(hm, request, data) {
        def uid = hm.remove("uid")
        def groupno = data.groupno
        def groupname = data.groupname
        def cutoff = data.cutoff
        def round2cutoff = data.round2cutoff
        def departmentIds = data.departmentIds
        def programIds = data.programIds
        def recDeptGroupId = data.recDeptGroupId
        def recversionId = data.recversionId
        def expertGroupId = data.expertGroupId
        def organizationId = data.organizationId
        
        if (!uid) {
            hm.msg = "User not authenticated"
            hm.flag = false
            return
        }
        
        if (!groupno || !groupname) {
            hm.msg = "Group number and group name are required"
            hm.flag = false
            return
        }
        
        if (!departmentIds || departmentIds.size() == 0) {
            hm.msg = "Please select at least one department"
            hm.flag = false
            return
        }
        
        if (!programIds || programIds.size() == 0) {
            hm.msg = "Please select at least one program"
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
        
        // Get recruitment version
        RecVersion recversion = recversionId ? RecVersion.get(recversionId) : RecVersion.findByOrganizationAndIscurrentforbackendprocessing(organization, true)
        if (!recversion) {
            hm.msg = "Recruitment version not found"
            hm.flag = false
            return
        }
        
        // Get RecDeptGroup if provided
        RecDeptGroup recDeptGroup = null
        if (recDeptGroupId && recDeptGroupId != "null") {
            recDeptGroup = RecDeptGroup.get(recDeptGroupId)
        }
        
        // Collect departments
        def departmentlist = []
        if (departmentIds instanceof List) {
            departmentIds.each { deptId ->
                Department dept = Department.get(deptId)
                if (dept) {
                    departmentlist.add(dept)
                }
            }
        } else {
            Department dept = Department.get(departmentIds)
            if (dept) {
                departmentlist.add(dept)
            }
        }
        
        // Collect programs
        def programlist = []
        if (programIds instanceof List) {
            programIds.each { progId ->
                Program program = Program.get(progId)
                if (program) {
                    programlist.add(program)
                }
            }
        } else {
            Program program = Program.get(programIds)
            if (program) {
                programlist.add(program)
            }
        }
        
        RecDeptExpertGroup recDeptExpertGroup
        
        if (expertGroupId) {
            // Update existing expert group
            recDeptExpertGroup = RecDeptExpertGroup.get(expertGroupId)
            if (!recDeptExpertGroup) {
                hm.msg = "Expert group not found"
                hm.flag = false
                return
            }
            
            // Clear existing associations
            recDeptExpertGroup.department?.clear()
            recDeptExpertGroup.program?.clear()
            
            // Update properties
            recDeptExpertGroup.groupno = Integer.parseInt(groupno.toString())
            recDeptExpertGroup.groupname = groupname
            recDeptExpertGroup.cutoff = cutoff ? Double.parseDouble(cutoff.toString()) : 0.0
            recDeptExpertGroup.round2cutoff = round2cutoff ? Double.parseDouble(round2cutoff.toString()) : 0.0
            recDeptExpertGroup.recdeptgroup = recDeptGroup
            recDeptExpertGroup.updation_date = new Date()
            recDeptExpertGroup.updation_ip_address = request.getRemoteAddr()
            
        } else {
            // Check for duplicate group number
            RecDeptExpertGroup existing = RecDeptExpertGroup.findByGroupnoAndOrganizationAndRecversion(
                Integer.parseInt(groupno.toString()), organization, recversion
            )
            if (existing) {
                hm.msg = "Group number already exists for this organization and version"
                hm.flag = false
                return
            }
            
            // Create new expert group
            recDeptExpertGroup = new RecDeptExpertGroup()
            recDeptExpertGroup.groupno = Integer.parseInt(groupno.toString())
            recDeptExpertGroup.groupname = groupname
            recDeptExpertGroup.cutoff = cutoff ? Double.parseDouble(cutoff.toString()) : 0.0
            recDeptExpertGroup.round2cutoff = round2cutoff ? Double.parseDouble(round2cutoff.toString()) : 0.0
            recDeptExpertGroup.organization = organization
            recDeptExpertGroup.recversion = recversion
            recDeptExpertGroup.recdeptgroup = recDeptGroup
            recDeptExpertGroup.username = uid
            recDeptExpertGroup.creation_date = new Date()
            recDeptExpertGroup.updation_date = new Date()
            recDeptExpertGroup.creation_ip_address = request.getRemoteAddr()
            recDeptExpertGroup.updation_ip_address = request.getRemoteAddr()
        }
        
        // Add departments
        departmentlist.each { dept ->
            recDeptExpertGroup.addToDepartment(dept)
        }
        
        // Add programs
        programlist.each { prog ->
            recDeptExpertGroup.addToProgram(prog)
        }
        
        // Save
        recDeptExpertGroup.save(flush: true, failOnError: true)
        
        hm.expertGroupId = recDeptExpertGroup.id
        hm.msg = expertGroupId ? "Expert group updated successfully" : "Expert group created successfully"
        hm.flag = true
    }
    
    /**
     * Delete expert group
     * Used by: DELETE /recExpert/deleteExpertGroup/:id
     */
    def deleteExpertGroup(hm, request, data) {
        def uid = hm.remove("uid")
        def expertGroupId = hm.remove("expertGroupId")
        
        if (!uid) {
            hm.msg = "User not authenticated"
            hm.flag = false
            return
        }
        
        if (!expertGroupId) {
            hm.msg = "Expert group ID is required"
            hm.flag = false
            return
        }
        
        RecDeptExpertGroup expertGroup = RecDeptExpertGroup.get(expertGroupId)
        if (!expertGroup) {
            hm.msg = "Expert group not found"
            hm.flag = false
            return
        }
        
        // Check if any experts are assigned to this group
        def expertsInGroup = RecExpert.findAllByRecdeptexpertgroup(expertGroup)
        if (expertsInGroup && expertsInGroup.size() > 0) {
            hm.msg = "Cannot delete expert group. ${expertsInGroup.size()} expert(s) are assigned to this group"
            hm.flag = false
            return
        }
        
        expertGroup.delete(flush: true, failOnError: true)
        
        hm.expertGroupId = expertGroupId
        hm.msg = "Expert group deleted successfully"
        hm.flag = true
    }
    
    /**
     * Get expert groups report
     * Used by: GET /recExpert/getExpertGroupReport
     */
    def getExpertGroupReport(hm, request, data) {
        def uid = hm.remove("uid")
        def organizationId = hm.remove("organizationId")
        def academicYearId = hm.remove("academicYearId")
        
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
        
        // Get academic year
        AcademicYear ay = null
        if (academicYearId) {
            ay = AcademicYear.get(academicYearId)
        } else {
            ay = AcademicYear.findByIsactive(true, [sort: 'sort_order', order: 'desc'])
        }
        
        if (!ay) {
            hm.msg = "Academic year not found"
            hm.flag = false
            return
        }
        
        // Get recruitment version
        RecVersion recversion = RecVersion.findByOrganizationAndAcademicyearAndIscurrent(organization, ay, true)
        if (!recversion) {
            hm.msg = "No current recruitment version found for this academic year"
            hm.flag = false
            return
        }
        
        // Get expert groups
        def recDeptExpertGroup = RecDeptExpertGroup.findAllByOrganizationAndRecversion(organization, recversion)
        
        // Build report with counts
        def expertGroupsReport = recDeptExpertGroup.collect { group ->
            def departmentCount = group.department?.size() ?: 0
            def programCount = group.program?.size() ?: 0
            def expertCount = RecExpert.countByRecdeptexpertgroup(group)
            
            [
                id: group.id,
                groupno: group.groupno,
                groupname: group.groupname,
                cutoff: group.cutoff,
                round2cutoff: group.round2cutoff,
                departmentCount: departmentCount,
                programCount: programCount,
                expertCount: expertCount,
                recdeptgroup: group.recdeptgroup ? [
                    id: group.recdeptgroup.id,
                    groupno: group.recdeptgroup.groupno
                ] : null
            ]
        }
        
        hm.expertGroups = expertGroupsReport
        hm.organization = [id: organization.id, name: organization.organization_name]
        hm.academicYear = [id: ay.id, year: ay.ay]
        hm.recversion = [id: recversion.id, version_number: recversion.version_number]
        hm.msg = "Expert groups report fetched successfully"
        hm.flag = true
    }
    
    // ═══════════════════════════════════════════════════════════════
    // Phase 5B: RecExpertType Management APIs
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Get list of expert types
     * Used by: GET /recExpert/getExpertTypeList
     */
    def getExpertTypeList(hm, request, data) {
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
        
        // Get expert types for this organization
        def recexperttypelist = RecExpertType.findAllByOranization(organization)
        
        hm.expertTypes = recexperttypelist.collect { [
            id: it.id,
            type: it.type,
            organization: [
                id: it.oranization?.id,
                name: it.oranization?.organization_name
            ]
        ] }
        
        hm.organization = [
            id: organization.id,
            name: organization.organization_name
        ]
        
        hm.msg = "Expert types fetched successfully"
        hm.flag = true
    }
    
    /**
     * Create or update expert type
     * Used by: POST /recExpert/saveExpertType
     */
    def saveExpertType(hm, request, data) {
        def uid = hm.remove("uid")
        def type = data.type
        def expertTypeId = data.expertTypeId
        
        if (!uid) {
            hm.msg = "User not authenticated"
            hm.flag = false
            return
        }
        
        if (!type) {
            hm.msg = "Expert type name is required"
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
        
        RecExpertType recExpertType
        
        if (expertTypeId) {
            // Update existing expert type
            recExpertType = RecExpertType.get(expertTypeId)
            if (!recExpertType) {
                hm.msg = "Expert type not found"
                hm.flag = false
                return
            }
            
            // Check for duplicate type name (excluding current record)
            RecExpertType existing = RecExpertType.findByTypeAndOranization(type, organization)
            if (existing && existing.id != recExpertType.id) {
                hm.msg = "Expert type with this name already exists"
                hm.flag = false
                return
            }
            
            recExpertType.type = type
            recExpertType.updation_date = new Date()
            recExpertType.updation_ip_address = request.getRemoteAddr()
            
        } else {
            // Check for duplicate type name
            RecExpertType existing = RecExpertType.findByTypeAndOranization(type, organization)
            if (existing) {
                hm.msg = "Expert type with this name already exists"
                hm.flag = false
                return
            }
            
            // Create new expert type
            recExpertType = new RecExpertType()
            recExpertType.type = type
            recExpertType.oranization = organization
            recExpertType.username = uid
            recExpertType.creation_date = new Date()
            recExpertType.updation_date = new Date()
            recExpertType.creation_ip_address = request.getRemoteAddr()
            recExpertType.updation_ip_address = request.getRemoteAddr()
        }
        
        recExpertType.save(flush: true, failOnError: true)
        
        hm.expertTypeId = recExpertType.id
        hm.msg = expertTypeId ? "Expert type updated successfully" : "Expert type created successfully"
        hm.flag = true
    }
    
    /**
     * Delete expert type
     * Used by: DELETE /recExpert/deleteExpertType/:id
     */
    def deleteExpertType(hm, request, data) {
        def uid = hm.remove("uid")
        def expertTypeId = hm.remove("expertTypeId")
        
        if (!uid) {
            hm.msg = "User not authenticated"
            hm.flag = false
            return
        }
        
        if (!expertTypeId) {
            hm.msg = "Expert type ID is required"
            hm.flag = false
            return
        }
        
        RecExpertType expertType = RecExpertType.get(expertTypeId)
        if (!expertType) {
            hm.msg = "Expert type not found"
            hm.flag = false
            return
        }
        
        // Check if any experts are using this type
        def expertsWithType = RecExpert.findAllByRecexperttype(expertType)
        if (expertsWithType && expertsWithType.size() > 0) {
            hm.msg = "Cannot delete expert type. ${expertsWithType.size()} expert(s) are using this type"
            hm.flag = false
            return
        }
        
        expertType.delete(flush: true, failOnError: true)
        
        hm.expertTypeId = expertTypeId
        hm.msg = "Expert type deleted successfully"
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
    
    /**
     * Get departments from RecBranch for given organization and version
     */
    private def getRecDepartment(Organization org, RecVersion recver) {
        def departmentlist = []
        
        def branches = RecBranch.findAllByOrganizationAndRecversion(org, recver)
        def deptIds = []
        
        branches.each { branch ->
            def dept = branch.program?.department
            if (dept && !deptIds.contains(dept.id)) {
                deptIds.add(dept.id)
                departmentlist.add([
                    id: dept.id,
                    name: dept.name
                ])
            }
        }
        
        return departmentlist
    }
    
    /**
     * Get programs from RecBranch for given organization and version
     */
    private def getRecProgram(Organization org, RecVersion recver) {
        def programlist = []
        
        def branches = RecBranch.findAllByOrganizationAndRecversion(org, recver)
        def progIds = []
        
        branches.each { branch ->
            def prog = branch.program
            if (prog && !progIds.contains(prog.id)) {
                progIds.add(prog.id)
                programlist.add([
                    id: prog.id,
                    name: prog.name
                ])
            }
        }
        
        return programlist
    }
}
