package recruitment

import grails.gorm.transactions.Transactional

@Transactional
class RecVersionService_2 {

    /**
     * Get candidate list with filtering by branch and/or post
     * Returns: List of applications with candidate details, addresses, and document counts
     */
    def getCandidateList(hm, request, data) {
        try {
            def inst = hm.remove("inst")
            def org = hm.remove("org")

            if (!inst || !org) {
                hm.msg = "Instructor or Organization not found"
                hm.flag = false
                return
            }

            // Get parameters
            def ayId = data.ay
            def orgId = data.org
            def recversionId = data.recversion
            def recbranchId = data.recbranchid
            def recpostId = data.recpostid

            // Validate required parameters
            if (!ayId) {
                hm.msg = "Academic Year ID is required"
                hm.flag = false
                return
            }

            if (!recversionId) {
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

            // Get academic year
            AcademicYear academicYear = AcademicYear.get(ayId)
            if (!academicYear) {
                hm.msg = "Academic Year not found with ID: ${ayId}"
                hm.flag = false
                return
            }

            // Get recruitment version
            RecVersion recVersion = RecVersion.get(recversionId)
            if (!recVersion) {
                hm.msg = "Recruitment Version not found with ID: ${recversionId}"
                hm.flag = false
                return
            }

            // Get branch and post (optional)
            RecBranch recbranch = null
            RecPost recpost = null

            if (recbranchId && recbranchId != 'null' && recbranchId != '') {
                recbranch = RecBranch.get(recbranchId)
                if (!recbranch) {
                    hm.msg = "Branch not found with ID: ${recbranchId}"
                    hm.flag = false
                    return
                }
            }

            if (recpostId && recpostId != 'null' && recpostId != '') {
                recpost = RecPost.get(recpostId)
                if (!recpost) {
                    hm.msg = "Post not found with ID: ${recpostId}"
                    hm.flag = false
                    return
                }
            }

            // Get all paid applications for this version
            def recapplicationlist = RecApplication.findAllByOrganizationAndRecversionAndIsfeespaid(
                organization, 
                recVersion, 
                true
            )

            def finalrecapplicationlist = []
            def permanentaddressList = []
            def localaddressList = []

            // Get address types
            AddressType permanentAddressType = AddressType.findByType("Permanent")
            AddressType localAddressType = AddressType.findByType("Local")

            // Filter applications based on branch and post
            if (recpost && recbranch) {
                // Both branch and post specified
                for (application in recapplicationlist) {
                    def postlist = application.recpost
                    def branchlist = application.recbranch
                    
                    for (post in postlist) {
                        if (post.id == recpost.id) {
                            for (branch in branchlist) {
                                if (branch.id == recbranch.id) {
                                    def recapplicationdocumentcount = RecApplicantDocument.countByRecapplicant(application?.recapplicant)
                                    
                                    finalrecapplicationlist.add([
                                        application: application,
                                        branch: recbranch,
                                        post: recpost,
                                        documentCount: recapplicationdocumentcount
                                    ])

                                    // Get addresses
                                    def permanent = Address.findByRecapplicantAndAddresstype(application?.recapplicant, permanentAddressType)
                                    permanentaddressList.add(permanent)

                                    def local = Address.findByRecapplicantAndAddresstype(application?.recapplicant, localAddressType)
                                    localaddressList.add(local)
                                }
                            }
                        }
                    }
                }
            } else if (recbranch && !recpost) {
                // Only branch specified
                for (application in recapplicationlist) {
                    def rblist = application.recbranch
                    
                    if (rblist && rblist.size() > 0) {
                        for (rb in rblist) {
                            if (rb.id == recbranch.id) {
                                def recapplicationdocumentcount = RecApplicantDocument.countByRecapplicant(application?.recapplicant)
                                
                                finalrecapplicationlist.add([
                                    application: application,
                                    branch: recbranch,
                                    post: application?.recpost,
                                    documentCount: recapplicationdocumentcount
                                ])

                                // Get addresses
                                def permanent = Address.findByRecapplicantAndAddresstype(application?.recapplicant, permanentAddressType)
                                permanentaddressList.add(permanent)

                                def local = Address.findByRecapplicantAndAddresstype(application?.recapplicant, localAddressType)
                                localaddressList.add(local)
                            }
                        }
                    }
                }
            } else if (recpost && !recbranch) {
                // Only post specified
                for (application in recapplicationlist) {
                    def rblist = application.recpost
                    
                    if (rblist && rblist.size() > 0) {
                        for (rb in rblist) {
                            if (rb.id == recpost.id) {
                                def recapplicationdocumentcount = RecApplicantDocument.countByRecapplicant(application?.recapplicant)
                                
                                finalrecapplicationlist.add([
                                    application: application,
                                    branch: application?.recbranch,
                                    post: recpost,
                                    documentCount: recapplicationdocumentcount
                                ])

                                // Get addresses
                                def permanent = Address.findByRecapplicantAndAddresstype(application?.recapplicant, permanentAddressType)
                                permanentaddressList.add(permanent)

                                def local = Address.findByRecapplicantAndAddresstype(application?.recapplicant, localAddressType)
                                localaddressList.add(local)
                            }
                        }
                    }
                }
            } else {
                // No filters - return all paid applications
                for (application in recapplicationlist) {
                    def recapplicationdocumentcount = RecApplicantDocument.countByRecapplicant(application?.recapplicant)
                    
                    finalrecapplicationlist.add([
                        application: application,
                        branch: application?.recbranch,
                        post: application?.recpost,
                        documentCount: recapplicationdocumentcount
                    ])

                    // Get addresses
                    def permanent = Address.findByRecapplicantAndAddresstype(application?.recapplicant, permanentAddressType)
                    permanentaddressList.add(permanent)

                    def local = Address.findByRecapplicantAndAddresstype(application?.recapplicant, localAddressType)
                    localaddressList.add(local)
                }
            }

            // Prepare response
            hm.candidateList = finalrecapplicationlist
            hm.total = finalrecapplicationlist.size()
            hm.permanentAddresses = permanentaddressList
            hm.localAddresses = localaddressList
            hm.recpost = recpost
            hm.recbranch = recbranch
            hm.recVersion = recVersion
            hm.academicYear = academicYear
            hm.organization = organization
            hm.msg = "Candidate list fetched successfully"
            hm.flag = true

        } catch (Exception e) {
            println("Error in getCandidateList: ${e.message}")
            e.printStackTrace()
            hm.msg = "Error fetching candidate list: ${e.message}"
            hm.flag = false
        }
    }

    /**
     * Download selected documents as ZIP file
     * Returns: ZIP file stream with all documents from selected applications
     */
    def downloadSelectedDocuments(hm, request, response, data) {
        try {
            def inst = hm.remove("inst")
            def org = hm.remove("org")

            if (!inst || !org) {
                hm.msg = "Instructor or Organization not found"
                hm.flag = false
                return
            }

            // Get parameters
            def ayId = data.ay
            def orgId = data.org
            def recversionId = data.recversion
            def applicationIds = data.applicationids // Array of application IDs
            def dept = data.dept // Optional: Department name for ZIP filename
            def post = data.post // Optional: Post name for ZIP filename

            // Validate required parameters
            if (!ayId) {
                hm.msg = "Academic Year ID is required"
                hm.flag = false
                return
            }

            if (!recversionId) {
                hm.msg = "Recruitment Version ID is required"
                hm.flag = false
                return
            }

            if (!applicationIds || applicationIds.size() == 0) {
                hm.msg = "Please select at least one application"
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

            // Get recruitment version
            RecVersion recVersion = RecVersion.get(recversionId)
            if (!recVersion) {
                hm.msg = "Recruitment Version not found with ID: ${recversionId}"
                hm.flag = false
                return
            }

            // Convert application IDs to Long array
            def applicationIdArray = []
            if (applicationIds instanceof List) {
                applicationIdArray = applicationIds.collect { it.toLong() }
            } else {
                applicationIdArray = [applicationIds.toLong()]
            }

            // Get distinct applicants from selected applications
            def selectedApplicants = RecApplication.createCriteria().list {
                projections {
                    distinct('recapplicant')
                }
                'in'('id', applicationIdArray)
            }

            if (selectedApplicants.size() == 0) {
                hm.msg = "No applicants found for selected applications"
                hm.flag = false
                return
            }

            // Get all documents for selected applicants
            def allDocuments = RecApplicantDocument.createCriteria().list {
                'in'('recapplicant', selectedApplicants)
            }

            if (allDocuments.size() == 0) {
                hm.msg = "No documents found for selected applicants"
                hm.flag = false
                return
            }

            // Get AWS configuration
            AWSBucket awsBucket = AWSBucket.findByContent("documents")
            if (!awsBucket) {
                hm.msg = "AWS Bucket configuration not found"
                hm.flag = false
                return
            }

            // Initialize AWS service
            common.AWSBucketService awsBucketService = new common.AWSBucketService()

            // Build ZIP filename
            String zipFilename = ""
            if (ayId) {
                zipFilename += ayId
            }
            if (dept) {
                zipFilename += "_" + dept
            }
            if (post) {
                zipFilename += "_" + post
            }
            zipFilename += "_allRecruitmentDocuments.zip"

            // Set response headers for ZIP download
            response.setContentType('APPLICATION/OCTET-STREAM')
            response.setHeader('Content-Disposition', "Attachment;Filename=${zipFilename}")

            // Create ZIP output stream
            def zipOutputStream = new java.util.zip.ZipOutputStream(response.outputStream)

            int successCount = 0
            int errorCount = 0

            // Process each document
            for (document in allDocuments) {
                try {
                    // Get application for this document
                    def application = RecApplication.findByRecversionAndRecapplicant(recVersion, document?.recapplicant)
                    
                    if (!application) {
                        println("Application not found for recapplicant: ${document?.recapplicant?.id}")
                        errorCount++
                        continue
                    }

                    // Build AWS file path
                    String awsFilePath = "cloud/" + document?.filepath + document?.filename
                    
                    // Get presigned URL from AWS
                    String presignedUrl = awsBucketService.getPresignedUrl(awsBucket.bucketname, awsFilePath, awsBucket.region)
                    
                    if (!presignedUrl) {
                        println("Failed to get presigned URL for: ${awsFilePath}")
                        errorCount++
                        continue
                    }

                    // Replace backslashes with forward slashes
                    presignedUrl = presignedUrl.replace("\\", "/")

                    // Download file from AWS
                    URL fileUrl = new URL(presignedUrl)
                    def connection = fileUrl.openConnection()
                    
                    try {
                        def dataStream = connection.inputStream
                        
                        if (dataStream) {
                            // Extract file extension
                            def filename = document?.filename
                            def ext = filename?.substring(filename.lastIndexOf('.') + 1) ?: "pdf"
                            
                            // Create safe folder name from application ID and candidate name
                            def safeName = application?.recapplicant?.fullname?.replaceAll("[^a-zA-Z0-9]", "_") ?: "unknown"
                            def folderName = application?.applicaitionid + '_' + safeName
                            
                            // Create safe document type name
                            def documentType = document?.recdocumenttype?.type?.replaceAll("[^a-zA-Z0-9]", "_") ?: "document"
                            
                            // Build ZIP entry name: FolderName/DocumentType.ext
                            def entryName = folderName + '/' + documentType + '.' + ext
                            
                            // Add file to ZIP
                            def zipEntry = new java.util.zip.ZipEntry(entryName)
                            zipOutputStream.putNextEntry(zipEntry)
                            zipOutputStream.write(dataStream.bytes)
                            zipOutputStream.closeEntry()
                            
                            successCount++
                        } else {
                            println("No data stream for: ${awsFilePath}")
                            errorCount++
                        }
                    } catch (Exception e) {
                        println("Error downloading file: ${awsFilePath}")
                        println("Error: ${e.message}")
                        errorCount++
                    } finally {
                        try {
                            connection?.inputStream?.close()
                        } catch (Exception ignored) {}
                    }
                    
                } catch (Exception e) {
                    println("Error processing document ID: ${document?.id}")
                    println("Error: ${e.message}")
                    errorCount++
                }
            }

            // Close ZIP stream
            zipOutputStream.close()

            // Log summary
            println("Download completed: ${successCount} files successful, ${errorCount} files failed")

            // Set flag to indicate this is a file download (not JSON response)
            hm.isFileDownload = true
            hm.successCount = successCount
            hm.errorCount = errorCount
            hm.msg = "Documents downloaded successfully"
            hm.flag = true

        } catch (Exception e) {
            println("Error in downloadSelectedDocuments: ${e.message}")
            e.printStackTrace()
            hm.msg = "Error downloading documents: ${e.message}"
            hm.flag = false
        }
    }
}
