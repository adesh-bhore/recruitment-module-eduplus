package recruitment

class LinkCategory {

    String name  //Subject Registration, Assessment Sceheme
    int sort_order
    boolean isactive
    String icon

    String creation_username
    String updation_username
    Date creation_date
    Date updation_date
    String creation_ip_address
    String updation_ip_address

    static belongsTo=[
            organization:Organization,roletype:RoleType
    ]
    static constraints = {
        icon nullable:true
        name unique: ['organization','roletype']
    }
    static mapping = {
    }


}
