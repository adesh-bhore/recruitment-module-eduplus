package recruitment

class RecEvaluationParameter {

    String parameter
    int parameter_number
    double maxmarks
    String username
    Date creation_date
    Date updation_date
    String creation_ip_address
    String updation_ip_address
    static belongsTo=[oranization:Organization,recversion:RecVersion,recexperttype:RecExpertType,recdeptexpertgroup : RecDeptExpertGroup]
       static constraints = {
    }
}
