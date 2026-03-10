package recruitment

class RecDeptExpertGroup {
    int groupno  //has to be 1..2...3...4
    String groupname  //COMP-IT-MCA
    double cutoff
    double round2cutoff
    String username
    Date creation_date
    Date updation_date
    String creation_ip_address
    String updation_ip_address
    static belongsTo=[organization:Organization,
                      recversion:RecVersion,
                      recdeptgroup:RecDeptGroup]
    static hasMany = [department:Department,program:Program]
    static constraints = {
        recdeptgroup nullable:  true
    }
    String toString(){groupname}
}
