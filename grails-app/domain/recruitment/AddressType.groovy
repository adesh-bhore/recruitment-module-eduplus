package recruitment

class AddressType {

    String type               //local,permanent
    boolean isactive

    String username
    Date creation_date
    Date updation_date
    String creation_ip_address
    String updation_ip_address

    static belongsTo=[organization:Organization] //Organization Not In Use

    static mapping = {
        isactive defaultValue:false
    }

    static constraints = {
        organization nullable:true
    }
}
