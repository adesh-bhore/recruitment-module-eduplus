package recruitment

import grails.converters.JSON
import grails.gorm.transactions.Transactional
import java.util.zip.ZipOutputStream
import java.util.zip.ZipEntry
import java.text.SimpleDateFormat
import common.AWSBucketService

/**
 * RecApplicationService_4
 * Phase 7: Bulk Operations & Reports
 * 
 * Handles:
 * - Bulk download documents as ZIP
 * - Bulk approve applications
 * - Bulk reject applications
 * - Export applications to Excel/CSV
 */
@Transactional
class RecApplicationService_4 {

    // ═══════════════════════════════════════════════════════════════
    // Phase 7: Bulk Operations & Reports APIs
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Bulk download applicant documents as ZIP
     * Used by: POST /recApplication/bulkDownloadDocuments
     */
    def bulkDownloadDocuments(hm, request, response, data) {
        def uid = hm.remove("uid")
        def applicantId = data.applicantId
        def versionId = data.versionId
        def documentIds = data.documentIds // Array of document IDs or "All"
        def bulkType = data.bulkType // "All" or "Selected"
        
        if (!uid) {
            hm.msg = "User not authenticated"
            hm.flag = false
            return
        }
        
        if (!applicantId || !versionId) {
            hm.msg = "Applicant ID and Version ID are required"
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
        
        // Find version
        RecVersion version = RecVersion.findById(versionId)
        if (!version) {
            hm.msg = "Version not found"
            hm.flag = false
            return
        }
        
        // Find application
        RecApplication application = RecApplication.findByRecversionAndRecapplicant(version, applicant)
        if (!application) {
            hm.msg = "Application not found"
            hm.flag = false
            return
        }
        
        // Get AWS configuration
        AWSBucket awsBucket = AWSBucket.findByContent("documents")
        if (!awsBucket) {
            hm.msg = "AWS bucket configuration not found"
            hm.flag = false
            return
        }
        
        AWSBucketService awsBucketService = new AWSBucketService()
        
        // Get document IDs
        def allDocuments = []
        if (bulkType == "All") {
            allDocuments = RecApplicantDocument.findAllByRecapplicant(applicant)*.id
        } else {
            allDocuments = documentIds
        }
        
        if (!allDocuments || allDocuments.isEmpty()) {
            hm.msg = "No documents found"
            hm.flag = false
            return
        }
        
        // Prepare ZIP file
        def safeEmail = applicant.email?.replaceAll("[^a-zA-Z0-9]", "_")
        def zipFilename = "${applicant.fullname}_${safeEmail}_allRecruitmentDocuments.zip"
        
        response.setContentType('APPLICATION/OCTET-STREAM')
        response.setHeader('Content-Disposition', "Attachment;Filename=${zipFilename}")
        
        ZipOutputStream zip = new ZipOutputStream(response.outputStream)
        
        int successCount = 0
        int failedCount = 0
        
        // Process each document
        for (docId in allDocuments) {
            try {
                RecApplicantDocument doc = RecApplicantDocument.findById(docId)
                if (!doc) continue
                
                // Get presigned URL
                def awsFilePath = "cloud/${doc.filepath}${doc.filename}"
                String presignedUrl = awsBucketService.getPresignedUrl(
                    awsBucket.bucketname, 
                    awsFilePath, 
                    awsBucket.region
                )
                
                if (presignedUrl) {
                    presignedUrl = presignedUrl.replace("\\", "/")
                    
                    // Download file from AWS
                    def url = new URL(presignedUrl)
                    def connection = url.openConnection()
                    def dataStream = connection.inputStream
                    
                    if (dataStream) {
                        // Extract file extension
                        def ext = doc.filename.split("\\.").last()
                        
                        // Create ZIP entry with safe filename
                        def entryName = "${doc.recdocumenttype?.type}_${application.applicaitionid}_${safeEmail}.${ext}"
                        def zipEntry = new ZipEntry(entryName)
                        
                        zip.putNextEntry(zipEntry)
                        zip.write(dataStream.bytes)
                        zip.closeEntry()
                        
                        successCount++
                    }
                }
            } catch (Exception e) {
                println("Error processing document ${docId}: ${e.message}")
                failedCount++
            }
        }
        
        zip.close()
        
        // Note: Cannot set hm values after streaming response
        // Response is already sent to client
    }
    
    /**
     * Bulk approve applications
     * Used by: POST /recApplication/bulkApproveApplications
     */
    def bulkApproveApplications(hm, request, data) {
        def uid = hm.remove("uid")
        def applicationIds = data.applicationIds // Array of application IDs
        def authorityTypeId = data.authorityTypeId
        def branchId = data.branchId // Optional
        def remark = data.remark // Optional
        
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
        
        if (!authorityTypeId) {
            hm.msg = "Authority type is required"
            hm.flag = false
            return
        }
        
        // Find instructor
        Login login = Login.findByUsername(uid)
        Instructor instructor = Instructor.findByUid(login.username)
        Organization organization = instructor.organization
        
        // Find authority type
        RecAuthorityType authorityType = RecAuthorityType.findById(authorityTypeId)
        if (!authorityType) {
            hm.msg = "Authority type not found"
            hm.flag = false
            return
        }
        
        // Find approved status master
        RecApplicationStatusMaster approvedStatus = RecApplicationStatusMaster.findByStatusAndOrganization('approved', organization)
        if (!approvedStatus) {
            hm.msg = "Approved status master not found"
            hm.flag = false
            return
        }
        
        // Find branch if specified
        RecBranch branch = null
        if (branchId) {
            branch = RecBranch.findById(branchId)
        }
        
        int successCount = 0
        int failedCount = 0
        def failedApplications = []
        
        // Process each application
        for (appId in applicationIds) {
            try {
                RecApplication application = RecApplication.findById(appId)
                if (!application) {
                    failedCount++
                    failedApplications.add([id: appId, reason: "Application not found"])
                    continue
                }
                
                // Find application status
                RecApplicationStatus appStatus
                if (branch) {
                    appStatus = RecApplicationStatus.findByOrganizationAndRecapplicationAndRecauthoritytypeAndRecbranch(
                        organization, application, authorityType, branch)
                } else {
                    appStatus = RecApplicationStatus.findByOrganizationAndRecapplicationAndRecauthoritytype(
                        organization, application, authorityType)
                }
                
                if (!appStatus) {
                    failedCount++
                    failedApplications.add([id: appId, reason: "Application status not found"])
                    continue
                }
                
                // Update status to approved
                appStatus.recapplicationstatusmaster = approvedStatus
                appStatus.approvedby = instructor
                appStatus.approve_date = new Date()
                appStatus.remark = remark ?: "Bulk approved"
                appStatus.username = uid
                appStatus.updation_date = new Date()
                appStatus.updation_ip_address = request.getRemoteAddr()
                appStatus.save(failOnError: true, flush: true)
                
                successCount++
                
            } catch (Exception e) {
                println("Error approving application ${appId}: ${e.message}")
                failedCount++
                failedApplications.add([id: appId, reason: e.message])
            }
        }
        
        hm.successCount = successCount
        hm.failedCount = failedCount
        hm.failedApplications = failedApplications
        hm.msg = "Bulk approval completed: ${successCount} succeeded, ${failedCount} failed"
        hm.flag = true
    }
    
    /**
     * Bulk reject applications
     * Used by: POST /recApplication/bulkRejectApplications
     */
    def bulkRejectApplications(hm, request, data) {
        def uid = hm.remove("uid")
        def applicationIds = data.applicationIds // Array of application IDs
        def rejectionReason = data.rejectionReason
        def authorityTypeId = data.authorityTypeId // Optional
        def branchId = data.branchId // Optional
        
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
        
        if (!rejectionReason) {
            hm.msg = "Rejection reason is required"
            hm.flag = false
            return
        }
        
        // Find instructor
        Login login = Login.findByUsername(uid)
        Instructor instructor = Instructor.findByUid(login.username)
        Organization organization = instructor.organization
        
        // Find rejected status master
        RecApplicationStatusMaster rejectedStatus = RecApplicationStatusMaster.findByStatusAndOrganization('rejected', organization)
        if (!rejectedStatus) {
            hm.msg = "Rejected status master not found"
            hm.flag = false
            return
        }
        
        int successCount = 0
        int failedCount = 0
        def failedApplications = []
        
        // Process each application
        for (appId in applicationIds) {
            try {
                RecApplication application = RecApplication.findById(appId)
                if (!application) {
                    failedCount++
                    failedApplications.add([id: appId, reason: "Application not found"])
                    continue
                }
                
                // Mark application as rejected
                application.isrejected = true
                application.save(failOnError: true, flush: true)
                
                // Update all status records to rejected
                def statusList = RecApplicationStatus.findAllByRecapplication(application)
                
                for (RecApplicationStatus status : statusList) {
                    // Apply filters if specified
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
                
                successCount++
                
            } catch (Exception e) {
                println("Error rejecting application ${appId}: ${e.message}")
                failedCount++
                failedApplications.add([id: appId, reason: e.message])
            }
        }
        
        hm.successCount = successCount
        hm.failedCount = failedCount
        hm.failedApplications = failedApplications
        hm.msg = "Bulk rejection completed: ${successCount} succeeded, ${failedCount} failed"
        hm.flag = true
    }
    
    /**
     * Export applications to Excel/CSV
     * Used by: GET /recApplication/exportApplications
     */
    def exportApplications(hm, request, response, data) {
        def uid = hm.remove("uid")
        def versionId = data.versionId
        def authorityType = data.authorityType // Optional filter
        def status = data.status // Optional filter
        def branchId = data.branchId // Optional filter
        def format = data.format ?: "csv" // csv or excel
        
        if (!uid) {
            hm.msg = "User not authenticated"
            hm.flag = false
            return
        }
        
        // Find instructor
        Login login = Login.findByUsername(uid)
        Instructor instructor = Instructor.findByUid(login.username)
        Organization organization = instructor.organization
        
        // Find version
        RecVersion version
        if (versionId) {
            version = RecVersion.findById(versionId)
        } else {
            version = RecVersion.findByOrganizationAndIscurrentforbackendprocessing(organization, true)
        }
        
        if (!version) {
            hm.msg = "Version not found"
            hm.flag = false
            return
        }
        
        // Get applications
        def applications = RecApplication.findAllByRecversionAndOrganizationAndIsfeespaid(
            version, organization, true)
        
        // Apply filters
        def filteredApplications = []
        for (RecApplication app : applications) {
            def statusRecords = RecApplicationStatus.findAllByRecapplication(app)
            
            // Filter by status
            if (status) {
                def hasStatus = statusRecords.any { it.recapplicationstatusmaster?.status == status }
                if (!hasStatus) continue
            }
            
            // Filter by branch
            if (branchId) {
                def hasBranch = app.recbranch.any { it.id == branchId.toLong() }
                if (!hasBranch) continue
            }
            
            filteredApplications.add(app)
        }
        
        // Generate CSV
        def filename = "applications_export_${new SimpleDateFormat('yyyyMMdd_HHmmss').format(new Date())}.csv"
        response.setContentType('text/csv')
        response.setHeader('Content-Disposition', "Attachment;Filename=${filename}")
        
        def writer = response.writer
        
        // CSV Header
        writer.write("Application ID,Applicant Name,Email,Mobile,Category,Application Date,Fees Paid,Status,Posts,Branches\n")
        
        // CSV Data
        for (RecApplication app : filteredApplications) {
            def applicant = app.recapplicant
            def statusRecords = RecApplicationStatus.findAllByRecapplication(app)
            def currentStatus = statusRecords?.first()?.recapplicationstatusmaster?.status ?: 'inprocess'
            
            def posts = app.recpost*.designation?.join("; ")
            def branches = app.recbranch*.name?.join("; ")
            
            writer.write("${app.applicaitionid},")
            writer.write("\"${applicant.fullname}\",")
            writer.write("${applicant.email},")
            writer.write("${applicant.mobilenumber},")
            writer.write("${applicant.reccategory?.name ?: '-'},")
            writer.write("${new SimpleDateFormat('yyyy-MM-dd').format(app.applicationdate)},")
            writer.write("${app.isfeespaid ? 'Yes' : 'No'},")
            writer.write("${currentStatus},")
            writer.write("\"${posts}\",")
            writer.write("\"${branches}\"\n")
        }
        
        writer.flush()
        writer.close()
        
        // Note: Cannot set hm values after streaming response
    }
}
