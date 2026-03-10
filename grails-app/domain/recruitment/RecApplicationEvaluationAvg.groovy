package recruitment

class RecApplicationEvaluationAvg
{
    double avg
    String username
    Date creation_date
    Date updation_date
    String creation_ip_address
    String updation_ip_address
    static belongsTo=[oranization:Organization,recversion:RecVersion,recdeptexpertgroup:RecDeptExpertGroup,
                      recapplication:RecApplication,recapplicant:RecApplicant]
    static constraints = {
        recdeptexpertgroup nullable: true
    }
}
