package recruitment

class EmployeeCategory
{
    String type //Teaching/Non Teaching
    String display_name
    String username
    Date creation_date
    Date updation_date
    String creation_ip_address
    String updation_ip_address
    static belongsTo=[organization:Organization]
    static constraints = {
        display_name nullable:true
        type unique: ['organization']
    }

}
