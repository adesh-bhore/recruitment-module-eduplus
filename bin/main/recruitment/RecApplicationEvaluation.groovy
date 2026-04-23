package recruitment

class RecApplicationEvaluation {

    double obtained_marks
    Date evaluation_date
    String username
    Date creation_date
    Date updation_date
    String creation_ip_address
    String updation_ip_address
    static belongsTo=[oranization:Organization,
                      recversion:RecVersion,
                      recdeptexpertgroup:RecDeptExpertGroup,
                      recevaluationparameter:RecEvaluationParameter,
                      recapplication:RecApplication,
                      recexpert:RecExpert,
                      recapplicant:RecApplicant,
                      recexperttype:RecExpertType]
    static constraints = {
        recdeptexpertgroup nullable : true
        recexpert nullable : true
    }
}
