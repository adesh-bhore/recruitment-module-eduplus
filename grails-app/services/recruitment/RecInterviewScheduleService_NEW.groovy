package recruitment

import grails.gorm.transactions.Transactional
import java.text.SimpleDateFormat
import common.AWSBucketService

@Transactional
class RecInterviewScheduleService_NEW {

    /**
     * Get interview schedules with filter data
     * Returns: Interview schedules, academic years, organizations, versions, departments, posts
     */
    def getInterviewScheduleList(hm, request) {
        try {
            def inst = hm.remove("inst")
            def org = hm.remove("org")

            if (!inst || !org) {
                hm.msg = "Instructor or Organization not found"
                hm.flag = false
                return
            }

            // Get ApplicationType for ERP
            ApplicationType at = ApplicationType.findByApplication_type("ERP")
            if (!at) {
                hm.msg = "Application Type 'ERP' not found"
                hm.flag = false
                return
            }

            // Get current academic year for Recruitment
            RoleType rt = RoleType.findByApplicationtypeAndTypeAndOrganization(at, "Recruitment", org)
            def aay = null
            if (rt) {
                aay = ApplicationAcademicYear.findByRoletypeAndIsActiveAndOrganization(rt, true, org)
            }

            if (!aay) {
                hm.msg = "Application Academic Year not set for Recruitment Module"
                hm.flag = false
                return
            }

            // Get active academic years
            def aylist = AcademicYear.findAllByIsactive(true, [sort: 'ay', order: 'asc'])
            def aylistFiltered = aylist.collect { ay ->
                [
                    id: ay.id,
                    ay: ay.ay
                ]
            }

            // Check if user has Management role for Recruitment
            RoleType recretment_rt = RoleType.findByApplicationtypeAndTypeAndOrganization(at, "Recruitment", org)
            def is_management = false
            def orglist = []
            
            if (recretment_rt) {
                def login = Login.findByPerson(inst.person)
                def rolelist = login?.roles
                
                if (rolelist) {
                    for (role in rolelist) {
                        if (role.role == 'Management' && role.roletype?.id == recretment_rt.id) {
                            is_management = true
                            break
                        }
                    }
                }
            }

            // Get organization list if user is management
            if (is_management) {
                def organizationGroup = org.organizationgroup
                if (organizationGroup) {
                    def orgs = Organization.findAllByIsactiveAndOrganizationgroup(true, organizationGroup)
                    orglist = orgs.collect { o ->
                        [
                            id: o.id,
                            organization_name: o.organization_name
                        ]
                    }
                }
            }

            // Get recruitment versions for current academic year
            def recversionlist = RecVersion.findAllByOrganizationAndAcademicyear(org, aay.academicyear)
            def recversionlistFiltered = recversionlist.collect { rv ->
                [
                    id: rv.id,
                    version_number: rv.version_number,
                    iscurrent: rv.iscurrent
                ]
            }

            // Get current version for backend processing
            RecVersion recver = RecVersion.findByOrganizationAndIscurrentforbackendprocessingAndAcademicyear(org, true, aay.academicyear)
            if (!recver) {
                recver = RecVersion.findByOrganizationAndAcademicyear(org, aay.academicyear)
            }

            def recverFiltered = null
            if (recver) {
                recverFiltered = [
                    id: recver.id,
                    version_number: recver.version_number,
                    iscurrent: recver.iscurrent
                ]
            }

            // Get posts for current version
            def postlist = []
            if (recver) {
                def posts = RecPost.findAllByRecversionAndOrganization(recver, org)
                postlist = posts.collect { p ->
                    def postName = "Unknown"
                    try {
                        postName = p.designation?.name ?: "Unknown"
                    } catch (Exception e) {
                        println("Warning: Could not load designation for post ID ${p.id}: ${e.message}")
                    }
                    [
                        id: p.id,
                        name: postName
                    ]
                }
            }

            // Get departments for current version
            def departmentlist = []
            if (recver) {
                departmentlist = getRecDepartment(org, recver)
            }

            // Get interview schedules for current version
            def interviewlist = []
            if (recver) {
                def interviews = RecInterviewScheduleDetails.findAllByOrganizationAndRecversion(org, recver)
                interviewlist = interviews.collect { interview ->
                    def deptName = interview.department?.name ?: "Unknown"
                    def postName = "Unknown"
                    try {
                        postName = interview.recpost?.designation?.name ?: "Unknown"
                    } catch (Exception e) {
                        println("Warning: Could not load designation for interview schedule ID ${interview.id}")
                    }
                    
                    [
                        id: interview.id,
                        interview_date: interview.interview_date,
                        interview_venue: interview.interview_venue,
                        interview_time: interview.interview_time,
                        department: [
                            id: interview.department?.id,
                            name: deptName
                        ],
                        recpost: [
                            id: interview.recpost?.id,
                            name: postName
                        ]
                    ]
                }
            }

            // Prepare response
            hm.aylist = aylistFiltered
            hm.orglist = orglist
            hm.org = [
                id: org.id,
                organization_name: org.organization_name
            ]
            hm.aay = aay ? [
                id: aay.id,
                academicyear: [
                    id: aay.academicyear.id,
                    ay: aay.academicyear.ay
                ]
            ] : null
            hm.recversionlist = recversionlistFiltered
            hm.recver = recverFiltered
            hm.postlist = postlist
            hm.departmentlist = departmentlist
            hm.interviewlist = interviewlist
            hm.is_management = is_management
            hm.msg = "Interview schedule data fetched successfully"
            hm.flag = true

        } catch (Exception e) {
            println("Error in getInterviewScheduleList: ${e.message}")
            e.printStackTrace()
            hm.msg = "Error fetching interview schedule data: ${e.message}"
            hm.flag = false
        }
    }

    /**
     * Helper method to get recruitment departments
     */
    private def getRecDepartment(Organization org, RecVersion recver) {
        def departmentlist = []
        try {
            def branches = RecBranch.findAllByOrganizationAndRecversion(org, recver)
            def deptIds = []
            
            for (branch in branches) {
                try {
                    def dept = branch.program?.department
                    if (dept && !deptIds.contains(dept.id)) {
                        deptIds.add(dept.id)
                        departmentlist.add([
                            id: dept.id,
                            name: dept.name
                        ])
                    }
                } catch (Exception e) {
                    println("Warning: Could not load program/department for branch ID ${branch.id}: ${e.message}")
                }
            }
        } catch (Exception e) {
            println("Error in getRecDepartment: ${e.message}")
        }
        return departmentlist
    }

    /**
     * Get recruitment versions by organization and academic year
     */
    def getRecVersion(hm, request, data) {
        try {
            def inst = hm.remove("inst")
            def org = hm.remove("org")

            if (!inst || !org) {
                hm.msg = "Instructor or Organization not found"
                hm.flag = false
                return
            }

            // Get parameters
            def orgId = data.org
            def ayId = data.ay

            if (!ayId) {
                hm.msg = "Academic Year ID is required"
                hm.flag = false
                return
            }

            // Get organization
            Organization organization = org
            if (orgId) {
                organization = Organization.get(orgId)
                if (!organization) {
                    hm.msg = "Organization not found with ID: ${orgId}"
                    hm.flag = false
                    return
                }
            }

            // Get academic year
            AcademicYear academicYear = AcademicYear.get(ayId)
            if (!academicYear) {
                hm.msg = "Academic Year not found with ID: ${ayId}"
                hm.flag = false
                return
            }

            // Get recruitment versions
            def recversionlist = RecVersion.findAllByOrganizationAndAcademicyear(organization, academicYear)
            def recversionlistFiltered = recversionlist.collect { rv ->
                [
                    id: rv.id,
                    version_number: rv.version_number,
                    iscurrent: rv.iscurrent
                ]
            }

            hm.recversionlist = recversionlistFiltered
            hm.msg = "Recruitment versions fetched successfully"
            hm.flag = true

        } catch (Exception e) {
            println("Error in getRecVersionByOrgAndAy: ${e.message}")
            e.printStackTrace()
            hm.msg = "Error fetching recruitment versions: ${e.message}"
            hm.flag = false
        }
    }

    /**
     * Get departments by organization and recruitment version
     */
    def getDept(hm, request, data) {
        try {
            def inst = hm.remove("inst")
            def org = hm.remove("org")

            if (!inst || !org) {
                hm.msg = "Instructor or Organization not found"
                hm.flag = false
                return
            }

            // Get parameters
            def orgId = data.org
            def recverId = data.recver

            if (!recverId) {
                hm.msg = "Recruitment Version ID is required"
                hm.flag = false
                return
            }

            // Get organization
            Organization organization = org
            if (orgId) {
                organization = Organization.get(orgId)
                if (!organization) {
                    hm.msg = "Organization not found with ID: ${orgId}"
                    hm.flag = false
                    return
                }
            }

            // Get recruitment version
            RecVersion recver = RecVersion.get(recverId)
            if (!recver) {
                hm.msg = "Recruitment Version not found with ID: ${recverId}"
                hm.flag = false
                return
            }

            // Get departments
            def departmentlist = getRecDepartment(organization, recver)

            hm.departmentlist = departmentlist
            hm.msg = "Departments fetched successfully"
            hm.flag = true

        } catch (Exception e) {
            println("Error in getDepartmentsByOrgAndVersion: ${e.message}")
            e.printStackTrace()
            hm.msg = "Error fetching departments: ${e.message}"
            hm.flag = false
        }
    }

    /**
     * Get posts by organization and recruitment version
     */
    def getPost(hm, request, data) {
        try {
            def inst = hm.remove("inst")
            def org = hm.remove("org")

            if (!inst || !org) {
                hm.msg = "Instructor or Organization not found"
                hm.flag = false
                return
            }

            // Get parameters
            def orgId = data.org
            def recverId = data.recver

            println("DEBUG getPost - orgId: ${orgId}, recverId: ${recverId}")
            println("DEBUG - User org: ${org.id}, User org name: ${org.organization_name}")

            if (!recverId) {
                hm.msg = "Recruitment Version ID is required"
                hm.flag = false
                return
            }

            // Get organization
            Organization organization = org
            if (orgId) {
                organization = Organization.get(orgId)
                println("DEBUG - Using provided org: ${organization?.id}")
                if (!organization) {
                    hm.msg = "Organization not found with ID: ${orgId}"
                    hm.flag = false
                    return
                }
            }

            // Get recruitment version
            RecVersion recver = RecVersion.get(recverId)
            println("DEBUG - RecVersion found: ${recver?.id}, version_number: ${recver?.version_number}, org: ${recver?.organization?.id}")
            if (!recver) {
                hm.msg = "Recruitment Version not found with ID: ${recverId}"
                hm.flag = false
                return
            }

            // Get posts
            def posts = RecPost.findAllByRecversionAndOrganization(recver, organization)
            println("DEBUG - Posts found: ${posts.size()}")
            
            if (posts.size() > 0) {
                println("DEBUG - First post: id=${posts[0].id}, recversion_id=${posts[0].recversion?.id}, org_id=${posts[0].organization?.id}")
            }
            def postlist = posts.collect { p ->
                def postName = "Unknown"
                try {
                    postName = p.designation?.name ?: "Unknown"
                } catch (Exception e) {
                    println("Warning: Could not load designation for post ID ${p.id}: ${e.message}")
                }
                [
                    id: p.id,
                    name: postName
                ]
            }

            hm.postlist = postlist
            hm.msg = "Posts fetched successfully"
            hm.flag = true

        } catch (Exception e) {
            println("Error in getPostsByOrgAndVersion: ${e.message}")
            e.printStackTrace()
            hm.msg = "Error fetching posts: ${e.message}"
            hm.flag = false
        }
    }

    /**
     * Get interview list by organization and recruitment version
     */
    def getInterviewList(hm, request, data) {
        try {
            def inst = hm.remove("inst")
            def org = hm.remove("org")

            if (!inst || !org) {
                hm.msg = "Instructor or Organization not found"
                hm.flag = false
                return
            }

            // Get parameters
            def orgId = data.org
            def recverId = data.recver

            if (!recverId) {
                hm.msg = "Recruitment Version ID is required"
                hm.flag = false
                return
            }

            // Get organization
            Organization organization = org
            if (orgId) {
                organization = Organization.get(orgId)
                if (!organization) {
                    hm.msg = "Organization not found with ID: ${orgId}"
                    hm.flag = false
                    return
                }
            }

            // Get recruitment version
            RecVersion recver = RecVersion.get(recverId)
            if (!recver) {
                hm.msg = "Recruitment Version not found with ID: ${recverId}"
                hm.flag = false
                return
            }

            // Get interview schedules
            def interviews = RecInterviewScheduleDetails.findAllByOrganizationAndRecversion(organization, recver)
            def interviewlist = interviews.collect { interview ->
                def deptName = interview.department?.name ?: "Unknown"
                def postName = "Unknown"
                try {
                    postName = interview.recpost?.designation?.name ?: "Unknown"
                } catch (Exception e) {
                    println("Warning: Could not load designation for interview schedule ID ${interview.id}")
                }
                
                [
                    id: interview.id,
                    interview_date: interview.interview_date,
                    interview_venue: interview.interview_venue,
                    interview_time: interview.interview_time,
                    department: [
                        id: interview.department?.id,
                        name: deptName
                    ],
                    recpost: [
                        id: interview.recpost?.id,
                        name: postName
                    ]
                ]
            }

            hm.interviewlist = interviewlist
            hm.msg = "Interview list fetched successfully"
            hm.flag = true

        } catch (Exception e) {
            println("Error in getInterviewListByOrgAndVersion: ${e.message}")
            e.printStackTrace()
            hm.msg = "Error fetching interview list: ${e.message}"
            hm.flag = false
        }
    }

    /**
     * Save new interview schedule
     */
    def saveInterviewSchedule(hm, request, data) {
        try {
            def inst = hm.remove("inst")
            def org = hm.remove("org")

            if (!inst || !org) {
                hm.msg = "Instructor or Organization not found"
                hm.flag = false
                return
            }

            // Get parameters
            def orgId = data.org
            def recversionId = data.recversion
            def departmentId = data.department
            def postId = data.post
            def interviewDate = data.interview_date
            def interviewVenue = data.interview_venue
            def interviewTime = data.interview_time

            // Validate required parameters
            if (!recversionId) {
                hm.msg = "Recruitment Version ID is required"
                hm.flag = false
                return
            }

            if (!departmentId) {
                hm.msg = "Department ID is required"
                hm.flag = false
                return
            }

            if (!postId) {
                hm.msg = "Post ID is required"
                hm.flag = false
                return
            }

            if (!interviewDate) {
                hm.msg = "Interview Date is required"
                hm.flag = false
                return
            }

            if (!interviewVenue) {
                hm.msg = "Interview Venue is required"
                hm.flag = false
                return
            }

            if (!interviewTime) {
                hm.msg = "Interview Time is required"
                hm.flag = false
                return
            }

            // Get organization
            Organization organization = org
            if (orgId) {
                organization = Organization.get(orgId)
                if (!organization) {
                    hm.msg = "Organization not found with ID: ${orgId}"
                    hm.flag = false
                    return
                }
            }

            // Get recruitment version
            RecVersion recversion = RecVersion.get(recversionId)
            if (!recversion) {
                hm.msg = "Recruitment Version not found with ID: ${recversionId}"
                hm.flag = false
                return
            }

            // Get department
            Department department = Department.get(departmentId)
            if (!department) {
                hm.msg = "Department not found with ID: ${departmentId}"
                hm.flag = false
                return
            }

            // Get post
            RecPost post = RecPost.get(postId)
            if (!post) {
                hm.msg = "Post not found with ID: ${postId}"
                hm.flag = false
                return
            }

            // Create new interview schedule
            RecInterviewScheduleDetails schedule = new RecInterviewScheduleDetails()
            
            // Parse date - accepts dd-MM-yyyy format (e.g., "25-12-2024")
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy")
            schedule.interview_date = sdf.parse(interviewDate.toString())
            
            schedule.interview_venue = interviewVenue
            schedule.interview_time = interviewTime
            schedule.recversion = recversion
            schedule.organization = organization
            schedule.department = department
            schedule.recpost = post
            schedule.username = inst.uid
            schedule.creation_ip_address = request.getRemoteAddr()
            schedule.creation_date = new Date()
            schedule.updation_ip_address = request.getRemoteAddr()
            schedule.updation_date = new Date()

            schedule.save(failOnError: true, flush: true)

            hm.schedule = [
                id: schedule.id,
                interview_date: schedule.interview_date,
                interview_venue: schedule.interview_venue,
                interview_time: schedule.interview_time
            ]
            hm.msg = "Interview schedule saved successfully"
            hm.flag = true

        } catch (Exception e) {
            println("Error in saveInterviewSchedule: ${e.message}")
            e.printStackTrace()
            hm.msg = "Error saving interview schedule: ${e.message}"
            hm.flag = false
        }
    }

    /**
     * Update existing interview schedule
     */
    def editInterviewSchedule(hm, request, data) {
        try {
            def inst = hm.remove("inst")
            def org = hm.remove("org")

            if (!inst || !org) {
                hm.msg = "Instructor or Organization not found"
                hm.flag = false
                return
            }

            // Get parameters
            def scheduleId = data.recid
            def interviewDate = data.interview_date
            def interviewVenue = data.interview_venue
            def interviewTime = data.interview_time

            // Validate required parameters
            if (!scheduleId) {
                hm.msg = "Schedule ID is required"
                hm.flag = false
                return
            }

            if (!interviewDate) {
                hm.msg = "Interview Date is required"
                hm.flag = false
                return
            }

            if (!interviewVenue) {
                hm.msg = "Interview Venue is required"
                hm.flag = false
                return
            }

            if (!interviewTime) {
                hm.msg = "Interview Time is required"
                hm.flag = false
                return
            }

            // Get interview schedule
            RecInterviewScheduleDetails schedule = RecInterviewScheduleDetails.get(scheduleId)
            if (!schedule) {
                hm.msg = "Interview schedule not found with ID: ${scheduleId}"
                hm.flag = false
                return
            }

            // Update schedule
            // Parse date - accepts dd-MM-yyyy format (e.g., "25-12-2024")
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy")
            schedule.interview_date = sdf.parse(interviewDate.toString())
            
            schedule.interview_venue = interviewVenue
            schedule.interview_time = interviewTime
            schedule.username = inst.uid
            schedule.updation_ip_address = request.getRemoteAddr()
            schedule.updation_date = new Date()

            schedule.save(failOnError: true, flush: true)

            hm.schedule = [
                id: schedule.id,
                interview_date: schedule.interview_date,
                interview_venue: schedule.interview_venue,
                interview_time: schedule.interview_time
            ]
            hm.msg = "Interview schedule updated successfully"
            hm.flag = true

        } catch (Exception e) {
            println("Error in updateInterviewSchedule: ${e.message}")
            e.printStackTrace()
            hm.msg = "Error updating interview schedule: ${e.message}"
            hm.flag = false
        }
    }

    /**
     * Delete interview schedule
     */
    def deleteSched(hm, request, data) {
        try {
            def inst = hm.remove("inst")
            def org = hm.remove("org")

            if (!inst || !org) {
                hm.msg = "Instructor or Organization not found"
                hm.flag = false
                return
            }

            // Get parameters
            def scheduleId = data.recshcid

            // Validate required parameters
            if (!scheduleId) {
                hm.msg = "Schedule ID is required"
                hm.flag = false
                return
            }

            // Get interview schedule
            RecInterviewScheduleDetails schedule = RecInterviewScheduleDetails.get(scheduleId)
            if (!schedule) {
                hm.msg = "Interview schedule not found with ID: ${scheduleId}"
                hm.flag = false
                return
            }

            // Delete schedule
            schedule.delete(failOnError: true, flush: true)

            hm.msg = "Interview schedule deleted successfully"
            hm.flag = true

        } catch (Exception e) {
            println("Error in deleteInterviewSchedule: ${e.message}")
            e.printStackTrace()
            hm.msg = "Error deleting interview schedule: ${e.message}"
            hm.flag = false
        }
    }

    /**
     * Send interview call letters via email
     * Sends emails to all candidates called for interview who haven't received mail yet
     */
    def sendInterviewCallLetters(hm, request, data, sendMailService) {
        try {
            def inst = hm.remove("inst")
            def org = hm.remove("org")

            if (!inst || !org) {
                hm.msg = "Instructor or Organization not found"
                hm.flag = false
                return
            }

            // Get parameters
            def recversionId = data.version

            if (!recversionId) {
                hm.msg = "Recruitment Version ID is required"
                hm.flag = false
                return
            }

            // Get recruitment version
            RecVersion recversion = RecVersion.get(recversionId)
            if (!recversion) {
                hm.msg = "Recruitment Version not found with ID: ${recversionId}"
                hm.flag = false
                return
            }

            // Get last authority (Management)
            RecAuthorityType auth = RecAuthorityType.findByIslastauthorityAndOrganization(true, org)
            if (!auth) {
                hm.msg = "Management authority not found for organization"
                hm.flag = false
                return
            }

            // Get candidates called for interview who haven't received mail
            def selectedbymgmt = RecApplicationStatus.findAllByIscalledforinterviewAndIsmailsentAndOrganizationAndRecauthoritytypeAndRecversion(
                true, false, org, auth, recversion
            )

            if (selectedbymgmt.size() == 0) {
                hm.msg = "No candidates found who are called for interview and haven't received mail"
                hm.flag = false
                hm.emailsSent = 0
                return
            }

            // Email configuration
            def username = org?.establishment_email
            def password = org?.establishment_email_credentials

            if (!username || !password) {
                hm.msg = "Organization email credentials not configured"
                hm.flag = false
                return
            }

            def emailsSent = 0
            def emailsFailed = 0
            def failedEmails = []

            // Send emails to each candidate
            for (app in selectedbymgmt) {
                try {
                    // Skip if version doesn't match
                    if (recversion?.id != app?.recapplication?.recversion?.id) {
                        continue
                    }

                    // Get interview schedule for department
                    RecInterviewScheduleDetails sche = RecInterviewScheduleDetails.findByOrganizationAndDepartmentAndRecversion(
                        org, app?.recbranch?.program?.department, recversion
                    )

                    if (!sche) {
                        println("Interview schedule not found for department: ${app?.recbranch?.program?.department?.name}")
                        continue
                    }

                    // Format interview date
                    SimpleDateFormat df = new SimpleDateFormat("dd/MMM/yyyy")
                    String interviewDate = df.format(sche?.interview_date)
                    String currentDate = df.format(new Date())

                    // Prepare email content
                    def emailBody = generateInterviewCallLetterHTML(
                        org, 
                        app.recapplication, 
                        app.recbranch, 
                        interviewDate, 
                        sche?.interview_venue,
                        sche?.interview_time,
                        currentDate
                    )

                    // Send email
                    def emailResult = sendMailService.sendmailwithcss(
                        username,
                        password,
                        app.recapplication.recapplicant.email,
                        "Faculty Interview Call Letter",
                        emailBody,
                        "",
                        username
                    )

                    if (emailResult == 1) {
                        // Update mail sent status
                        app.ismailsent = true
                        app.mailsentdate = new Date()
                        app.save(flush: true, failOnError: true)
                        emailsSent++
                    } else {
                        emailsFailed++
                        failedEmails.add(app.recapplication.recapplicant.email)
                    }

                } catch (Exception e) {
                    println("Error sending email to ${app.recapplication.recapplicant.email}: ${e.message}")
                    emailsFailed++
                    failedEmails.add(app.recapplication.recapplicant.email)
                }
            }

            hm.emailsSent = emailsSent
            hm.emailsFailed = emailsFailed
            hm.failedEmails = failedEmails
            hm.msg = "Interview call letters sent successfully. Sent: ${emailsSent}, Failed: ${emailsFailed}"
            hm.flag = true

        } catch (Exception e) {
            println("Error in sendInterviewCallLetters: ${e.message}")
            e.printStackTrace()
            hm.msg = "Error sending interview call letters: ${e.message}"
            hm.flag = false
        }
    }

    /**
     * Preview interview call letters
     * Returns list of candidates with their interview details for preview
     */
    def previewCallLetters(hm, request, data) {
        try {
            def inst = hm.remove("inst")
            def org = hm.remove("org")

            if (!inst || !org) {
                hm.msg = "Instructor or Organization not found"
                hm.flag = false
                return
            }

            // Get parameters
            def recversionId = data.version

            if (!recversionId) {
                hm.msg = "Recruitment Version ID is required"
                hm.flag = false
                return
            }

            // Get recruitment version
            RecVersion recversion = RecVersion.get(recversionId)
            if (!recversion) {
                hm.msg = "Recruitment Version not found with ID: ${recversionId}"
                hm.flag = false
                return
            }

            // Get last authority (Management)
            RecAuthorityType auth = RecAuthorityType.findByIslastauthorityAndOrganization(true, org)
            if (!auth) {
                hm.msg = "Management authority not found for organization"
                hm.flag = false
                return
            }

            // Get all candidates called for interview
            def selectedbymgmt = RecApplicationStatus.findAllByIscalledforinterviewAndOrganizationAndRecauthoritytypeAndRecversion(
                true, org, auth, recversion
            )

            if (selectedbymgmt.size() == 0) {
                hm.msg = "Call letter preview generated successfully"
                hm.flag = true
                hm.callLetterList = []
                hm.totalCandidates = 0
                return
            }

            def callLetterList = []

            // Prepare preview data for each candidate
            for (app in selectedbymgmt) {
                try {
                    // Skip if version doesn't match
                    if (recversion?.id != app?.recapplication?.recversion?.id) {
                        continue
                    }

                    // Get interview schedule for department
                    RecInterviewScheduleDetails sche = RecInterviewScheduleDetails.findByOrganizationAndDepartmentAndRecversion(
                        org, app?.recbranch?.program?.department, recversion
                    )

                    if (!sche) {
                        continue
                    }

                    // Format interview date
                    SimpleDateFormat df = new SimpleDateFormat("dd/MMM/yyyy")
                    String interviewDate = df.format(sche?.interview_date)

                    // Get applicant details
                    def applicant = app.recapplication.recapplicant

                    // Prepare call letter data
                    def callLetter = [
                        applicant: [
                            id: applicant?.id,
                            email: applicant?.email,
                            name: applicant?.fullname ?: "Unknown"
                        ],
                        branch: [
                            id: app.recbranch?.id,
                            name: app.recbranch?.name ?: "Unknown"
                        ],
                        department: [
                            id: app.recbranch?.program?.department?.id,
                            name: app.recbranch?.program?.department?.name ?: "Unknown"
                        ],
                        interview: [
                            date: interviewDate,
                            venue: sche?.interview_venue,
                            time: sche?.interview_time
                        ],
                        mailStatus: [
                            sent: app.ismailsent,
                            sentDate: app.mailsentdate
                        ]
                    ]

                    callLetterList.add(callLetter)

                } catch (Exception e) {
                    println("Error preparing preview for application ${app.id}: ${e.message}")
                }
            }

            hm.callLetterList = callLetterList
            hm.totalCandidates = callLetterList.size()
            hm.msg = "Call letter preview generated successfully"
            hm.flag = true

        } catch (Exception e) {
            println("Error in previewCallLetters: ${e.message}")
            e.printStackTrace()
            hm.msg = "Error generating call letter preview: ${e.message}"
            hm.flag = false
        }
    }

}
