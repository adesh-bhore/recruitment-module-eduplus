package recruitment

import grails.gorm.transactions.Transactional
import java.text.SimpleDateFormat
import javax.servlet.http.Part
import common.AWSUploadDocumentsService

@Transactional
class RecApplicationService_1 {
    
    /**
     * Get active recruitment versions (within date range)
     * Used by: GET /recApplication/getActiveRecruitments
     */
    def getActiveRecruitments(hm, request) {
        try {
            def uid = hm.remove("uid")
            
            if (!uid) {
                hm.msg = "User not found"
                hm.flag = false
                return
            }
            
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd")
            Date todate = new Date()
            
            // Get all current versions
            def recver = RecVersion.findAllByIscurrent(true)
            def versionlist = []
            
            // Filter by date range
            for (ver in recver) {
                if (todate.before(ver?.to_date) && todate.after(ver?.from_date)) {
                    versionlist.add([
                        id: ver.id,
                        version_number: ver.version_number,
                        version_date: sdf.format(ver.version_date),
                        from_date: sdf.format(ver.from_date),
                        to_date: sdf.format(ver.to_date),
                        organization: [
                            id: ver.organization.id,
                            name: ver.organization.organization_name
                        ]
                    ])
                }
            }
            
            // Get applicant's submitted applications
            def applicant = RecApplicant.findByEmail(uid.trim())
            def submittedApplications = []
            
            if (applicant) {
                for (ver in RecVersion.findAllByIscurrentforbackendprocessing(true)) {
                    def app = RecApplication.findByRecversionAndRecapplicantAndIsfeespaid(ver, applicant, true)
                    if (app != null) {
                        submittedApplications.add([
                            id: app.id,
                            applicaitionid: app.applicaitionid,
                            version_id: ver.id,
                            version_number: ver.version_number,
                            isfeespaid: app.isfeespaid
                        ])
                    }
                }
            }
            
            hm.activeRecruitments = versionlist
            hm.submittedApplications = submittedApplications
            hm.msg = "Active recruitments fetched successfully"
            hm.flag = true
            
        } catch (Exception e) {
            println("Error in getActiveRecruitments: ${e.message}")
            hm.msg = "Error fetching active recruitments: ${e.message}"
            hm.flag = false
        }
    }
    
    /**
     * Get all form data needed for application submission
     * Used by: GET /recApplication/getApplicationFormData
     */
    def getApplicationFormData(hm, request) {
        try {
            def uid = hm.remove("uid")
            def recverId = hm.remove("recver")
            
            if (!uid || !recverId) {
                hm.msg = "User or recruitment version not found"
                hm.flag = false
                return
            }
            
            RecVersion recver = RecVersion.findById(recverId)
            if (!recver) {
                hm.msg = "Recruitment version not found"
                hm.flag = false
                return
            }
            
            // Get applicant details
            RecApplicant recapplicant = RecApplicant.findByEmail(uid)
            
            // Get master data
            def post = RecPost.findAllByRecversionAndIsactiveAndOrganization(recver, true, recver.organization)
            def branch = RecBranch.findAllByOrganizationAndIsactiveAndRecversion(recver.organization, true, recver)
            def category = RecCategory.findAllByIsactive(true)
            def degree = RecDegree.findAllByIsactive(true)
            def recclass = RecClass.findAllByOrganization(recver.organization)
            def maritalstatus = MaritalStatus.findAllByOrganization(recver.organization)
            def genderlist = Gender.findAll()
            def minoritytypelist = MinorityTypeDetails.findAllByOrganization(recver.organization)
            def salutationlist = Salutation.findAllByOrganizationAndIsactive(recver.organization, true)
            def recexperiencelist = RecExperienceType.findAllByOrganizationAndIsactive(recver.organization, true)
            def recdegreestatus = RecDegreeStatus.findAllByOrganizationAndIsactive(recver.organization, true)
            def recdegreename = RecDegreeName.findAllByOrganizationAndIsactive(recver.organization, true)
            
            // Get states, districts, cities, countries
            def state = ERPState.list()
            def district = ERPDistrict.list()
            def country = ERPCountry.list()
            def city = ERPCity.list()
            
            // Get existing application if any
            RecApplication recapplication = RecApplication.findByOrganizationAndRecapplicantAndRecversion(recver.organization, recapplicant, recver)
            
            // Get selected posts and branches
            def selectedPosts = []
            def selectedBranches = []
            if (recapplication) {
                selectedPosts = recapplication.recpost?.collect { it.id } ?: []
                selectedBranches = recapplication.recbranch?.collect { it.id } ?: []
            }
            
            // Get addresses
            AddressType localAddressType = AddressType.findByType("Local")
            AddressType permanentAddressType = AddressType.findByType("Permanent")
            def localaddress = recapplicant ? Address.findByRecapplicantAndAddresstype(recapplicant, localAddressType) : null
            def peraddress = recapplicant ? Address.findByRecapplicantAndAddresstype(recapplicant, permanentAddressType) : null
            
            // Get academic qualifications
            def recapplicantacademicslist = []
            for (RecDegree rd : degree) {
                RecApplicantAcademics recapplicantacademics = RecApplicantAcademics.findByRecapplicantAndRecdegree(recapplicant, rd)
                if (recapplicantacademics) {
                    recapplicantacademicslist.add([
                        id: recapplicantacademics.id,
                        degree_id: rd.id,
                        degree_name: rd.name,
                        name_of_degree: recapplicantacademics.name_of_degree,
                        yearofpassing: recapplicantacademics.yearofpassing,
                        university: recapplicantacademics.university,
                        branch: recapplicantacademics.branch,
                        cpi_marks: recapplicantacademics.cpi_marks
                    ])
                }
            }
            
            // Get experience details
            RecExperienceType teachingexptype = RecExperienceType.findByTypeAndIsactive("Teaching", true)
            RecExperienceType industryexptype = RecExperienceType.findByTypeAndIsactive("Industrial/Research", true)
            RecExperienceType nonteachingexptype = RecExperienceType.findByTypeAndIsactive("Non-Teaching", true)
            
            RecExperience applicantteachingexp = RecExperience.findByRecexperiencetypeAndRecapplicant(teachingexptype, recapplicant)
            RecExperience applicantindustryexp = RecExperience.findByRecexperiencetypeAndRecapplicant(industryexptype, recapplicant)
            RecExperience applicantnonteachingexp = RecExperience.findByRecexperiencetypeAndRecapplicant(nonteachingexptype, recapplicant)
            
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd")
            
            // Build response
            hm.recversion = [
                id: recver.id,
                version_number: recver.version_number,
                version_date: df.format(recver.version_date),
                from_date: df.format(recver.from_date),
                to_date: df.format(recver.to_date),
                feesamountcategory: recver.feesamountcategory
            ]
            
            hm.posts = post.collect { p -> [id: p.id, designation: p.designation?.name] }
            hm.branches = branch.collect { b -> [id: b.id, name: b.name, branch_abbrivation: b.branch_abbrivation, program: b.program?.name] }
            hm.categories = category.collect { c -> [id: c.id, name: c.name] }
            hm.degrees = degree.collect { d -> [id: d.id, name: d.name] }
            hm.classes = recclass.collect { c -> [id: c.id, name: c.name] }
            hm.maritalStatusList = maritalstatus.collect { m -> [id: m.id, name: m.name] }
            hm.genderList = genderlist.collect { g -> [id: g.id, name: g.type] }
            hm.minorityList = minoritytypelist.collect { m -> [id: m.id, name: m.name] }
            hm.salutationList = salutationlist.collect { s -> [id: s.id, name: s.name] }
            hm.experienceTypeList = recexperiencelist.collect { e -> [id: e.id, type: e.type] }
            hm.degreeStatusList = recdegreestatus.collect { d -> [id: d.id, name: d.name] }
            hm.degreeNameList = recdegreename.collect { d -> [id: d.id, name: d.name] }
            
            hm.states = state.collect { s -> [id: s.id, name: s.state] }
            hm.districts = district.collect { d -> [id: d.id, name: d.district] }
            hm.cities = city.collect { c -> [id: c.id, name: c.city] }
            hm.countries = country.collect { c -> [id: c.id, name: c.name] }
            
            // Applicant data
            if (recapplicant) {
                hm.applicant = [
                    id: recapplicant.id,
                    email: recapplicant.email,
                    fullname: recapplicant.fullname,
                    cast: recapplicant.cast,
                    pancardno: recapplicant.pancardno,
                    aadhaarcardno: recapplicant.aadhaarcardno,
                    dateofbirth: recapplicant.dateofbirth ? df.format(recapplicant.dateofbirth) : null,
                    mobilenumber: recapplicant.mobilenumber,
                    area_of_specialization: recapplicant.area_of_specialization,
                    any_other_info_related_to_post: recapplicant.any_other_info_related_to_post,
                    present_salary: recapplicant.present_salary,
                    ishandicapped: recapplicant.ishandicapped,
                    category_id: recapplicant.reccategory?.id,
                    maritalstatus_id: recapplicant.maritalstatus?.id,
                    salutation_id: recapplicant.salutation?.id,
                    minority_id: recapplicant.minoritytypedetails?.id,
                    gender_id: recapplicant.gender?.id
                ]
            } else {
                hm.applicant = null
            }
            
            // Application data
            if (recapplication) {
                hm.application = [
                    id: recapplication.id,
                    applicaitionid: recapplication.applicaitionid,
                    place: recapplication.place,
                    isfeespaid: recapplication.isfeespaid,
                    selectedPosts: selectedPosts,
                    selectedBranches: selectedBranches
                ]
            } else {
                hm.application = null
            }
            
            // Address data
            hm.localAddress = localaddress ? [
                id: localaddress.id,
                address: localaddress.address,
                taluka: localaddress.taluka,
                pin: localaddress.pin,
                country_id: localaddress.country?.id,
                state_id: localaddress.state?.id,
                district_id: localaddress.district?.id,
                city_id: localaddress.city?.id
            ] : null
            
            hm.permanentAddress = peraddress ? [
                id: peraddress.id,
                address: peraddress.address,
                taluka: peraddress.taluka,
                pin: peraddress.pin,
                country_id: peraddress.country?.id,
                state_id: peraddress.state?.id,
                district_id: peraddress.district?.id,
                city_id: peraddress.city?.id
            ] : null
            
            // Academic qualifications
            hm.academics = recapplicantacademicslist
            
            // Experience data
            hm.teachingExperience = applicantteachingexp ? [
                id: applicantteachingexp.id,
                years: applicantteachingexp.years,
                months: applicantteachingexp.months
            ] : null
            
            hm.industryExperience = applicantindustryexp ? [
                id: applicantindustryexp.id,
                years: applicantindustryexp.years,
                months: applicantindustryexp.months
            ] : null
            
            hm.nonTeachingExperience = applicantnonteachingexp ? [
                id: applicantnonteachingexp.id,
                years: applicantnonteachingexp.years,
                months: applicantnonteachingexp.months
            ] : null
            
            hm.msg = "Form data fetched successfully"
            hm.flag = true
            
        } catch (Exception e) {
            println("Error in getApplicationFormData: ${e.message}")
            hm.msg = "Error fetching form data: ${e.message}"
            hm.flag = false
        }
    }
    
    /**
     * Submit or update application
     * Used by: POST /recApplication/submitApplication
     */
    def submitApplication(hm, request, data) {
        try {
            def uid = hm.remove("uid")
            def recverId = data.recver
            
            if (!uid || !recverId) {
                hm.msg = "User or recruitment version not found"
                hm.flag = false
                return
            }
            
            // Validate required fields
            if (!data.selectedPostId) {
                hm.msg = "Please select at least one post"
                hm.flag = false
                return
            }
            
            if (!data.branch || (data.branch instanceof List && data.branch.isEmpty())) {
                hm.msg = "Please select at least one branch"
                hm.flag = false
                return
            }
            
            RecVersion recver = RecVersion.findById(recverId)
            if (!recver) {
                hm.msg = "Recruitment version not found"
                hm.flag = false
                return
            }
            
            // Parse selected posts
            ArrayList posts = []
            data.selectedPostId?.split(",").each { postId ->
                def post = RecPost.findById(postId.toLong())
                if (post) posts.add(post)
            }
            
            // Parse selected branches
            ArrayList branches = []
            if (data.branch instanceof List) {
                data.branch.each { branchId ->
                    branches.add(RecBranch.findById(branchId))
                }
            } else {
                branches.add(RecBranch.findById(data.branch))
            }

            
            // Get or create applicant
            RecApplicant recapplicant = RecApplicant.findByEmail(uid)
            
            // Parse master data with validation
            RecCategory reccategory = data.category ? RecCategory.findById(data.category) : null
            if (!reccategory) {
                println("ERROR: RecCategory not found for ID: ${data.category}")
                hm.msg = "Invalid category selected. Category ID ${data.category} not found."
                hm.flag = false
                return
            }
            
            MaritalStatus maritalStatus = data.maritalstatus ? MaritalStatus.findById(data.maritalstatus) : null
            MinorityTypeDetails minority = (data.minority && data.minority != 'null') ? MinorityTypeDetails.findById(data.minority) : null
            Gender gender = (data.gender && data.gender != 'null') ? Gender.findById(data.gender) : null
            Salutation salutation = data.salutation ? Salutation.findById(data.salutation) : null
            Date dateofbirth = data.dateofbirth ? new SimpleDateFormat("yyyy-MM-dd").parse(data.dateofbirth) : null
            
            // Save or update applicant
            if (recapplicant == null) {
                recapplicant = new RecApplicant()
                recapplicant.email = uid
                recapplicant.creation_date = new Date()
                recapplicant.creation_ip_address = request.getRemoteAddr()
            }
            
            recapplicant.fullname = data.fullname?.toString()?.toUpperCase()
            recapplicant.cast = data.cast
            recapplicant.pancardno = data.pancardno
            recapplicant.aadhaarcardno = data.aadharcardno
            recapplicant.dateofbirth = dateofbirth
            recapplicant.mobilenumber = data.mobilenumber
            recapplicant.area_of_specialization = data.area_of_specialization
            recapplicant.any_other_info_related_to_post = data.any_other_info_related_to_post
            recapplicant.present_salary = data.present_salary
            recapplicant.username = uid
            recapplicant.updation_date = new Date()
            recapplicant.updation_ip_address = request.getRemoteAddr()
            recapplicant.reccategory = reccategory
            recapplicant.maritalstatus = maritalStatus
            recapplicant.salutation = salutation
            recapplicant.minoritytypedetails = minority
            recapplicant.gender = gender
            recapplicant.ishandicapped = data.ishandicapped ? true : false
            recapplicant.save(failOnError: true, flush: true)
            
            // Save permanent address
            AddressType permanentAddressType = AddressType.findByType("Permanent")
            def peraddress = Address.findByRecapplicantAndAddresstype(recapplicant, permanentAddressType)
            
            if (!peraddress) {
                peraddress = new Address()
                peraddress.recapplicant = recapplicant
                peraddress.organization = recver.organization
                peraddress.addresstype = permanentAddressType
                peraddress.creation_date = new Date()
                peraddress.creation_ip_address = request.getRemoteAddr()
            }
            
            peraddress.username = uid
            peraddress.updation_date = new Date()
            peraddress.updation_ip_address = request.getRemoteAddr()
            peraddress.address = data.padd
            peraddress.taluka = data.ptaluka
            peraddress.pin = data.ppin
            peraddress.country = data.pcountry ? ERPCountry.findById(data.pcountry) : null
            peraddress.state = data.pstate ? ERPState.findById(data.pstate) : null
            peraddress.district = data.pdist ? ERPDistrict.findById(data.pdist) : null
            peraddress.city = data.pcity ? ERPCity.findById(data.pcity) : null
            peraddress.save(failOnError: true, flush: true)
            
            // Save local address
            AddressType localAddressType = AddressType.findByType("Local")
            def localaddress = Address.findByRecapplicantAndAddresstype(recapplicant, localAddressType)
            
            if (!localaddress) {
                localaddress = new Address()
                localaddress.recapplicant = recapplicant
                localaddress.organization = recver.organization
                localaddress.addresstype = localAddressType
                localaddress.creation_date = new Date()
                localaddress.creation_ip_address = request.getRemoteAddr()
            }
            
            localaddress.username = uid
            localaddress.updation_date = new Date()
            localaddress.updation_ip_address = request.getRemoteAddr()
            localaddress.address = data.lladd
            localaddress.taluka = data.ltaluka
            localaddress.pin = data.lpin
            localaddress.country = data.lcountry ? ERPCountry.findById(data.lcountry) : null
            localaddress.state = data.lstate ? ERPState.findById(data.lstate) : null
            localaddress.district = data.ldist ? ERPDistrict.findById(data.ldist) : null
            localaddress.city = data.lcity ? ERPCity.findById(data.lcity) : null
            localaddress.save(failOnError: true, flush: true)
            
            // Save or update application
            RecApplication recapplication = RecApplication.findByOrganizationAndRecapplicantAndRecversion(recver.organization, recapplicant, recver)
            SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd")
            
            if (recapplication == null) {
                recapplication = new RecApplication()
                
                // Generate application ID
                String applicaitionid = recver.organization.organization_number
                String version_date = df.format(recver.version_date)
                String track = "" + (recver.applicationtrack)
                String prefix = ""
                for (int i = 0; i < (5 - track.size()); i++)
                    prefix = prefix + "0"
                applicaitionid = applicaitionid + version_date + prefix + track
                
                // Increment track
                recver.applicationtrack = recver.applicationtrack + 1
                recver.save(failOnError: true, flush: true)
                
                recapplication.applicaitionid = applicaitionid
                recapplication.feesreceiptid = null
                recapplication.applicationdate = new Date()
                recapplication.place = data.place
                
                // Check if fees should be auto-paid
                if (recver?.feesamountcategory == 0.0) {
                    def recdocumenttypes = RecDocumentType.findAllByIsactiveAndIscompulsory(true, true)
                    def recapplicantdocument = RecApplicantDocument.findAllByRecapplicant(recapplicant)
                    if (recapplicantdocument.size() >= recdocumenttypes.size())
                        recapplication.isfeespaid = true
                } else {
                    recapplication.isfeespaid = false
                }
                
                recapplication.amount = 0
                recapplication.username = uid
                recapplication.creation_date = new Date()
                recapplication.updation_date = new Date()
                recapplication.creation_ip_address = request.getRemoteAddr()
                recapplication.updation_ip_address = request.getRemoteAddr()
                recapplication.organization = recver.organization
                recapplication.recapplicant = recapplicant
                recapplication.recversion = recver
                recapplication.reconlinetransaction = null
                recapplication.save(failOnError: true, flush: true)
                
                // Add posts
                for (rp in posts) {
                    recapplication.addToRecpost(rp)
                    recapplication.save(failOnError: true, flush: true)
                }
                
                // Add branches
                for (rb in branches) {
                    recapplication.addToRecbranch(rb)
                    recapplication.save(failOnError: true, flush: true)
                }
            } else {
                // Update existing application
                recapplication.place = data.place
                recapplication.username = uid
                recapplication.updation_date = new Date()
                recapplication.updation_ip_address = request.getRemoteAddr()
                recapplication.save(failOnError: true, flush: true)
                
                // Clear and re-add posts
                recapplication.recpost.clear()
                recapplication.save(failOnError: true, flush: true)
                for (rp in posts) {
                    recapplication.addToRecpost(rp)
                    recapplication.save(failOnError: true, flush: true)
                }
                
                // Clear and re-add branches
                recapplication.recbranch.clear()
                recapplication.save(failOnError: true, flush: true)
                for (rb in branches) {
                    recapplication.addToRecbranch(rb)
                    recapplication.save(failOnError: true, flush: true)
                }
            }
            
            // Create RecApplicationStatus for each authority and branch
            def authoritylist = RecAuthorityType.findAllByOrganization(recver.organization).sort { it.serial_no }
            
            for (auth in authoritylist) {
                for (rb in branches) {
                    RecApplicationStatus recApplicationStatus = RecApplicationStatus.findByOrganizationAndRecapplicationAndRecversionAndRecbranchAndRecauthoritytype(
                        recver.organization, recapplication, recver, rb, auth)
                    
                    if (recApplicationStatus == null) {
                        recApplicationStatus = new RecApplicationStatus()
                        recApplicationStatus.remark = null
                        recApplicationStatus.iscalledforinterview = false
                        RecApplicationStatusMaster recApplicationStatusMaster = RecApplicationStatusMaster.findByStatusAndOrganization('inprocess', recver.organization)
                        recApplicationStatus.ismailsent = false
                        recApplicationStatus.mailsentdate = null
                        recApplicationStatus.username = uid
                        recApplicationStatus.updation_date = new Date()
                        recApplicationStatus.updation_ip_address = request.getRemoteAddr()
                        recApplicationStatus.creation_date = new Date()
                        recApplicationStatus.creation_ip_address = request.getRemoteAddr()
                        recApplicationStatus.organization = recver.organization
                        recApplicationStatus.recapplication = recapplication
                        recApplicationStatus.recauthoritytype = auth
                        recApplicationStatus.approve_date = null
                        recApplicationStatus.approvedby = null
                        recApplicationStatus.recbranch = rb
                        recApplicationStatus.recversion = recver
                        recApplicationStatus.recapplicationstatusmaster = recApplicationStatusMaster
                        recApplicationStatus.save(failOnError: true, flush: true)
                    }
                }
            }
            
            hm.applicationId = recapplication.id
            hm.applicaitionid = recapplication.applicaitionid
            hm.msg = "Application submitted successfully"
            hm.flag = true
            
        } catch (Exception e) {
            println("Error in submitApplication: ${e.message}")
            e.printStackTrace()
            hm.msg = "Error submitting application: ${e.message}"
            hm.flag = false
        }
    }
    
    /**
     * Get all applications for logged-in applicant
     * Used by: GET /recApplication/getMyApplications
     */
    def getMyApplications(hm, request) {
        try {
            def uid = hm.remove("uid")
            
            if (!uid) {
                hm.msg = "User not found"
                hm.flag = false
                return
            }
            
            RecApplicant recapplicant = RecApplicant.findByEmail(uid)
            if (!recapplicant) {
                hm.msg = "Applicant not found"
                hm.flag = false
                return
            }
            
            def applications = RecApplication.findAllByRecapplicant(recapplicant)
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd")
            
            hm.applications = applications.collect { app ->
                [
                    id: app.id,
                    applicaitionid: app.applicaitionid,
                    feesreceiptid: app.feesreceiptid,
                    applicationdate: app.applicationdate ? df.format(app.applicationdate) : null,
                    place: app.place,
                    isfeespaid: app.isfeespaid,
                    isrejected: app.isrejected,
                    amount: app.amount,
                    receiptdate: app.receiptdate ? df.format(app.receiptdate) : null,
                    version: [
                        id: app.recversion.id,
                        version_number: app.recversion.version_number,
                        version_date: df.format(app.recversion.version_date)
                    ],
                    organization: [
                        id: app.organization.id,
                        name: app.organization.organization_name
                    ],
                    posts: app.recpost?.collect { p -> [id: p.id, designation: p.designation?.name] } ?: [],
                    branches: app.recbranch?.collect { b -> [id: b.id, name: b.name] } ?: []
                ]
            }
            
            hm.msg = "Applications fetched successfully"
            hm.flag = true
            
        } catch (Exception e) {
            println("Error in getMyApplications: ${e.message}")
            hm.msg = "Error fetching applications: ${e.message}"
            hm.flag = false
        }
    }

    
    /**
     * Get detailed application information
     * Used by: GET /recApplication/getApplicationDetails/:id
     */
    def getApplicationDetails(hm, request) {
        try {
            def uid = hm.remove("uid")
            def applicationId = hm.remove("applicationId")
            
            if (!uid || !applicationId) {
                hm.msg = "User or application not found"
                hm.flag = false
                return
            }
            
            RecApplication recapplication = RecApplication.findById(applicationId)
            if (!recapplication) {
                hm.msg = "Application not found"
                hm.flag = false
                return
            }
            
            // Verify ownership
            if (recapplication.recapplicant.email != uid) {
                hm.msg = "Unauthorized access"
                hm.flag = false
                return
            }
            
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd")
            
            // Application details
            hm.application = [
                id: recapplication.id,
                applicaitionid: recapplication.applicaitionid,
                feesreceiptid: recapplication.feesreceiptid,
                applicationdate: recapplication.applicationdate ? df.format(recapplication.applicationdate) : null,
                place: recapplication.place,
                isfeespaid: recapplication.isfeespaid,
                isrejected: recapplication.isrejected,
                amount: recapplication.amount,
                receiptdate: recapplication.receiptdate ? df.format(recapplication.receiptdate) : null
            ]
            
            // Version details
            hm.version = [
                id: recapplication.recversion.id,
                version_number: recapplication.recversion.version_number,
                version_date: df.format(recapplication.recversion.version_date),
                from_date: df.format(recapplication.recversion.from_date),
                to_date: df.format(recapplication.recversion.to_date)
            ]
            
            // Organization details
            hm.organization = [
                id: recapplication.organization.id,
                name: recapplication.organization.organization_name
            ]
            
            // Posts
            hm.posts = recapplication.recpost?.collect { p ->
                [
                    id: p.id,
                    designation: p.designation?.name
                ]
            } ?: []
            
            // Branches
            hm.branches = recapplication.recbranch?.collect { b ->
                [
                    id: b.id,
                    name: b.name,
                    branch_abbrivation: b.branch_abbrivation,
                    program: b.program?.name
                ]
            } ?: []
            
            // Applicant details
            def applicant = recapplication.recapplicant
            hm.applicant = [
                id: applicant.id,
                email: applicant.email,
                fullname: applicant.fullname,
                cast: applicant.cast,
                pancardno: applicant.pancardno,
                aadhaarcardno: applicant.aadhaarcardno,
                dateofbirth: applicant.dateofbirth ? df.format(applicant.dateofbirth) : null,
                mobilenumber: applicant.mobilenumber,
                area_of_specialization: applicant.area_of_specialization,
                any_other_info_related_to_post: applicant.any_other_info_related_to_post,
                present_salary: applicant.present_salary,
                ishandicapped: applicant.ishandicapped,
                category: applicant.reccategory?.name,
                maritalstatus: applicant.maritalstatus?.name,
                salutation: applicant.salutation?.name,
                minority: applicant.minoritytypedetails?.name,
                gender: applicant.gender?.type
            ]
            
            // Addresses
            AddressType localAddressType = AddressType.findByType("Local")
            AddressType permanentAddressType = AddressType.findByType("Permanent")
            def localaddress = Address.findByRecapplicantAndAddresstype(applicant, localAddressType)
            def peraddress = Address.findByRecapplicantAndAddresstype(applicant, permanentAddressType)
            
            hm.localAddress = localaddress ? [
                address: localaddress.address,
                taluka: localaddress.taluka,
                pin: localaddress.pin,
                country: localaddress.country?.name,
                state: localaddress.state?.state,
                district: localaddress.district?.district,
                city: localaddress.city?.city
            ] : null
            
            hm.permanentAddress = peraddress ? [
                address: peraddress.address,
                taluka: peraddress.taluka,
                pin: peraddress.pin,
                country: peraddress.country?.name,
                state: peraddress.state?.state,
                district: peraddress.district?.district,
                city: peraddress.city?.city
            ] : null
            
            // Academic qualifications
            def academics = RecApplicantAcademics.findAllByRecapplicant(applicant)
            hm.academics = academics.collect { ac ->
                [
                    degree: ac.recdegree?.name,
                    name_of_degree: ac.name_of_degree,
                    yearofpassing: ac.yearofpassing,
                    university: ac.university,
                    branch: ac.branch,
                    cpi_marks: ac.cpi_marks
                ]
            }
            
            // Experience
            RecExperienceType teachingexptype = RecExperienceType.findByTypeAndIsactive("Teaching", true)
            RecExperienceType industryexptype = RecExperienceType.findByTypeAndIsactive("Industrial/Research", true)
            RecExperienceType nonteachingexptype = RecExperienceType.findByTypeAndIsactive("Non-Teaching", true)
            
            RecExperience teachingexp = RecExperience.findByRecexperiencetypeAndRecapplicant(teachingexptype, applicant)
            RecExperience industryexp = RecExperience.findByRecexperiencetypeAndRecapplicant(industryexptype, applicant)
            RecExperience nonteachingexp = RecExperience.findByRecexperiencetypeAndRecapplicant(nonteachingexptype, applicant)
            
            hm.teachingExperience = teachingexp ? [years: teachingexp.years, months: teachingexp.months] : null
            hm.industryExperience = industryexp ? [years: industryexp.years, months: industryexp.months] : null
            hm.nonTeachingExperience = nonteachingexp ? [years: nonteachingexp.years, months: nonteachingexp.months] : null
            
            // Application status
            def statusList = RecApplicationStatus.findAllByRecapplication(recapplication)
            hm.statusList = statusList.collect { status ->
                [
                    authority: status.recauthoritytype?.type,
                    branch: status.recbranch?.name,
                    status: status.recapplicationstatusmaster?.status,
                    remark: status.remark,
                    approve_date: status.approve_date ? df.format(status.approve_date) : null,
                    approvedby: status.approvedby ? "${status.approvedby.person?.firstName} ${status.approvedby.person?.lastName}".trim() : null,
                    iscalledforinterview: status.iscalledforinterview,
                    ismailsent: status.ismailsent,
                    mailsentdate: status.mailsentdate ? df.format(status.mailsentdate) : null
                ]
            }
            
            hm.msg = "Application details fetched successfully"
            hm.flag = true
            
        } catch (Exception e) {
            println("Error in getApplicationDetails: ${e.message}")
            hm.msg = "Error fetching application details: ${e.message}"
            hm.flag = false
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // Phase 2: Document Management APIs
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Get document types required for application
     * Used by: GET /recApplication/getDocumentTypes
     */
    def getDocumentTypes(hm, request) {
        try {
            def uid = hm.remove("uid")
            
            if (!uid) {
                hm.msg = "User not found"
                hm.flag = false
                return
            }
            
            RecApplicant recapplicant = RecApplicant.findByEmail(uid)
            if (!recapplicant) {
                hm.msg = "Applicant not found"
                hm.flag = false
                return
            }
            
            // Get all active document types
            def documentTypes = RecDocumentType.findAllByIsactive(true)
            
            // Get uploaded documents for this applicant
            def uploadedDocuments = RecApplicantDocument.findAllByRecapplicant(recapplicant)
            
            hm.documentTypes = documentTypes.collect { dt ->
                def uploaded = uploadedDocuments.find { it.recdocumenttype.id == dt.id }
                [
                    id: dt.id,
                    type: dt.type,
                    extension: dt.extension,
                    iscompulsory: dt.iscompulsory,
                    uploaded: uploaded != null,
                    uploadedDocId: uploaded?.id,
                    uploadedFileName: uploaded?.filename,
                    uploadedDate: uploaded?.creation_date
                ]
            }
            
            hm.msg = "Document types fetched successfully"
            hm.flag = true
            
        } catch (Exception e) {
            println("Error in getDocumentTypes: ${e.message}")
            hm.msg = "Error fetching document types: ${e.message}"
            hm.flag = false
        }
    }
    
    /**
     * Upload document to AWS S3
     * Used by: POST /recApplication/uploadDocument
     */
    def uploadDocument(hm, request, data) {
        try {
            def uid = hm.remove("uid")
            def documentTypeId = data.documenttype
            
            if (!uid) {
                hm.msg = "User not found"
                hm.flag = false
                return
            }
            
            RecApplicant recapplicant = RecApplicant.findByEmail(uid)
            if (!recapplicant) {
                hm.msg = "Applicant not found"
                hm.flag = false
                return
            }
            
            if (!documentTypeId) {
                hm.msg = "Please select document type"
                hm.flag = false
                return
            }
            
            // Convert string to Long if needed
            Long docTypeId = documentTypeId instanceof String ? documentTypeId.toLong() : documentTypeId
            
            println("Looking for RecDocumentType with ID: ${docTypeId}")
            RecDocumentType recdocumenttype = RecDocumentType.findById(docTypeId)
            
            if (!recdocumenttype) {
                println("RecDocumentType not found for ID: ${docTypeId}")
                hm.msg = "Document type not found. Please check document type ID."
                hm.flag = false
                return
            }
            
            if (!recdocumenttype.isactive) {
                println("RecDocumentType is not active for ID: ${docTypeId}")
                hm.msg = "Document type is not active"
                hm.flag = false
                return
            }
            
            // Get file from multipart request
            def file = request.getFile("documentname")
            if (!file || file.empty) {
                hm.msg = "Please select a file to upload"
                hm.flag = false
                return
            }
            
            String strFilename = file.originalFilename
            String extension = strFilename.substring(strFilename.lastIndexOf('.') + 1).toLowerCase()
            
            // Validate file extension (document type may have multiple extensions separated by comma)
            def allowedExtensions = recdocumenttype.extension.toLowerCase().split(',').collect { it.trim() }
            if (!allowedExtensions.contains(extension)) {
                hm.msg = "Please select ${recdocumenttype.extension} file. Uploaded file has extension: ${extension}"
                hm.flag = false
                return
            }
            
            // AWS S3 configuration
            AWSBucket awsBucket = AWSBucket.findByContent("documents")
            AWSFolderPath afp = AWSFolderPath.findById(5)
            
            if (!awsBucket || !afp) {
                hm.msg = "AWS configuration not found"
                hm.flag = false
                return
            }
            
            // Prepare file path
            String path = "recruitment/documents/" + recapplicant.id + "/"
            String awsfp = afp.path + path
            String existsFilePath = ""
            
            // Check if document already exists
            RecApplicantDocument recapplicantdocument = RecApplicantDocument.findByRecapplicantAndRecdocumenttype(recapplicant, recdocumenttype)
            
            if (recapplicantdocument == null) {
                // Create new document record
                recapplicantdocument = new RecApplicantDocument()
                recapplicantdocument.filepath = path
                recapplicantdocument.filename = file.originalFilename
                recapplicantdocument.username = uid
                recapplicantdocument.creation_date = new Date()
                recapplicantdocument.updation_date = new Date()
                recapplicantdocument.creation_ip_address = request.getRemoteAddr()
                recapplicantdocument.updation_ip_address = request.getRemoteAddr()
                recapplicantdocument.recapplicant = recapplicant
                recapplicantdocument.recdocumenttype = recdocumenttype
                recapplicantdocument.save(failOnError: true, flush: true)
            } else {
                // Update existing document record
                existsFilePath = awsfp + recapplicantdocument.filepath + recapplicantdocument.filename
                recapplicantdocument.filename = file.originalFilename
                recapplicantdocument.username = uid
                recapplicantdocument.updation_date = new Date()
                recapplicantdocument.updation_ip_address = request.getRemoteAddr()
                recapplicantdocument.save(failOnError: true, flush: true)
            }
            
            // Upload to AWS S3
            Part filePart = request.getPart("documentname")
            AWSUploadDocumentsService awsUploadDocumentsService = new AWSUploadDocumentsService()
            boolean isUploaded = awsUploadDocumentsService.uploadDocument(filePart, awsfp, file.originalFilename, existsFilePath)
            
            if (!isUploaded) {
                hm.msg = "Failed to upload document to AWS S3"
                hm.flag = false
                return
            }
            
            // If photo, update applicant record
            if (recdocumenttype.type == 'Photo') {
                recapplicant.photopath = path
                recapplicant.photoname = file.originalFilename
                recapplicant.save(failOnError: true, flush: true)
            }
            
            hm.documentId = recapplicantdocument.id
            hm.filename = file.originalFilename
            hm.msg = "Document uploaded successfully"
            hm.flag = true
            
        } catch (Exception e) {
            println("Error in uploadDocument: ${e.message}")
            e.printStackTrace()
            hm.msg = "Error uploading document: ${e.message}"
            hm.flag = false
        }
    }
    
    /**
     * Download document from AWS S3
     * Used by: GET /recApplication/downloadDocument
     */
    def downloadDocument(hm, request) {
        try {
            def uid = hm.remove("uid")
            def documentId = hm.remove("documentId")
            
            if (!uid) {
                hm.msg = "User not found"
                hm.flag = false
                return
            }
            
            if (!documentId) {
                hm.msg = "Document ID not provided"
                hm.flag = false
                return
            }
            
            RecApplicant recapplicant = RecApplicant.findByEmail(uid)
            if (!recapplicant) {
                hm.msg = "Applicant not found"
                hm.flag = false
                return
            }
            
            RecApplicantDocument recdocument = RecApplicantDocument.findById(documentId)
            if (!recdocument) {
                hm.msg = "Document not found"
                hm.flag = false
                return
            }
            
            // Verify ownership
            if (recdocument.recapplicant.id != recapplicant.id) {
                hm.msg = "Unauthorized access"
                hm.flag = false
                return
            }
            
            // Return file info for download
            hm.filepath = recdocument.filepath
            hm.filename = recdocument.filename
            hm.documentType = recdocument.recdocumenttype.type
            hm.extension = recdocument.recdocumenttype.extension
            hm.msg = "Document info fetched successfully"
            hm.flag = true
            
        } catch (Exception e) {
            println("Error in downloadDocument: ${e.message}")
            hm.msg = "Error downloading document: ${e.message}"
            hm.flag = false
        }
    }
    
    /**
     * Delete document from AWS S3
     * Used by: POST /recApplication/deleteDocument
     */
    def deleteDocument(hm, request, data) {
        try {
            def uid = hm.remove("uid")
            def documentId = data.documentId
            
            if (!uid) {
                hm.msg = "User not found"
                hm.flag = false
                return
            }
            
            if (!documentId) {
                hm.msg = "Document ID not provided"
                hm.flag = false
                return
            }
            
            RecApplicant recapplicant = RecApplicant.findByEmail(uid)
            if (!recapplicant) {
                hm.msg = "Applicant not found"
                hm.flag = false
                return
            }
            
            RecApplicantDocument recdocument = RecApplicantDocument.findById(documentId)
            if (!recdocument) {
                hm.msg = "Document not found"
                hm.flag = false
                return
            }
            
            // Verify ownership
            if (recdocument.recapplicant.id != recapplicant.id) {
                hm.msg = "Unauthorized access"
                hm.flag = false
                return
            }
            
            // AWS S3 configuration
            AWSFolderPath afp = AWSFolderPath.findById(5)
            if (!afp) {
                hm.msg = "AWS configuration not found"
                hm.flag = false
                return
            }
            
            String awsfp = afp.path + recdocument.filepath + recdocument.filename
            
            // Delete from AWS S3
            AWSUploadDocumentsService awsUploadDocumentsService = new AWSUploadDocumentsService()
            boolean isDeleted = awsUploadDocumentsService.deleteDocument(awsfp)
            
            if (!isDeleted) {
                println("Warning: Failed to delete document from AWS S3, but will delete database record")
            }
            
            // Delete database record
            recdocument.delete(failOnError: true, flush: true)
            
            hm.msg = "Document deleted successfully"
            hm.flag = true
            
        } catch (Exception e) {
            println("Error in deleteDocument: ${e.message}")
            e.printStackTrace()
            hm.msg = "Error deleting document: ${e.message}"
            hm.flag = false
        }
    }
    
    /**
     * Get applicant photo
     * Used by: GET /recApplication/getApplicantPhoto
     */
    def getApplicantPhoto(hm, request) {
        try {
            def uid = hm.remove("uid")
            def applicantId = hm.remove("applicantId")
            
            if (!uid) {
                hm.msg = "User not found"
                hm.flag = false
                return
            }
            
            RecApplicant recapplicant
            if (applicantId) {
                recapplicant = RecApplicant.findById(applicantId)
            } else {
                recapplicant = RecApplicant.findByEmail(uid)
            }
            
            if (!recapplicant) {
                hm.msg = "Applicant not found"
                hm.flag = false
                return
            }
            
            if (!recapplicant.photopath || !recapplicant.photoname) {
                hm.msg = "Photo not uploaded"
                hm.flag = false
                return
            }
            
            // Return photo info
            hm.photopath = recapplicant.photopath
            hm.photoname = recapplicant.photoname
            hm.fullpath = recapplicant.photopath + recapplicant.photoname
            hm.msg = "Photo info fetched successfully"
            hm.flag = true
            
        } catch (Exception e) {
            println("Error in getApplicantPhoto: ${e.message}")
            hm.msg = "Error fetching photo: ${e.message}"
            hm.flag = false
        }
    }
}
