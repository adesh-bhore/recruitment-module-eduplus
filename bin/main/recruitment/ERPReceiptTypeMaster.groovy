package recruitment

class ERPReceiptTypeMaster {

    String type
    boolean is_consider_payment_synchronization

    String creation_username
    String updation_username
    Date creation_date
    Date updation_date
    String creation_ip_address
    String updation_ip_address

    static belongsTo=[organization:Organization]

    static constraints = {
        type unique: ['organization']
    }

    static mapping = {
        is_consider_payment_synchronization defaultValue: false
    }
}
