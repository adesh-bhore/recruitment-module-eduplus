package recruitment

class ERPMCQOptions {
    
    Integer opno                    // Option number (1-5)
    String option_statement         // Option text (HTML supported)
    Boolean iscorrecetoption        // Is this the correct answer
    String option_file_path         // AWS S3 path for option image
    String option_file_name         // File name for option image
    
    // Audit fields
    String username
    Date creation_date
    Date updation_date
    String creation_ip_address
    String updation_ip_address
    
    // Relationships
    ERPMCQQuestionBank erpmcquestionbank
    Organization organization
    
    static constraints = {
        opno nullable: false
        option_statement nullable: true, maxSize: 5000
        iscorrecetoption nullable: false
        option_file_path nullable: true
        option_file_name nullable: true
        username nullable: true
        creation_date nullable: true
        updation_date nullable: true
        creation_ip_address nullable: true
        updation_ip_address nullable: true
        erpmcquestionbank nullable: false
        organization nullable: false
    }
    
    static mapping = {
        table 'erpmcqoptions'
        version false
        id generator: 'identity'
        option_statement type: 'text'
    }
}
