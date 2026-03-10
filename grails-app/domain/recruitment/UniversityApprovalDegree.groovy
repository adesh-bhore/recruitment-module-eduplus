package recruitment

class UniversityApprovalDegree {

    String degree
    boolean isactive

    String creation_username
    String updation_username
    Date creation_date
    Date updation_date
    String creation_ip_address
    String updation_ip_address

    static belongsTo=[organization : Organization]

    static constraints = {
        degree unique: ['organization']
    }

    static mapping = {
        isactive defaultValue: true
    }
}
