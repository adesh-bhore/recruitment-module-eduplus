package recruitment

class RecPost
{
    boolean isactive
    String username
    Date creation_date
    Date updation_date
    String creation_ip_address
    String updation_ip_address
    static belongsTo=[organization:Organization,recversion:RecVersion,designation:Designation]
    static constraints = {
    }
    static mapping = {
        isactive defaultValue: true
    }
}
