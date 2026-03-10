package recruitment

class RoleHistory {

    String formname
    String revokeformname
    String creation_username
    String updation_username
    String creation_ip_address
    String updation_ip_address
    Date creation_date
    Date updation_date

    static belongsTo=[role: Role,rolerevokeby:Person, roleassignedby : Person, roleassignedto : Person, organization : Organization]

    static constraints = {
        rolerevokeby nullable: true
        revokeformname nullable: true
        updation_username nullable: true
        updation_ip_address nullable: true
        updation_date nullable: true
    }
}
