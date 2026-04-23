package recruitment

class ERPMCQExamSecretCode {
    
    String secret_code
    Double obtained_score
    Boolean isexamgiven
    Date examgivendate
    Date start_time
    Date end_time
    Integer extra_time
    String username
    Date creation_date
    Date updation_date
    String creation_ip_address
    String updation_ip_address
    
    static belongsTo = [
        organization: Organization,
        recapplicant: RecApplicant,
        recapplication: RecApplication,
        recversion: RecVersion,
        recdeptgroup: RecDeptGroup
    ]
    
    static constraints = {
        secret_code nullable: false, blank: false, unique: true, size: 6..6
        obtained_score nullable: false
        isexamgiven nullable: false
        examgivendate nullable: true
        start_time nullable: true
        end_time nullable: true
        extra_time nullable: false
        username nullable: true
        creation_date nullable: true
        updation_date nullable: true
        creation_ip_address nullable: true
        updation_ip_address nullable: true
    }
    
    static mapping = {
        table 'erpmcqexam_secret_code'
        version false
    }
}
