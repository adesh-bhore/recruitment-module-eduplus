package recruitment

class ERPMCQQuestionBank {
    
    Integer qno
    String question_statement
    String question_file_path
    String question_file_name
    Integer weightage
    Integer unitno
    Boolean isapproved
    
    // Embedded options (legacy - kept for backward compatibility)
    String option_a
    String option_b
    String option_c
    String option_d
    String correct_option  // A, B, C, or D
    
    String username
    Date creation_date
    Date updation_date
    String creation_ip_address
    String updation_ip_address
    
    static belongsTo = [
        reccourse: RecCourse,
        recdeptgroup: RecDeptGroup,
        erpmcqexamname: ERPMCQExamName,
        organization: Organization,
        instructor: Instructor,
        difficultylevel: DifficultyLevel
    ]
    
    static constraints = {
        qno nullable: true
        question_statement nullable: true, blank: true
        question_file_path nullable: true
        question_file_name nullable: true
        weightage nullable: false, min: 1
        unitno nullable: true
        isapproved nullable: false
        option_a nullable: false, maxSize: 500
        option_b nullable: false, maxSize: 500
        option_c nullable: false, maxSize: 500
        option_d nullable: false, maxSize: 500
        correct_option nullable: false, inList: ['A', 'B', 'C', 'D']
        username nullable: true
        creation_date nullable: true
        updation_date nullable: true
        creation_ip_address nullable: true
        updation_ip_address nullable: true
        instructor nullable: true
        difficultylevel nullable: true
        recdeptgroup nullable: true
        erpmcqexamname nullable: true
    }
    
    static mapping = {
        table 'erpmcqquestion_bank'
        version false
        question_statement column: 'question_text', type: 'text'
    }
}
