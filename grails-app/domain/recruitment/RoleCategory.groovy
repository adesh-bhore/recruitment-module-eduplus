package recruitment

class RoleCategory {
    String name
    String color

    String creation_username
    String updation_username
    Date creation_date
    Date updation_date
    String creation_ip_address
    String updation_ip_address

    static belongsTo=[organization:Organization]

    static constraints = {
        color nullable: true
        name unique: ['organization']
    }
}
