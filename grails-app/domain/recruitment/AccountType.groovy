package recruitment

class AccountType {

    String type
    String display_name
    boolean isactive

    String creation_username
    String updation_username
    Date creation_date
    Date updation_date
    String creation_ip_address
    String updation_ip_address

    static belongsTo=[ organization:Organization]

    static constraints = {
        isactive defaultValue: true
        type unique: ['organization']
    }
}
