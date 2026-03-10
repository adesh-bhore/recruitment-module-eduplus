package recruitment

class RecBranch
{
    String name
    String branch_abbrivation
    boolean isactive
    String username
    Date creation_date
    Date updation_date
    String creation_ip_address
    String updation_ip_address
    static belongsTo=[organization:Organization,recversion:RecVersion,program:Program]
    static constraints = {
         branch_abbrivation nullable:true
    }
    static mapping = {
        isactive defaultValue: true
    }
}
