package recruitment

class RecApplicationRoundTransaction {

    boolean isrejected
    String username
    Date creation_date
    Date updation_date
    String creation_ip_address
    String updation_ip_address
    static belongsTo=[organization:Organization,
                      recapplicationround:RecApplicationRound,
                      recapplicant:RecApplicant,
                      recversion:RecVersion,
                      recdeptexpertgroup:RecDeptExpertGroup,
                      recapplication:RecApplication]
    static constraints = {
    }
}
