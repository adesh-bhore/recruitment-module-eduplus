package recruitment

class ERPNotificationToType {

    String type        //parent/student/guardian
    boolean isactive
    String username
    Date creation_date
    Date updation_date
    String creation_ip_address
    String updation_ip_address
    static belongsTo=[organization:Organization]
    static constraints = {
        type unique: ['organization']
    }
}
