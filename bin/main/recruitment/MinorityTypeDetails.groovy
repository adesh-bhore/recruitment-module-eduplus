package recruitment

class MinorityTypeDetails {

    String name                                                  //Linguistic- Hindi / Religious- Muslim

    String username
    Date creation_date
    Date updation_date
    String creation_ip_address
    String updation_ip_address

    static belongsTo=[organization:Organization,minoritytype:MinorityType]
    static constraints = {}
}
