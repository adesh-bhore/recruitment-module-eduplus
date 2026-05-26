package recruitment

import grails.gorm.transactions.Transactional
import java.text.SimpleDateFormat

@Transactional
class RecApplicationService_2 {
    
    // ═══════════════════════════════════════════════════════════════
    // Phase 4: Admin - Application Listing & Filtering APIs
    // ═══════════════════════════════════════════════════════════════
    
    // OLD METHOD: recauthorityselection (from RecApplicationController)
    /**
     * Get authority applications - detect user's authority roles
     * Used by: GET /recApplication/getAuthorityApplications
     */
    def getAuthorityApplications(hm, request, data) {
        def uid = hm.remove("uid")
            
            if (!uid) {
                hm.msg = "User not authenticated"
                hm.flag = false
                return
            }
            
            // Find instructor
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
            
            ArrayList authoritylist = new ArrayList()
            
            // Check if HOD (with safe navigation for broken department relationship)
            try {
                if (instructor?.department && instructor?.id == instructor?.department?.hod?.id) {
                    authoritylist.add("HOD")
                }
            } catch (Exception e) {
                println("Warning: Could not check HOD status: ${e.message}")
            }
            
            // Check faculty posts for other authorities
            try {
                for (ERPFacultyPost e : instructor?.erpfacultypost) {
                    if (e.name == "Establishment Section") {
                        authoritylist.add("Establishment Section")
                        break
                    }
                }
            } catch (Exception e) {
                println("Warning: Could not check Establishment Section: ${e.message}")
            }
            
            try {
                for (ERPFacultyPost e : instructor?.erpfacultypost) {
                    if (e.name == "Management") {
                        authoritylist.add("Management")
                        break
                    }
                }
            } catch (Exception e) {
                println("Warning: Could not check Management: ${e.message}")
            }
            
            try {
                for (ERPFacultyPost e : instructor?.erpfacultypost) {
                    if (e.name == "Dean" || e.name == "DEAN") {
                        authoritylist.add("Dean")
                        break
                    }
                }
            } catch (Exception e) {
                println("Warning: Could not check Dean: ${e.message}")
            }
            
            // Get instructor details (with safe navigation)
            def deptName = null
            def deptId = null
            try {
                if (instructor.department) {
                    deptName = instructor.department.name
                    deptId = instructor.department.id
                }
            } catch (Exception e) {
                println("Warning: Could not load department: ${e.message}")
            }
            
            hm.instructor = [
                id: instructor.id,
                name: "${instructor.person?.firstName ?: ''} ${instructor.person?.lastName ?: ''}".trim(),
                department: deptName,
                departmentId: deptId,
                organization: instructor.organization?.organization_name,
                organizationId: instructor.organization?.id
            ]
            
            hm.authorityList = authoritylist
            hm.msg = "Authority list fetched successfully"
            hm.flag = true
    }
    
    // OLD METHOD: recApplicationSummary (from RecApplicationController)
    /**
     * Get application summary with filters
     * Used by: GET /recApplication/getApplicationSummary
     */
    def getApplicationSummary(hm, request, data) {
        def uid = hm.remove("uid")
        def authorityType = hm.remove("authorityType")
        def recverId = hm.remove("recver")
        def status = hm.remove("status")
        def branchId = hm.remove("recbranch")
        def postId = hm.remove("recpost")
        def fromdate = hm.remove("fromdate")
        def todate = hm.remove("todate")
        def page = hm.remove("page") ? hm.remove("page").toInteger() : 1
        def pageSize = hm.remove("pageSize") ? hm.remove("pageSize").toInteger() : 50
            
            if (!uid) {
                hm.msg = "User not authenticated"
                hm.flag = false
                return
            }
            
            if (!authorityType) {
                hm.msg = "Authority type is required"
                hm.flag = false
                return
            }
            
            // Find instructor
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
            
            // Get recruitment version
            RecVersion recversion
            if (recverId) {
                recversion = RecVersion.findById(recverId)
            } else {
                recversion = RecVersion.findByOrganizationAndIscurrentforbackendprocessing(organization, true)
            }
            
            if (!recversion) {
                hm.msg = "Recruitment version not found"
                hm.flag = false
                return
            }
            
            // Get applications based on authority type
            def applications = []
            def totalCount = 0
            
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd")
            
            switch (authorityType) {
                case "HOD":
                    // HOD sees applications for their department's branches only
                    def recapplicationlist = RecApplication.findAllByRecversionAndOrganizationAndIsfeespaid(
                        recversion, organization, true)
                    
                    for (RecApplication reca : recapplicationlist) {
                        for (RecBranch rb : reca.recbranch) {
                            if (rb.program?.department?.hod?.id == instructor.id) {
                                // Apply additional filters
                                if (applyFilters(reca, status, branchId, postId, fromdate, todate, df)) {
                                    applications.add(buildApplicationSummary(reca, rb, authorityType, df))
                                }
                                break
                            }
                        }
                    }
                    break
                    
                case "Dean":
                case "Establishment Section":
                case "Management":
                    // These authorities see all applications
                    def recapplicationlist = RecApplication.findAllByRecversionAndOrganizationAndIsfeespaid(
                        recversion, organization, true)
                    
                    for (RecApplication reca : recapplicationlist) {
                        if (applyFilters(reca, status, branchId, postId, fromdate, todate, df)) {
                            applications.add(buildApplicationSummary(reca, null, authorityType, df))
                        }
                    }
                    break
                    
                default:
                    hm.msg = "Invalid authority type"
                    hm.flag = false
                    return
            }
            
            totalCount = applications.size()
            
            // Apply pagination
            def startIndex = (page - 1) * pageSize
            def endIndex = Math.min(startIndex + pageSize, totalCount)
            def paginatedApplications = applications.subList(startIndex, endIndex)
            
            hm.totalCount = totalCount
            hm.page = page
            hm.pageSize = pageSize
            hm.totalPages = Math.ceil(totalCount / pageSize).toInteger()
            hm.applications = paginatedApplications
            hm.msg = "Applications fetched successfully"
            hm.flag = true
    }
    
    /**
     * Helper method to apply filters
     */
    private boolean applyFilters(RecApplication reca, status, branchId, postId, fromdate, todate, df) {
        // Status filter
        if (status) {
            def appStatus = RecApplicationStatus.findByRecapplication(reca)
            if (appStatus?.recapplicationstatusmaster?.status != status) {
                return false
            }
        }
        
        // Branch filter
        if (branchId) {
            def hasBranch = reca.recbranch?.find { it.id == branchId.toLong() }
            if (!hasBranch) {
                return false
            }
        }
        
        // Post filter
        if (postId) {
            def hasPost = reca.recpost?.find { it.id == postId.toLong() }
            if (!hasPost) {
                return false
            }
        }
        
        // Date range filter
        if (fromdate && reca.applicationdate) {
            Date from = df.parse(fromdate)
            if (reca.applicationdate.before(from)) {
                return false
            }
        }
        
        if (todate && reca.applicationdate) {
            Date to = df.parse(todate)
            if (reca.applicationdate.after(to)) {
                return false
            }
        }
        
        return true
    }
    
    /**
     * Helper method to build application summary
     */
    private Map buildApplicationSummary(RecApplication reca, RecBranch specificBranch, authorityType, df) {
        def appStatus = RecApplicationStatus.findByRecapplication(reca)
        
        return [
            applicationId: reca.id,
            applicaitionid: reca.applicaitionid,
            applicantName: reca.recapplicant?.fullname,
            email: reca.recapplicant?.email,
            mobilenumber: reca.recapplicant?.mobilenumber,
            category: reca.recapplicant?.reccategory?.name,
            applicationdate: reca.applicationdate ? df.format(reca.applicationdate) : null,
            isfeespaid: reca.isfeespaid,
            posts: reca.recpost?.collect { it.designation?.name },
            branches: specificBranch ? [specificBranch.name] : reca.recbranch?.collect { it.name },
            status: appStatus?.recapplicationstatusmaster?.status,
            currentAuthority: authorityType
        ]
    }
    
    
    /**
     * Get detailed application list with complete information
     * Used by: GET /recApplication/getDetailedApplicationList
     */
    def getDetailedApplicationList(hm, request, data) {
        def uid = hm.remove("uid")
        def authorityType = hm.remove("authorityType")
        def recverId = hm.remove("recver")
        def status = hm.remove("status")
        def branchId = hm.remove("recbranch")
        def applicationId = hm.remove("applicationId")
            
            if (!uid) {
                hm.msg = "User not authenticated"
                hm.flag = false
                return
            }
            
            if (!authorityType) {
                hm.msg = "Authority type is required"
                hm.flag = false
                return
            }
            
            // Find instructor
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
            
            // Get recruitment version
            RecVersion recversion
            if (recverId) {
                recversion = RecVersion.findById(recverId)
            } else {
                recversion = RecVersion.findByOrganizationAndIscurrentforbackendprocessing(organization, true)
            }
            
            if (!recversion) {
                hm.msg = "Recruitment version not found"
                hm.flag = false
                return
            }
            
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd")
            
            // Get applications
            def applications = []
            def recapplicationlist
            
            if (applicationId) {
                // Get specific application
                def app = RecApplication.findById(applicationId)
                if (app) {
                    recapplicationlist = [app]
                } else {
                    recapplicationlist = []
                }
            } else {
                recapplicationlist = RecApplication.findAllByRecversionAndOrganizationAndIsfeespaid(
                    recversion, organization, true)
            }
            
            for (RecApplication reca : recapplicationlist) {
                // Apply authority-based filtering
                boolean includeApp = false
                
                switch (authorityType) {
                    case "HOD":
                        for (RecBranch rb : reca.recbranch) {
                            if (rb.program?.department?.hod?.id == instructor.id) {
                                includeApp = true
                                break
                            }
                        }
                        break
                    case "Dean":
                    case "Establishment Section":
                    case "Management":
                        includeApp = true
                        break
                }
                
                if (!includeApp) continue
                
                // Apply filters
                if (status) {
                    def appStatus = RecApplicationStatus.findByRecapplication(reca)
                    if (appStatus?.recapplicationstatusmaster?.status != status) {
                        continue
                    }
                }
                
                if (branchId) {
                    def hasBranch = reca.recbranch?.find { it.id == branchId.toLong() }
                    if (!hasBranch) {
                        continue
                    }
                }
                
                // Build detailed application data
                def applicant = reca.recapplicant
                
                // Calculate age
                def age = 0
                if (applicant.dateofbirth) {
                    def today = new Date()
                    age = today.year - applicant.dateofbirth.year
                }
                
                // Get academics
                def academics = RecApplicantAcademics.findAllByRecapplicant(applicant)
                def academicsList = academics.collect { ac ->
                    [
                        degree: ac.recdegree?.name,
                        name_of_degree: ac.name_of_degree,
                        university: ac.university,
                        yearofpassing: ac.yearofpassing,
                        branch: ac.branch,
                        cpi_marks: ac.cpi_marks
                    ]
                }
                
                // Get experience
                RecExperienceType teachingexptype = RecExperienceType.findByTypeAndIsactive("Teaching", true)
                RecExperienceType industryexptype = RecExperienceType.findByTypeAndIsactive("Industrial/Research", true)
                
                RecExperience teachingexp = RecExperience.findByRecexperiencetypeAndRecapplicant(teachingexptype, applicant)
                RecExperience industryexp = RecExperience.findByRecexperiencetypeAndRecapplicant(industryexptype, applicant)
                
                // Get documents
                def docs = RecApplicantDocument.findAllByRecapplicant(applicant)
                def documentsList = docs.collect { rd ->
                    [
                        id: rd.id,
                        documentType: rd.recdocumenttype?.type,
                        filename: rd.filename,
                        uploaded: true
                    ]
                }
                
                // Get approval status
                def statusList = RecApplicationStatus.findAllByRecapplication(reca)
                def approvalStatusList = statusList.collect { st ->
                    [
                        authority: st.recauthoritytype?.type,
                        branch: st.recbranch?.name,
                        status: st.recapplicationstatusmaster?.status,
                        approvedBy: st.approvedby ? "${st.approvedby.person?.firstName} ${st.approvedby.person?.lastName}".trim() : null,
                        approveDate: st.approve_date ? df.format(st.approve_date) : null,
                        remark: st.remark
                    ]
                }
                
                applications.add([
                    application: [
                        id: reca.id,
                        applicaitionid: reca.applicaitionid,
                        applicationdate: reca.applicationdate ? df.format(reca.applicationdate) : null,
                        place: reca.place,
                        isfeespaid: reca.isfeespaid
                    ],
                    applicant: [
                        id: applicant.id,
                        fullname: applicant.fullname,
                        email: applicant.email,
                        mobilenumber: applicant.mobilenumber,
                        dateofbirth: applicant.dateofbirth ? df.format(applicant.dateofbirth) : null,
                        age: age,
                        category: applicant.reccategory?.name,
                        cast: applicant.cast,
                        pancardno: applicant.pancardno,
                        area_of_specialization: applicant.area_of_specialization
                    ],
                    posts: reca.recpost?.collect { p -> [id: p.id, designation: p.designation?.name] },
                    branches: reca.recbranch?.collect { b -> [id: b.id, name: b.name] },
                    academics: academicsList,
                    experience: [
                        teaching: teachingexp ? [years: teachingexp.no_of_years, months: teachingexp.no_of_months] : null,
                        industry: industryexp ? [years: industryexp.no_of_years, months: industryexp.no_of_months] : null
                    ],
                    documents: documentsList,
                    approvalStatus: approvalStatusList
                ])
            }
            
            hm.applications = applications
            hm.msg = "Detailed applications fetched successfully"
            hm.flag = true
    }
    
    // OLD METHOD: recApplicationSummaryCount (from RecApplicationController)
    /**
     * Get application counts and statistics
     * Used by: GET /recApplication/getApplicationCounts
     */
    def getApplicationCounts(hm, request, data) {
        println("=== getApplicationCounts START ===")
        def uid = hm.remove("uid")
        def authorityType = data?.authorityType
        def recverId = data?.recver
        
        println("uid: ${uid}, authorityType: ${authorityType}, recverId: ${recverId}")
            
            if (!uid) {
                hm.msg = "User not authenticated"
                hm.flag = false
                return
            }
            
            if (!authorityType) {
                hm.msg = "Authority type is required"
                hm.flag = false
                return
            }
            
            // Find instructor
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
            if (!organization) {
                hm.msg = "Organization not found"
                hm.flag = false
                return
            }
            
            println("Found instructor: ${instructor.id}, org: ${organization.id}")
            
            // Get recruitment version
            RecVersion recversion = null
            if (recverId) {
                recversion = RecVersion.findById(recverId as Long)
            } else {
                recversion = RecVersion.findByOrganizationAndIscurrentforbackendprocessing(organization, true)
            }
            
            if (!recversion) {
                hm.msg = "Recruitment version not found"
                hm.flag = false
                return
            }
            
            println("Found recversion: ${recversion.id}")
            
            // Get all applications - use executeQuery to avoid lazy loading issues
            def recapplicationlist = RecApplication.executeQuery(
                "FROM RecApplication WHERE recversion = :rv AND organization = :org AND isfeespaid = true",
                [rv: recversion, org: organization]
            )
            
            println("Found ${recapplicationlist?.size() ?: 0} applications")
            
            if (!recapplicationlist) {
                // Return empty counts
                hm.totalApplications = 0
                hm.byStatus = [inprocess: 0, approved: 0, rejected: 0]
                hm.byBranch = []
                hm.byPost = []
                hm.byCategory = []
                hm.feesPaid = 0
                hm.feesNotPaid = 0
                hm.msg = "No applications found"
                hm.flag = true
                return
            }
            
            // Filter by authority
            def filteredApps = []
            recapplicationlist.each { RecApplication reca ->
                if (authorityType == "HOD") {
                    def branches = reca.recbranch
                    if (branches) {
                        branches.each { RecBranch rb ->
                            if (rb?.program?.department?.hod?.id == instructor.id) {
                                filteredApps.add(reca)
                                return // break from inner each
                            }
                        }
                    }
                } else {
                    filteredApps.add(reca)
                }
            }
            
            println("Filtered to ${filteredApps.size()} applications")
            
            // Count by status
            def byStatus = [inprocess: 0, approved: 0, rejected: 0]
            def byBranch = [:]
            def byPost = [:]
            def byCategory = [:]
            def feesPaid = 0
            def feesNotPaid = 0
            
            filteredApps.each { RecApplication reca ->
                try {
                    // Status count
                    def appStatus = RecApplicationStatus.findByRecapplication(reca)
                    def statusName = appStatus?.recapplicationstatusmaster?.status ?: 'inprocess'
                    byStatus[statusName] = (byStatus[statusName] ?: 0) + 1
                    
                    // Branch count
                    def branches = reca.recbranch
                    if (branches) {
                        branches.each { RecBranch rb ->
                            if (rb) {
                                def branchKey = rb.id.toString()
                                if (!byBranch[branchKey]) {
                                    byBranch[branchKey] = [
                                        branchId: rb.id,
                                        branchName: rb.name,
                                        count: 0,
                                        approved: 0,
                                        rejected: 0,
                                        inprocess: 0
                                    ]
                                }
                                byBranch[branchKey].count++
                                byBranch[branchKey][statusName] = (byBranch[branchKey][statusName] ?: 0) + 1
                            }
                        }
                    }
                    
                    // Post count
                    def posts = reca.recpost
                    if (posts) {
                        posts.each { RecPost rp ->
                            if (rp) {
                                def postKey = rp.id.toString()
                                if (!byPost[postKey]) {
                                    byPost[postKey] = [
                                        postId: rp.id,
                                        designation: rp.designation?.name ?: 'Unknown',
                                        count: 0
                                    ]
                                }
                                byPost[postKey].count++
                            }
                        }
                    }
                    
                    // Category count
                    def categoryName = reca.recapplicant?.reccategory?.name ?: 'Unknown'
                    byCategory[categoryName] = (byCategory[categoryName] ?: 0) + 1
                    
                    // Fees count
                    if (reca.isfeespaid) {
                        feesPaid++
                    } else {
                        feesNotPaid++
                    }
                } catch (Exception e) {
                    println("Error processing application ${reca.id}: ${e.message}")
                }
            }
            
            hm.totalApplications = filteredApps.size()
            hm.byStatus = byStatus
            hm.byBranch = byBranch.values().toList()
            hm.byPost = byPost.values().toList()
            hm.byCategory = byCategory.collect { k, v -> [category: k, count: v] }
            hm.feesPaid = feesPaid
            hm.feesNotPaid = feesNotPaid
            hm.msg = "Application counts fetched successfully"
            hm.flag = true
            
            println("=== getApplicationCounts END ===")
    }
    
    
    // OLD METHOD: applicationform (from RecApplicationController)
    /**
     * Get application data for specific application
     * Used by: GET /recApplication/getApplicationData
     */
    def getApplicationData(hm, request, data) {
        def uid = hm.remove("uid")
        def applicationId = hm.remove("applicationId")
            
            if (!uid) {
                hm.msg = "User not authenticated"
                hm.flag = false
                return
            }
            
            if (!applicationId) {
                hm.msg = "Application ID is required"
                hm.flag = false
                return
            }
            
            RecApplication recapp = RecApplication.findById(applicationId)
            if (!recapp) {
                hm.msg = "Application not found"
                hm.flag = false
                return
            }
            
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd")
            
            // Calculate age
            def age = 0
            if (recapp.recapplicant.dateofbirth) {
                def today = new Date()
                age = today.year - recapp.recapplicant.dateofbirth.year
            }
            
            // Application basic info
            hm.application = [
                id: recapp.id,
                applicaitionid: recapp.applicaitionid,
                applicationdate: recapp.applicationdate ? df.format(recapp.applicationdate) : null,
                place: recapp.place,
                isfeespaid: recapp.isfeespaid,
                feesreceiptid: recapp.feesreceiptid,
                amount: recapp.amount
            ]
            
            // Version info
            hm.version = [
                id: recapp.recversion.id,
                version_number: recapp.recversion.version_number,
                version_date: df.format(recapp.recversion.version_date)
            ]
            
            // Organization info
            hm.organization = [
                id: recapp.organization.id,
                name: recapp.organization.organization_name
            ]
            
            // Applicant info
            hm.applicant = [
                id: recapp.recapplicant.id,
                email: recapp.recapplicant.email,
                fullname: recapp.recapplicant.fullname,
                dateofbirth: recapp.recapplicant.dateofbirth ? df.format(recapp.recapplicant.dateofbirth) : null,
                age: age,
                mobilenumber: recapp.recapplicant.mobilenumber,
                cast: recapp.recapplicant.cast,
                pancardno: recapp.recapplicant.pancardno,
                aadhaarcardno: recapp.recapplicant.aadhaarcardno,
                area_of_specialization: recapp.recapplicant.area_of_specialization,
                any_other_info_related_to_post: recapp.recapplicant.any_other_info_related_to_post,
                present_salary: recapp.recapplicant.present_salary,
                ishandicapped: recapp.recapplicant.ishandicapped,
                category: recapp.recapplicant.reccategory?.name,
                maritalstatus: recapp.recapplicant.maritalstatus?.name,
                salutation: recapp.recapplicant.salutation?.name,
                minority: recapp.recapplicant.minoritytypedetails?.name,
                gender: recapp.recapplicant.gender?.type
            ]
            
            // Posts
            hm.posts = recapp.recpost?.collect { p ->
                [id: p.id, designation: p.designation?.name]
            } ?: []
            
            // Branches
            hm.branches = recapp.recbranch?.collect { b ->
                [id: b.id, name: b.name, branch_abbrivation: b.branch_abbrivation, program: b.program?.name]
            } ?: []
            
            // Addresses
            AddressType permanentAddressType = AddressType.findByType("Permanent")
            AddressType localAddressType = AddressType.findByType("Local")
            def permanentaddress = Address.findByRecapplicantAndAddresstype(recapp.recapplicant, permanentAddressType)
            def localaddress = Address.findByRecapplicantAndAddresstype(recapp.recapplicant, localAddressType)
            
            hm.permanentAddress = permanentaddress ? [
                address: permanentaddress.address,
                taluka: permanentaddress.taluka,
                pin: permanentaddress.pin,
                country: permanentaddress.country?.name,
                state: permanentaddress.state?.state,
                district: permanentaddress.district?.district,
                city: permanentaddress.city?.city
            ] : null
            
            hm.localAddress = localaddress ? [
                address: localaddress.address,
                taluka: localaddress.taluka,
                pin: localaddress.pin,
                country: localaddress.country?.name,
                state: localaddress.state?.state,
                district: localaddress.district?.district,
                city: localaddress.city?.city
            ] : null
            
            // Academic qualifications
            def academics = RecApplicantAcademics.findAllByRecapplicant(recapp.recapplicant)
            hm.academics = academics.collect { ac ->
                [
                    degree: ac.recdegree?.name,
                    name_of_degree: ac.name_of_degree,
                    yearofpassing: ac.yearofpassing,
                    university: ac.university,
                    branch: ac.branch,
                    cpi_marks: ac.cpi_marks,
                    degree_status: ac.recdegreestatus?.name
                ]
            }
            
            // Experience
            RecExperienceType teachingexptype = RecExperienceType.findByTypeAndIsactive("Teaching", true)
            RecExperienceType industryexptype = RecExperienceType.findByTypeAndIsactive("Industrial/Research", true)
            RecExperienceType nonteachingexptype = RecExperienceType.findByTypeAndIsactive("Non-Teaching", true)
            
            RecExperience teachingexp = RecExperience.findByRecexperiencetypeAndRecapplicant(teachingexptype, recapp.recapplicant)
            RecExperience industryexp = RecExperience.findByRecexperiencetypeAndRecapplicant(industryexptype, recapp.recapplicant)
            RecExperience nonteachingexp = RecExperience.findByRecexperiencetypeAndRecapplicant(nonteachingexptype, recapp.recapplicant)
            
            hm.teachingExperience = teachingexp ? [years: teachingexp.no_of_years, months: teachingexp.no_of_months] : null
            hm.industryExperience = industryexp ? [years: industryexp.no_of_years, months: industryexp.no_of_months] : null
            hm.nonTeachingExperience = nonteachingexp ? [years: nonteachingexp.no_of_years, months: nonteachingexp.no_of_months] : null
            
            // Documents
            def docs = RecApplicantDocument.findAllByRecapplicant(recapp.recapplicant)
            hm.documents = docs.collect { rd ->
                [
                    id: rd.id,
                    documentType: rd.recdocumenttype?.type,
                    filename: rd.filename
                ]
            }
            
            // Approval workflow
            def statusList = RecApplicationStatus.findAllByRecapplication(recapp)
            hm.approvalWorkflow = statusList.collect { status ->
                [
                    authority: status.recauthoritytype?.type,
                    branch: status.recbranch?.name,
                    status: status.recapplicationstatusmaster?.status,
                    remark: status.remark,
                    approve_date: status.approve_date ? df.format(status.approve_date) : null,
                    approvedby: status.approvedby ? "${status.approvedby.person?.firstName} ${status.approvedby.person?.lastName}".trim() : null,
                    iscalledforinterview: status.iscalledforinterview
                ]
            }
            
            hm.msg = "Application data fetched successfully"
            hm.flag = true
    }
    
    // OLD METHOD: getRecapplicants (from RecApplicationController)
    /**
     * Get applicants list with filtering
     * Used by: GET /recApplication/getApplicantsList
     */
    def getApplicantsList(hm, request, data) {
        def uid = hm.remove("uid")
        def recverId = hm.remove("recver")
        def category = hm.remove("category")
        def searchText = hm.remove("searchText")
        def page = hm.remove("page") ? hm.remove("page").toInteger() : 1
        def pageSize = hm.remove("pageSize") ? hm.remove("pageSize").toInteger() : 50
            
            if (!uid) {
                hm.msg = "User not authenticated"
                hm.flag = false
                return
            }
            
            // Find instructor
            Login login = Login.findByUsername(uid)
            Instructor instructor = Instructor.findByUid(login.username)
            Organization organization = instructor.organization
            
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd")
            
            // Get applicants
            def applicants = []
            def allApplicants = RecApplicant.list()
            
            for (RecApplicant applicant : allApplicants) {
                // Apply filters
                if (category && applicant.reccategory?.name != category) {
                    continue
                }
                
                if (searchText) {
                    def search = searchText.toLowerCase()
                    if (!applicant.fullname?.toLowerCase()?.contains(search) && 
                        !applicant.email?.toLowerCase()?.contains(search)) {
                        continue
                    }
                }
                
                // Get applications for this applicant
                def apps = RecApplication.findAllByRecapplicant(applicant)
                
                // Filter by version if specified
                if (recverId) {
                    apps = apps.findAll { it.recversion.id == recverId.toLong() }
                }
                
                if (apps.isEmpty()) continue
                
                // Get latest application
                def latestApp = apps.sort { it.applicationdate }.reverse()[0]
                
                applicants.add([
                    id: applicant.id,
                    fullname: applicant.fullname,
                    email: applicant.email,
                    mobilenumber: applicant.mobilenumber,
                    category: applicant.reccategory?.name,
                    applicationCount: apps.size(),
                    latestApplication: [
                        id: latestApp.id,
                        applicaitionid: latestApp.applicaitionid,
                        version: latestApp.recversion.version_number,
                        status: RecApplicationStatus.findByRecapplication(latestApp)?.recapplicationstatusmaster?.status ?: 'inprocess'
                    ]
                ])
            }
            
            def totalCount = applicants.size()
            
            // Apply pagination
            def startIndex = (page - 1) * pageSize
            def endIndex = Math.min(startIndex + pageSize, totalCount)
            def paginatedApplicants = applicants.subList(startIndex, endIndex)
            
            hm.totalCount = totalCount
            hm.page = page
            hm.pageSize = pageSize
            hm.totalPages = Math.ceil(totalCount / pageSize).toInteger()
            hm.applicants = paginatedApplicants
            hm.msg = "Applicants list fetched successfully"
            hm.flag = true
    }
    
    // ═══════════════════════════════════════════════════════════════
    // Phase 5: Admin - Application Approval & Shortlisting APIs
    // ═══════════════════════════════════════════════════════════════
    
    // OLD METHOD: saverecapplicationshortlist (from RecApplicationController)
    /**
     * Process application (approve/reject/shortlist)
     * Used by: POST /recApplication/processApplication
     */
    def processApplication(hm, request, data) {
        def uid = hm.remove("uid")
        def applicationId = data.applicationId
        def branchId = data.branchId
        def authorityTypeId = data.authorityTypeId
        def action = data.action // 'approve', 'reject', 'shortlist'
        def remark = data.remark
            
            if (!uid) {
                hm.msg = "User not authenticated"
                hm.flag = false
                return
            }
            
            if (!applicationId || !authorityTypeId || !action) {
                hm.msg = "Required parameters missing"
                hm.flag = false
                return
            }
            
            // Find instructor
            Login login = Login.findByUsername(uid)
            Instructor instructor = Instructor.findByUid(login.username)
            Organization organization = instructor.organization
            
            // Find application
            RecApplication recapplication = RecApplication.findById(applicationId)
            if (!recapplication) {
                hm.msg = "Application not found"
                hm.flag = false
                return
            }
            
            // Find authority type
            RecAuthorityType recauthoritytype = RecAuthorityType.findById(authorityTypeId)
            if (!recauthoritytype) {
                hm.msg = "Authority type not found"
                hm.flag = false
                return
            }
            
            // Find branch if specified
            RecBranch recbranch = null
            if (branchId) {
                recbranch = RecBranch.findById(branchId)
            }
            
            // Find application status
            RecApplicationStatus recapplicationstatus
            if (recbranch) {
                recapplicationstatus = RecApplicationStatus.findByOrganizationAndRecapplicationAndRecauthoritytypeAndRecbranch(
                    organization, recapplication, recauthoritytype, recbranch)
            } else {
                recapplicationstatus = RecApplicationStatus.findByOrganizationAndRecapplicationAndRecauthoritytype(
                    organization, recapplication, recauthoritytype)
            }
            
            if (!recapplicationstatus) {
                hm.msg = "Application status not found"
                hm.flag = false
                return
            }
            
            // Get status master based on action
            def statusName = action == 'approve' ? 'approved' : (action == 'reject' ? 'rejected' : 'shortlisted')
            RecApplicationStatusMaster statusMaster = RecApplicationStatusMaster.findByStatusAndOrganization(statusName, organization)
            
            if (!statusMaster) {
                hm.msg = "Status master not found for: ${statusName}"
                hm.flag = false
                return
            }
            
            // Update status
            recapplicationstatus.recapplicationstatusmaster = statusMaster
            recapplicationstatus.approvedby = instructor
            recapplicationstatus.approve_date = new Date()
            recapplicationstatus.remark = remark ?: ""
            recapplicationstatus.username = uid
            recapplicationstatus.updation_date = new Date()
            recapplicationstatus.updation_ip_address = request.getRemoteAddr()
            
            if (action == 'shortlist') {
                recapplicationstatus.iscalledforinterview = true
            }
            
            recapplicationstatus.save(failOnError: true, flush: true)
            
            hm.msg = "Application ${action}ed successfully"
            hm.flag = true
            hm.applicationId = recapplication.id
            hm.status = statusName
    }
    
    // OLD METHOD: notifyshortlistcandidateinput (from RecApplicationController)
    /**
     * Notify shortlisted candidates
     * Used by: POST /recApplication/notifyShortlistedCandidates
     */
    def notifyShortlistedCandidates(hm, request, data) {
        def uid = hm.remove("uid")
        def applicationIds = data.applicationIds // Array of application IDs
        def emailSubject = data.emailSubject
        def emailBody = data.emailBody
            
            if (!uid) {
                hm.msg = "User not authenticated"
                hm.flag = false
                return
            }
            
            if (!applicationIds || applicationIds.isEmpty()) {
                hm.msg = "No applications selected"
                hm.flag = false
                return
            }
            
            // Find instructor
            Login login = Login.findByUsername(uid)
            Instructor instructor = Instructor.findByUid(login.username)
            
            def notifiedCount = 0
            def failedCount = 0
            
            for (appId in applicationIds) {
                try {
                    RecApplication recapp = RecApplication.findById(appId)
                    if (!recapp) continue
                    
                    // Get shortlisted status
                    def statusList = RecApplicationStatus.findAllByRecapplication(recapp)
                    def shortlistedStatus = statusList.find { 
                        it.recapplicationstatusmaster?.status == 'shortlisted' || 
                        it.iscalledforinterview == true 
                    }
                    
                    if (shortlistedStatus) {
                        // Mark as mail sent
                        shortlistedStatus.ismailsent = true
                        shortlistedStatus.mailsentdate = new Date()
                        shortlistedStatus.save(failOnError: true, flush: true)
                        
                        // TODO: Send actual email using SendMailService
                        // sendMailService.sendMail(
                        //     to: recapp.recapplicant.email,
                        //     subject: emailSubject,
                        //     body: emailBody
                        // )
                        
                        notifiedCount++
                    }
                } catch (Exception e) {
                    println("Error notifying application ${appId}: ${e.message}")
                    failedCount++
                }
            }
            
            hm.notifiedCount = notifiedCount
            hm.failedCount = failedCount
            hm.msg = "Notification sent to ${notifiedCount} candidates"
            hm.flag = true
    }
    
    // OLD METHOD: rejectaaplication (from RecApplicationController)
    /**
     * Reject application with reason
     * Used by: POST /recApplication/rejectApplication
     */
    def rejectApplication(hm, request, data) {
        def uid = hm.remove("uid")
        def applicationId = data.applicationId
        def branchId = data.branchId
        def authorityTypeId = data.authorityTypeId
        def rejectionReason = data.rejectionReason
            
            if (!uid) {
                hm.msg = "User not authenticated"
                hm.flag = false
                return
            }
            
            if (!applicationId || !rejectionReason) {
                hm.msg = "Application ID and rejection reason are required"
                hm.flag = false
                return
            }
            
            // Find instructor
            Login login = Login.findByUsername(uid)
            Instructor instructor = Instructor.findByUid(login.username)
            Organization organization = instructor.organization
            
            // Find application
            RecApplication recapplication = RecApplication.findById(applicationId)
            if (!recapplication) {
                hm.msg = "Application not found"
                hm.flag = false
                return
            }
            
            // Mark application as rejected
            recapplication.isrejected = true
            recapplication.save(failOnError: true, flush: true)
            
            // Update all status records to rejected
            def statusList = RecApplicationStatus.findAllByRecapplication(recapplication)
            RecApplicationStatusMaster rejectedStatus = RecApplicationStatusMaster.findByStatusAndOrganization('rejected', organization)
            
            for (RecApplicationStatus status : statusList) {
                if (branchId && status.recbranch?.id != branchId.toLong()) {
                    continue
                }
                
                if (authorityTypeId && status.recauthoritytype?.id != authorityTypeId.toLong()) {
                    continue
                }
                
                status.recapplicationstatusmaster = rejectedStatus
                status.remark = rejectionReason
                status.approvedby = instructor
                status.approve_date = new Date()
                status.username = uid
                status.updation_date = new Date()
                status.updation_ip_address = request.getRemoteAddr()
                status.save(failOnError: true, flush: true)
            }
            
            hm.msg = "Application rejected successfully"
            hm.flag = true
            hm.applicationId = recapplication.id
    }
    
    // OLD METHOD: recapplicantattendance (from RecApplicationController)
    /**
     * Get list of shortlisted candidates for attendance management
     * Used by: GET /recApplication/getShortlistedCandidates
     */
    def getShortlistedCandidates(hm, request, data) {
        def uid = hm.remove("uid")
        
        if (!uid) {
            hm.msg = "User not authenticated"
            hm.flag = false
            return
        }
        
        // Find instructor and organization
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
        if (!organization) {
            hm.msg = "Organization not found"
            hm.flag = false
            return
        }
        
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd")
        
        // Get last authority type (usually Management)
        RecAuthorityType recauthoritytype = RecAuthorityType.findByIslastauthorityAndOrganization(true, organization)
        if (!recauthoritytype) {
            hm.msg = "Last authority type not found"
            hm.flag = false
            return
        }
        
        // Get all shortlisted candidates (called for interview)
        def recApplicationStatuslist = RecApplicationStatus.findAllByOrganizationAndRecauthoritytypeAndIscalledforinterview(
            organization, recauthoritytype, true)
        
        // Filter by current recruitment version
        TreeSet recapplicationstatusid = new TreeSet()
        for (RecApplicationStatus recs : recApplicationStatuslist) {
            if (recs.recapplication.recversion.iscurrentforbackendprocessing == true) {
                recapplicationstatusid.add(recs.recapplication.id)
            }
        }
        
        // Get degree types
        def ugacademics = RecDegree.findByNameAndIsactive("B.E. or equivalent", true)
        def pgacademics = RecDegree.findByNameLikeAndIsactive("%M.E%", true)
        def phdacademics = RecDegree.findByNameAndIsactive("Ph.D.", true)
        def otheracademics = RecDegree.findByNameAndIsactive("Any other", true)
        
        // Get experience types
        def experienceteaching = RecExperienceType.findByType("Teaching")
        def experienceindustry = RecExperienceType.findByType("Industrial/Research")
        
        // Build candidate list
        def candidatesList = []
        
        for (appId in recapplicationstatusid) {
            RecApplication recapplication = RecApplication.findById(appId)
            if (!recapplication) continue
            
            RecApplicationStatus recStatus = RecApplicationStatus.findByRecapplication(recapplication)
            if (!recStatus) continue
            
            def applicant = recapplication.recapplicant
            
            // Get academics
            def ugAcademic = RecApplicantAcademics.findByRecapplicantAndRecdegree(applicant, ugacademics)
            def pgAcademic = RecApplicantAcademics.findByRecapplicantAndRecdegree(applicant, pgacademics)
            def phdAcademic = RecApplicantAcademics.findByRecapplicantAndRecdegree(applicant, phdacademics)
            def otherAcademic = RecApplicantAcademics.findByRecapplicantAndRecdegree(applicant, otheracademics)
            
            // Get experience
            def teaching = RecExperience.findByRecapplicantAndRecexperiencetype(applicant, experienceteaching)
            def industry = RecExperience.findByRecapplicantAndRecexperiencetype(applicant, experienceindustry)
            
            // Calculate age
            def age = 0
            if (applicant.dateofbirth) {
                def today = new Date()
                age = today.year - applicant.dateofbirth.year
            }
            
            candidatesList.add([
                applicationStatus: [
                    id: recStatus.id,
                    iscalledforinterview: recStatus.iscalledforinterview,
                    approve_date: recStatus.approve_date ? df.format(recStatus.approve_date) : null,
                    remark: recStatus.remark
                ],
                application: [
                    id: recapplication.id,
                    applicaitionid: recapplication.applicaitionid,
                    applicationdate: recapplication.applicationdate ? df.format(recapplication.applicationdate) : null
                ],
                applicant: [
                    id: applicant.id,
                    fullname: applicant.fullname,
                    email: applicant.email,
                    mobilenumber: applicant.mobilenumber,
                    dateofbirth: applicant.dateofbirth ? df.format(applicant.dateofbirth) : null,
                    age: age,
                    category: applicant.reccategory?.name
                ],
                branches: recapplication.recbranch?.collect { b ->
                    [id: b.id, name: b.name]
                },
                posts: recapplication.recpost?.collect { p ->
                    [id: p.id, designation: p.designation?.name]
                },
                academics: [
                    ug: ugAcademic ? [
                        degree: ugAcademic.recdegree?.name,
                        name_of_degree: ugAcademic.name_of_degree,
                        university: ugAcademic.university,
                        yearofpassing: ugAcademic.yearofpassing,
                        cpi_marks: ugAcademic.cpi_marks
                    ] : null,
                    pg: pgAcademic ? [
                        degree: pgAcademic.recdegree?.name,
                        name_of_degree: pgAcademic.name_of_degree,
                        university: pgAcademic.university,
                        yearofpassing: pgAcademic.yearofpassing,
                        cpi_marks: pgAcademic.cpi_marks
                    ] : null,
                    phd: phdAcademic ? [
                        degree: phdAcademic.recdegree?.name,
                        name_of_degree: phdAcademic.name_of_degree,
                        university: phdAcademic.university,
                        yearofpassing: phdAcademic.yearofpassing,
                        cpi_marks: phdAcademic.cpi_marks
                    ] : null,
                    other: otherAcademic ? [
                        degree: otherAcademic.recdegree?.name,
                        name_of_degree: otherAcademic.name_of_degree,
                        university: otherAcademic.university,
                        yearofpassing: otherAcademic.yearofpassing,
                        cpi_marks: otherAcademic.cpi_marks
                    ] : null
                ],
                experience: [
                    teaching: teaching ? [
                        years: teaching.no_of_years,
                        months: teaching.no_of_months
                    ] : null,
                    industry: industry ? [
                        years: industry.no_of_years,
                        months: industry.no_of_months
                    ] : null
                ]
            ])
        }
        
        hm.candidates = candidatesList
        hm.totalCount = candidatesList.size()
        hm.msg = "Shortlisted candidates fetched successfully"
        hm.flag = true
    }
    
    // OLD METHOD: recSummary (from RecApplicationController)
    /**
     * Get recruitment summary dashboard data with filters
     * Used by: GET /recApplication/getRecruitmentSummary
     */
    def getRecruitmentSummary(hm, request, data) {
        def uid = hm.remove("uid")
        def authorityType = data?.authorityType
        def academicYearId = data?.academicYearId
        
        if (!uid) {
            hm.msg = "User not authenticated"
            hm.flag = false
            return
        }
        
        // Find instructor and organization
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
        if (!organization) {
            hm.msg = "Organization not found"
            hm.flag = false
            return
        }
        
        // Get current recruitment version
        RecVersion recversion = RecVersion.findByOrganizationAndIscurrentforbackendprocessing(organization, true)
        if (!recversion) {
            hm.msg = "Current recruitment version not found"
            hm.flag = false
            return
        }
        
        def recversionlist = RecVersion.findAllByOrganizationAndIscurrentforbackendprocessing(organization, true)
        
        // Get Application Academic Year
        ApplicationType at = ApplicationType.findByApplication_type("ERP")
        RoleType rt = RoleType.findByApplicationtypeAndTypeAndOrganization(at, "Recruitment", organization)
        ApplicationAcademicYear aay = ApplicationAcademicYear.findByRoletypeAndIsActiveAndOrganization(rt, true, organization)
        
        if (!aay) {
            hm.msg = "Application Academic Year Not Set for Recruitment Module"
            hm.flag = false
            return
        }
        
        // Get academic years
        def ay = AcademicYear.list().sort { it.ay }.reverse(true)
        AcademicYear currentAy = AcademicYear.findByAy(aay.academicyear)
        
        if (academicYearId) {
            currentAy = AcademicYear.findById(academicYearId as Long)
        }
        
        // Get authority type and faculty post
        ERPFacultyPost facultypost = null
        RecAuthorityType recAuthorityType = null
        
        if (authorityType) {
            facultypost = ERPFacultyPost.findByNameAndOrganization(authorityType, organization)
            if (facultypost) {
                recAuthorityType = RecAuthorityType.findByErpfacultypostAndOrganization(facultypost, organization)
            }
        }
        
        // Get applications based on authority
        def recapplication = []
        def totalrecapplications = []
        
        if (authorityType == "HOD") {
            // Filter by HOD's department
            def recapplicationlist = RecApplication.findAllByRecversionAndOrganizationAndIsfeespaid(recversion, organization, true)
            
            for (RecApplication reca : recapplicationlist) {
                for (RecBranch rb : reca.recbranch) {
                    if (rb.program?.department?.hod?.id == instructor.id) {
                        recapplication.add(reca)
                        break
                    }
                }
            }
            
            // Total applications (including unpaid)
            def allApps = RecApplication.findAllByRecversionAndOrganization(recversion, organization)
            for (RecApplication reca : allApps) {
                for (RecBranch rb : reca.recbranch) {
                    if (rb.program?.department?.hod?.id == instructor.id) {
                        totalrecapplications.add(reca)
                        break
                    }
                }
            }
        } else {
            // Show all applications
            recapplication.addAll(RecApplication.findAllByRecversionAndOrganizationAndIsfeespaid(recversion, organization, true))
            totalrecapplications.addAll(RecApplication.findAllByRecversionAndOrganization(recversion, organization))
        }
        
        // Build summary array
        def array = []
        array.add([count: totalrecapplications.size(), link: false, label: "Total Applications"])
        array.add([count: recapplication.size(), link: true, label: "Paid Applications"])
        
        // Get application status masters
        def recapplicationstatusmasterlist = RecApplicationStatusMaster.findAllByOrganization(organization)
        
        // Get send interview letter button visibility setting
        def sendinterviewletterbuttonvisible = ERPRoleTypeSettings.findByNameIlikeAndOrganization(
            'Send Interview Call Letter Button Visible', organization)?.value
        
        // Date range
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd")
        def todate = df.format(new Date())
        def fromdate = recversion?.version_date ? df.format(recversion.version_date) : todate
        
        // Get branch list based on authority
        def rec_branch_list = []
        def deptlist = []
        
        if (recAuthorityType) {
            if (recAuthorityType.is_programwise_authority) {
                def deptrolename = ['HOD']
                def instrolename = ['Institute ERP Coordinator']
                
                // Get departments by role
                def dept_list = []
                if (authorityType == "HOD" && instructor.department) {
                    dept_list.add(instructor.department)
                } else {
                    dept_list = Department.findAllByOrganization(organization)
                }
                
                def program_list = Program.findAllByOrganizationAndDepartmentInList(organization, dept_list)
                
                if (recversionlist) {
                    rec_branch_list = RecBranch.findAllByOrganizationAndRecversionInListAndProgramInList(
                        organization, recversionlist, program_list)
                } else {
                    rec_branch_list = RecBranch.findAllByOrganizationAndProgramInList(organization, program_list)
                }
            } else if (recAuthorityType.is_streamwise_authority) {
                def streams = Stream.findAllByDeanAndOrganization(instructor, organization)
                deptlist = Department.createCriteria().list() {
                    'in'('stream', streams)
                    eq('organization', organization)
                }
            }
        }
        
        // Build response
        hm.recAuthorityType = recAuthorityType ? [
            id: recAuthorityType.id,
            type: recAuthorityType.type,
            is_programwise_authority: recAuthorityType.is_programwise_authority,
            is_streamwise_authority: recAuthorityType.is_streamwise_authority
        ] : null
        
        hm.rec_branch_list = rec_branch_list.collect { branch ->
            [
                id: branch.id,
                name: branch.name,
                program: [
                    id: branch.program?.id,
                    name: branch.program?.name
                ]
            ]
        }
        
        hm.deptlist = deptlist.collect { dept ->
            [
                id: dept.id,
                name: dept.name
            ]
        }
        
        hm.fromdate = fromdate
        hm.todate = todate
        hm.recversionlist = recversionlist.collect { rv ->
            [
                id: rv.id,
                version_number: rv.version_number,
                version_date: rv.version_date ? df.format(rv.version_date) : null
            ]
        }
        
        hm.academicYears = ay.collect { academicYear ->
            [
                id: academicYear.id,
                ay: academicYear.ay
            ]
        }
        
        hm.currentAcademicYear = currentAy ? [
            id: currentAy.id,
            ay: currentAy.ay
        ] : null
        
        hm.authorityType = authorityType
        hm.summaryArray = array
        hm.sendinterviewletterbuttonvisible = sendinterviewletterbuttonvisible
        
        hm.recversion = [
            id: recversion.id,
            version_number: recversion.version_number,
            version_date: recversion.version_date ? df.format(recversion.version_date) : null
        ]
        
        hm.recapplicationstatusmasterlist = recapplicationstatusmasterlist.collect { status ->
            [
                id: status.id,
                status: status.status
            ]
        }
        
        hm.msg = "Recruitment summary fetched successfully"
        hm.flag = true
    }
}
    }
    
    // OLD METHOD: recApplicationSummary_management (from RecApplicationController)
    /**
     * Get application summary for Management/Establishment Section with detailed authority status
     * Used by: GET /recApplication/getApplicationSummaryManagement
     */
    def getApplicationSummaryManagement(hm, request, data) {
        def uid = hm.remove("uid")
        def versionId = data?.version
        def statusParam = data?.status
        def fromdate = data?.fromdate
        def todate = data?.todate
        def recAuthorityTypeId = data?.recAuthorityType
        
        if (!uid) {
            hm.msg = "User not authenticated"
            hm.flag = false
            return
        }
        
        // Find instructor
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
        
        // Get recruitment version
        RecVersion recversion = RecVersion.findById(versionId)
        if (!recversion) {
            hm.msg = "Recruitment version not found"
            hm.flag = false
            return
        }
        
        // Get status master
        def recApplicationStatusMaster = []
        if (statusParam == 'All') {
            recApplicationStatusMaster = RecApplicationStatusMaster.findAllByOrganization(organization)
        } else {
            def statusMaster = RecApplicationStatusMaster.findById(statusParam)
            if (statusMaster) {
                recApplicationStatusMaster = [statusMaster]
            }
        }
        
        // Parse dates
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd")
        Date fromdateDate = null
        Date todateDate = null
        
        if (fromdate) {
            fromdateDate = df.parse(fromdate)
        }
        if (todate) {
            todateDate = df.parse(todate)
        }
        
        ArrayList deptlist = new ArrayList()
        ArrayList branchlist = new ArrayList()
        def recapplication = []
        
        RecAuthorityType currentauth = RecAuthorityType.findByIdAndOrganization(recAuthorityTypeId, instructor.organization)
        if (!currentauth) {
            hm.msg = "Authority type not found"
            hm.flag = false
            return
        }
        
        RecAuthorityType hodentry = RecAuthorityType.findByTypeAndOrganization("HOD", instructor.organization)
        
        // Handle programwise authority
        if (currentauth.is_programwise_authority) {
            def recapplicationlist = null
            if (fromdateDate && todateDate && recversion) {
                recapplicationlist = RecApplication.createCriteria().list() {
                    eq("recversion", recversion)
                    eq("organization", organization)
                    eq("isfeespaid", true)
                    between("applicationdate", fromdateDate, todateDate + 1)
                }
            }
            
            def recapplicationstatushod = null
            RecAuthorityType hod = RecAuthorityType.findByTypeAndOrganization("HOD", instructor.organization)
            
            if (recapplicationlist) {
                def recbranch = RecBranch.findByOrganizationAndRecversionAndProgram(organization, recversion, instructor?.program)
                
                if (recbranch) {
                    recapplicationstatushod = RecApplicationStatus.createCriteria().list() {
                        eq("organization", organization)
                        eq("recbranch", recbranch)
                        eq("recauthoritytype", hod)
                        'in'("recapplicationstatusmaster", recApplicationStatusMaster)
                        'in'("recapplication", recapplicationlist)
                    }
                }
            }
            
            recapplicationstatushod?.each { status ->
                RecApplication reca = status.recapplication
                reca.recbranch?.each { RecBranch rb ->
                    if (rb.program?.department?.hod?.id == instructor.id) {
                        deptlist.add(rb.program.department)
                        if (!recapplication.contains(reca)) {
                            recapplication.add(reca)
                        }
                        return // break
                    }
                }
            }
        }
        
        // Handle streamwise authority
        if (currentauth.is_streamwise_authority) {
            def recapplicationlist = null
            if (fromdateDate && todateDate && recversion) {
                recapplicationlist = RecApplication.createCriteria().list() {
                    eq("recversion", recversion)
                    eq("organization", organization)
                    eq("isfeespaid", true)
                    between("applicationdate", fromdateDate, todateDate + 1)
                }
            }
            
            def recapplicationstatushod = null
            RecAuthorityType hod = RecAuthorityType.findByTypeAndOrganization("HOD", instructor.organization)
            
            if (recapplicationlist) {
                def recbranch = RecBranch.findByOrganizationAndRecversionAndProgram(organization, recversion, instructor?.program)
                
                if (recbranch) {
                    recapplicationstatushod = RecApplicationStatus.createCriteria().list() {
                        eq("organization", organization)
                        eq("recbranch", recbranch)
                        eq("recauthoritytype", currentauth)
                        'in'("recapplicationstatusmaster", recApplicationStatusMaster)
                        'in'("recapplication", recapplicationlist)
                    }
                }
            }
            
            recapplicationstatushod?.each { status ->
                RecApplication reca = status.recapplication
                reca.recbranch?.each { RecBranch rb ->
                    if (rb.program?.department?.hod?.id == instructor.id) {
                        deptlist.add(rb.program.department)
                        if (!recapplication.contains(reca)) {
                            recapplication.add(reca)
                        }
                        return // break
                    }
                }
            }
        }
        // Handle Management authority
        else if (currentauth.type == "Management") {
            def departmentlist = Department.findAllByOrganization(organization)
            def recapplicationlist = null
            
            if (fromdateDate && todateDate && recversion) {
                recapplicationlist = RecApplication.createCriteria().list() {
                    eq("recversion", recversion)
                    eq("organization", organization)
                    eq("isfeespaid", true)
                    between("applicationdate", fromdateDate, todateDate + 1)
                }
            }
            
            def recapplicationstatusmanagement = null
            RecAuthorityType management = RecAuthorityType.findByTypeAndOrganization("Management", instructor.organization)
            
            if (recapplicationlist) {
                recapplicationstatusmanagement = RecApplicationStatus.createCriteria().list() {
                    eq("organization", organization)
                    eq("recauthoritytype", management)
                    'in'("recapplicationstatusmaster", recApplicationStatusMaster)
                    'in'("recapplication", recapplicationlist)
                }
            }
            
            recapplicationstatusmanagement?.each { status ->
                RecApplication reca = status.recapplication
                if (status.recbranch?.program?.department?.id) {
                    deptlist.add(status.recbranch.program.department)
                    branchlist.add(status.recbranch.name)
                }
                if (!recapplication.contains(reca)) {
                    recapplication.add(reca)
                }
            }
        }
        // Handle Establishment Section or other authorities
        else {
            def recapplicationlist = null
            if (fromdateDate && todateDate && recversion) {
                recapplicationlist = RecApplication.createCriteria().list() {
                    eq("recversion", recversion)
                    eq("organization", organization)
                    eq("isfeespaid", true)
                    between("applicationdate", fromdateDate, todateDate + 1)
                }
            }
            
            def recapplicationstatusest = null
            RecAuthorityType est = RecAuthorityType.findByTypeAndOrganization("Establishment Section", instructor.organization)
            
            if (recapplicationlist) {
                recapplicationstatusest = RecApplicationStatus.createCriteria().list() {
                    eq("organization", organization)
                    eq("recauthoritytype", currentauth)
                    'in'("recapplicationstatusmaster", recApplicationStatusMaster)
                    'in'("recapplication", recapplicationlist)
                }
            }
            
            if (recapplicationstatusest) {
                recapplication.addAll(recapplicationstatusest*.recapplication)
            }
            
            recapplicationstatusest?.each { status ->
                if (status.recbranch?.program?.department?.id) {
                    deptlist.add(status.recbranch.program.department)
                    branchlist.add(status.recbranch.name)
                }
            }
        }
        
        // Build detailed response with authority status lists
        RecAuthorityType recauthoritytype = RecAuthorityType.findByTypeAndOrganization(currentauth.type, instructor.organization)
        ArrayList checkedlist = new ArrayList()
        ArrayList remarklist = new ArrayList()
        ArrayList authoritystatuslisthod = new ArrayList()
        ArrayList authoritystatuslistregistrar = new ArrayList()
        ArrayList authoritystatuslistManagement = new ArrayList()
        ArrayList localaddress = new ArrayList()
        ArrayList permanentaddress = new ArrayList()
        
        int i = 0
        int approvecount = 0
        RecAuthorityType management = RecAuthorityType.findByTypeAndOrganization("Management", instructor.organization)
        def programlist = Program.findAllByOrganization(instructor.organization)
        
        for (RecApplication r : recapplication) {
            // Get addresses
            AddressType at = AddressType.findByType("Permanent")
            def permanent = Address.findByRecapplicantAndAddresstype(r?.recapplicant, at)
            permanentaddress.add(permanent)
            
            at = AddressType.findByType("Local")
            def local = Address.findByRecapplicantAndAddresstype(r?.recapplicant, at)
            localaddress.add(local)
            
            RecBranch recbranch = null
            RecAuthorityType hod = RecAuthorityType.findByTypeAndOrganization("HOD", instructor.organization)
            RecAuthorityType registrar = RecAuthorityType.findByTypeAndOrganization("Establishment Section", instructor.organization)
            
            // For HOD
            Department dept = deptlist[i]
            RecApplicationStatus recapplicationstatushod = null
            
            for (RecBranch rb : r.recbranch) {
                if (rb?.program?.department?.id == dept?.id) {
                    recapplicationstatushod = RecApplicationStatus.findByOrganizationAndRecapplicationAndRecauthoritytypeAndRecbranch(
                        organization, r, hod, rb)
                    if (recapplicationstatushod != null) {
                        recbranch = rb
                        break
                    }
                }
            }
            
            if (recapplicationstatushod == null) {
                for (Program pg : programlist) {
                    if (pg?.department?.id == dept?.id) {
                        RecBranch rcb = RecBranch.findByOrganizationAndIsactiveAndRecversionAndProgram(
                            organization, true, recversion, pg)
                        recapplicationstatushod = RecApplicationStatus.findByOrganizationAndRecapplicationAndRecauthoritytypeAndRecbranch(
                            organization, r, hod, rcb)
                        if (recapplicationstatushod != null) {
                            recbranch = rcb
                            break
                        }
                    }
                }
            }
            authoritystatuslisthod.add(recapplicationstatushod)
            
            // For Registrar
            RecApplicationStatus recapplicationstatusregistrar = RecApplicationStatus.findByOrganizationAndRecapplicationAndRecauthoritytypeAndRecbranch(
                organization, r, registrar, recbranch)
            authoritystatuslistregistrar.add(recapplicationstatusregistrar)
            
            // For current authority
            RecApplicationStatus recapplicationstatus = RecApplicationStatus.findByOrganizationAndRecapplicationAndRecauthoritytypeAndRecbranch(
                organization, r, recauthoritytype, recbranch)
            
            if (recapplicationstatus == null) {
                if (authoritystatuslisthod[i]?.iscalledforinterview == true && 
                    authoritystatuslistregistrar[i]?.iscalledforinterview == true) {
                    checkedlist.add("true")
                } else {
                    checkedlist.add("false")
                }
                remarklist.add("")
            } else {
                if (recapplicationstatus.iscalledforinterview == true) {
                    checkedlist.add("true")
                } else if (recapplicationstatus.iscalledforinterview == false) {
                    checkedlist.add("false")
                }
                remarklist.add(recapplicationstatus.remark)
            }
            
            if (recapplicationstatus?.approvedby != null) {
                approvecount++
            }
            
            // For Management
            def recapplicationstatusManagement = RecApplicationStatus.findByOrganizationAndRecapplicationAndRecauthoritytypeAndRecbranch(
                organization, r, management, recbranch)
            
            if (currentauth.type == "Management") {
                recapplicationstatusManagement = RecApplicationStatus.findByOrganizationAndRecapplicationAndRecauthoritytype(
                    organization, r, recauthoritytype)
            }
            authoritystatuslistManagement.add(recapplicationstatusManagement)
            
            i++
        }
        
        boolean isapproved = false
        if (i == approvecount) {
            isapproved = true
        }
        
        def sendinterviewletterbuttonvisible = ERPRoleTypeSettings.findByNameIlikeAndOrganization(
            'Send Interview Call Letter Button Visible', organization)?.value
        
        // Build response
        hm.authoritystatuslistManagement = authoritystatuslistManagement.collect { status ->
            status ? [
                id: status.id,
                status: status.recapplicationstatusmaster?.status,
                iscalledforinterview: status.iscalledforinterview,
                remark: status.remark,
                approvedBy: status.approvedby ? "${status.approvedby.person?.firstName} ${status.approvedby.person?.lastName}".trim() : null
            ] : null
        }
        
        hm.deptlist = deptlist.collect { dept ->
            [
                id: dept.id,
                name: dept.name
            ]
        }
        
        hm.branchlist = branchlist
        hm.isapproved = isapproved
        
        hm.recapplication = recapplication.collect { app ->
            [
                id: app.id,
                applicaitionid: app.applicaitionid,
                applicantName: app.recapplicant?.fullname,
                email: app.recapplicant?.email,
                mobilenumber: app.recapplicant?.mobilenumber,
                category: app.recapplicant?.reccategory?.name
            ]
        }
        
        hm.checkedlist = checkedlist
        hm.remarklist = remarklist
        
        hm.authoritystatuslisthod = authoritystatuslisthod.collect { status ->
            status ? [
                id: status.id,
                status: status.recapplicationstatusmaster?.status,
                iscalledforinterview: status.iscalledforinterview,
                remark: status.remark
            ] : null
        }
        
        hm.authoritystatuslistregistrar = authoritystatuslistregistrar.collect { status ->
            status ? [
                id: status.id,
                status: status.recapplicationstatusmaster?.status,
                iscalledforinterview: status.iscalledforinterview,
                remark: status.remark
            ] : null
        }
        
        hm.permanentaddress = permanentaddress.collect { addr ->
            addr ? [
                id: addr.id,
                add: addr.add,
                taluka: addr.taluka,
                pin: addr.pin,
                country: addr.country?.name,
                state: addr.state?.name,
                dist: addr.dist?.name,
                city: addr.city?.name
            ] : null
        }
        
        hm.localaddress = localaddress.collect { addr ->
            addr ? [
                id: addr.id,
                add: addr.add,
                taluka: addr.taluka,
                pin: addr.pin,
                country: addr.country?.name,
                state: addr.state?.name,
                dist: addr.dist?.name,
                city: addr.city?.name
            ] : null
        }
        
        hm.sendinterviewletterbuttonvisible = sendinterviewletterbuttonvisible
        hm.recversion = versionId
        hm.status = statusParam
        hm.fromdate = fromdate
        hm.todate = todate
        hm.recAuthorityType = recAuthorityTypeId
        
        hm.msg = "Application summary for management fetched successfully"
        hm.flag = true
    }
}
