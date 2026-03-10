package recruitment

class UserType {
    String type
    boolean istypeset
    String username
    Date creation_date
    Date updation_date
    String creation_ip_address
    String updation_ip_address
    static belongsTo=[application_type:ApplicationType, organization:Organization]
    static constraints = {
        type nullable:false
    }

}
