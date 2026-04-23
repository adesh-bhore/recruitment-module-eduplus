package recruitment

class RecExpert {

    int expertno
    String expname
    String loginname
    String password
    boolean isblocked
    String username
    Date creation_date
    Date updation_date
    String creation_ip_address
    String updation_ip_address
    static belongsTo=[oranization:Organization,
                      recversion:RecVersion,
                      recdeptexpertgroup:RecDeptExpertGroup,
                      recexperttype:RecExpertType]
    static constraints = {
    }
    static mapping = {
        isblocked defaultValue: false
    }
}
