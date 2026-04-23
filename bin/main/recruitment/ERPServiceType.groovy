package recruitment

class ERPServiceType {

    String type   //Regular/Visiting
    boolean isapplicableforpayroll   //Regular/Visiting

    String username
    Date creation_date
    Date updation_date
    String creation_ip_address
    String updation_ip_address

    static belongsTo=[organization:Organization]

    static constraints = {
        type unique: ['organization']
    }

    static mapping = {
        isapplicableforpayroll defaultValue: true
    }

}
