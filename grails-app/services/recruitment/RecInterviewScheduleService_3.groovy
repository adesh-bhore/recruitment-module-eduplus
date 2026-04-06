package recruitment

import grails.gorm.transactions.Transactional
import java.text.SimpleDateFormat
import common.AWSBucketService

@Transactional
class RecInterviewScheduleService_3 {
    // ── Document Type Management Service Methods ─────────────────────

    /**
    * Get all RecDocumentType
    * Used by: GET /recInterviewSchedule/getRecDocumentTypeList
    */
    def getRecDocumentTypeList(hm, request) {
        def inst = hm.remove("inst")
        def org  = hm.remove("org")

        if (!inst || !org) {
            hm.msg = "Instructor or Organization not found"
            hm.flag = false
            return
        }

        def documentTypeList = RecDocumentType.list()

        hm.documentTypeList = documentTypeList.collect { dt ->
            [
                id           : dt.id,
                type         : dt.type,
                size         : dt.size,
                extension    : dt.extension,
                info         : dt.info,
                resolution   : dt.resolution,
                isactive     : dt.isactive,
                iscompulsory : dt.iscompulsory
            ]
        }

        hm.msg  = "Document type list fetched successfully"
        hm.flag = true
    }

    /**
    * Create a new RecDocumentType (duplicate check by type)
    * Used by: POST /recInterviewSchedule/saveRecDocumentType
    */
    def saveRecDocumentType(hm, request, data) {
        def inst = hm.remove("inst")
        def org  = hm.remove("org")

        if (!inst || !org) {
            hm.msg = "Instructor or Organization not found"
            hm.flag = false
            return
        }

        def type       = data.type?.toString()?.trim()
        def size       = data.size?.toString()?.trim()
        def extension  = data.extension?.toString()?.trim()
        def info       = data.info?.toString()?.trim()
        def resolution = data.resolution?.toString()?.trim()
        def isactive   = data.isactive != null ? data.isactive : true
        def iscompulsory = data.iscompulsory != null ? data.iscompulsory : true

        if (!type) {
            hm.msg = "type is required"
            hm.flag = false
            return
        }

        // Duplicate check
        RecDocumentType existing = RecDocumentType.findByType(type)
        if (existing) {
            hm.msg  = "Document type already exists"
            hm.flag = false
            return
        }

        def loginId = request.getHeader("EPC-UID")
        Login login = Login.findByUsernameAndIsloginblocked(loginId, false)

        RecDocumentType recDocumentType = new RecDocumentType(
            type                : type,
            size                : size,
            extension           : extension,
            info                : info,
            resolution          : resolution,
            isactive            : isactive,
            iscompulsory        : iscompulsory,
            username            : login?.username,
            creation_ip_address : request.getRemoteAddr(),
            creation_date       : new Date(),
            updation_ip_address : request.getRemoteAddr(),
            updation_date       : new Date()
        )
        recDocumentType.save(failOnError: true, flush: true)

        hm.msg  = "Saved successfully"
        hm.flag = true
    }

    /**
    * Update an existing RecDocumentType
    * Used by: POST /recInterviewSchedule/editRecDocumentType
    */
    def editRecDocumentType(hm, request, data) {
        def inst = hm.remove("inst")
        def org  = hm.remove("org")

        if (!inst || !org) {
            hm.msg = "Instructor or Organization not found"
            hm.flag = false
            return
        }

        def editId     = data.editId
        def type       = data.type?.toString()?.trim()
        def size       = data.size?.toString()?.trim()
        def extension  = data.extension?.toString()?.trim()
        def info       = data.info?.toString()?.trim()
        def resolution = data.resolution?.toString()?.trim()
        def isactive   = data.isactive
        def iscompulsory = data.iscompulsory

        if (!editId) { hm.msg = "editId is required"; hm.flag = false; return }

        RecDocumentType recDocumentType = RecDocumentType.get(editId)
        if (!recDocumentType) {
            hm.msg  = "Document type not found"
            hm.flag = false
            return
        }

        def loginId = request.getHeader("EPC-UID")
        Login login = Login.findByUsernameAndIsloginblocked(loginId, false)

        recDocumentType.type                = type ?: recDocumentType.type
        recDocumentType.size                = size ?: recDocumentType.size
        recDocumentType.extension           = extension ?: recDocumentType.extension
        recDocumentType.info                = info ?: recDocumentType.info
        recDocumentType.resolution          = resolution ?: recDocumentType.resolution
        recDocumentType.isactive            = isactive != null ? isactive : recDocumentType.isactive
        recDocumentType.iscompulsory        = iscompulsory != null ? iscompulsory : recDocumentType.iscompulsory
        recDocumentType.username            = login?.username
        recDocumentType.updation_ip_address = request.getRemoteAddr()
        recDocumentType.updation_date       = new Date()
        recDocumentType.save(failOnError: true, flush: true)

        hm.msg  = "Updated successfully"
        hm.flag = true
    }

    /**
    * Delete a RecDocumentType (fails gracefully if in use by FK constraint)
    * Used by: POST /recInterviewSchedule/deleteRecDocumentType
    */
    def deleteRecDocumentType(hm, request, data) {
        def inst = hm.remove("inst")
        def org  = hm.remove("org")

        if (!inst || !org) {
            hm.msg = "Instructor or Organization not found"
            hm.flag = false
            return
        }

        def deleteId = data.deleteId
        if (!deleteId) { hm.msg = "deleteId is required"; hm.flag = false; return }

        RecDocumentType recDocumentType = RecDocumentType.get(deleteId)
        if (!recDocumentType) {
            hm.msg  = "Document type not found"
            hm.flag = false
            return
        }

        try {
            recDocumentType.delete(flush: true, failOnError: true)
            hm.msg  = "Deleted successfully"
            hm.flag = true
        } catch (Exception ex) {
            hm.msg  = "Cannot be deleted. Document type may be in use."
            hm.flag = false
        }
    }

    /**
    * Toggle isActive on a RecDocumentType
    * Used by: POST /recInterviewSchedule/isActiveDocumentType
    */
    def toggleDocumentTypeActive(hm, request, data) {
        def inst = hm.remove("inst")
        def org  = hm.remove("org")

        if (!inst || !org) {
            hm.msg = "Instructor or Organization not found"
            hm.flag = false
            return
        }

        def activeId = data.ActiveId
        if (!activeId) { hm.msg = "ActiveId is required"; hm.flag = false; return }

        RecDocumentType recDocumentType = RecDocumentType.get(activeId)
        if (!recDocumentType) {
            hm.msg  = "Document type not found"
            hm.flag = false
            return
        }

        recDocumentType.isactive            = !recDocumentType.isactive
        recDocumentType.updation_ip_address = request.getRemoteAddr()
        recDocumentType.updation_date       = new Date()
        recDocumentType.save(flush: true, failOnError: true)

        hm.isactive = recDocumentType.isactive
        hm.msg      = "Status updated successfully"
        hm.flag     = true
    }


    // ── Document Viewing Service Methods ─────────────────────────────

    /**
     * Get list of applications with documents for current recruitment versions
     * Used by: GET /recInterviewSchedule/recdocumentList
     */
    def getRecDocumentList(hm, request) {
        def inst = hm.remove("inst")
        def org  = hm.remove("org")

        if (!inst || !org) {
            hm.msg = "Instructor or Organization not found"
            hm.flag = false
            return
        }

        // Get current recruitment versions
        def recversionall = RecVersion.findAllByOrganizationAndIscurrentforbackendprocessing(org, true)
        
        def applicationList = []
        
        for (rv in recversionall) {
            def allrecApplication = RecApplication.findAllByRecversionAndOrganization(rv, org)
            
            for (ra in allrecApplication) {
                try {
                    def applicantName = ra.recapplicant?.fullname ?: "Unknown"
                    def applicantEmail = ra.recapplicant?.email ?: "N/A"
                    
                    // Count documents for this applicant
                    def documentCount = RecApplicantDocument.countByRecapplicant(ra.recapplicant)
                    
                    applicationList.add([
                        id: ra.id,
                        applicant: [
                            id: ra.recapplicant?.id,
                            name: applicantName,
                            email: applicantEmail
                        ],
                        recversion: [
                            id: rv.id,
                            version_number: rv.version_number
                        ],
                        documentCount: documentCount
                    ])
                } catch (Exception e) {
                    println("Warning: Could not process application ${ra.id}: ${e.message}")
                }
            }
        }

        hm.applicationList = applicationList
        hm.totalApplications = applicationList.size()
        hm.msg  = "Document list fetched successfully"
        hm.flag = true
    }

    /**
     * Get documents for a specific applicant with presigned URLs
     * Used by: POST /recInterviewSchedule/getdoc
     */
    def getApplicantDocuments(hm, request, data) {
        def inst = hm.remove("inst")
        def org  = hm.remove("org")

        if (!inst || !org) {
            hm.msg = "Instructor or Organization not found"
            hm.flag = false
            return
        }

        def recapcntid = data.recapcntid
        
        if (!recapcntid) {
            hm.msg = "recapcntid is required"
            hm.flag = false
            return
        }

        RecApplicant recApplicant = RecApplicant.get(recapcntid)
        if (!recApplicant) {
            hm.msg = "Applicant not found"
            hm.flag = false
            return
        }

        // Get all documents for this applicant
        def rd = RecApplicantDocument.findAllByRecapplicant(recApplicant)
        
        def documentList = []
        
        // Check if AWS is configured
        AWSBucket aws = AWSBucket.findByContent("documents")
        
        if (aws) {
            // AWS is configured, generate presigned URLs
            AWSBucketService awsBucketService = new AWSBucketService()
            
            for (r in rd) {
                try {
                    if (r?.filepath != null && r?.filename) {
                        def link = "cloud/" + r.filepath + r.filename
                        String url = awsBucketService.getPresignedUrl(aws.bucketname, link, aws.region)
                        
                        documentList.add([
                            id: r.id,
                            documentType: r.recdocumenttype?.type ?: "Unknown",
                            filename: r.filename,
                            filepath: r.filepath,
                            url: url
                        ])
                    }
                } catch (Exception e) {
                    println("Warning: Could not generate URL for document ${r.id}: ${e.message}")
                    // Add document without URL
                    documentList.add([
                        id: r.id,
                        documentType: r.recdocumenttype?.type ?: "Unknown",
                        filename: r.filename,
                        filepath: r.filepath,
                        url: null,
                        error: "Could not generate presigned URL"
                    ])
                }
            }
        } else {
            // AWS not configured, return document info without URLs
            for (r in rd) {
                documentList.add([
                    id: r.id,
                    documentType: r.recdocumenttype?.type ?: "Unknown",
                    filename: r.filename,
                    filepath: r.filepath,
                    url: null,
                    note: "AWS not configured"
                ])
            }
        }

        hm.documentList = documentList
        hm.totalDocuments = documentList.size()
        hm.applicant = [
            id: recApplicant.id,
            name: recApplicant.fullname,
            email: recApplicant.email
        ]
        hm.msg  = "Documents fetched successfully"
        hm.flag = true
    }

    /**
     * Get document statistics for recruitment versions
     * Used by: GET /recInterviewSchedule/getDocumentStatistics
     */
    def getDocumentStatistics(hm, request) {
        def inst = hm.remove("inst")
        def org  = hm.remove("org")

        if (!inst || !org) {
            hm.msg = "Instructor or Organization not found"
            hm.flag = false
            return
        }

        // Get current recruitment versions
        def recversionall = RecVersion.findAllByOrganizationAndIscurrentforbackendprocessing(org, true)
        
        def totalApplications = 0
        def totalDocuments = 0
        def applicationsWithDocuments = 0
        def applicationsWithoutDocuments = 0
        
        def versionStats = []
        
        for (rv in recversionall) {
            def applications = RecApplication.findAllByRecversionAndOrganization(rv, org)
            def versionDocCount = 0
            def versionAppsWithDocs = 0
            def versionAppsWithoutDocs = 0
            
            for (ra in applications) {
                try {
                    def docCount = RecApplicantDocument.countByRecapplicant(ra.recapplicant)
                    versionDocCount += docCount
                    
                    if (docCount > 0) {
                        versionAppsWithDocs++
                    } else {
                        versionAppsWithoutDocs++
                    }
                } catch (Exception e) {
                    println("Warning: Could not count documents for application ${ra.id}")
                }
            }
            
            totalApplications += applications.size()
            totalDocuments += versionDocCount
            applicationsWithDocuments += versionAppsWithDocs
            applicationsWithoutDocuments += versionAppsWithoutDocs
            
            versionStats.add([
                recversion: [
                    id: rv.id,
                    version_number: rv.version_number
                ],
                applications: applications.size(),
                documents: versionDocCount,
                applicationsWithDocuments: versionAppsWithDocs,
                applicationsWithoutDocuments: versionAppsWithoutDocs
            ])
        }

        hm.statistics = [
            totalApplications: totalApplications,
            totalDocuments: totalDocuments,
            applicationsWithDocuments: applicationsWithDocuments,
            applicationsWithoutDocuments: applicationsWithoutDocuments,
            averageDocumentsPerApplication: totalApplications > 0 ? (totalDocuments / totalApplications).round(2) : 0
        ]
        hm.versionStats = versionStats
        hm.msg  = "Document statistics fetched successfully"
        hm.flag = true
    }

    // ────────────────────────────────────────────────────────────────
}
