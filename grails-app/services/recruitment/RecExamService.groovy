package recruitment

import grails.gorm.transactions.Transactional
import java.security.SecureRandom

/**
 * RecExamService
 * Phase 1: Core Exam Management
 * 
 * Handles:
 * - Generate secret codes for exam
 * - Fetch exam applicant data
 * - Allocate questions to applicants
 */
@Transactional
class RecExamService {
    
    // Inject InformationService
    def informationService

    /**
     * Generate secret codes for shortlisted candidates
     * Used by: POST /recExam/generateSecretCodes
     */
    def generateSecretCodes(hm, request, data) {
        try {
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
            
            Organization org = instructor.organization
            if (!org) {
                hm.msg = "Organization not found"
                hm.flag = false
                return
            }
            
            // Get current recruitment version
            RecVersion recversion = RecVersion.findByOrganizationAndIscurrentforbackendprocessing(org, true)
            if (!recversion) {
                hm.msg = "Current recruitment version not found"
                hm.flag = false
                return
            }
            
            // Get shortlisted candidates (called for interview by any authority)
            RecAuthorityType auth = RecAuthorityType.findByTypeAndOrganization("Management", org)
            if (!auth) {
                hm.msg = "Management authority type not found"
                hm.flag = false
                return
            }
            
            // Get shortlisted candidates (called for interview by management)
            def selectedbymgmt = RecApplicationStatus.findAllByIscalledforinterviewAndOrganizationAndRecauthoritytype(
                true, org, auth)

            
            // Build branch to group mapping
            def recdeptgrp = RecDeptGroup.findAllByOrganization(org)
            HashMap branch_grp = new HashMap()
            for (deptgrp in recdeptgrp) {
                for (dept in deptgrp.department) {
                    branch_grp.put(dept.id, deptgrp.groupno)
                }
            }
            
            ArrayList statussched = new ArrayList()
            int createdCount = 0
            int existingCount = 0
            
            for (app in selectedbymgmt) {
                // Check if application belongs to current version
                if (recversion.id != app.recapplication.recversion.id) {
                    continue
                }
                
                def deptId = app.recbranch?.program?.department?.id
                def groupNo = branch_grp.get(deptId)
                
                if (groupNo == null) {
                    continue
                }
                
                ArrayList rec = new ArrayList()
                rec.add(app.recapplication) // 0..app and user
                rec.add(app.recbranch) // 1..branch
                rec.add(groupNo) // 2..group
                statussched.add(rec)
                
                // Find department group
                RecDeptGroup deptGroup = RecDeptGroup.findByOrganizationAndGroupno(org, groupNo)
                
                if (!deptGroup) {
                    continue
                }
                
                // Check if secret code already exists
                ERPMCQExamSecretCode secretCode = ERPMCQExamSecretCode.findByOrganizationAndRecapplicantAndRecapplicationAndRecversionAndRecdeptgroup(
                    org, app.recapplication.recapplicant, app.recapplication, recversion, deptGroup)
                
                if (secretCode == null) {
                    // Create new secret code
                    secretCode = new ERPMCQExamSecretCode()
                    secretCode.username = uid
                    secretCode.creation_date = new Date()
                    secretCode.updation_date = new Date()
                    secretCode.creation_ip_address = request.getRemoteAddr()
                    secretCode.updation_ip_address = request.getRemoteAddr()
                    
                    secretCode.obtained_score = 0.0
                    
                    // Generate unique secret code
                    String code = ""
                    while (true) {
                        code = randomString(6)
                        if (ERPMCQExamSecretCode.findAllBySecret_code(code).size() == 0) {
                            break
                        }
                    }
                    secretCode.secret_code = code
                    secretCode.isexamgiven = false
                    secretCode.examgivendate = null
                    secretCode.start_time = null
                    secretCode.end_time = null
                    secretCode.extra_time = 0
                    
                    secretCode.organization = org
                    secretCode.recapplicant = app.recapplication.recapplicant
                    secretCode.recapplication = app.recapplication
                    secretCode.recversion = recversion
                    secretCode.recdeptgroup = deptGroup
                    secretCode.save(flush: true, failOnError: true)
                    
                    createdCount++
                } else {
                    existingCount++
                }
            }
            
            hm.statussched = statussched
            hm.recversion = [
                id: recversion.id,
                version_number: recversion.version_number,
                version_date: recversion.version_date
            ]
            hm.createdCount = createdCount
            hm.existingCount = existingCount
            hm.totalCount = statussched.size()
            hm.msg = "Secret codes generated: ${createdCount} new, ${existingCount} existing"
            hm.flag = true
            
        } catch (Exception e) {
            println("Error in generateSecretCodes: ${e.message}")
            e.printStackTrace()
            hm.msg = "Error generating secret codes: ${e.message}"
            hm.flag = false
        }
    }
    
    /**
     * Get exam applicant data
     * Used by: GET /recExam/getExamApplicants
     */
    def getExamApplicants(hm, request, data) {
        try {
            def uid = hm.remove("uid")
            def groupId = data?.groupId
            
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
            
            Organization org = instructor.organization
            if (!org) {
                hm.msg = "Organization not found"
                hm.flag = false
                return
            }
            
            // Get current recruitment version
            RecVersion recversion = RecVersion.findByOrganizationAndIscurrentforbackendprocessing(org, true)
            if (!recversion) {
                hm.msg = "Current recruitment version not found"
                hm.flag = false
                return
            }
            
            // Get exam applicant data
            def examapplicantdata
            if (groupId) {
                def grp = RecDeptGroup.findById(groupId as Long)
                if (!grp) {
                    hm.msg = "Group not found"
                    hm.flag = false
                    return
                }
                examapplicantdata = ERPMCQExamSecretCode.findAllByRecversionAndOrganizationAndRecdeptgroup(
                    recversion, org, grp)
            } else {
                examapplicantdata = ERPMCQExamSecretCode.findAllByRecversionAndOrganization(
                    recversion, org)
            }
            
            // Format response
            def formattedData = []
            examapplicantdata.each { secretCode ->
                formattedData.add([
                    id: secretCode.id,
                    secret_code: secretCode.secret_code,
                    obtained_score: secretCode.obtained_score,
                    isexamgiven: secretCode.isexamgiven,
                    examgivendate: secretCode.examgivendate,
                    start_time: secretCode.start_time,
                    end_time: secretCode.end_time,
                    extra_time: secretCode.extra_time,
                    applicant: [
                        id: secretCode.recapplicant.id,
                        fullname: secretCode.recapplicant.fullname,
                        email: secretCode.recapplicant.email,
                        mobilenumber: secretCode.recapplicant.mobilenumber
                    ],
                    application: [
                        id: secretCode.recapplication.id,
                        applicaitionid: secretCode.recapplication.applicaitionid
                    ],
                    group: [
                        id: secretCode.recdeptgroup.id,
                        groupno: secretCode.recdeptgroup.groupno
                    ]
                ])
            }
            
            hm.examapplicantdata = formattedData
            hm.totalCount = formattedData.size()
            hm.recversion = [
                id: recversion.id,
                version_number: recversion.version_number,
                version_date: recversion.version_date
            ]
            hm.msg = "Exam applicant data fetched successfully"
            hm.flag = true
            
        } catch (Exception e) {
            println("Error in getExamApplicants: ${e.message}")
            e.printStackTrace()
            hm.msg = "Error fetching exam applicants: ${e.message}"
            hm.flag = false
        }
    }
    
    /**
     * Allocate questions to applicants based on weightage
     * Used by: POST /recExam/allocateQuestions
     */
    def allocateQuestions(hm, request, data) {
        try {
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
            
            Organization org = instructor.organization
            if (!org) {
                hm.msg = "Organization not found"
                hm.flag = false
                return
            }
            
            // Get current recruitment version
            RecVersion recversion = RecVersion.findByOrganizationAndIscurrentforbackendprocessing(org, true)
            if (!recversion) {
                hm.msg = "Current recruitment version not found"
                hm.flag = false
                return
            }
            
            // Get groups and exam name
            def grps = RecDeptGroup.findAllByOrganization(org)
            def ename = ERPMCQExamName.findByNameAndOrganization("VIT-FACULTY-RECRUITMENT-ONLINE-TEST", org)
            
            if (!ename) {
                hm.msg = "Exam name not found"
                hm.flag = false
                return
            }
            
            def crses = RecCourse.findAllByErpmcqexamnameAndOrganizationAndIsDeleted(ename, org, false)
            
            if (grps.size() == 0) {
                hm.msg = "Groups not formed"
                hm.flag = false
                return
            }
            
            if (crses.size() == 0) {
                hm.msg = "Courses not added"
                hm.flag = false
                return
            }
            
            // Prepare question sets per group
            TreeMap<Integer, ArrayList> deptgrp = new TreeMap<Integer, ArrayList>()
            for (grp in grps) {
                ArrayList temp = new ArrayList()
                deptgrp.put(grp.id, temp)
            }
            
            // Check for enough questions and prepare question set
            for (grp in grps) {
                for (crs in crses) {
                    def quesincrs = ERPMCQQuestionBank.findAllByReccourseAndRecdeptgroupAndOrganizationAndErpmcqexamnameAndIsapproved(
                        crs, grp, org, ename, true)
                    
                    int calwt = 0
                    for (que in quesincrs) {
                        calwt += que.weightage
                        deptgrp.get(grp.id).add(que)
                        if (crs.totalmarksinexam == calwt) {
                            break // done
                        }
                    }
                    
                    if (crs.totalmarksinexam != calwt) {
                        hm.msg = "Not enough questions in bank for course: ${crs.course_name}. " +
                                "Entered Weightage: ${calwt}, Required: ${crs.totalmarksinexam}, Group #${grp.groupno}"
                        hm.flag = false
                        return
                    }
                }
            }
            
            // Allocate questions to applicants
            int allocatedCount = 0
            int skippedCount = 0
            
            for (grp in grps) {
                def applicants = ERPMCQExamSecretCode.findAllByOrganizationAndRecversionAndRecdeptgroup(
                    org, recversion, grp)
                
                for (user in applicants) {
                    // Check if already allocated
                    def already = ERPMCQQuestionAllocationtoApplicant.findAllByRecapplicantAndRecapplicationAndRecdeptgroup(
                        user.recapplicant, user.recapplication, user.recdeptgroup)
                    
                    if (already.size() > 0) {
                        skippedCount++
                        continue
                    }
                    
                    def setques = deptgrp.get(grp.id)
                    for (que in setques) {
                        ERPMCQQuestionAllocationtoApplicant allocation = new ERPMCQQuestionAllocationtoApplicant()
                        allocation.username = uid
                        allocation.creation_date = new Date()
                        allocation.updation_date = new Date()
                        allocation.creation_ip_address = request.getRemoteAddr()
                        allocation.updation_ip_address = request.getRemoteAddr()
                        
                        allocation.answeripaddress = null
                        allocation.organization = org
                        allocation.recversion = recversion
                        allocation.recapplicant = user.recapplicant
                        allocation.recdeptgroup = user.recdeptgroup
                        allocation.recapplication = user.recapplication
                        allocation.reccourse = que.reccourse
                        allocation.erpmcqquestionbank = que
                        allocation.studentselectedoption = null
                        allocation.save(flush: true, failOnError: true)
                        
                        allocatedCount++
                    }
                }
            }
            
            def all = ERPMCQQuestionAllocationtoApplicant.findAllByOrganizationAndRecversion(org, recversion)
            
            hm.allocatedCount = allocatedCount
            hm.skippedCount = skippedCount
            hm.totalAllocations = all.size()
            hm.msg = "Questions allocated successfully: ${allocatedCount} new allocations, ${skippedCount} skipped (already allocated)"
            hm.flag = true
            
        } catch (Exception e) {
            println("Error in allocateQuestions: ${e.message}")
            e.printStackTrace()
            hm.msg = "Error allocating questions: ${e.message}"
            hm.flag = false
        }
    }
    
    /**
     * Generate random string for secret code
     */
    private String randomString(int len) {
        final String AB = "0123456789"
        SecureRandom rnd = new SecureRandom()
        StringBuilder sb = new StringBuilder(len)
        for (int i = 0; i < len; i++) {
            sb.append(AB.charAt(rnd.nextInt(AB.length())))
        }
        return sb.toString()
    }
    
    // =====================================================
    // PHASE 2: Scheduling & Control
    // =====================================================
    
    /**
     * Get all department groups for scheduling
     * Used by: GET /recExam/getGroups
     */
    def getGroups(hm, request, data) {
        try {
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
            
            Organization org = instructor.organization
            if (!org) {
                hm.msg = "Organization not found"
                hm.flag = false
                return
            }
            
            // Get all department groups
            def grps = RecDeptGroup.findAllByOrganization(org)
            
            // Format response
            def formattedGroups = []
            grps.each { grp ->
                def depts = []
                grp.department?.each { dept ->
                    depts.add([
                        id: dept.id,
                        name: dept.name
                    ])
                }
                
                formattedGroups.add([
                    id: grp.id,
                    groupno: grp.groupno,
                    departments: depts
                ])
            }
            
            hm.groups = formattedGroups
            hm.totalCount = formattedGroups.size()
            hm.msg = "Groups fetched successfully"
            hm.flag = true
            
        } catch (Exception e) {
            println("Error in getGroups: ${e.message}")
            e.printStackTrace()
            hm.msg = "Error fetching groups: ${e.message}"
            hm.flag = false
        }
    }
    
    /**
     * Get exam schedule for a specific group
     * Used by: GET /recExam/getSchedule
     */
    def getSchedule(hm, request, data) {
        try {
            def uid = hm.remove("uid")
            def groupId = data?.groupId
            
            if (!uid) {
                hm.msg = "User not authenticated"
                hm.flag = false
                return
            }
            
            if (!groupId) {
                hm.msg = "Group ID is required"
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
            
            Organization org = instructor.organization
            if (!org) {
                hm.msg = "Organization not found"
                hm.flag = false
                return
            }
            
            // Get current recruitment version
            RecVersion recversion = RecVersion.findByOrganizationAndIscurrentforbackendprocessing(org, true)
            if (!recversion) {
                hm.msg = "Current recruitment version not found"
                hm.flag = false
                return
            }
            
            // Get group
            def grp = RecDeptGroup.findById(groupId as Long)
            if (!grp) {
                hm.msg = "Group not found"
                hm.flag = false
                return
            }
            
            // Get exam applicant data
            def examapplicantdata = ERPMCQExamSecretCode.findAllByRecversionAndOrganizationAndRecdeptgroup(
                recversion, org, grp)
            
            // Format response
            def formattedData = []
            examapplicantdata.each { secretCode ->
                formattedData.add([
                    id: secretCode.id,
                    secret_code: secretCode.secret_code,
                    start_time: secretCode.start_time,
                    end_time: secretCode.end_time,
                    extra_time: secretCode.extra_time,
                    isexamgiven: secretCode.isexamgiven,
                    obtained_score: secretCode.obtained_score,
                    examgivendate: secretCode.examgivendate,
                    applicant: [
                        id: secretCode.recapplicant.id,
                        fullname: secretCode.recapplicant.fullname,
                        email: secretCode.recapplicant.email,
                        mobilenumber: secretCode.recapplicant.mobilenumber
                    ],
                    application: [
                        id: secretCode.recapplication.id,
                        applicaitionid: secretCode.recapplication.applicaitionid
                    ]
                ])
            }
            
            hm.examapplicantdata = formattedData
            hm.group = [
                id: grp.id,
                groupno: grp.groupno
            ]
            hm.recversion = [
                id: recversion.id,
                version_number: recversion.version_number
            ]
            hm.msg = "Schedule fetched successfully"
            hm.flag = true
            
        } catch (Exception e) {
            println("Error in getSchedule: ${e.message}")
            e.printStackTrace()
            hm.msg = "Error fetching schedule: ${e.message}"
            hm.flag = false
        }
    }
    
    /**
     * Set schedule for individual candidate
     * Used by: POST /recExam/setSchedule
     */
    def setSchedule(hm, request, data) {
        try {
            def uid = hm.remove("uid")
            def secretId = data?.secretId
            def startTime = data?.startTime
            def endTime = data?.endTime
            
            if (!uid) {
                hm.msg = "User not authenticated"
                hm.flag = false
                return
            }
            
            if (!secretId) {
                hm.msg = "Secret ID is required"
                hm.flag = false
                return
            }
            
            if (!startTime || !endTime) {
                hm.msg = "Start time and end time are required"
                hm.flag = false
                return
            }
            
            // Find secret code
            def secret = ERPMCQExamSecretCode.findById(secretId as Long)
            if (!secret) {
                hm.msg = "Secret code not found"
                hm.flag = false
                return
            }
            
            // Parse dates
            Date parsedStartTime = Date.parse("yyyy-MM-dd'T'HH:mm:ss'Z'", startTime)
            Date parsedEndTime = Date.parse("yyyy-MM-dd'T'HH:mm:ss'Z'", endTime)
            
            // Update schedule
            secret.start_time = parsedStartTime
            secret.end_time = parsedEndTime
            secret.username = uid
            secret.isexamgiven = false
            secret.updation_date = new Date()
            secret.updation_ip_address = request.getRemoteAddr()
            secret.save(flush: true, failOnError: true)
            
            hm.secretCode = [
                id: secret.id,
                secret_code: secret.secret_code,
                start_time: secret.start_time,
                end_time: secret.end_time
            ]
            hm.msg = "Schedule set successfully"
            hm.flag = true
            
        } catch (Exception e) {
            println("Error in setSchedule: ${e.message}")
            e.printStackTrace()
            hm.msg = "Error setting schedule: ${e.message}"
            hm.flag = false
        }
    }
    
    /**
     * Set schedule for all candidates in a group
     * Used by: POST /recExam/setScheduleForAll
     */
    def setScheduleForAll(hm, request, data) {
        try {
            def uid = hm.remove("uid")
            def groupId = data?.groupId
            def startTime = data?.startTime
            def endTime = data?.endTime
            
            if (!uid) {
                hm.msg = "User not authenticated"
                hm.flag = false
                return
            }
            
            if (!groupId) {
                hm.msg = "Group ID is required"
                hm.flag = false
                return
            }
            
            if (!startTime || !endTime) {
                hm.msg = "Start time and end time are required"
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
            
            Organization org = instructor.organization
            if (!org) {
                hm.msg = "Organization not found"
                hm.flag = false
                return
            }
            
            // Get current recruitment version
            RecVersion recversion = RecVersion.findByOrganizationAndIscurrentforbackendprocessing(org, true)
            if (!recversion) {
                hm.msg = "Current recruitment version not found"
                hm.flag = false
                return
            }
            
            // Get group
            def grp = RecDeptGroup.findById(groupId as Long)
            if (!grp) {
                hm.msg = "Group not found"
                hm.flag = false
                return
            }
            
            // Get all exam applicants in group
            def examapplicantdata = ERPMCQExamSecretCode.findAllByRecversionAndOrganizationAndRecdeptgroup(
                recversion, org, grp)
            
            // Parse dates
            Date parsedStartTime = Date.parse("yyyy-MM-dd'T'HH:mm:ss'Z'", startTime)
            Date parsedEndTime = Date.parse("yyyy-MM-dd'T'HH:mm:ss'Z'", endTime)
            
            // Update all
            int updatedCount = 0
            for (secret in examapplicantdata) {
                secret.start_time = parsedStartTime
                secret.end_time = parsedEndTime
                secret.username = uid
                secret.updation_date = new Date()
                secret.updation_ip_address = request.getRemoteAddr()
                secret.save(flush: true, failOnError: true)
                updatedCount++
            }
            
            hm.updatedCount = updatedCount
            hm.group = [
                id: grp.id,
                groupno: grp.groupno
            ]
            hm.msg = "Schedule set for all candidates"
            hm.flag = true
            
        } catch (Exception e) {
            println("Error in setScheduleForAll: ${e.message}")
            e.printStackTrace()
            hm.msg = "Error setting schedule for all: ${e.message}"
            hm.flag = false
        }
    }
    
    /**
     * Extend exam time for a candidate
     * Used by: POST /recExam/extendTime
     */
    def extendTime(hm, request, data) {
        try {
            def uid = hm.remove("uid")
            def secretId = data?.secretId
            def extraMinutes = data?.extraMinutes
            
            if (!uid) {
                hm.msg = "User not authenticated"
                hm.flag = false
                return
            }
            
            if (!secretId) {
                hm.msg = "Secret ID is required"
                hm.flag = false
                return
            }
            
            if (!extraMinutes) {
                hm.msg = "Extra minutes is required"
                hm.flag = false
                return
            }
            
            // Find secret code
            def secret = ERPMCQExamSecretCode.findById(secretId as Long)
            if (!secret) {
                hm.msg = "Secret code not found"
                hm.flag = false
                return
            }
            
            // Extend end time
            Calendar cal = Calendar.getInstance()
            cal.setTime(secret.end_time)
            cal.add(Calendar.MINUTE, extraMinutes as Integer)
            
            secret.extra_time = secret.extra_time + (extraMinutes as Integer)
            secret.end_time = cal.getTime()
            secret.username = uid
            secret.isexamgiven = false
            secret.updation_date = new Date()
            secret.updation_ip_address = request.getRemoteAddr()
            secret.save(flush: true, failOnError: true)
            
            hm.secretCode = [
                id: secret.id,
                secret_code: secret.secret_code,
                end_time: secret.end_time,
                extra_time: extraMinutes,
                total_extra_time: secret.extra_time
            ]
            hm.msg = "Exam time extended by ${extraMinutes} minutes"
            hm.flag = true
            
        } catch (Exception e) {
            println("Error in extendTime: ${e.message}")
            e.printStackTrace()
            hm.msg = "Error extending time: ${e.message}"
            hm.flag = false
        }
    }
    
    /**
     * Stop exam and calculate result
     * Used by: POST /recExam/stopExam
     */
    def stopExam(hm, request, data) {
        try {
            def uid = hm.remove("uid")
            def secretId = data?.secretId
            
            if (!uid) {
                hm.msg = "User not authenticated"
                hm.flag = false
                return
            }
            
            if (!secretId) {
                hm.msg = "Secret ID is required"
                hm.flag = false
                return
            }
            
            // Find secret code
            def secret = ERPMCQExamSecretCode.findById(secretId as Long)
            if (!secret) {
                hm.msg = "Secret code not found"
                hm.flag = false
                return
            }
            
            // Stop exam
            secret.isexamgiven = true
            secret.examgivendate = new Date()
            secret.username = uid
            secret.updation_date = new Date()
            secret.updation_ip_address = request.getRemoteAddr()
            secret.save(flush: true, failOnError: true)
            
            // Calculate result using InformationService
            String ip = request.getRemoteAddr()
            informationService.calculateresult(secret, ip)
            
            // Reload to get updated score
            secret.refresh()
            
            hm.secretCode = [
                id: secret.id,
                secret_code: secret.secret_code,
                isexamgiven: secret.isexamgiven,
                examgivendate: secret.examgivendate,
                obtained_score: secret.obtained_score
            ]
            hm.msg = "Exam stopped and result calculated"
            hm.flag = true
            
        } catch (Exception e) {
            println("Error in stopExam: ${e.message}")
            e.printStackTrace()
            hm.msg = "Error stopping exam: ${e.message}"
            hm.flag = false
        }
    }
    
    /**
     * Start or restart exam for a candidate
     * Used by: POST /recExam/startExam
     */
    def startExam(hm, request, data) {
        try {
            def uid = hm.remove("uid")
            def secretId = data?.secretId
            
            if (!uid) {
                hm.msg = "User not authenticated"
                hm.flag = false
                return
            }
            
            if (!secretId) {
                hm.msg = "Secret ID is required"
                hm.flag = false
                return
            }
            
            // Find secret code
            def secret = ERPMCQExamSecretCode.findById(secretId as Long)
            if (!secret) {
                hm.msg = "Secret code not found"
                hm.flag = false
                return
            }
            
            // Start/restart exam
            secret.isexamgiven = false
            secret.examgivendate = new Date()
            secret.username = uid
            secret.updation_date = new Date()
            secret.updation_ip_address = request.getRemoteAddr()
            secret.save(flush: true, failOnError: true)
            
            hm.secretCode = [
                id: secret.id,
                secret_code: secret.secret_code,
                isexamgiven: secret.isexamgiven,
                start_time: secret.start_time,
                end_time: secret.end_time
            ]
            hm.msg = "Exam started/restarted successfully"
            hm.flag = true
            
        } catch (Exception e) {
            println("Error in startExam: ${e.message}")
            e.printStackTrace()
            hm.msg = "Error starting exam: ${e.message}"
            hm.flag = false
        }
    }
}
