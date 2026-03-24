package recruitment

import grails.gorm.transactions.Transactional
import java.text.SimpleDateFormat

@Transactional
class RecApplicationService_2 {
    
    // ═══════════════════════════════════════════════════════════════
    // Phase 4: Admin - Application Listing & Filtering APIs
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Get authority applications - detect user's authority roles
     * Used by: GET /recApplication/getAuthorityApplications
     */
    def getAuthorityApplications(hm, request) {
        try {
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
            
        } catch (Exception e) {
            println("Error in getAuthorityApplications: ${e.message}")
            e.printStackTrace()
            hm.msg = "Error fetching authority list: ${e.message}"
            hm.flag = false
        }
    }
    
    /**
     * Get application summary with filters
     * Used by: GET /recApplication/getApplicationSummary
     */
    def getApplicationSummary(hm, request) {
        try {
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
            
        } catch (Exception e) {
            println("Error in getApplicationSummary: ${e.message}")
            e.printStackTrace()
            hm.msg = "Error fetching applications: ${e.message}"
            hm.flag = false
        }
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
    def getDetailedApplicationList(hm, request) {
        try {
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
                        teaching: teachingexp ? [years: teachingexp.years, months: teachingexp.months] : null,
                        industry: industryexp ? [years: industryexp.years, months: industryexp.months] : null
                    ],
                    documents: documentsList,
                    approvalStatus: approvalStatusList
                ])
            }
            
            hm.applications = applications
            hm.msg = "Detailed applications fetched successfully"
            hm.flag = true
            
        } catch (Exception e) {
            println("Error in getDetailedApplicationList: ${e.message}")
            e.printStackTrace()
            hm.msg = "Error fetching detailed applications: ${e.message}"
            hm.flag = false
        }
    }
    
    /**
     * Get application counts and statistics
     * Used by: GET /recApplication/getApplicationCounts
     */
    def getApplicationCounts(hm, request) {
        try {
            def uid = hm.remove("uid")
            def authorityType = hm.remove("authorityType")
            def recverId = hm.remove("recver")
            
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
            Instructor instructor = Instructor.findByUid(login.username)
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
            
            // Get all applications
            def recapplicationlist = RecApplication.findAllByRecversionAndOrganizationAndIsfeespaid(
                recversion, organization, true)
            
            // Filter by authority
            def filteredApps = []
            for (RecApplication reca : recapplicationlist) {
                if (authorityType == "HOD") {
                    for (RecBranch rb : reca.recbranch) {
                        if (rb.program?.department?.hod?.id == instructor.id) {
                            filteredApps.add(reca)
                            break
                        }
                    }
                } else {
                    filteredApps.add(reca)
                }
            }
            
            // Count by status
            def byStatus = [inprocess: 0, approved: 0, rejected: 0]
            def byBranch = [:]
            def byPost = [:]
            def byCategory = [:]
            def feesPaid = 0
            def feesNotPaid = 0
            
            for (RecApplication reca : filteredApps) {
                // Status count
                def appStatus = RecApplicationStatus.findByRecapplication(reca)
                def statusName = appStatus?.recapplicationstatusmaster?.status ?: 'inprocess'
                byStatus[statusName] = (byStatus[statusName] ?: 0) + 1
                
                // Branch count
                for (RecBranch rb : reca.recbranch) {
                    def branchKey = rb.id
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
                    byBranch[branchKey][statusName]++
                }
                
                // Post count
                for (RecPost rp : reca.recpost) {
                    def postKey = rp.id
                    if (!byPost[postKey]) {
                        byPost[postKey] = [
                            postId: rp.id,
                            designation: rp.designation?.name,
                            count: 0
                        ]
                    }
                    byPost[postKey].count++
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
            
        } catch (Exception e) {
            println("Error in getApplicationCounts: ${e.message}")
            e.printStackTrace()
            hm.msg = "Error fetching application counts: ${e.message}"
            hm.flag = false
        }
    }
    
    /**
     * Get application data for specific application
     * Used by: GET /recApplication/getApplicationData
     */
    def getApplicationData(hm, request) {
        try {
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
            
            hm.teachingExperience = teachingexp ? [years: teachingexp.years, months: teachingexp.months] : null
            hm.industryExperience = industryexp ? [years: industryexp.years, months: industryexp.months] : null
            hm.nonTeachingExperience = nonteachingexp ? [years: nonteachingexp.years, months: nonteachingexp.months] : null
            
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
            
        } catch (Exception e) {
            println("Error in getApplicationData: ${e.message}")
            e.printStackTrace()
            hm.msg = "Error fetching application data: ${e.message}"
            hm.flag = false
        }
    }
    
    /**
     * Get applicants list with filtering
     * Used by: GET /recApplication/getApplicantsList
     */
    def getApplicantsList(hm, request) {
        try {
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
            
        } catch (Exception e) {
            println("Error in getApplicantsList: ${e.message}")
            e.printStackTrace()
            hm.msg = "Error fetching applicants list: ${e.message}"
            hm.flag = false
        }
    }
}
