package recruitment

class StatusType {
    String name  //Admission Cancellation Status

    boolean isactive
    String creation_username
    String updation_username
    Date creation_date
    Date updation_date
    String creation_ip_address
    String updation_ip_address

    static belongsTo=[organization:Organization]
    static mapping = {
        isactive defaultValue:true
    }
    static constraints = {
    }
}
