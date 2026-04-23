package recruitment

class RoleGroup {

    String name
    boolean isotherrole
    int sort_order

    String creation_username
    String updation_username
    Date creation_date
    Date updation_date
    String creation_ip_address
    String updation_ip_address

    static belongsTo=[
            organization:Organization,
            usertype : UserType,
            rolelevel: RoleLevel
    ]

    static mapping = {
        isotherrole defaultValue: false
        sort_order defaultValue: 0
    }

}
