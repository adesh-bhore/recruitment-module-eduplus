package recruitment

import grails.gorm.transactions.Transactional

@Transactional
class RecExpertService2 {
    
    // ═══════════════════════════════════════════════════════════════
    // Phase 3: Marks Evaluation APIs
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Get applicants list assigned to expert for marks evaluation
     * Used by: GET /recExpert/getApplicantsForMarks
     */
    def getApplicantsForMarks(hm, request, data) {
        def expertLoginname = hm.remove("expertLoginname")
        
        if (!expertLoginname) {
            hm.msg = "Expert not authenticated"
            hm.flag = false
            return
        }
        
        // Find expert by loginname
        RecExpert recexpert = RecExpert.findByLoginname(expertLoginname)
        if (!recexpert) {
            hm.msg = "Expert not found"
            hm.flag = false
            return
        }
        
        if (recexpert.isblocked) {
            hm.msg = "Expert account is blocked"
            hm.flag = false
            return
        }
        
        def recv = recexpert.recversion
        
        // Get evaluation parameters for this expert
        def recevaluationparameter = RecEvaluationParameter.findAllByOranizationAndRecversionAndRecexperttypeAndRecdeptexpertgroup(
            recexpert.oranization, recv, recexpert.recexperttype, recexpert.recdeptexpertgroup
        )
        
        if (!recevaluationparameter) {
            hm.msg = "Evaluation parameters not found for this expert group"
            hm.flag = false
            return
        }
        
        // Get applications assigned to this expert group (Round 1)
        RecApplicationRound round = RecApplicationRound.findByRoundnumberAndOrganization(1, recexpert.oranization)
        if (!round) {
            hm.msg = "Application round not found"
            hm.flag = false
            return
        }
        
        def applicationList = RecApplicationRoundTransaction.createCriteria().list() {
            projections {
                distinct('recapplication')
            }
            and {
                eq('recversion', recv)
                eq('recdeptexpertgroup', recexpert.recdeptexpertgroup)
                eq('organization', recexpert.oranization)
                eq('recapplicationround', round)
                eq('isrejected', false)
            }
        }
        
        // Get applicants who already have marks from this expert
        def distinctApplicantMarks = RecApplicationEvaluation.createCriteria().list() {
            projections {
                distinct("recapplication")
            }
            and {
                eq('recexpert', recexpert)
                eq('oranization', recexpert.oranization)
                eq('recdeptexpertgroup', recexpert.recdeptexpertgroup)
                eq('recversion', recv)
                'in'('recevaluationparameter', recevaluationparameter)
                eq('recexperttype', recexpert.recexperttype)
            }
        }
        
        // Calculate max total marks
        def maxTotalMarks = 0.0
        recevaluationparameter.each { param ->
            maxTotalMarks += param.maxmarks
        }
        
        // Build applicants list with marks status
        def applicants = []
        applicationList.each { application ->
            def hasMarks = distinctApplicantMarks.contains(application)
            def totalMarks = 0.0
            
            if (hasMarks) {
                // Calculate total marks for this applicant
                recevaluationparameter.each { param ->
                    def evaluations = RecApplicationEvaluation.findAllByOranizationAndRecversionAndRecdeptexpertgroupAndRecapplicationAndRecapplicantAndRecexpertAndRecexperttypeAndRecevaluationparameter(
                        recexpert.oranization, recv, recexpert.recdeptexpertgroup, application, application.recapplicant, recexpert, recexpert.recexperttype, param
                    )
                    evaluations.each { eval ->
                        totalMarks += eval.obtained_marks
                    }
                }
            }
            
            applicants.add([
                id: application.id,
                applicationId: application.applicaitionid,
                fullname: application.recapplicant?.fullname,
                email: application.recapplicant?.email,
                hasMarks: hasMarks,
                totalMarks: totalMarks,
                maxTotalMarks: maxTotalMarks
            ])
        }
        
        // Build evaluation parameters list
        def evaluationParameters = recevaluationparameter.collect { [
            id: it.id,
            parameter: it.parameter,
            parameter_number: it.parameter_number,
            maxmarks: it.maxmarks
        ] }
        
        hm.applicants = applicants
        hm.evaluationParameters = evaluationParameters
        hm.maxTotalMarks = maxTotalMarks
        hm.msg = "Applicants fetched successfully"
        hm.flag = true
    }
    
    /**
     * Get evaluation form for specific applicant
     * Used by: GET /recExpert/getEvaluationForm
     */
    def getEvaluationForm(hm, request, data) {
        def expertLoginname = hm.remove("expertLoginname")
        def applicationId = hm.remove("applicationId")
        
        if (!expertLoginname) {
            hm.msg = "Expert not authenticated"
            hm.flag = false
            return
        }
        
        if (!applicationId) {
            hm.msg = "Application ID is required"
            hm.flag = false
            return
        }
        
        // Find expert by loginname
        RecExpert recexpert = RecExpert.findByLoginname(expertLoginname)
        if (!recexpert) {
            hm.msg = "Expert not found"
            hm.flag = false
            return
        }
        
        if (recexpert.isblocked) {
            hm.msg = "Expert account is blocked"
            hm.flag = false
            return
        }
        
        // Find application
        RecApplication recapplication = RecApplication.get(applicationId)
        if (!recapplication) {
            hm.msg = "Application not found"
            hm.flag = false
            return
        }
        
        def recv = recexpert.recversion
        
        // Get evaluation parameters
        def recevaluationparameter = RecEvaluationParameter.findAllByOranizationAndRecversionAndRecexperttypeAndRecdeptexpertgroup(
            recexpert.oranization, recv, recexpert.recexperttype, recexpert.recdeptexpertgroup
        )
        
        if (!recevaluationparameter) {
            hm.msg = "Evaluation parameters not found"
            hm.flag = false
            return
        }
        
        // Build evaluation form with existing marks
        def evaluationParameters = []
        recevaluationparameter.each { param ->
            def obtainedMarks = 0.0
            
            // Check if expert already added marks for this parameter
            def existingEvaluations = RecApplicationEvaluation.findAllByOranizationAndRecversionAndRecdeptexpertgroupAndRecapplicationAndRecapplicantAndRecexpertAndRecexperttypeAndRecevaluationparameter(
                recexpert.oranization, recv, recexpert.recdeptexpertgroup, recapplication, recapplication.recapplicant, recexpert, recexpert.recexperttype, param
            )
            
            if (existingEvaluations) {
                obtainedMarks = existingEvaluations[0].obtained_marks
            }
            
            evaluationParameters.add([
                id: param.id,
                parameter: param.parameter,
                parameter_number: param.parameter_number,
                maxmarks: param.maxmarks,
                obtainedMarks: obtainedMarks
            ])
        }
        
        // Build application details
        hm.application = [
            id: recapplication.id,
            applicationId: recapplication.applicaitionid,
            applicant: [
                id: recapplication.recapplicant?.id,
                fullname: recapplication.recapplicant?.fullname,
                email: recapplication.recapplicant?.email
            ]
        ]
        
        hm.evaluationParameters = evaluationParameters
        hm.msg = "Evaluation form fetched successfully"
        hm.flag = true
    }
    
    /**
     * Submit marks for applicant
     * Used by: POST /recExpert/submitMarks
     */
    def submitMarks(hm, request, data) {
        def expertLoginname = hm.remove("expertLoginname")
        def applicationId = data.applicationId
        def marks = data.marks
        
        if (!expertLoginname) {
            hm.msg = "Expert not authenticated"
            hm.flag = false
            return
        }
        
        if (!applicationId || !marks) {
            hm.msg = "Application ID and marks are required"
            hm.flag = false
            return
        }
        
        // Find expert by loginname
        RecExpert recexpert = RecExpert.findByLoginname(expertLoginname)
        if (!recexpert) {
            hm.msg = "Expert not found"
            hm.flag = false
            return
        }
        
        if (recexpert.isblocked) {
            hm.msg = "Expert account is blocked"
            hm.flag = false
            return
        }
        
        // Find application
        RecApplication recapplication = RecApplication.get(applicationId)
        if (!recapplication) {
            hm.msg = "Application not found"
            hm.flag = false
            return
        }
        
        def recv = recexpert.recversion
        
        // Save marks for each parameter
        marks.each { markData ->
            def parameterId = markData.parameterId
            def obtainedMarks = markData.obtainedMarks
            
            RecEvaluationParameter param = RecEvaluationParameter.get(parameterId)
            if (!param) {
                return // Skip invalid parameter
            }
            
            // Validate marks
            if (obtainedMarks < 0 || obtainedMarks > param.maxmarks) {
                hm.msg = "Invalid marks for parameter ${param.parameter}. Must be between 0 and ${param.maxmarks}"
                hm.flag = false
                return
            }
            
            // Check if expert already added marks
            def existingEvaluations = RecApplicationEvaluation.findAllByOranizationAndRecversionAndRecdeptexpertgroupAndRecapplicationAndRecapplicantAndRecexpertAndRecexperttypeAndRecevaluationparameter(
                recexpert.oranization, recv, recexpert.recdeptexpertgroup, recapplication, recapplication.recapplicant, recexpert, recexpert.recexperttype, param
            )
            
            if (existingEvaluations) {
                // Update existing marks
                existingEvaluations.each { eval ->
                    eval.obtained_marks = obtainedMarks
                    eval.updation_date = new Date()
                    eval.updation_ip_address = request.getRemoteAddr()
                    eval.save(flush: true, failOnError: true)
                }
            } else {
                // Create new evaluation
                RecApplicationEvaluation evaluation = new RecApplicationEvaluation()
                evaluation.obtained_marks = obtainedMarks
                evaluation.evaluation_date = new Date()
                evaluation.username = recexpert.loginname
                evaluation.creation_date = new Date()
                evaluation.updation_date = new Date()
                evaluation.creation_ip_address = request.getRemoteAddr()
                evaluation.updation_ip_address = request.getRemoteAddr()
                evaluation.oranization = recexpert.oranization
                evaluation.recversion = recv
                evaluation.recdeptexpertgroup = recexpert.recdeptexpertgroup
                evaluation.recevaluationparameter = param
                evaluation.recapplication = recapplication
                evaluation.recexpert = recexpert
                evaluation.recexperttype = recexpert.recexperttype
                evaluation.recapplicant = recapplication.recapplicant
                evaluation.save(flush: true, failOnError: true)
            }
        }
        
        // Calculate parameter-wise averages and total average
        def recevaluationparameter = RecEvaluationParameter.findAllByOranizationAndRecversionAndRecdeptexpertgroup(
            recexpert.oranization, recv, recexpert.recdeptexpertgroup
        )
        
        def totalAverage = 0.0
        def parameterWiseAverages = []
        
        recevaluationparameter.each { param ->
            // Get sum of marks for this parameter from all experts
            def marksSum = RecApplicationEvaluation.createCriteria().get() {
                projections {
                    sum('obtained_marks')
                }
                and {
                    eq('oranization', recexpert.oranization)
                    eq('recversion', recv)
                    eq('recdeptexpertgroup', recexpert.recdeptexpertgroup)
                    eq('recevaluationparameter', param)
                    eq('recapplication', recapplication)
                    eq('recapplicant', recapplication.recapplicant)
                }
            }
            
            // Get count of evaluations for this parameter
            def evaluationCount = RecApplicationEvaluation.findAllByOranizationAndRecversionAndRecdeptexpertgroupAndRecapplicationAndRecapplicantAndRecevaluationparameter(
                recexpert.oranization, recv, recexpert.recdeptexpertgroup, recapplication, recapplication.recapplicant, param
            ).size()
            
            if (marksSum && evaluationCount > 0) {
                def avgMarks = (marksSum / evaluationCount).round(2)
                
                // Save or update parameter-wise average
                RecApplicationParameterWiseEvaluation paramsTotal = RecApplicationParameterWiseEvaluation.findByOranizationAndRecversionAndRecdeptexpertgroupAndRecapplicationAndRecapplicantAndRecevaluationparameter(
                    recexpert.oranization, recv, recexpert.recdeptexpertgroup, recapplication, recapplication.recapplicant, param
                )
                
                RecApplicationParameterWiseEvaluation pwe
                if (paramsTotal) {
                    pwe = paramsTotal
                } else {
                    pwe = new RecApplicationParameterWiseEvaluation()
                    pwe.evaluation_date = new Date()
                    pwe.creation_date = new Date()
                    pwe.creation_ip_address = request.getRemoteAddr()
                }
                
                pwe.obtained_marks = avgMarks
                pwe.username = recexpert.loginname
                pwe.updation_date = new Date()
                pwe.updation_ip_address = request.getRemoteAddr()
                pwe.oranization = recexpert.oranization
                pwe.recversion = recv
                pwe.recdeptexpertgroup = recexpert.recdeptexpertgroup
                pwe.recapplication = recapplication
                pwe.recapplicant = recapplication.recapplicant
                pwe.recevaluationparameter = param
                pwe.save(flush: true, failOnError: true)
                
                totalAverage += avgMarks
                parameterWiseAverages.add([
                    parameterId: param.id,
                    average: avgMarks
                ])
            }
        }
        
        // Save or update total average
        RecApplicationEvaluationAvg recExist = RecApplicationEvaluationAvg.findByOranizationAndRecversionAndRecdeptexpertgroupAndRecapplicationAndRecapplicant(
            recexpert.oranization, recv, recexpert.recdeptexpertgroup, recapplication, recapplication.recapplicant
        )
        
        RecApplicationEvaluationAvg raea
        if (recExist) {
            raea = recExist
        } else {
            raea = new RecApplicationEvaluationAvg()
            raea.creation_date = new Date()
            raea.creation_ip_address = request.getRemoteAddr()
        }
        
        raea.avg = totalAverage.round(2)
        raea.username = recexpert.loginname
        raea.updation_date = new Date()
        raea.updation_ip_address = request.getRemoteAddr()
        raea.oranization = recexpert.oranization
        raea.recversion = recv
        raea.recdeptexpertgroup = recexpert.recdeptexpertgroup
        raea.recapplication = recapplication
        raea.recapplicant = recapplication.recapplicant
        raea.save(flush: true, failOnError: true)
        
        hm.applicationId = applicationId
        hm.totalMarks = marks.sum { it.obtainedMarks }
        hm.parameterWiseAverages = parameterWiseAverages
        hm.totalAverage = totalAverage.round(2)
        hm.msg = "Marks submitted successfully"
        hm.flag = true
    }
    
    // ═══════════════════════════════════════════════════════════════
    // Phase 4: Dashboard & Reports APIs
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Get expert groups dashboard for instructors
     * Used by: GET /recExpert/getExpertGroupsDashboard
     */
    def getExpertGroupsDashboard(hm, request, data) {
        def uid = hm.remove("uid")
        
        if (!uid) {
            hm.msg = "User not authenticated"
            hm.flag = false
            return
        }
        
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
        
        Organization organization = instructor.organization
        
        // Get current recruitment version
        RecVersion recversion = RecVersion.findByOrganizationAndIscurrentforbackendprocessing(organization, true)
        if (!recversion) {
            hm.msg = "No current recruitment version found"
            hm.flag = false
            return
        }
        
        // Get expert groups for this organization and version
        def recdeptexpertgroup = RecDeptExpertGroup.findAllByOrganizationAndRecversion(organization, recversion)
        
        hm.expertGroups = recdeptexpertgroup.collect { [
            id: it.id,
            groupno: it.groupno,
            groupname: it.groupname,
            cutoff: it.cutoff
        ] }
        
        hm.recversion = [
            id: recversion.id,
            version_number: recversion.version_number,
            academicyear: recversion.academicyear?.ay
        ]
        
        hm.organization = [
            id: organization.id,
            name: organization.organization_name
        ]
        
        hm.msg = "Expert groups fetched successfully"
        hm.flag = true
    }
    
    /**
     * Get applicant marks report for specific expert group
     * Used by: GET /recExpert/getApplicantMarksReport
     */
    def getApplicantMarksReport(hm, request, data) {
        def uid = hm.remove("uid")
        def expertGroupId = hm.remove("expertGroupId")
        
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
        
        Organization organization = instructor.organization
        
        // Get expert group
        RecDeptExpertGroup recdeptexpertgroup = RecDeptExpertGroup.get(expertGroupId)
        if (!recdeptexpertgroup) {
            hm.msg = "Expert group not found"
            hm.flag = false
            return
        }
        
        RecVersion recv = recdeptexpertgroup.recversion
        
        // Get applications for this expert group (Round 1)
        RecApplicationRound round = RecApplicationRound.findByRoundnumberAndOrganization(1, organization)
        if (!round) {
            hm.msg = "Application round not found"
            hm.flag = false
            return
        }
        
        def recApplicationTransactions = RecApplicationRoundTransaction.findAllByRecversionAndRecdeptexpertgroupAndOrganizationAndRecapplicationroundAndIsrejected(
            recv, recdeptexpertgroup, organization, round, false
        )
        
        // Get evaluation parameters
        def recevaluationparameter = RecEvaluationParameter.findAllByOranizationAndRecversionAndRecdeptexpertgroup(
            organization, recv, recdeptexpertgroup
        )
        
        // Calculate max marks
        def maxMarks = 0.0
        recevaluationparameter.each { param ->
            maxMarks += param.maxmarks
        }
        
        // Build applicants list with marks
        def finalMarksList = []
        def evaluatedCount = 0
        def pendingCount = 0
        
        recApplicationTransactions.each { transaction ->
            def application = transaction.recapplication
            def applicant = transaction.recapplicant
            
            // Get posts
            def posts = []
            if (application.recpost) {
                application.recpost.each { post ->
                    posts.add(post.post)
                }
            }
            
            // Get parameter-wise marks
            def parameterData = []
            def total = 0.0
            def hasMarks = false
            
            recevaluationparameter.each { param ->
                def paramsTotal = RecApplicationParameterWiseEvaluation.findByOranizationAndRecversionAndRecdeptexpertgroupAndRecapplicationAndRecapplicantAndRecevaluationparameter(
                    organization, recv, recdeptexpertgroup, application, applicant, param
                )
                
                if (paramsTotal) {
                    parameterData.add([
                        parameterId: param.id,
                        parameter: param.parameter,
                        maxmarks: param.maxmarks,
                        obtainedMarks: paramsTotal.obtained_marks
                    ])
                    total += paramsTotal.obtained_marks
                    hasMarks = true
                } else {
                    parameterData.add([
                        parameterId: param.id,
                        parameter: param.parameter,
                        maxmarks: param.maxmarks,
                        obtainedMarks: 0.0
                    ])
                }
            }
            
            if (hasMarks) {
                evaluatedCount++
            } else {
                pendingCount++
            }
            
            def percentage = maxMarks > 0 ? ((total / maxMarks) * 100).round(2) : 0.0
            
            finalMarksList.add([
                applicationId: application.applicaitionid,
                applicantId: applicant.id,
                fullname: applicant.fullname,
                email: applicant.email,
                posts: posts,
                parameterMarks: parameterData,
                totalMarks: total.round(2),
                maxTotalMarks: maxMarks,
                percentage: percentage
            ])
        }
        
        hm.applicants = finalMarksList
        hm.evaluationParameters = recevaluationparameter.collect { [
            id: it.id,
            parameter: it.parameter,
            parameter_number: it.parameter_number,
            maxmarks: it.maxmarks
        ] }
        hm.expertGroup = [
            id: recdeptexpertgroup.id,
            groupno: recdeptexpertgroup.groupno,
            groupname: recdeptexpertgroup.groupname,
            cutoff: recdeptexpertgroup.cutoff
        ]
        hm.maxTotalMarks = maxMarks
        hm.totalApplicants = recApplicationTransactions.size()
        hm.evaluatedApplicants = evaluatedCount
        hm.pendingApplicants = pendingCount
        hm.msg = "Applicant marks fetched successfully"
        hm.flag = true
    }
}
