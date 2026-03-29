package recruitment

class ERPMCQQuestionBank {
    
    String question_text
    String option_a
    String option_b
    String option_c
    String option_d
    String correct_option  // A, B, C, or D
    Integer weightage
    Boolean isapproved
    String username
    Date creation_date
    Date updation_date
    String creation_ip_address
    String updation_ip_address
    
    static belongsTo = [
        reccourse: RecCourse,
        recdeptgroup: RecDeptGroup,
        erpmcqexamname: ERPMCQExamName,
        organization: Organization
    ]
    
    static constraints = {
        question_text nullable: false, blank: false
        option_a nullable: false, blank: false
        option_b nullable: false, blank: false
        option_c nullable: false, blank: false
        option_d nullable: false, blank: false
        correct_option nullable: false, inList: ['A', 'B', 'C', 'D']
        weightage nullable: false, min: 1
        isapproved nullable: false
        username nullable: true
        creation_date nullable: true
        updation_date nullable: true
        creation_ip_address nullable: true
        updation_ip_address nullable: true
    }
    
    static mapping = {
        table 'erpmcqquestion_bank'
        version false
        question_text type: 'text'
    }
}
