package recruitment

import grails.gorm.transactions.Transactional
import common.AWSUploadDocumentsService
import common.AWSBucketService
import javax.servlet.http.Part

@Transactional
class RecOnlineMcqService {
    
    // ═══════════════════════════════════════════════════════════════
    // Phase 1: Question Bank Management APIs (Admin)
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Get instructor's courses list
     * Used by: GET /recOnlineMcq/getCourses
     */
    def getInstructorCourses(hm, request, data) {
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
        
        Organization org = instructor.organization
        def instructorCourses = RecCourse.findAllByOrganizationAndInstructor(org, instructor)
        
        hm.courses = instructorCourses.collect { course ->
            [
                id: course.id,
                name: course.course_name,
                code: course.course_code,
                minimum_questions: course.minimum_number_of_questions_in_bank,
                exam_questions: course.number_of_questions_to_be_picked_for_exam,
                total_marks: course.totalmarksinexam
            ]
        }
        
        hm.msg = "Courses fetched successfully"
        hm.flag = true
    }
    
    /**
     * Get all questions for a course with pagination
     * Used by: GET /recOnlineMcq/getQuestionBank
     */
    def getQuestionBank(hm, request, data) {
        def uid = hm.remove("uid")
        def courseId = hm.remove("courseId")
        def groupNo = hm.remove("groupNo")
        def page = hm.remove("page") ? hm.remove("page").toInteger() : 1
        def pageSize = hm.remove("pageSize") ? hm.remove("pageSize").toInteger() : 50
        
        if (!uid) {
            hm.msg = "User not authenticated"
            hm.flag = false
            return
        }
        
        if (!courseId) {
            hm.msg = "Course ID is required"
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
        
        Organization org = instructor.organization
        RecCourse course = RecCourse.findById(courseId)
        
        if (!course) {
            hm.msg = "Course not found"
            hm.flag = false
            return
        }
        
        // Verify instructor owns this course
        if (course.instructor?.id != instructor.id) {
            hm.msg = "Unauthorized access to course"
            hm.flag = false
            return
        }
        
        // Get all questions for the course
        def allQuestions = ERPMCQQuestionBank.findAllByReccourseAndOrganization(course, org)
        
        // Filter by group if specified
        if (groupNo) {
            RecDeptGroup recdeptgroup = RecDeptGroup.findById(groupNo)
            if (recdeptgroup) {
                allQuestions = allQuestions.findAll { it.recdeptgroup?.id == recdeptgroup.id }
            }
        }
        
        def totalCount = allQuestions.size()
        
        // Apply pagination
        def startIndex = (page - 1) * pageSize
        def endIndex = Math.min(startIndex + pageSize, totalCount)
        def paginatedQuestions = allQuestions.subList(startIndex, endIndex)
        
        // Get AWS configuration for presigned URLs
        AWSBucket awsBucket = null
        AWSFolderPath awsFolderPath = null
        AWSBucketService awsBucketService = null
        
        try {
            awsBucket = AWSBucket.findByContent("documents")
            awsFolderPath = AWSFolderPath.findById(5)
            awsBucketService = new AWSBucketService()
        } catch (Exception e) {
            println("AWS configuration error: ${e.message}")
        }
        
        def questionsList = []
        for (que in paginatedQuestions) {
            HashMap temp = new HashMap()
            temp.put("que_id", que?.id)
            temp.put("qno", que?.qno)
            temp.put("question_statement", que?.question_statement)
            temp.put("weightage", que?.weightage)
            temp.put("isapproved", que?.isapproved)
            temp.put("unitno", que?.unitno)
            temp.put("recdeptgroup", que?.recdeptgroup?.groupno)
            temp.put("recdeptgroupId", que?.recdeptgroup?.id)
            
            if (awsBucket && awsFolderPath && awsBucketService && 
                que?.question_file_path != null && que?.question_file_name != null) {
                try {
                    def path = awsFolderPath.path + que?.question_file_path + que?.question_file_name
                    def doc_url = awsBucketService.getPresignedUrl(awsBucket.bucketname, path, awsBucket.region)
                    temp.put("doc_url", doc_url)
                    temp.put("question_file_name", que?.question_file_name)
                } catch (Exception e) {
                    println("AWS presigned URL error: ${e.message}")
                    temp.put("doc_url", null)
                    temp.put("question_file_name", que?.question_file_name)
                }
            } else {
                temp.put("doc_url", null)
                temp.put("question_file_name", null)
            }
            
            questionsList.add(temp)
        }
        
        // Get department groups for filter
        def recdeptgroups = RecDeptGroup.findAllByOrganization(org)
        
        hm.course = [
            id: course.id,
            name: course.course_name,
            code: course.course_code
        ]
        hm.questions = questionsList
        hm.totalCount = totalCount
        hm.page = page
        hm.pageSize = pageSize
        hm.totalPages = Math.ceil(totalCount / pageSize).toInteger()
        hm.deptGroups = recdeptgroups.collect { [id: it.id, groupno: it.groupno] }
        hm.msg = "Question bank fetched successfully"
        hm.flag = true
    }
    
    /**
     * Create new MCQ question with options
     * Used by: POST /recOnlineMcq/createQuestion
     */
    def createQuestion(hm, request, data) {
        def uid = hm.remove("uid")
        def courseId = data.courseId
        def qno = data.qno
        def questionStatement = data.question_statement
        def weightage = data.weightage
        def isapproved = data.isapproved
        def recdeptgroupId = data.recdeptgroup
        def selectoption = data.selectoption ? data.selectoption.toInteger() : 2
        
        if (!uid) {
            hm.msg = "User not authenticated"
            hm.flag = false
            return
        }
        
        if (!courseId || !qno || !questionStatement || !weightage || !recdeptgroupId) {
            hm.msg = "Required fields missing: courseId, qno, question_statement, weightage, recdeptgroup"
            hm.flag = false
            return
        }
        
        if (selectoption < 1 || selectoption > 5) {
            hm.msg = "Number of options must be between 1 and 5"
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
        
        Organization org = instructor.organization
        RecCourse course = RecCourse.findById(courseId)
        
        if (!course) {
            hm.msg = "Course not found"
            hm.flag = false
            return
        }
        
        // Verify instructor owns this course
        if (course.instructor?.id != instructor.id) {
            hm.msg = "Unauthorized access to course"
            hm.flag = false
            return
        }
        
        RecDeptGroup recdeptgroup = RecDeptGroup.findById(recdeptgroupId)
        if (!recdeptgroup) {
            hm.msg = "Department group not found"
            hm.flag = false
            return
        }
        
        DifficultyLevel difficultylevel = DifficultyLevel.findByName("Moderate")
        def ename = ERPMCQExamName.findByNameAndOrganization("VIT-FACULTY-RECRUITMENT-ONLINE-TEST", org)
        
        // Collect option statements and find correct answer
        def optionStatements = [:]
        def correctAnswerIndex = 0
        
        for (int i = 1; i <= selectoption; i++) {
            def optionStatement = data["option_statement_${i}"]
            if (optionStatement) {
                optionStatements[i] = optionStatement
                if (data.correct_answer == "isanswer${i}") {
                    correctAnswerIndex = i
                }
            }
        }
        
        // Validate we have at least 2 options and a correct answer
        if (optionStatements.size() < 2) {
            hm.msg = "At least 2 options are required"
            hm.flag = false
            return
        }
        
        if (correctAnswerIndex == 0) {
            hm.msg = "Correct answer must be specified"
            hm.flag = false
            return
        }
        
        // Create question
        ERPMCQQuestionBank question = new ERPMCQQuestionBank()
        question.qno = qno.toInteger()
        question.question_statement = questionStatement
        question.weightage = weightage.toInteger()
        question.isapproved = (isapproved == 'true' || isapproved == true)
        
        // Populate embedded options (required by database schema)
        question.option_a = optionStatements[1] ?: "N/A"
        question.option_b = optionStatements[2] ?: "N/A"
        question.option_c = optionStatements[3] ?: "N/A"
        question.option_d = optionStatements[4] ?: "N/A"
        
        // Set correct option (A, B, C, or D)
        def correctOptionMap = [1: 'A', 2: 'B', 3: 'C', 4: 'D']
        question.correct_option = correctOptionMap[correctAnswerIndex]
        
        question.username = uid
        question.creation_date = new Date()
        question.updation_date = new Date()
        question.creation_ip_address = request.getRemoteAddr()
        question.updation_ip_address = request.getRemoteAddr()
        question.organization = org
        question.reccourse = course
        question.instructor = instructor
        question.difficultylevel = difficultylevel
        question.recdeptgroup = recdeptgroup
        question.erpmcqexamname = ename
        
        // Handle question file upload
        AWSUploadDocumentsService awsUploadDocumentsService = new AWSUploadDocumentsService()
        AWSFolderPath afp = AWSFolderPath.findById(5)
        
        def qfile = request.getFile('qfile')
        if (qfile && !qfile.empty) {
            Part filePart = request.getPart("qfile")
            String path = "recruitment/questionimage/" + course.id + "/"
            String awsfp = afp.path + path
            String existsFilePath = ""
            existsFilePath = awsfp + path + qfile.originalFilename
            boolean isUploaded = awsUploadDocumentsService.uploadDocument(filePart, awsfp, qfile.originalFilename, existsFilePath)
            
            if (isUploaded) {
                question.question_file_path = path
                question.question_file_name = qfile.originalFilename
            } else {
                question.question_file_path = null
                question.question_file_name = null
            }
        } else {
            question.question_file_path = null
            question.question_file_name = null
        }
        
        question.save(flush: true, failOnError: true)
        
        // Create separate option records (for new structure)
        for (int i = 1; i <= selectoption; i++) {
            def optionStatement = data["option_statement_${i}"]
            def isCorrect = data.correct_answer == "isanswer${i}"
            
            if (!optionStatement) {
                continue
            }
            
            ERPMCQOptions option = new ERPMCQOptions()
            option.opno = i
            option.option_statement = optionStatement
            option.iscorrecetoption = isCorrect
            
            // Handle option file upload
            def opfile = request.getFile("file${i}")
            if (opfile && !opfile.empty) {
                Part filePart = request.getPart("file${i}")
                String path = "recruitment/questionimage/" + course.id + "/"
                String awsfp = afp.path + path
                String existsFilePath = ""
                existsFilePath = awsfp + path + opfile.originalFilename
                boolean isUploaded = awsUploadDocumentsService.uploadDocument(filePart, awsfp, opfile.originalFilename, existsFilePath)
                
                if (isUploaded) {
                    option.option_file_path = path
                    option.option_file_name = opfile.originalFilename
                } else {
                    option.option_file_path = null
                    option.option_file_name = null
                }
            } else {
                option.option_file_name = null
                option.option_file_path = null
            }
            
            option.username = uid
            option.creation_date = new Date()
            option.updation_date = new Date()
            option.creation_ip_address = request.getRemoteAddr()
            option.updation_ip_address = request.getRemoteAddr()
            option.organization = org
            option.erpmcquestionbank = question
            option.save(flush: true, failOnError: true)
        }
        
        hm.questionId = question.id
        hm.msg = "Question created successfully"
        hm.flag = true
    }
    
    /**
     * Update existing question with options
     * Used by: PUT /recOnlineMcq/updateQuestion/:id
     */
    def updateQuestion(hm, request, data) {
        def uid = hm.remove("uid")
        def questionId = hm.remove("questionId")
        def courseId = data.courseId
        def questionStatement = data.question_statement
        def weightage = data.weightage
        def isapproved = data.isapproved
        def recdeptgroupId = data.recdeptgroup
        def selectoption = data.selectoption ? data.selectoption.toInteger() : 2
        
        if (!uid) {
            hm.msg = "User not authenticated"
            hm.flag = false
            return
        }
        
        if (!questionId) {
            hm.msg = "Question ID is required"
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
        
        Organization org = instructor.organization
        ERPMCQQuestionBank question = ERPMCQQuestionBank.findById(questionId)
        
        if (!question) {
            hm.msg = "Question not found"
            hm.flag = false
            return
        }
        
        RecCourse course = question.reccourse
        
        // Verify instructor owns this course
        if (course.instructor?.id != instructor.id) {
            hm.msg = "Unauthorized access to question"
            hm.flag = false
            return
        }
        
        // Update question fields
        if (questionStatement) {
            question.question_statement = questionStatement
        }
        
        if (weightage) {
            question.weightage = weightage.toInteger()
        }
        
        if (isapproved != null) {
            question.isapproved = (isapproved == 'true' || isapproved == true)
        }
        
        if (recdeptgroupId) {
            RecDeptGroup recdeptgroup = RecDeptGroup.findById(recdeptgroupId)
            if (recdeptgroup) {
                question.recdeptgroup = recdeptgroup
            }
        }
        
        question.username = uid
        question.updation_date = new Date()
        question.updation_ip_address = request.getRemoteAddr()
        
        // Collect updated option statements and find correct answer
        def optionStatements = [:]
        def correctAnswerIndex = 0
        
        for (int i = 1; i <= selectoption; i++) {
            def optionStatement = data["option_statement_${i}"]
            if (optionStatement) {
                optionStatements[i] = optionStatement
                if (data.correct_answer == "isanswer${i}") {
                    correctAnswerIndex = i
                }
            }
        }
        
        // Update embedded options if provided
        if (optionStatements.size() >= 2) {
            question.option_a = optionStatements[1] ?: question.option_a
            question.option_b = optionStatements[2] ?: question.option_b
            question.option_c = optionStatements[3] ?: question.option_c
            question.option_d = optionStatements[4] ?: question.option_d
            
            // Update correct option if provided
            if (correctAnswerIndex > 0) {
                def correctOptionMap = [1: 'A', 2: 'B', 3: 'C', 4: 'D']
                question.correct_option = correctOptionMap[correctAnswerIndex]
            }
        }
        
        // Handle question file upload with try-catch
        try {
            AWSUploadDocumentsService awsUploadDocumentsService = new AWSUploadDocumentsService()
            AWSFolderPath afp = AWSFolderPath.findById(5)
            
            def qfile = request.getFile('qfile')
            if (qfile && !qfile.empty && afp) {
                Part filePart = request.getPart("qfile")
                String path = "recruitment/questionimage/" + course.id + "/"
                String awsfp = afp.path + path
                String existsFilePath = ""
                
                // Delete old file if exists
                if (question.question_file_path && question.question_file_name) {
                    existsFilePath = afp.path + question.question_file_path + question.question_file_name
                }
                
                boolean isUploaded = awsUploadDocumentsService.uploadDocument(filePart, awsfp, qfile.originalFilename, existsFilePath)
                
                if (isUploaded) {
                    question.question_file_path = path
                    question.question_file_name = qfile.originalFilename
                }
            }
        } catch (Exception e) {
            println("AWS upload error for question file: ${e.message}")
        }
        
        question.save(flush: true, failOnError: true)
        
        // Update options
        def existingOptions = ERPMCQOptions.findAllByErpmcquestionbank(question)
        def correctAnswerFound = false
        
        for (int i = 1; i <= selectoption; i++) {
            def optionStatement = data["option_statement_${i}"]
            def isCorrect = data.correct_answer == "isanswer${i}"
            def optionId = data["option_id_${i}"]
            
            if (!optionStatement) {
                continue
            }
            
            ERPMCQOptions option
            
            // Find existing option or create new
            if (optionId) {
                option = ERPMCQOptions.findById(optionId)
            }
            
            if (!option) {
                option = existingOptions.find { it.opno == i }
            }
            
            if (!option) {
                option = new ERPMCQOptions()
                option.opno = i
                option.organization = org
                option.erpmcquestionbank = question
                option.creation_date = new Date()
                option.creation_ip_address = request.getRemoteAddr()
            }
            
            option.option_statement = optionStatement
            option.iscorrecetoption = isCorrect
            
            // Handle option file upload with try-catch
            try {
                AWSUploadDocumentsService awsUploadDocumentsService = new AWSUploadDocumentsService()
                AWSFolderPath afp = AWSFolderPath.findById(5)
                
                def opfile = request.getFile("file${i}")
                if (opfile && !opfile.empty && afp) {
                    Part filePart = request.getPart("file${i}")
                    String path = "recruitment/questionimage/" + course.id + "/"
                    String awsfp = afp.path + path
                    String existsFilePath = ""
                    
                    // Delete old file if exists
                    if (option.option_file_path && option.option_file_name) {
                        existsFilePath = afp.path + option.option_file_path + option.option_file_name
                    }
                    
                    boolean isUploaded = awsUploadDocumentsService.uploadDocument(filePart, awsfp, opfile.originalFilename, existsFilePath)
                    
                    if (isUploaded) {
                        option.option_file_path = path
                        option.option_file_name = opfile.originalFilename
                    }
                }
            } catch (Exception e) {
                println("AWS upload error for option ${i}: ${e.message}")
            }
            
            option.username = uid
            option.updation_date = new Date()
            option.updation_ip_address = request.getRemoteAddr()
            option.save(flush: true, failOnError: true)
        }
        
        hm.questionId = question.id
        hm.msg = "Question updated successfully"
        hm.flag = true
    }
    
    /**
     * Toggle question approval status
     * Used by: PATCH /recOnlineMcq/toggleApproval/:id
     */
    def toggleQuestionApproval(hm, request, data) {
        def uid = hm.remove("uid")
        def questionId = hm.remove("questionId")
        def isapproved = data.isapproved
        
        if (!uid) {
            hm.msg = "User not authenticated"
            hm.flag = false
            return
        }
        
        if (!questionId) {
            hm.msg = "Question ID is required"
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
        
        ERPMCQQuestionBank question = ERPMCQQuestionBank.get(questionId)
        
        if (!question) {
            hm.msg = "Question not found"
            hm.flag = false
            return
        }
        
        // Verify instructor owns this course (simplified check)
        def courseInstructorId = question.instructor?.id
        if (courseInstructorId && courseInstructorId != instructor.id) {
            hm.msg = "Unauthorized access to question"
            hm.flag = false
            return
        }
        
        question.isapproved = (isapproved == 'true' || isapproved == true)
        question.username = uid
        question.updation_date = new Date()
        question.updation_ip_address = request.getRemoteAddr()
        question.save(flush: true, failOnError: true)
        
        hm.questionId = question.id
        hm.isapproved = question.isapproved
        hm.msg = "Question approval status updated successfully"
        hm.flag = true
    }
    
    // ═══════════════════════════════════════════════════════════════
    // Phase 2: Question Details and File Management APIs
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Get full question details with all options for editing
     * Used by: GET /recOnlineMcq/getQuestionDetails
     */
    def getQuestionDetails(hm, request, data) {
        def uid = hm.remove("uid")
        def questionId = hm.remove("questionId")
        
        if (!uid) {
            hm.msg = "User not authenticated"
            hm.flag = false
            return
        }
        
        if (!questionId) {
            hm.msg = "Question ID is required"
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
        
        Organization org = instructor.organization
        ERPMCQQuestionBank question = ERPMCQQuestionBank.get(questionId)
        
        if (!question) {
            hm.msg = "Question not found"
            hm.flag = false
            return
        }
        
        // Verify instructor owns this question
        def questionInstructorId = question.instructor?.id
        if (questionInstructorId && questionInstructorId != instructor.id) {
            hm.msg = "Unauthorized access to question"
            hm.flag = false
            return
        }
        
        // Get AWS configuration with try-catch
        def questionFileUrl = null
        try {
            if (question.question_file_path && question.question_file_name) {
                AWSBucket awsBucket = AWSBucket.findByContent("documents")
                AWSFolderPath awsFolderPath = AWSFolderPath.findById(5)
                AWSBucketService awsBucketService = new AWSBucketService()
                
                if (awsBucket && awsFolderPath) {
                    def path = awsFolderPath.path + question.question_file_path + question.question_file_name
                    questionFileUrl = awsBucketService.getPresignedUrl(awsBucket.bucketname, path, awsBucket.region)
                }
            }
        } catch (Exception e) {
            println("AWS error for question file: ${e.message}")
        }
        
        // Build question object
        hm.question = [
            id: question.id,
            qno: question.qno,
            question_statement: question.question_statement,
            weightage: question.weightage,
            isapproved: question.isapproved,
            unitno: question.unitno,
            recdeptgroup_id: question.recdeptgroup?.id,
            question_file_name: question.question_file_name,
            question_file_url: questionFileUrl
        ]
        
        // Get all options for this question
        def erpmcqoptions = ERPMCQOptions.findAllByErpmcquestionbank(question)
        def optionsList = []
        
        for (option in erpmcqoptions) {
            def optionFileUrl = null
            
            try {
                if (option.option_file_path && option.option_file_name) {
                    AWSBucket awsBucket = AWSBucket.findByContent("documents")
                    AWSFolderPath awsFolderPath = AWSFolderPath.findById(5)
                    AWSBucketService awsBucketService = new AWSBucketService()
                    
                    if (awsBucket && awsFolderPath) {
                        def path = awsFolderPath.path + option.option_file_path + option.option_file_name
                        optionFileUrl = awsBucketService.getPresignedUrl(awsBucket.bucketname, path, awsBucket.region)
                    }
                }
            } catch (Exception e) {
                println("AWS error for option ${option.id}: ${e.message}")
            }
            
            optionsList.add([
                id: option.id,
                opno: option.opno,
                option_statement: option.option_statement,
                iscorrecetoption: option.iscorrecetoption,
                option_file_name: option.option_file_name,
                option_file_url: optionFileUrl
            ])
        }
        
        hm.options = optionsList
        
        // Get department groups for dropdown
        def recdeptgroups = RecDeptGroup.findAllByOrganization(org)
        hm.deptGroups = recdeptgroups.collect { [id: it.id, groupno: it.groupno] }
        
        hm.msg = "Question details fetched successfully"
        hm.flag = true
    }
    
    /**
     * Get only options for a specific question
     * Used by: GET /recOnlineMcq/getQuestionOptions
     */
    def getQuestionOptions(hm, request, data) {
        def uid = hm.remove("uid")
        def questionId = hm.remove("questionId")
        
        if (!uid) {
            hm.msg = "User not authenticated"
            hm.flag = false
            return
        }
        
        if (!questionId) {
            hm.msg = "Question ID is required"
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
        
        ERPMCQQuestionBank question = ERPMCQQuestionBank.get(questionId)
        
        if (!question) {
            hm.msg = "Question not found"
            hm.flag = false
            return
        }
        
        // Verify instructor owns this question
        def questionInstructorId = question.instructor?.id
        if (questionInstructorId && questionInstructorId != instructor.id) {
            hm.msg = "Unauthorized access to question"
            hm.flag = false
            return
        }
        
        // Get all options for this question
        def erpmcqoptions = ERPMCQOptions.findAllByErpmcquestionbank(question)
        def optionsList = []
        
        for (option in erpmcqoptions) {
            def optionFileUrl = null
            
            try {
                if (option.option_file_path && option.option_file_name) {
                    AWSBucket awsBucket = AWSBucket.findByContent("documents")
                    AWSFolderPath awsFolderPath = AWSFolderPath.findById(5)
                    AWSBucketService awsBucketService = new AWSBucketService()
                    
                    if (awsBucket && awsFolderPath) {
                        def path = awsFolderPath.path + option.option_file_path + option.option_file_name
                        optionFileUrl = awsBucketService.getPresignedUrl(awsBucket.bucketname, path, awsBucket.region)
                    }
                }
            } catch (Exception e) {
                println("AWS error for option ${option.id}: ${e.message}")
            }
            
            optionsList.add([
                id: option.id,
                opno: option.opno,
                option_statement: option.option_statement,
                iscorrecetoption: option.iscorrecetoption,
                option_file_name: option.option_file_name,
                option_file_url: optionFileUrl
            ])
        }
        
        hm.questionId = questionId
        hm.options = optionsList
        hm.msg = "Options fetched successfully"
        hm.flag = true
    }
    
    /**
     * Delete question image file from AWS
     * Used by: DELETE /recOnlineMcq/deleteQuestionFile
     */
    def deleteQuestionFile(hm, request, data) {
        def uid = hm.remove("uid")
        def questionId = hm.remove("questionId")
        
        if (!uid) {
            hm.msg = "User not authenticated"
            hm.flag = false
            return
        }
        
        if (!questionId) {
            hm.msg = "Question ID is required"
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
        
        ERPMCQQuestionBank question = ERPMCQQuestionBank.get(questionId)
        
        if (!question) {
            hm.msg = "Question not found"
            hm.flag = false
            return
        }
        
        // Verify instructor owns this question
        def questionInstructorId = question.instructor?.id
        if (questionInstructorId && questionInstructorId != instructor.id) {
            hm.msg = "Unauthorized access to question"
            hm.flag = false
            return
        }
        
        // Delete file from AWS if exists
        if (question.question_file_path && question.question_file_name) {
            try {
                AWSBucket awsBucket = AWSBucket.findByContent("documents")
                AWSFolderPath awsFolderPath = AWSFolderPath.findById(5)
                
                if (awsBucket && awsFolderPath) {
                    AWSBucketService awsBucketService = new AWSBucketService()
                    def filePath = awsFolderPath.path + question.question_file_path + question.question_file_name
                    
                    // Delete from AWS S3
                    awsBucketService.deleteObject(awsBucket.bucketname, awsBucket.region, filePath)
                    println("Deleted file from AWS: ${filePath}")
                }
            } catch (Exception e) {
                println("AWS delete error: ${e.message}")
                // Continue to clear database fields even if AWS delete fails
            }
        }
        
        // Clear file references in database
        question.question_file_name = null
        question.question_file_path = null
        question.username = uid
        question.updation_date = new Date()
        question.updation_ip_address = request.getRemoteAddr()
        question.save(flush: true, failOnError: true)
        
        hm.questionId = question.id
        hm.msg = "Question file deleted successfully"
        hm.flag = true
    }
    
    /**
     * Delete option image file from AWS
     * Used by: DELETE /recOnlineMcq/deleteOptionFile
     */
    def deleteOptionFile(hm, request, data) {
        def uid = hm.remove("uid")
        def optionId = hm.remove("optionId")
        
        if (!uid) {
            hm.msg = "User not authenticated"
            hm.flag = false
            return
        }
        
        if (!optionId) {
            hm.msg = "Option ID is required"
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
        
        ERPMCQOptions option = ERPMCQOptions.get(optionId)
        
        if (!option) {
            hm.msg = "Option not found"
            hm.flag = false
            return
        }
        
        // Verify instructor owns the question that this option belongs to
        def questionInstructorId = option.erpmcquestionbank?.instructor?.id
        if (questionInstructorId && questionInstructorId != instructor.id) {
            hm.msg = "Unauthorized access to option"
            hm.flag = false
            return
        }
        
        // Delete file from AWS if exists
        if (option.option_file_path && option.option_file_name) {
            try {
                AWSBucket awsBucket = AWSBucket.findByContent("documents")
                AWSFolderPath awsFolderPath = AWSFolderPath.findById(5)
                
                if (awsBucket && awsFolderPath) {
                    AWSBucketService awsBucketService = new AWSBucketService()
                    def filePath = awsFolderPath.path + option.option_file_path + option.option_file_name
                    
                    // Delete from AWS S3
                    awsBucketService.deleteObject(awsBucket.bucketname, awsBucket.region, filePath)
                    println("Deleted file from AWS: ${filePath}")
                }
            } catch (Exception e) {
                println("AWS delete error: ${e.message}")
                // Continue to clear database fields even if AWS delete fails
            }
        }
        
        // Clear file references in database
        option.option_file_name = null
        option.option_file_path = null
        option.username = uid
        option.updation_date = new Date()
        option.updation_ip_address = request.getRemoteAddr()
        option.save(flush: true, failOnError: true)
        
        hm.optionId = option.id
        hm.msg = "Option file deleted successfully"
        hm.flag = true
    }
    
    // ═══════════════════════════════════════════════════════════════
    // Phase 3: Student View and Question Display APIs
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Get all approved questions for a course (student exam view)
     * Used by: GET /recOnlineMcq/studentView
     */
    def getStudentView(hm, request, data) {
        def uid = hm.remove("uid")
        def courseId = hm.remove("courseId")
        
        if (!uid) {
            hm.msg = "User not authenticated"
            hm.flag = false
            return
        }
        
        if (!courseId) {
            hm.msg = "Course ID is required"
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
        
        Organization org = instructor.organization
        RecCourse course = RecCourse.get(courseId)
        
        if (!course) {
            hm.msg = "Course not found"
            hm.flag = false
            return
        }
        
        // Get all questions for total count
        def allQuestions = ERPMCQQuestionBank.findAllByReccourse(course)
        def totalQuestions = allQuestions.size()
        
        // Get only approved questions
        def approvedQuestions = ERPMCQQuestionBank.findAllByReccourseAndOrganizationAndIsapproved(course, org, true)
        
        // Sort by question number
        approvedQuestions = approvedQuestions.sort { it.qno }
        
        // Get AWS configuration with try-catch
        AWSBucket awsBucket = null
        AWSFolderPath awsFolderPath = null
        AWSBucketService awsBucketService = null
        
        try {
            awsBucket = AWSBucket.findByContent("documents")
            awsFolderPath = AWSFolderPath.findById(5)
            awsBucketService = new AWSBucketService()
        } catch (Exception e) {
            println("AWS configuration error: ${e.message}")
        }
        
        // Build questions list with options
        def questionsList = []
        for (question in approvedQuestions) {
            def questionFileUrl = null
            
            // Get question file URL
            try {
                if (awsBucket && awsFolderPath && awsBucketService && 
                    question.question_file_path && question.question_file_name) {
                    def path = awsFolderPath.path + question.question_file_path + question.question_file_name
                    questionFileUrl = awsBucketService.getPresignedUrl(awsBucket.bucketname, path, awsBucket.region)
                }
            } catch (Exception e) {
                println("AWS error for question ${question.id}: ${e.message}")
            }
            
            // Get options for this question
            def questionOptions = ERPMCQOptions.findAllByErpmcquestionbankAndOrganization(question, org)
            def optionsList = []
            
            for (option in questionOptions) {
                def optionFileUrl = null
                
                try {
                    if (awsBucket && awsFolderPath && awsBucketService && 
                        option.option_file_path && option.option_file_name) {
                        def path = awsFolderPath.path + option.option_file_path + option.option_file_name
                        optionFileUrl = awsBucketService.getPresignedUrl(awsBucket.bucketname, path, awsBucket.region)
                    }
                } catch (Exception e) {
                    println("AWS error for option ${option.id}: ${e.message}")
                }
                
                optionsList.add([
                    id: option.id,
                    opno: option.opno,
                    option_statement: option.option_statement,
                    iscorrecetoption: option.iscorrecetoption,
                    option_file_name: option.option_file_name,
                    option_file_url: optionFileUrl
                ])
            }
            
            questionsList.add([
                que_id: question.id,
                qno: question.qno,
                question_statement: question.question_statement,
                weightage: question.weightage,
                unitno: question.unitno,
                question_file_name: question.question_file_name,
                question_file_url: questionFileUrl,
                options: optionsList
            ])
        }
        
        // Get difficulty levels
        def difficultyLevels = DifficultyLevel.list()
        
        hm.course = [
            id: course.id,
            name: course.course_name,
            code: course.course_code
        ]
        hm.questions = questionsList
        hm.totalQuestions = totalQuestions
        hm.approvedQuestions = approvedQuestions.size()
        hm.difficultyLevels = difficultyLevels.collect { [id: it.id, name: it.name, description: it.description] }
        hm.msg = "Questions fetched successfully"
        hm.flag = true
    }
    
    /**
     * Get a specific question with all its options (detailed view)
     * Used by: GET /recOnlineMcq/getQuestionWithOptions
     */
    def getQuestionWithOptions(hm, request, data) {
        def uid = hm.remove("uid")
        def questionId = hm.remove("questionId")
        
        if (!uid) {
            hm.msg = "User not authenticated"
            hm.flag = false
            return
        }
        
        if (!questionId) {
            hm.msg = "Question ID is required"
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
        
        Organization org = instructor.organization
        ERPMCQQuestionBank question = ERPMCQQuestionBank.get(questionId)
        
        if (!question) {
            hm.msg = "Question not found"
            hm.flag = false
            return
        }
        
        // Get AWS configuration with try-catch
        def questionFileUrl = null
        try {
            if (question.question_file_path && question.question_file_name) {
                AWSBucket awsBucket = AWSBucket.findByContent("documents")
                AWSFolderPath awsFolderPath = AWSFolderPath.findById(5)
                AWSBucketService awsBucketService = new AWSBucketService()
                
                if (awsBucket && awsFolderPath) {
                    def path = awsFolderPath.path + question.question_file_path + question.question_file_name
                    questionFileUrl = awsBucketService.getPresignedUrl(awsBucket.bucketname, path, awsBucket.region)
                }
            }
        } catch (Exception e) {
            println("AWS error for question file: ${e.message}")
        }
        
        // Build question object
        hm.question = [
            id: question.id,
            qno: question.qno,
            question_statement: question.question_statement,
            weightage: question.weightage,
            isapproved: question.isapproved,
            unitno: question.unitno,
            question_file_name: question.question_file_name,
            question_file_url: questionFileUrl
        ]
        
        // Get all options for this question
        def questionOptions = ERPMCQOptions.findAllByErpmcquestionbankAndOrganization(question, org)
        def optionsList = []
        
        for (option in questionOptions) {
            def optionFileUrl = null
            
            try {
                if (option.option_file_path && option.option_file_name) {
                    AWSBucket awsBucket = AWSBucket.findByContent("documents")
                    AWSFolderPath awsFolderPath = AWSFolderPath.findById(5)
                    AWSBucketService awsBucketService = new AWSBucketService()
                    
                    if (awsBucket && awsFolderPath) {
                        def path = awsFolderPath.path + option.option_file_path + option.option_file_name
                        optionFileUrl = awsBucketService.getPresignedUrl(awsBucket.bucketname, path, awsBucket.region)
                    }
                }
            } catch (Exception e) {
                println("AWS error for option ${option.id}: ${e.message}")
            }
            
            optionsList.add([
                id: option.id,
                opno: option.opno,
                option_statement: option.option_statement,
                iscorrecetoption: option.iscorrecetoption,
                option_file_name: option.option_file_name,
                option_file_url: optionFileUrl
            ])
        }
        
        hm.options = optionsList
        hm.msg = "Question with options fetched successfully"
        hm.flag = true
    }
}
