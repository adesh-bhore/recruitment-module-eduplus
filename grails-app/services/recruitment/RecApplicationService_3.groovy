package recruitment

import grails.converters.JSON
import java.text.SimpleDateFormat

/**
 * RecApplicationService_3
 * Phase 6: Qualification & Experience Management
 * 
 * Handles:
 * - Get qualification details
 * - Update qualifications
 * - Delete qualifications
 * - Get experience details
 * - Get degrees by exam
 */
class RecApplicationService_3 {

    // ═══════════════════════════════════════════════════════════════
    // Phase 6: Qualification & Experience Management APIs
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Get qualification details for an applicant
     * Used by: GET /recApplication/getQualificationDetails
     */
    def getQualificationDetails(hm, request, data) {
        try {
            def uid = hm.remove("uid")
            def applicantId = data.applicantId
            
            if (!uid) {
                hm.msg = "User not authenticated"
                hm.flag = false
                return
            }
            
            if (!applicantId) {
                hm.msg = "Applicant ID is required"
                hm.flag = false
                return
            }
            
            // Find applicant
            RecApplicant applicant = RecApplicant.findById(applicantId)
            if (!applicant) {
                hm.msg = "Applicant not found"
                hm.flag = false
                return
            }
            
            // Get all qualifications
            def qualificationList = RecApplicantAcademics.findAllByRecapplicant(applicant)
            
            // Organize by degree type
            def qualifications = [
                ug: null,
                pg: null,
                phd: null,
                setnet: null,
                others: []
            ]
            
            for (RecApplicantAcademics q : qualificationList) {
                def qualData = [
                    id: q.id,
                    degree: q.recdegree?.name,
                    degreeName: q.recdegreename?.name ?: q.name_of_degree,
                    yearOfPassing: q.yearofpassing,
                    class: q.recclass?.name,
                    university: q.university,
                    branch: q.branch,
                    marks: q.cpi_marks,
                    degreeStatus: q.recdegreestatus?.name
                ]
                
                if (q.recdegree) {
                    switch (q.recdegree.name) {
                        case 'UG':
                            qualifications.ug = qualData
                            break
                        case 'PG':
                            qualifications.pg = qualData
                            break
                        case 'Ph.D.':
                            qualifications.phd = qualData
                            break
                        case 'SET/NET':
                            qualifications.setnet = qualData
                            break
                        default:
                            qualifications.others.add(qualData)
                            break
                    }
                }
            }
            
            hm.qualifications = qualifications
            hm.totalCount = qualificationList.size()
            hm.msg = "Qualification details fetched successfully"
            hm.flag = true
            
        } catch (Exception e) {
            println("Error in getQualificationDetails: ${e.message}")
            e.printStackTrace()
            hm.msg = "Error fetching qualification details: ${e.message}"
            hm.flag = false
        }
    }
    
    /**
     * Update qualification details
     * Used by: POST /recApplication/updateQualification
     */
    def updateQualification(hm, request, data) {
        try {
            def uid = hm.remove("uid")
            def qualificationId = data.qualificationId
            def degreeId = data.degreeId
            def degreeNameId = data.degreeNameId
            def classId = data.classId
            def degreeStatusId = data.degreeStatusId
            def yearOfPassing = data.yearOfPassing
            def university = data.university
            def branch = data.branch
            def cpiMarks = data.cpiMarks
            
            if (!uid) {
                hm.msg = "User not authenticated"
                hm.flag = false
                return
            }
            
            if (!qualificationId) {
                hm.msg = "Qualification ID is required"
                hm.flag = false
                return
            }
            
            // Find qualification record
            RecApplicantAcademics qualification = RecApplicantAcademics.findById(qualificationId)
            if (!qualification) {
                hm.msg = "Qualification not found"
                hm.flag = false
                return
            }
            
            // Update fields
            if (degreeId) {
                RecDegree degree = RecDegree.findById(degreeId)
                qualification.recdegree = degree
            }
            
            if (degreeNameId) {
                RecDegreeName degreeName = RecDegreeName.findById(degreeNameId)
                qualification.recdegreename = degreeName
            }
            
            if (classId) {
                RecClass recClass = RecClass.findById(classId)
                qualification.recclass = recClass
            }
            
            if (degreeStatusId) {
                RecDegreeStatus degreeStatus = RecDegreeStatus.findById(degreeStatusId)
                qualification.recdegreestatus = degreeStatus
            }
            
            if (yearOfPassing) {
                qualification.yearofpassing = yearOfPassing
            }
            
            if (university) {
                qualification.university = university
            }
            
            if (branch) {
                qualification.branch = branch
            }
            
            if (cpiMarks) {
                qualification.cpi_marks = Double.parseDouble(cpiMarks.toString())
            }
            
            qualification.username = uid
            qualification.updation_date = new Date()
            qualification.updation_ip_address = request.getRemoteAddr()
            qualification.save(failOnError: true, flush: true)
            
            hm.msg = "Qualification updated successfully"
            hm.flag = true
            hm.qualificationId = qualification.id
            
        } catch (Exception e) {
            println("Error in updateQualification: ${e.message}")
            e.printStackTrace()
            hm.msg = "Error updating qualification: ${e.message}"
            hm.flag = false
        }
    }
    
    /**
     * Delete qualification
     * Used by: POST /recApplication/deleteQualification
     */
    def deleteQualification(hm, request, data) {
        try {
            def uid = hm.remove("uid")
            def qualificationId = data.qualificationId
            
            if (!uid) {
                hm.msg = "User not authenticated"
                hm.flag = false
                return
            }
            
            if (!qualificationId) {
                hm.msg = "Qualification ID is required"
                hm.flag = false
                return
            }
            
            // Find qualification record
            RecApplicantAcademics qualification = RecApplicantAcademics.findById(qualificationId)
            if (!qualification) {
                hm.msg = "Qualification not found"
                hm.flag = false
                return
            }
            
            def applicantId = qualification.recapplicant.id
            
            // Delete qualification
            qualification.delete(flush: true, failOnError: true)
            
            hm.msg = "Qualification deleted successfully"
            hm.flag = true
            hm.applicantId = applicantId
            
        } catch (Exception e) {
            println("Error in deleteQualification: ${e.message}")
            e.printStackTrace()
            hm.msg = "Error deleting qualification: ${e.message}"
            hm.flag = false
        }
    }
    
    /**
     * Get experience details for an applicant
     * Used by: GET /recApplication/getExperienceDetails
     */
    def getExperienceDetails(hm, request, data) {
        try {
            def uid = hm.remove("uid")
            def applicantId = data.applicantId
            
            if (!uid) {
                hm.msg = "User not authenticated"
                hm.flag = false
                return
            }
            
            if (!applicantId) {
                hm.msg = "Applicant ID is required"
                hm.flag = false
                return
            }
            
            // Find applicant
            RecApplicant applicant = RecApplicant.findById(applicantId)
            if (!applicant) {
                hm.msg = "Applicant not found"
                hm.flag = false
                return
            }
            
            // Get all experience records
            def experienceList = RecExperience.findAllByRecapplicant(applicant)
            
            // Calculate totals by type
            int teachingYears = 0
            int teachingMonths = 0
            int industrialYears = 0
            int industrialMonths = 0
            int nonTeachingYears = 0
            int nonTeachingMonths = 0
            
            def experiences = []
            
            for (RecExperience exp : experienceList) {
                def expData = [
                    id: exp.id,
                    type: exp.recexperiencetype?.type,
                    organization: exp.organization,
                    designation: exp.designation,
                    fromDate: exp.from_date,
                    toDate: exp.to_date,
                    years: exp.no_of_years ?: 0,
                    months: exp.no_of_months ?: 0,
                    responsibilities: exp.responsibilities
                ]
                experiences.add(expData)
                
                // Calculate totals
                if (exp.recexperiencetype?.type == 'Teaching') {
                    teachingYears += (exp.no_of_years ?: 0)
                    teachingMonths += (exp.no_of_months ?: 0)
                } else if (exp.recexperiencetype?.type == 'Industrial/Research') {
                    industrialYears += (exp.no_of_years ?: 0)
                    industrialMonths += (exp.no_of_months ?: 0)
                } else if (exp.recexperiencetype?.type == 'Non-Teaching') {
                    nonTeachingYears += (exp.no_of_years ?: 0)
                    nonTeachingMonths += (exp.no_of_months ?: 0)
                }
            }
            
            // Normalize months to years
            if (teachingMonths >= 12) {
                teachingYears += (teachingMonths / 12).intValue()
                teachingMonths = teachingMonths % 12
            }
            
            if (industrialMonths >= 12) {
                industrialYears += (industrialMonths / 12).intValue()
                industrialMonths = industrialMonths % 12
            }
            
            if (nonTeachingMonths >= 12) {
                nonTeachingYears += (nonTeachingMonths / 12).intValue()
                nonTeachingMonths = nonTeachingMonths % 12
            }
            
            hm.experiences = experiences
            hm.totalCount = experienceList.size()
            hm.summary = [
                teaching: [years: teachingYears, months: teachingMonths],
                industrial: [years: industrialYears, months: industrialMonths],
                nonTeaching: [years: nonTeachingYears, months: nonTeachingMonths]
            ]
            hm.msg = "Experience details fetched successfully"
            hm.flag = true
            
        } catch (Exception e) {
            println("Error in getExperienceDetails: ${e.message}")
            e.printStackTrace()
            hm.msg = "Error fetching experience details: ${e.message}"
            hm.flag = false
        }
    }
    
    /**
     * Get degrees by exam/degree type
     * Used by: GET /recApplication/getDegreesByExam
     */
    def getDegreesByExam(hm, request, data) {
        try {
            def uid = hm.remove("uid")
            def examId = data.examId
            def organizationId = data.organizationId
            
            if (!uid) {
                hm.msg = "User not authenticated"
                hm.flag = false
                return
            }
            
            if (!examId || !organizationId) {
                hm.msg = "Exam ID and Organization ID are required"
                hm.flag = false
                return
            }
            
            // Find degree
            RecDegree recDegree = RecDegree.findById(examId)
            if (!recDegree) {
                hm.msg = "Degree not found"
                hm.flag = false
                return
            }
            
            // Find organization
            Organization organization = Organization.findById(organizationId)
            if (!organization) {
                hm.msg = "Organization not found"
                hm.flag = false
                return
            }
            
            // Get all degree names for this degree type
            def degreeNames = RecDegreeName.findAllByRecdegree(recDegree)
            
            // Filter by organization and format response
            def degreeList = []
            for (RecDegreeName degreeName : degreeNames) {
                if (degreeName.organization?.id == organization.id) {
                    degreeList.add([
                        id: degreeName.id,
                        name: degreeName.name,
                        isRequired: degreeName.recdegree?.required ?: false
                    ])
                }
            }
            
            hm.degrees = degreeList
            hm.totalCount = degreeList.size()
            hm.msg = "Degrees fetched successfully"
            hm.flag = true
            
        } catch (Exception e) {
            println("Error in getDegreesByExam: ${e.message}")
            e.printStackTrace()
            hm.msg = "Error fetching degrees: ${e.message}"
            hm.flag = false
        }
    }
}
