package recruitment

class FormName {

    String name                                      //Student Profile
    String routename                                      //route name
    boolean isActive
    int display_order

    String creation_username
    String updation_username
    Date creation_date
    Date updation_date
    String creation_ip_address
    String updation_ip_address

    static belongsTo=[organization:Organization,usertype: UserType]
    static constraints = {
        organization nullable:true
        usertype nullable:true
        routename nullable:true
    }
    static mapping = {
        display_order defaultValue: 0
    }

}
