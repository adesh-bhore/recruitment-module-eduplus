package recruitment

import grails.gorm.transactions.Transactional
import java.text.SimpleDateFormat

@Transactional
class RecInterviewScheduleService_2 {
    // ── Branch Service Methods ───────────────────────────────────────

    /**
     * Get all RecBranch for org + programs + current rec versions
     * Used by: GET /recInterviewSchedule/getRecBranchList
     */
    def getRecBranchList(hm, request) {
        try {
            def inst = hm.remove("inst")
            def org  = hm.remove("org")

            if (!inst || !org) {
                hm.msg = "Instructor or Organization not found"
                hm.flag = false
                return
            }

            def recallBranch = RecBranch.findAllByOrganization(org)
            def recversionall = RecVersion.findAllByOrganizationAndIscurrentforbackendprocessing(org, true)
            def programList   = Program.findAllByOrganization(org)

            hm.recallBranch = recallBranch.collect { b ->
                [
                    id                : b.id,
                    name              : b.name,
                    branch_abbrivation: b.branch_abbrivation,
                    isactive          : b.isactive,
                    recversion        : b.recversion ? [id: b.recversion.id, version_number: b.recversion.version_number, version_date: b.recversion.version_date] : null,
                    program           : b.program   ? [id: b.program.id,    name: b.program.name] : null
                ]
            }

            hm.recversionall = recversionall.collect { rv ->
                [id: rv.id, version_number: rv.version_number, version_date: rv.version_date]
            }

            hm.program = programList.collect { p ->
                [id: p.id, name: p.name]
            }

            hm.msg  = "Branch list fetched successfully"
            hm.flag = true

        } catch (Exception e) {
            println("Error in getRecBranchList: ${e.message}")
            hm.msg  = "Error fetching branch list: ${e.message}"
            hm.flag = false
        }
    }

    /**
     * Create a new RecBranch (duplicate check by name + org + version + program)
     * Used by: POST /recInterviewSchedule/saverecBranch
     */
    def saveRecBranch(hm, request, data) {
        try {
            def inst = hm.remove("inst")
            def org  = hm.remove("org")

            if (!inst || !org) {
                hm.msg = "Instructor or Organization not found"
                hm.flag = false
                return
            }

            def recverId    = data.recver
            def programId   = data.program
            def branchName  = data.recbranch?.toString()?.trim()
            def branchAbbr  = data.recbranchabbr?.toString()?.trim()

            if (!recverId || !programId || !branchName || !branchAbbr) {
                hm.msg = "recver, program, recbranch and recbranchabbr are required"
                hm.flag = false
                return
            }

            RecVersion recversion = RecVersion.get(recverId)
            if (!recversion) { hm.msg = "Recruitment Version not found"; hm.flag = false; return }

            Program program = Program.get(programId)
            if (!program) { hm.msg = "Program not found"; hm.flag = false; return }

            // Duplicate check
            RecBranch existing = RecBranch.findByNameAndOrganizationAndRecversionAndProgram(branchName, org, recversion, program)
            if (existing) {
                hm.msg  = "Branch already exists"
                hm.flag = false
                return
            }

            def loginId = request.getHeader("EPC-UID")
            Login login = Login.findByUsernameAndIsloginblocked(loginId, false)

            RecBranch recBranch = new RecBranch(
                name              : branchName,
                branch_abbrivation: branchAbbr,
                recversion        : recversion,
                program           : program,
                organization      : org,
                isactive          : true,
                username          : login?.username,
                creation_ip_address : request.getRemoteAddr(),
                creation_date       : new Date(),
                updation_ip_address : request.getRemoteAddr(),
                updation_date       : new Date()
            )
            recBranch.save(failOnError: true, flush: true)

            hm.msg  = "Saved successfully"
            hm.flag = true

        } catch (Exception e) {
            println("Error in saveRecBranch: ${e.message}")
            hm.msg  = "Error saving branch: ${e.message}"
            hm.flag = false
        }
    }

    /**
     * Update an existing RecBranch
     * Used by: POST /recInterviewSchedule/editRecBranch
     */
    def editRecBranch(hm, request, data) {
        try {
            def inst = hm.remove("inst")
            def org  = hm.remove("org")

            if (!inst || !org) {
                hm.msg = "Instructor or Organization not found"
                hm.flag = false
                return
            }

            def editId     = data.editId
            def programId  = data.program
            def branchName = data.recbranch?.toString()?.trim()
            def branchAbbr = data.recbranchabbr?.toString()?.trim()

            if (!editId) { hm.msg = "editId is required"; hm.flag = false; return }

            RecBranch recBranch = RecBranch.get(editId)
            if (!recBranch) {
                hm.msg  = "Branch not found"
                hm.flag = false
                return
            }

            Program program = programId ? Program.get(programId) : recBranch.program

            def loginId = request.getHeader("EPC-UID")
            Login login = Login.findByUsernameAndIsloginblocked(loginId, false)

            recBranch.name               = branchName ?: recBranch.name
            recBranch.branch_abbrivation = branchAbbr ?: recBranch.branch_abbrivation
            recBranch.program            = program
            recBranch.organization       = org
            recBranch.isactive           = true
            recBranch.username           = login?.username
            recBranch.updation_ip_address = request.getRemoteAddr()
            recBranch.updation_date       = new Date()
            recBranch.save(failOnError: true, flush: true)

            hm.msg  = "Updated successfully"
            hm.flag = true

        } catch (Exception e) {
            println("Error in editRecBranch: ${e.message}")
            hm.msg  = "Error updating branch: ${e.message}"
            hm.flag = false
        }
    }

    /**
     * Delete a RecBranch (fails gracefully if in use by FK constraint)
     * Used by: POST /recInterviewSchedule/deleterecBranch
     */
    def deleteRecBranch(hm, request, data) {
        try {
            def inst = hm.remove("inst")
            def org  = hm.remove("org")

            if (!inst || !org) {
                hm.msg = "Instructor or Organization not found"
                hm.flag = false
                return
            }

            def brcid = data.brcid
            if (!brcid) { hm.msg = "brcid is required"; hm.flag = false; return }

            RecBranch recBranch = RecBranch.get(brcid)
            if (!recBranch) {
                hm.msg  = "Branch not found"
                hm.flag = false
                return
            }

            try {
                recBranch.delete(flush: true, failOnError: true)
                hm.msg  = "Deleted successfully"
                hm.flag = true
            } catch (Exception ex) {
                hm.msg  = "Cannot be deleted. Branch may be in use."
                hm.flag = false
            }

        } catch (Exception e) {
            println("Error in deleteRecBranch: ${e.message}")
            hm.msg  = "Error deleting branch: ${e.message}"
            hm.flag = false
        }
    }

    /**
     * Toggle isActive on a RecBranch
     * Used by: POST /recInterviewSchedule/isActiveBranch
     */
    def toggleBranchActive(hm, request, data) {
        try {
            def inst = hm.remove("inst")
            def org  = hm.remove("org")

            if (!inst || !org) {
                hm.msg = "Instructor or Organization not found"
                hm.flag = false
                return
            }

            def activeId = data.ActiveId
            if (!activeId) { hm.msg = "ActiveId is required"; hm.flag = false; return }

            RecBranch recBranch = RecBranch.get(activeId)
            if (!recBranch) {
                hm.msg  = "Branch not found"
                hm.flag = false
                return
            }

            recBranch.isactive            = !recBranch.isactive
            recBranch.updation_ip_address = request.getRemoteAddr()
            recBranch.updation_date       = new Date()
            recBranch.save(flush: true, failOnError: true)

            hm.isactive = recBranch.isactive
            hm.msg      = "Status updated successfully"
            hm.flag     = true

        } catch (Exception e) {
            println("Error in toggleBranchActive: ${e.message}")
            hm.msg  = "Error updating branch status: ${e.message}"
            hm.flag = false
        }
    }

    // ────────────────────────────────────────────────────────────────

    // ── Post Management Service Methods ──────────────────────────────

    /**
     * Get all RecPost for org + designations + current rec versions
     * Used by: GET /recInterviewSchedule/getRecPostList
     */
    def getRecPostList(hm, request) {
        try {
            def inst = hm.remove("inst")
            def org  = hm.remove("org")

            if (!inst || !org) {
                hm.msg = "Instructor or Organization not found"
                hm.flag = false
                return
            }

            def recallPost = RecPost.findAllByOrganization(org)
            def recversionall = RecVersion.findAllByOrganizationAndIscurrentforbackendprocessing(org, true)
            def designationList = Designation.findAllByOrganization(org)

            hm.recallPost = recallPost.collect { p ->
                def designationData = null
                try {
                    designationData = p.designation ? [id: p.designation.id, name: p.designation.name] : null
                } catch (Exception e) {
                    println("Warning: Could not load designation for post ID ${p.id}: ${e.message}")
                }
                
                [
                    id          : p.id,
                    isactive    : p.isactive,
                    recversion  : p.recversion ? [id: p.recversion.id, version_number: p.recversion.version_number] : null,
                    designation : designationData
                ]
            }

            hm.recversionall = recversionall.collect { rv ->
                [id: rv.id, version_number: rv.version_number, version_date: rv.version_date]
            }

            hm.designation = designationList.collect { d ->
                [id: d.id, name: d.name]
            }

            hm.msg  = "Post list fetched successfully"
            hm.flag = true

        } catch (Exception e) {
            println("Error in getRecPostList: ${e.message}")
            hm.msg  = "Error fetching post list: ${e.message}"
            hm.flag = false
        }
    }

    /**
     * Create a new RecPost (duplicate check by org + version + designation)
     * Used by: POST /recInterviewSchedule/saverecPost
     */
    def saveRecPost(hm, request, data) {
        try {
            def inst = hm.remove("inst")
            def org  = hm.remove("org")

            if (!inst || !org) {
                hm.msg = "Instructor or Organization not found"
                hm.flag = false
                return
            }

            def recverId      = data.recver
            def designationId = data.designation

            if (!recverId || !designationId) {
                hm.msg = "recver and designation are required"
                hm.flag = false
                return
            }

            RecVersion recversion = RecVersion.get(recverId)
            if (!recversion) { hm.msg = "Recruitment Version not found"; hm.flag = false; return }

            Designation designation = Designation.get(designationId)
            if (!designation) { hm.msg = "Designation not found"; hm.flag = false; return }

            // Duplicate check
            RecPost existing = RecPost.findByOrganizationAndRecversionAndDesignation(org, recversion, designation)
            if (existing) {
                hm.msg  = "Post already exists"
                hm.flag = false
                return
            }

            def loginId = request.getHeader("EPC-UID")
            Login login = Login.findByUsernameAndIsloginblocked(loginId, false)

            RecPost recPost = new RecPost(
                recversion          : recversion,
                designation         : designation,
                organization        : org,
                isactive            : true,
                username            : login?.username,
                creation_ip_address : request.getRemoteAddr(),
                creation_date       : new Date(),
                updation_ip_address : request.getRemoteAddr(),
                updation_date       : new Date()
            )
            recPost.save(failOnError: true, flush: true)

            hm.msg  = "Saved successfully"
            hm.flag = true

        } catch (Exception e) {
            println("Error in saveRecPost: ${e.message}")
            hm.msg  = "Error saving post: ${e.message}"
            hm.flag = false
        }
    }

    /**
     * Update an existing RecPost
     * Used by: POST /recInterviewSchedule/editRecPost
     */
    def editRecPost(hm, request, data) {
        try {
            def inst = hm.remove("inst")
            def org  = hm.remove("org")

            if (!inst || !org) {
                hm.msg = "Instructor or Organization not found"
                hm.flag = false
                return
            }

            def editId        = data.editId
            def designationId = data.designation

            if (!editId) { hm.msg = "editId is required"; hm.flag = false; return }

            RecPost recPost = RecPost.get(editId)
            if (!recPost) {
                hm.msg  = "Post not found"
                hm.flag = false
                return
            }

            Designation designation = designationId ? Designation.get(designationId) : recPost.designation

            def loginId = request.getHeader("EPC-UID")
            Login login = Login.findByUsernameAndIsloginblocked(loginId, false)

            recPost.designation         = designation
            recPost.organization        = org
            recPost.isactive            = true
            recPost.username            = login?.username
            recPost.updation_ip_address = request.getRemoteAddr()
            recPost.updation_date       = new Date()
            recPost.save(failOnError: true, flush: true)

            hm.msg  = "Updated successfully"
            hm.flag = true

        } catch (Exception e) {
            println("Error in editRecPost: ${e.message}")
            hm.msg  = "Error updating post: ${e.message}"
            hm.flag = false
        }
    }

    /**
     * Delete a RecPost (fails gracefully if in use by FK constraint)
     * Used by: POST /recInterviewSchedule/deleterecPost
     */
    def deleteRecPost(hm, request, data) {
        try {
            def inst = hm.remove("inst")
            def org  = hm.remove("org")

            if (!inst || !org) {
                hm.msg = "Instructor or Organization not found"
                hm.flag = false
                return
            }

            def brcid = data.brcid
            if (!brcid) { hm.msg = "brcid is required"; hm.flag = false; return }

            RecPost recPost = RecPost.get(brcid)
            if (!recPost) {
                hm.msg  = "Post not found"
                hm.flag = false
                return
            }

            try {
                recPost.delete(flush: true, failOnError: true)
                hm.msg  = "Deleted successfully"
                hm.flag = true
            } catch (Exception ex) {
                hm.msg  = "Cannot be deleted. Post may be in use."
                hm.flag = false
            }

        } catch (Exception e) {
            println("Error in deleteRecPost: ${e.message}")
            hm.msg  = "Error deleting post: ${e.message}"
            hm.flag = false
        }
    }

    /**
     * Toggle isActive on a RecPost
     * Used by: POST /recInterviewSchedule/isActivePost
     */
    def togglePostActive(hm, request, data) {
        try {
            def inst = hm.remove("inst")
            def org  = hm.remove("org")

            if (!inst || !org) {
                hm.msg = "Instructor or Organization not found"
                hm.flag = false
                return
            }

            def activeId = data.ActiveId
            if (!activeId) { hm.msg = "ActiveId is required"; hm.flag = false; return }

            RecPost recPost = RecPost.get(activeId)
            if (!recPost) {
                hm.msg  = "Post not found"
                hm.flag = false
                return
            }

            recPost.isactive            = !recPost.isactive
            recPost.updation_ip_address = request.getRemoteAddr()
            recPost.updation_date       = new Date()
            recPost.save(flush: true, failOnError: true)

            hm.isactive = recPost.isactive
            hm.msg      = "Status updated successfully"
            hm.flag     = true

        } catch (Exception e) {
            println("Error in togglePostActive: ${e.message}")
            hm.msg  = "Error updating post status: ${e.message}"
            hm.flag = false
        }
    }

    // ────────────────────────────────────────────────────────────────

    // ── Post Assignment Service Methods ──────────────────────────────

    /**
     * Get all ERPFacultyPost and Instructors for post assignment
     * Used by: GET /recInterviewSchedule/getAssignPostList
     */
    def getAssignPostList(hm, request) {
        try {
            def inst = hm.remove("inst")
            def org  = hm.remove("org")

            if (!inst || !org) {
                hm.msg = "Instructor or Organization not found"
                hm.flag = false
                return
            }

            def erpPostList = ERPFacultyPost.findAllByOrganization(org)
            def allInstructors = Instructor.findAllByOrganizationAndIscurrentlyworking(org, true)

            hm.erpPost = erpPostList.collect { p ->
                [
                    id   : p.id,
                    name : p.name
                ]
            }

            hm.allInst = allInstructors.collect { i ->
                def personName = "Unknown"
                try {
                    def person = i.person
                    personName = "${person?.firstName ?: ''} ${person?.middleName ?: ''} ${person?.lastName ?: ''}".trim()
                } catch (Exception e) {
                    println("Warning: Could not load person for instructor ID ${i.id}: ${e.message}")
                }
                
                // Get assigned posts for this instructor
                def assignedPosts = []
                try {
                    assignedPosts = i.erpfacultypost?.collect { post ->
                        [
                            id   : post.id,
                            name : post.name
                        ]
                    } ?: []
                } catch (Exception e) {
                    println("Warning: Could not load posts for instructor ID ${i.id}: ${e.message}")
                }
                
                [
                    id           : i.id,
                    uid          : i.uid,
                    employee_code: i.employee_code,
                    name         : personName,
                    assignedPosts: assignedPosts
                ]
            }

            hm.msg  = "Post assignment data fetched successfully"
            hm.flag = true

        } catch (Exception e) {
            println("Error in getAssignPostList: ${e.message}")
            hm.msg  = "Error fetching post assignment data: ${e.message}"
            hm.flag = false
        }
    }

    /**
     * Assign ERPFacultyPost to Instructor (many-to-many)
     * Used by: POST /recInterviewSchedule/saveassignPost
     */
    def saveAssignPost(hm, request, data) {
        try {
            def currentInst = hm.remove("inst")
            def org  = hm.remove("org")

            if (!currentInst || !org) {
                hm.msg = "Instructor or Organization not found"
                hm.flag = false
                return
            }

            def instId = data.inst
            def postId = data.post

            if (!instId || !postId) {
                hm.msg = "inst and post are required"
                hm.flag = false
                return
            }

            Instructor instructor = Instructor.get(instId)
            if (!instructor) {
                hm.msg = "Instructor not found"
                hm.flag = false
                return
            }

            ERPFacultyPost erpFacultyPost = ERPFacultyPost.get(postId)
            if (!erpFacultyPost) {
                hm.msg = "Faculty post not found"
                hm.flag = false
                return
            }

            // Check if already assigned
            def postlist = instructor.erpfacultypost
            if (postlist) {
                def isavailable = postlist.any { it.id == erpFacultyPost.id }
                if (isavailable) {
                    hm.msg = "Post already assigned to this instructor"
                    hm.flag = false
                    return
                }
            }

            // Assign post
            instructor.addToErpfacultypost(erpFacultyPost)
            instructor.save(flush: true, failOnError: true)

            hm.msg  = "Post assigned successfully"
            hm.flag = true

        } catch (Exception e) {
            println("Error in saveAssignPost: ${e.message}")
            hm.msg  = "Error assigning post: ${e.message}"
            hm.flag = false
        }
    }

    /**
     * Remove ERPFacultyPost assignment from Instructor
     * Used by: POST /recInterviewSchedule/deleteassignPost
     */
    def deleteAssignPost(hm, request, data) {
        try {
            def currentInst = hm.remove("inst")
            def org  = hm.remove("org")

            if (!currentInst || !org) {
                hm.msg = "Instructor or Organization not found"
                hm.flag = false
                return
            }

            def instId = data.instid
            def postId = data.postid

            if (!instId || !postId) {
                hm.msg = "instid and postid are required"
                hm.flag = false
                return
            }

            Instructor instructor = Instructor.get(instId)
            if (!instructor) {
                hm.msg = "Instructor not found"
                hm.flag = false
                return
            }

            ERPFacultyPost erpFacultyPost = ERPFacultyPost.get(postId)
            if (!erpFacultyPost) {
                hm.msg = "Faculty post not found"
                hm.flag = false
                return
            }

            // Remove post assignment
            instructor.removeFromErpfacultypost(erpFacultyPost)
            instructor.save(flush: true, failOnError: true)

            hm.msg  = "Post assignment removed successfully"
            hm.flag = true

        } catch (Exception e) {
            println("Error in deleteAssignPost: ${e.message}")
            hm.msg  = "Error removing post assignment: ${e.message}"
            hm.flag = false
        }
    }

    // ────────────────────────────────────────────────────────────────

    /**
     * Helper method to generate HTML email content for interview call letter
     */
    private String generateInterviewCallLetterHTML(org, recapplication, recbranch, interviewDate, venue, time, currentDate) {
        def applicant = recapplication.recapplicant
        def fullName = applicant?.fullname ?: "Candidate"

        def html = """
<div style="width:90%; background:#fff; font-family: Arial, sans-serif;">
    <div class="body" style="background:#f2edf5; padding:20px;">
        <table style="width:100%; font-size:16px; border: 1px solid black; border-collapse: collapse; background:#fff;">
            <tr>
                <td style="width:15%; padding:10px; text-align:center;">
                    ${org?.org_logo ? "<img src='${org.org_logo}' style='height:100px; width:auto;' />" : ""}
                </td>
                <td style="width:70%; padding:10px; text-align:center;">
                    <span style="font-size:18px; font-weight:900;">${org?.organization_name ?: ''}</span>
                </td>
                <td style="width:15%; padding:10px; text-align:center;">
                    ${org?.organizationgroup?.group_featured_logo ? "<img src='${org.organizationgroup.group_featured_logo}' style='height:100px; width:auto;' />" : ""}
                </td>
            </tr>
        </table>
        
        <div style="padding:20px; background:#fff; margin-top:10px;">
            <p style="text-align:right;">Date: ${currentDate}</p>
            
            <p>Dear ${fullName},</p>
            
            <p>Greetings from <b>${org?.organization_name ?: ''}</b>!</p>
            
            <p>We are pleased to inform you that your application for the position in <b>${recbranch?.name ?: 'the department'}</b> 
            has been shortlisted for the interview process.</p>
            
            <p><b>Interview Details:</b></p>
            <table style="margin-left:20px; font-size:15px;">
                <tr>
                    <td style="padding:5px;"><b>Date:</b></td>
                    <td style="padding:5px;">${interviewDate}</td>
                </tr>
                <tr>
                    <td style="padding:5px;"><b>Time:</b></td>
                    <td style="padding:5px;">${time ?: 'Will be communicated'}</td>
                </tr>
                <tr>
                    <td style="padding:5px;"><b>Venue:</b></td>
                    <td style="padding:5px;">${venue ?: 'Will be communicated'}</td>
                </tr>
            </table>
            
            <p><b>Please bring the following documents:</b></p>
            <ul>
                <li>Updated Resume/CV</li>
                <li>All Educational Certificates (Originals and Photocopies)</li>
                <li>Experience Certificates (if applicable)</li>
                <li>Valid Photo ID Proof</li>
                <li>Recent Passport Size Photographs (2 copies)</li>
            </ul>
            
            <p>Please confirm your attendance by replying to this email.</p>
            
            <p>We look forward to meeting you.</p>
            
            <p><b>Best Regards,</b><br/>
            <b>Establishment Department</b><br/>
            <b>${org?.organization_name ?: ''}</b></p>
        </div>
        
        <div style="text-align:center; margin-top:20px;">
            <img width='300px' src='https://vierp-test.s3.ap-south-1.amazonaws.com/epclogo/logo.png' />
        </div>
    </div>
</div>
"""
        return html
    }

}
