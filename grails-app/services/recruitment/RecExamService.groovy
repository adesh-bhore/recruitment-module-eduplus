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
    
    // =====================================================
    // PHASE 3: Supervision
    // =====================================================
    
    /**
     * Get current exams (today's exams for supervisor dashboard)
     * Used by: GET /recExam/getCurrentExams
     */
    def getCurrentExams(hm, request, data) {
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
            
            // Get all exam applicant data
            def examapplicantdata = ERPMCQExamSecretCode.findAllByRecversionAndOrganization(recversion, org)
            
            // Filter for today's exams
            ArrayList currentexam = new ArrayList()
            Date today = new Date()
            
            for (app in examapplicantdata) {
                if (app.start_time == null) {
                    continue
                }
                
                Date examdate = app.start_time
                
                // Check if exam is scheduled for today
                if (examdate.getDate() == today.getDate() && 
                    examdate.getMonth() == today.getMonth() && 
                    examdate.getYear() == today.getYear()) {
                    
                    currentexam.add([
                        id: app.id,
                        secret_code: app.secret_code,
                        start_time: app.start_time,
                        end_time: app.end_time,
                        extra_time: app.extra_time,
                        isexamgiven: app.isexamgiven,
                        obtained_score: app.obtained_score,
                        examgivendate: app.examgivendate,
                        applicant: [
                            id: app.recapplicant.id,
                            fullname: app.recapplicant.fullname,
                            email: app.recapplicant.email,
                            mobilenumber: app.recapplicant.mobilenumber
                        ],
                        application: [
                            id: app.recapplication.id,
                            applicaitionid: app.recapplication.applicaitionid
                        ],
                        group: [
                            id: app.recdeptgroup.id,
                            groupno: app.recdeptgroup.groupno
                        ]
                    ])
                }
            }
            
            hm.currentexam = currentexam
            hm.totalCount = currentexam.size()
            hm.recversion = [
                id: recversion.id,
                version_number: recversion.version_number
            ]
            hm.msg = "Current exams fetched successfully"
            hm.flag = true
            
        } catch (Exception e) {
            println("Error in getCurrentExams: ${e.message}")
            e.printStackTrace()
            hm.msg = "Error fetching current exams: ${e.message}"
            hm.flag = false
        }
    }
    
    /**
     * Get all instructors and supervisors
     * Used by: GET /recExam/getSupervisors
     */
    def getSupervisors(hm, request, data) {
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
            
            // Get all instructors
            def allemployeesforsupervision = Instructor.findAllByOrganization(org)
            
            // Build supervisor list
            ArrayList supervisorlist = new ArrayList()
            ArrayList allInstructorsList = new ArrayList()
            
            for (emp in allemployeesforsupervision) {
                Login l = Login.findByUsername(emp.uid)
                if (l == null) {
                    continue
                }
                
                // Construct full name from person
                String fullname = emp.uid
                if (emp.person) {
                    def nameParts = []
                    if (emp.person.firstName) nameParts.add(emp.person.firstName)
                    if (emp.person.middleName) nameParts.add(emp.person.middleName)
                    if (emp.person.lastName) nameParts.add(emp.person.lastName)
                    if (nameParts.size() > 0) {
                        fullname = nameParts.join(' ')
                    }
                }
                
                boolean isSupervisor = false
                for (role in l.roles) {
                    if (role.role == "Supervisor") {
                        isSupervisor = true
                        supervisorlist.add([
                            id: emp.id,
                            uid: emp.uid,
                            fullname: fullname,
                            email: emp.official_email_id ?: emp.person?.email
                        ])
                        break
                    }
                }
                
                allInstructorsList.add([
                    id: emp.id,
                    uid: emp.uid,
                    fullname: fullname,
                    email: emp.official_email_id ?: emp.person?.email,
                    isSupervisor: isSupervisor
                ])
            }
            
            hm.allInstructors = allInstructorsList
            hm.supervisorList = supervisorlist
            hm.totalInstructors = allInstructorsList.size()
            hm.totalSupervisors = supervisorlist.size()
            hm.msg = "Supervisors fetched successfully"
            hm.flag = true
            
        } catch (Exception e) {
            println("Error in getSupervisors: ${e.message}")
            e.printStackTrace()
            hm.msg = "Error fetching supervisors: ${e.message}"
            hm.flag = false
        }
    }
    
    /**
     * Appoint supervisor role to an instructor
     * Used by: POST /recExam/appointSupervisor
     */
    def appointSupervisor(hm, request, data) {
        try {
            def uid = hm.remove("uid")
            def instructorId = data?.instructorId
            
            if (!uid) {
                hm.msg = "User not authenticated"
                hm.flag = false
                return
            }
            
            if (!instructorId) {
                hm.msg = "Instructor ID is required"
                hm.flag = false
                return
            }
            
            // Find instructor
            Instructor inst = Instructor.findById(instructorId as Long)
            if (!inst) {
                hm.msg = "Instructor not found"
                hm.flag = false
                return
            }
            
            // Find application type
            ApplicationType aterp = ApplicationType.findByApplication_type("ERP")
            if (!aterp) {
                hm.msg = "ERP application type not found"
                hm.flag = false
                return
            }
            
            // Find role type
            RoleType estb = RoleType.findByTypeAndApplicationtypeAndOrganization(
                "Establishment Section", aterp, inst.organization)
            if (!estb) {
                hm.msg = "Establishment Section role type not found"
                hm.flag = false
                return
            }
            
            // Find supervisor role
            Role role_estb = Role.findByRoleAndIsRoleSetAndRoletypeAndOrganization(
                "Supervisor", true, estb, inst.organization)
            if (!role_estb) {
                hm.msg = "Supervisor role not found"
                hm.flag = false
                return
            }
            
            // Add role to login
            Login l = Login.findByUsername(inst.uid)
            if (!l) {
                hm.msg = "Login not found for instructor"
                hm.flag = false
                return
            }
            
            // Check if already has supervisor role
            boolean alreadySupervisor = false
            for (role in l.roles) {
                if (role.role == "Supervisor") {
                    alreadySupervisor = true
                    break
                }
            }
            
            if (!alreadySupervisor) {
                l.addToRoles(role_estb)
                l.save(failOnError: true, flush: true)
            }
            
            // Construct full name
            String fullname = inst.uid
            if (inst.person) {
                def nameParts = []
                if (inst.person.firstName) nameParts.add(inst.person.firstName)
                if (inst.person.middleName) nameParts.add(inst.person.middleName)
                if (inst.person.lastName) nameParts.add(inst.person.lastName)
                if (nameParts.size() > 0) {
                    fullname = nameParts.join(' ')
                }
            }
            
            hm.instructor = [
                id: inst.id,
                uid: inst.uid,
                fullname: fullname,
                email: inst.official_email_id ?: inst.person?.email,
                isSupervisor: true
            ]
            hm.msg = alreadySupervisor ? "Instructor already has supervisor role" : "Supervisor role assigned successfully"
            hm.flag = true
            
        } catch (Exception e) {
            println("Error in appointSupervisor: ${e.message}")
            e.printStackTrace()
            hm.msg = "Error appointing supervisor: ${e.message}"
            hm.flag = false
        }
    }
    
    // =====================================================
    // PHASE 4: Results & Selection
    // =====================================================
    
    /**
     * Get all expert groups for result viewing
     * Used by: GET /recExam/getExpertGroups
     */
    def getExpertGroups(hm, request, data) {
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
            
            // Get expert groups
            def exprtgrp = RecDeptExpertGroup.findAllByOrganizationAndRecversion(org, recversion)
            
            // Format response
            def formattedGroups = []
            exprtgrp.each { grp ->
                def programs = []
                grp.program?.each { prog ->
                    programs.add([
                        id: prog.id,
                        name: prog.name
                    ])
                }
                
                formattedGroups.add([
                    id: grp.id,
                    cutoff: grp.cutoff,
                    groupno: grp.groupno,
                    groupname: grp.groupname,
                    deptGroup: [
                        id: grp.recdeptgroup?.id,
                        groupno: grp.recdeptgroup?.groupno
                    ],
                    programs: programs
                ])
            }
            
            hm.expertGroups = formattedGroups
            hm.totalCount = formattedGroups.size()
            hm.recversion = [
                id: recversion.id,
                version_number: recversion.version_number
            ]
            hm.msg = "Expert groups fetched successfully"
            hm.flag = true
            
        } catch (Exception e) {
            println("Error in getExpertGroups: ${e.message}")
            e.printStackTrace()
            hm.msg = "Error fetching expert groups: ${e.message}"
            hm.flag = false
        }
    }
    
    /**
     * Get results by expert group (sorted by score)
     * Used by: GET /recExam/getResultsByGroup
     */
    def getResultsByGroup(hm, request, data) {
        try {
            def uid = hm.remove("uid")
            def expertGroupId = data?.expertGroupId
            
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
            
            // Get expert group
            def xgrp = RecDeptExpertGroup.findById(expertGroupId as Long)
            if (!xgrp) {
                hm.msg = "Expert group not found"
                hm.flag = false
                return
            }
            
            // Get all exam applicant data
            def examapplicantdata = ERPMCQExamSecretCode.findAllByRecversionAndOrganization(recversion, org)
            
            // Filter by expert group criteria
            ArrayList selectedgrpwise = new ArrayList()
            
            for (app in examapplicantdata) {
                // Check if dept group matches
                if (app.recdeptgroup.id != xgrp.recdeptgroup.id) {
                    continue
                }
                
                // Check if program matches
                def xdept = xgrp.program*.id
                def appdept = app.recapplication.recbranch*.program*.id.flatten()
                xdept.retainAll(appdept)
                
                if (xdept.size() > 0) {
                    selectedgrpwise.add(app)
                }
            }
            
            // Sort by obtained_score (descending)
            selectedgrpwise.sort { -it.obtained_score }
            
            // Format response
            def formattedResults = []
            selectedgrpwise.each { app ->
                // Construct applicant name
                String applicantName = app.recapplicant.id.toString()
                if (app.recapplicant.person) {
                    def nameParts = []
                    if (app.recapplicant.person.firstName) nameParts.add(app.recapplicant.person.firstName)
                    if (app.recapplicant.person.middleName) nameParts.add(app.recapplicant.person.middleName)
                    if (app.recapplicant.person.lastName) nameParts.add(app.recapplicant.person.lastName)
                    if (nameParts.size() > 0) {
                        applicantName = nameParts.join(' ')
                    }
                }
                
                formattedResults.add([
                    id: app.id,
                    secret_code: app.secret_code,
                    obtained_score: app.obtained_score,
                    isexamgiven: app.isexamgiven,
                    examgivendate: app.examgivendate,
                    applicant: [
                        id: app.recapplicant.id,
                        fullname: applicantName,
                        email: app.recapplicant.person?.email
                    ],
                    application: [
                        id: app.recapplication.id,
                        applicaitionid: app.recapplication.applicaitionid
                    ],
                    branch: app.recapplication.recbranch?.collect { branch ->
                        [
                            id: branch.id,
                            name: branch.name
                        ]
                    }
                ])
            }
            
            hm.toppers = formattedResults
            hm.totalCount = formattedResults.size()
            hm.expertGroup = [
                id: xgrp.id,
                cutoff: xgrp.cutoff,
                groupname: xgrp.groupname
            ]
            hm.recversion = [
                id: recversion.id,
                version_number: recversion.version_number
            ]
            hm.msg = "Results fetched successfully"
            hm.flag = true
            
        } catch (Exception e) {
            println("Error in getResultsByGroup: ${e.message}")
            e.printStackTrace()
            hm.msg = "Error fetching results: ${e.message}"
            hm.flag = false
        }
    }
    
    /**
     * Save selected applications based on cutoff
     * Used by: POST /recExam/saveSelectedApplications
     */
    def saveSelectedApplications(hm, request, data) {
        try {
            def uid = hm.remove("uid")
            def expertGroupId = data?.expertGroupId
            def cutoff = data?.cutoff
            def applications = data?.applications
            
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
            
            if (cutoff == null) {
                hm.msg = "Cutoff is required"
                hm.flag = false
                return
            }
            
            if (!applications || applications.size() == 0) {
                hm.msg = "Applications list is required"
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
            
            // Get expert group
            RecDeptExpertGroup recxpertgrp = RecDeptExpertGroup.findById(expertGroupId as Long)
            if (!recxpertgrp) {
                hm.msg = "Expert group not found"
                hm.flag = false
                return
            }
            
            // Update cutoff in expert group
            recxpertgrp.cutoff = cutoff as Double
            recxpertgrp.save(flush: true, failOnError: true)
            
            // Get required entities
            def ename = ERPMCQExamName.findByNameAndOrganization("VIT-FACULTY-RECRUITMENT-ONLINE-TEST", org)
            if (!ename) {
                hm.msg = "Exam name not found"
                hm.flag = false
                return
            }
            
            RecExpertType recexperttype = RecExpertType.findByOranizationAndType(org, 'COMMON')
            if (!recexperttype) {
                hm.msg = "Expert type 'COMMON' not found"
                hm.flag = false
                return
            }
            
            RecEvaluationParameter recevaluationparameter = RecEvaluationParameter.findByParameterAndOranizationAndRecexperttypeAndRecdeptexpertgroupAndRecversion(
                'Written Test', org, recexperttype, recxpertgrp, recversion)
            if (!recevaluationparameter) {
                hm.msg = "Evaluation parameter 'Written Test' not found"
                hm.flag = false
                return
            }
            
            RecApplicationRound round1 = RecApplicationRound.findByRoundnumberAndOrganization("1", org)
            if (!round1) {
                hm.msg = "Application round 1 not found"
                hm.flag = false
                return
            }
            
            // Process each application
            int selectedCount = 0
            int rejectedCount = 0
            def results = []
            
            for (appData in applications) {
                def app = RecApplication.findById(appData.applicationId as Long)
                if (!app) {
                    continue
                }
                
                double actualmarks = appData.marks as Double
                boolean isRejected = actualmarks < (cutoff as Double)
                
                if (isRejected) {
                    rejectedCount++
                } else {
                    selectedCount++
                }
                
                // Create/update RecApplicationRoundTransaction
                RecApplicationRoundTransaction recApplicationRoundTransaction = RecApplicationRoundTransaction.findByRecdeptexpertgroupAndRecapplicationAndOrganizationAndRecapplicationroundAndRecapplicantAndRecversion(
                    recxpertgrp, app, org, round1, app.recapplicant, recversion)
                
                if (recApplicationRoundTransaction == null) {
                    recApplicationRoundTransaction = new RecApplicationRoundTransaction()
                }
                
                recApplicationRoundTransaction.recapplicationround = round1
                recApplicationRoundTransaction.recapplication = app
                recApplicationRoundTransaction.organization = org
                recApplicationRoundTransaction.recversion = recversion
                recApplicationRoundTransaction.recapplicant = app.recapplicant
                recApplicationRoundTransaction.recdeptexpertgroup = recxpertgrp
                recApplicationRoundTransaction.isrejected = isRejected
                recApplicationRoundTransaction.username = uid
                recApplicationRoundTransaction.creation_date = new Date()
                recApplicationRoundTransaction.updation_date = new Date()
                recApplicationRoundTransaction.creation_ip_address = request.getRemoteAddr()
                recApplicationRoundTransaction.updation_ip_address = request.getRemoteAddr()
                recApplicationRoundTransaction.save(failOnError: true, flush: true)
                
                // Create/update RecApplicationParameterWiseEvaluation
                RecApplicationParameterWiseEvaluation parameterWiseEvaluation = RecApplicationParameterWiseEvaluation.findByOranizationAndRecversionAndRecdeptexpertgroupAndRecevaluationparameterAndRecapplicationAndRecapplicant(
                    org, recversion, recxpertgrp, recevaluationparameter, app, app.recapplicant)
                
                if (parameterWiseEvaluation == null) {
                    parameterWiseEvaluation = new RecApplicationParameterWiseEvaluation()
                    parameterWiseEvaluation.obtained_marks = (recevaluationparameter.maxmarks * actualmarks) / ename.max_score
                    parameterWiseEvaluation.evaluation_date = new Date()
                    parameterWiseEvaluation.oranization = org
                    parameterWiseEvaluation.recversion = recversion
                    parameterWiseEvaluation.recdeptexpertgroup = recxpertgrp
                    parameterWiseEvaluation.recevaluationparameter = recevaluationparameter
                    parameterWiseEvaluation.recapplication = app
                    parameterWiseEvaluation.recapplicant = app.recapplicant
                    parameterWiseEvaluation.username = uid
                    parameterWiseEvaluation.creation_date = new Date()
                    parameterWiseEvaluation.updation_date = new Date()
                    parameterWiseEvaluation.creation_ip_address = request.getRemoteAddr()
                    parameterWiseEvaluation.updation_ip_address = request.getRemoteAddr()
                    parameterWiseEvaluation.save(failOnError: true, flush: true)
                }
                
                // Create/update RecApplicationEvaluationAvg
                RecApplicationEvaluationAvg evaluationAvg = RecApplicationEvaluationAvg.findByOranizationAndRecversionAndRecdeptexpertgroupAndRecapplicationAndRecapplicant(
                    org, recversion, recxpertgrp, app, app.recapplicant)
                
                if (evaluationAvg == null) {
                    evaluationAvg = new RecApplicationEvaluationAvg()
                    evaluationAvg.avg = (recevaluationparameter.maxmarks * actualmarks) / ename.max_score
                    evaluationAvg.oranization = org
                    evaluationAvg.recversion = recversion
                    evaluationAvg.recdeptexpertgroup = recxpertgrp
                    evaluationAvg.recapplication = app
                    evaluationAvg.recapplicant = app.recapplicant
                    evaluationAvg.username = uid
                    evaluationAvg.creation_date = new Date()
                    evaluationAvg.updation_date = new Date()
                    evaluationAvg.creation_ip_address = request.getRemoteAddr()
                    evaluationAvg.updation_ip_address = request.getRemoteAddr()
                    evaluationAvg.save(failOnError: true, flush: true)
                }
                
                // Construct applicant name
                String applicantName = app.recapplicant.id.toString()
                if (app.recapplicant.person) {
                    def nameParts = []
                    if (app.recapplicant.person.firstName) nameParts.add(app.recapplicant.person.firstName)
                    if (app.recapplicant.person.middleName) nameParts.add(app.recapplicant.person.middleName)
                    if (app.recapplicant.person.lastName) nameParts.add(app.recapplicant.person.lastName)
                    if (nameParts.size() > 0) {
                        applicantName = nameParts.join(' ')
                    }
                }
                
                results.add([
                    applicationId: app.id,
                    applicantName: applicantName,
                    marks: actualmarks,
                    status: isRejected ? 'rejected' : 'selected',
                    isRejected: isRejected
                ])
            }
            
            hm.summary = [
                totalProcessed: applications.size(),
                selected: selectedCount,
                rejected: rejectedCount,
                cutoff: cutoff
            ]
            hm.results = results
            hm.msg = "Applications processed successfully"
            hm.flag = true
            
        } catch (Exception e) {
            println("Error in saveSelectedApplications: ${e.message}")
            e.printStackTrace()
            hm.msg = "Error saving selected applications: ${e.message}"
            hm.flag = false
        }
    }
}
