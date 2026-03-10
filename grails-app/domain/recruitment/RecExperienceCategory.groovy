package recruitment

class RecExperienceCategory {

    String category
    String display_name
    boolean isactive

    String creation_username
    String updation_username
    Date creation_date
    Date updation_date
    String creation_ip_address
    String updation_ip_address

    static constraints = {
        display_name nullable : true
    }
    static mapping = {
        isactive defaultValue: true
    }
    static belongsTo=[organization:Organization]
}
