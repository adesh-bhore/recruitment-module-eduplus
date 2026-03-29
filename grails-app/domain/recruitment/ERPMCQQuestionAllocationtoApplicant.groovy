package recruitment

class ERPMCQQuestionAllocationtoApplicant {
    
    String studentselectedoption  // A, B, C, D or null if not answered
    String answeripaddress
    String username
    Date creation_date
    Date updation_date
    String creation_ip_address
    String updation_ip_address
    
    static belongsTo = [
        organization: Organization,
        recversion: RecVersion,
        recapplicant: RecApplicant,
        recapplication: RecApplication,
        recdeptgroup: RecDeptGroup,
        reccourse: RecCourse,
        erpmcqquestionbank: ERPMCQQuestionBank
    ]
    
    static constraints = {
        studentselectedoption nullable: true, inList: ['A', 'B', 'C', 'D']
        answeripaddress nullable: true
        username nullable: true
        creation_date nullable: true
        updation_date nullable: true
        creation_ip_address nullable: true
        updation_ip_address nullable: true
    }
    
    static mapping = {
        table 'erpmcqquestion_allocationto_applicant'
        version false
    }
}
