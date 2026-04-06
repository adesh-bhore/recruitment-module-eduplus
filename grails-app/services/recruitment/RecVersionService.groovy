package recruitment

import grails.gorm.transactions.Transactional

@Transactional
class RecVersionService {

    def getTest(hm,request,data) {
        println("In getTest: ${data}")
        def inst = hm.remove("inst")
        def org = hm.remove("org")
        println("org:${org}")

        def recVersionList = RecVersion.findAllByOrganization(org)
        println(recVersionList)
        hm.msg = "Success"
        hm.flag = true
        return
    }

    /**
     * Get filter data for statistics report
     * Returns: academic years, organizations (if management), current academic year, rec versions
     */
    def getStatisticsFilterData(hm, request) {
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

            // Check if user has Management role for Recruitment
            RoleType recretment_rt = RoleType.findByApplicationtypeAndTypeAndOrganization(at, "Recruitment", org)
            def is_management = false
            
            if (recretment_rt) {
                // Get login through Person
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

            // Get active academic years - only return essential fields
            def aylist = AcademicYear.findAllByIsactive(true, [sort: 'ay', order: 'asc'])
            def aylistFiltered = aylist.collect { ay ->
                [
                    id: ay.id,
                    ay: ay.ay,
                    isactive: ay.isactive
                ]
            }

            // Get current academic year for Registration
            RoleType rt = RoleType.findByApplicationtypeAndTypeAndOrganization(at, "Registration", org)
            def aay = null
            def aayFiltered = null
            if (rt) {
                aay = ApplicationAcademicYear.findByRoletypeAndIsActiveAndOrganization(rt, true, org)
                if (aay) {
                    aayFiltered = [
                        id: aay.id,
                        academicyear: [
                            id: aay.academicyear.id,
                            ay: aay.academicyear.ay
                        ],
                        isActive: aay.isActive
                    ]
                }
            }

            // Get recruitment versions for current academic year - only essential fields
            def recversionlist = []
            def recversion = null
            def recversionFiltered = null
            if (aay?.academicyear) {
                recversionlist = RecVersion.findAllByOrganizationAndAcademicyear(org, aay.academicyear)
                def recversionlistFiltered = recversionlist.collect { rv ->
                    [
                        id: rv.id,
                        version_number: rv.version_number,
                        iscurrent: rv.iscurrent,
                        academicyear: [
                            id: rv.academicyear.id,
                            ay: rv.academicyear.ay
                        ]
                    ]
                }
                
                recversion = RecVersion.findByOrganizationAndAcademicyearAndIscurrent(org, aay.academicyear, true)
                if (recversion) {
                    recversionFiltered = [
                        id: recversion.id,
                        version_number: recversion.version_number,
                        iscurrent: recversion.iscurrent,
                        academicyear: [
                            id: recversion.academicyear.id,
                            ay: recversion.academicyear.ay
                        ]
                    ]
                }
                
                hm.recversionlist = recversionlistFiltered
            }

            // Get organization list if user is management - only essential fields
            def orglist = []
            if (is_management) {
                def organizationGroup = org.organizationgroup
                if (organizationGroup) {
                    def orgs = Organization.findAllByIsactiveAndOrganizationgroup(true, organizationGroup)
                    orglist = orgs.collect { o ->
                        [
                            id: o.id,
                            organization_name: o.organization_name,
                            isactive: o.isactive
                        ]
                    }
                }
            }

            // Prepare response data - only essential organization fields
            hm.aylist = aylistFiltered
            hm.recversion = recversionFiltered
            hm.aay = aayFiltered
            hm.orglist = orglist
            hm.org = [
                id: org.id,
                organization_name: org.organization_name,
                isactive: org.isactive
            ]
            hm.is_management = is_management
            hm.msg = "Filter data fetched successfully"
            hm.flag = true
    }

    /**
     * Get recruitment versions by academic year and organization
     * Returns: List of recruitment versions for the selected academic year and organization
     */
    def getRecVersionDetails(hm, request, data) {
        def inst = hm.remove("inst")
        def org = hm.remove("org")

        if (!inst || !org) {
            hm.msg = "Instructor or Organization not found"
            hm.flag = false
            return
        }

            // Get parameters from request
            def ayId = data.ay
            def orgId = data.org

            // Validate required parameters
            if (!ayId) {
                hm.msg = "Academic Year ID is required"
                hm.flag = false
                return
            }

            // If organization ID is provided, use it; otherwise use user's organization
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

            // Fetch recruitment versions for the selected academic year and organization
            def recversionlist = RecVersion.findAllByOrganizationAndAcademicyear(
                organization, 
                academicYear,
                [sort: 'version_number', order: 'asc']
            )

            // Filter response to only essential fields
            def recversionlistFiltered = recversionlist.collect { rv ->
                [
                    id: rv.id,
                    version_number: rv.version_number,
                    iscurrent: rv.iscurrent,
                    academicyear: [
                        id: rv.academicyear.id,
                        ay: rv.academicyear.ay
                    ]
                ]
            }

            // Prepare response
            hm.recversionlist = recversionlistFiltered
            hm.total = recversionlistFiltered.size()
            hm.academicYear = [
                id: academicYear.id,
                ay: academicYear.ay
            ]
            hm.organization = [
                id: organization.id,
                organization_name: organization.organization_name
            ]
            hm.msg = "Recruitment versions fetched successfully"
            hm.flag = true
    }

    /**
     * Get statistics summary report
     * Returns: Matrix of applications by branch and post with totals
     */
    def getStatisticsSummaryReport(hm, request, data) {
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

            // Get all paid applications for this version
            def recapplicationlist = RecApplication.findAllByOrganizationAndRecversionAndIsfeespaid(
                organization, 
                recVersion, 
                true
            )

            def totalapplicationcount = recapplicationlist.size()

            if (recapplicationlist.size() == 0) {
                hm.msg = "No paid applications found for this recruitment version"
                hm.flag = false
                hm.totalapplicationcount = 0
                return
            }

            // Collect unique posts and branches
            def postlist = []
            def branchlist = []
            def postidlist = []
            def branchidlist = []

            for (application in recapplicationlist) {
                // Collect branches
                def rblist = application.recbranch
                for (rb in rblist) {
                    if (!branchidlist.contains(rb.id)) {
                        branchidlist.add(rb.id)
                        branchlist.add(rb)
                    }
                }

                // Collect posts
                def rplist = application.recpost
                for (rp in rplist) {
                    if (!postidlist.contains(rp.id)) {
                        postidlist.add(rp.id)
                        postlist.add(rp)
                    }
                }
            }

            // Build statistics matrix: branch x post
            def statisticsMatrix = []

            for (branch in branchlist) {
                def branchData = [:]
                branchData.branch = [
                    id: branch.id,
                    name: branch.name
                ]
                branchData.postCounts = []

                // Count applications for each post in this branch
                for (post in postlist) {
                    def count = 0
                    for (application in recapplicationlist) {
                        def pl = application.recpost
                        def rb = application.recbranch
                        if (pl.id.contains(post.id) && rb.id.contains(branch.id)) {
                            count++
                        }
                    }
                    
                    // Get designation name safely
                    def postName = "Unknown"
                    try {
                        postName = post.designation?.name ?: "Unknown"
                    } catch (Exception e) {
                        println("Warning: Could not load designation for post ID ${post.id}: ${e.message}")
                    }
                    
                    branchData.postCounts.add([
                        post: [
                            id: post.id,
                            name: postName
                        ], 
                        count: count
                    ])
                }

                // Calculate branch totals
                def branchDistinctCount = 0
                def branchTotalCount = 0
                for (application in recapplicationlist) {
                    def rblist = application.recbranch
                    if (rblist.id.contains(branch.id)) {
                        branchDistinctCount++
                        branchTotalCount += application.recpost ? application.recpost.size() : 0
                    }
                }
                branchData.distinctTotal = branchDistinctCount
                branchData.allTotal = branchTotalCount

                statisticsMatrix.add(branchData)
            }

            // Calculate post totals - only essential fields
            def postTotals = []
            for (post in postlist) {
                def distinctCount = 0
                def allCount = 0
                for (application in recapplicationlist) {
                    def postlist_app = application.recpost
                    if (postlist_app.id.contains(post.id)) {
                        distinctCount++
                        allCount += application.recbranch ? application.recbranch.size() : 0
                    }
                }
                
                // Get designation name safely
                def postName = "Unknown"
                try {
                    postName = post.designation?.name ?: "Unknown"
                } catch (Exception e) {
                    println("Warning: Could not load designation for post ID ${post.id}: ${e.message}")
                }
                
                postTotals.add([
                    post: [
                        id: post.id,
                        name: postName
                    ],
                    distinctCount: distinctCount,
                    allCount: allCount
                ])
            }

            // Filter lists to only essential fields
            def postlistFiltered = postlist.collect { p ->
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

            def branchlistFiltered = branchlist.collect { b ->
                [
                    id: b.id,
                    name: b.name
                ]
            }

            // Prepare response
            hm.statisticsMatrix = statisticsMatrix
            hm.postlist = postlistFiltered
            hm.branchlist = branchlistFiltered
            hm.postTotals = postTotals
            hm.totalapplicationcount = totalapplicationcount
            hm.recVersion = [
                id: recVersion.id,
                version_number: recVersion.version_number
            ]
            hm.academicYear = [
                id: academicYear.id,
                ay: academicYear.ay
            ]
            hm.organization = [
                id: organization.id,
                organization_name: organization.organization_name
            ]
            hm.msg = "Statistics summary report generated successfully"
            hm.flag = true
    }

    /**
     * Get total post counts (distinct or all)
     * Returns: Count of applications for a specific post
     */
    def getTotalPostCounts(hm, request, data) {
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
            def recpostId = data.recpostid
            def type = data.type // 'distinct' or 'all'

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

            if (!recpostId) {
                hm.msg = "Post ID is required"
                hm.flag = false
                return
            }

            if (!type || !(type in ['distinct', 'all'])) {
                hm.msg = "Type is required and must be 'distinct' or 'all'"
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

            // Get post
            RecPost post = RecPost.get(recpostId)
            if (!post) {
                hm.msg = "Post not found with ID: ${recpostId}"
                hm.flag = false
                return
            }

            // Get all paid applications for this version
            def recapplicationlist = RecApplication.findAllByOrganizationAndRecversionAndIsfeespaid(
                organization, 
                recVersion, 
                true
            )

            def count = 0
            if (recapplicationlist.size() > 0) {
                for (application in recapplicationlist) {
                    def postlist = application.recpost
                    if (postlist && postlist.size() > 0) {
                        for (rp in postlist) {
                            if (rp.id == post.id) {
                                if (type == 'distinct') {
                                    // Count each application once
                                    count = count + 1
                                } else if (type == 'all') {
                                    // Count all branches for this application
                                    def branchcount = application.recbranch ? application.recbranch.size() : 0
                                    count = count + branchcount
                                }
                                break // Found the post, no need to continue inner loop
                            }
                        }
                    }
                }
            }

            // Get designation name safely
            def postName = "Unknown"
            try {
                postName = post.designation?.name ?: "Unknown"
            } catch (Exception e) {
                println("Warning: Could not load designation for post ID ${post.id}: ${e.message}")
            }
            
            // Prepare response - only essential fields
            hm.count = count
            hm.type = type
            hm.post = [
                id: post.id,
                name: postName
            ]
            hm.recVersion = [
                id: recVersion.id,
                version_number: recVersion.version_number
            ]
            hm.academicYear = [
                id: academicYear.id,
                ay: academicYear.ay
            ]
            hm.organization = [
                id: organization.id,
                organization_name: organization.organization_name
            ]
            hm.msg = "Post count fetched successfully"
            hm.flag = true
    }

    /**
     * Get total department/branch counts (distinct or all)
     * Returns: Count of applications for a specific branch
     */
    def getTotalDeptCounts(hm, request, data) {
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
            def type = data.type // 'distinct' or 'all'

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

            if (!recbranchId) {
                hm.msg = "Branch ID is required"
                hm.flag = false
                return
            }

            if (!type || !(type in ['distinct', 'all'])) {
                hm.msg = "Type is required and must be 'distinct' or 'all'"
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

            // Get branch
            RecBranch recbranch = RecBranch.get(recbranchId)
            if (!recbranch) {
                hm.msg = "Branch not found with ID: ${recbranchId}"
                hm.flag = false
                return
            }

            // Get all paid applications for this version
            def recapplicationlist = RecApplication.findAllByOrganizationAndRecversionAndIsfeespaid(
                organization, 
                recVersion, 
                true
            )

            def count = 0
            if (recapplicationlist.size() > 0) {
                for (application in recapplicationlist) {
                    def rblist = application.recbranch
                    if (rblist && rblist.size() > 0) {
                        for (rb in rblist) {
                            if (rb.id == recbranch.id) {
                                if (type == 'distinct') {
                                    // Count each application once
                                    count = count + 1
                                } else if (type == 'all') {
                                    // Count all posts for this application
                                    def postcount = application.recpost ? application.recpost.size() : 0
                                    count = count + postcount
                                }
                                break // Found the branch, no need to continue inner loop
                            }
                        }
                    }
                }
            }

            // Prepare response - only essential fields
            hm.count = count
            hm.type = type
            hm.branch = [
                id: recbranch.id,
                name: recbranch.name
            ]
            hm.recVersion = [
                id: recVersion.id,
                version_number: recVersion.version_number
            ]
            hm.academicYear = [
                id: academicYear.id,
                ay: academicYear.ay
            ]
            hm.organization = [
                id: organization.id,
                organization_name: organization.organization_name
            ]
            hm.msg = "Department count fetched successfully"
            hm.flag = true
    }
}
