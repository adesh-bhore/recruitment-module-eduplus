package recruitment

class Status {

    String name  //Initiated / in-process / rejected / approved / refund-initiated / refund-completed
    int sort
    boolean isactive
    String creation_username
    String updation_username
    Date creation_date
    Date updation_date
    String creation_ip_address
    String updation_ip_address

    static belongsTo=[organization:Organization,statustype:StatusType]
    static mapping = {
        isactive defaultValue:true
    }
    static constraints = {
    }

}
