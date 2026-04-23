package recruitment

import grails.plugins.orm.auditable.Auditable

class Role  implements Auditable{
    String role
    String icon
    int sort_order
    String role_displayname
    boolean isRoleSet
    boolean isapplicabletobulk
    String username
    Date creation_date
    Date updation_date
    String creation_ip_address
    String updation_ip_address

    static belongsTo=[roletype:RoleType,usertype:UserType,organization:Organization, rolecategory:RoleCategory]

    static constraints = {
        organization nullable: true
        role nullable:false
        usertype nullable:true
        rolecategory nullable:true
        role_displayname nullable:true
        icon nullable:true
    }

    static mapping = {
        isapplicabletobulk defaultValue: false
    }

}
