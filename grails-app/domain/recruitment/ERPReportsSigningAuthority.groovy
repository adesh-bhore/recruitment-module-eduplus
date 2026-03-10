package recruitment

class ERPReportsSigningAuthority {

    String name   // reportname
    int sortorder
    String key_name
    String value_name
    String description
    boolean isactive

    String creation_username
    String updation_username
    Date creation_date
    Date updation_date
    String creation_ip_address
    String updation_ip_address

    static belongsTo=[ organization:Organization]

    static mapping = {
        isactive defaultValue: true
    }
    static constraints = {
    }
}
