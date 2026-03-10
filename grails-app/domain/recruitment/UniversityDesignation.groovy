package recruitment

class UniversityDesignation {

    String designation
    boolean isactive

    String creation_username
    String updation_username
    Date creation_date
    Date updation_date
    String creation_ip_address
    String updation_ip_address

    static belongsTo=[organization : Organization]

    static constraints = {
        designation unique: ['organization']
    }

    static mapping = {
        isactive defaultValue: true
    }
}
