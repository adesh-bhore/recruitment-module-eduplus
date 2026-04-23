package recruitment

class ApiConfiguration {

    String name         // volp // JWT
    String url          // classroom.volp
    String secret_key //jwt key
    int expiry

    String username
    Date creation_date
    Date updation_date
    String creation_ip_address
    String updation_ip_address

    static constraints = {
        url nullable: true
        secret_key nullable: true
    }
    static mapping = {
        expiry defaultValue: 1
    }
}
