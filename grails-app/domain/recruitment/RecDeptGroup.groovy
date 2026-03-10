package recruitment

class RecDeptGroup {

    int groupno  //has to be 1..2...3...4

    String username
    Date creation_date
    Date updation_date
    String creation_ip_address
    String updation_ip_address
    static belongsTo=[organization:Organization,recversion:RecVersion]
    static hasMany = [department:Department,program:Program]
    static constraints = {
        recversion nullable:true
    }
}
