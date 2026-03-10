package recruitment

class ERPCountry {

    String name
    String nationality
    String username
    Date creation_date
    Date updation_date
    String creation_ip_address
    String updation_ip_address
    static constraints = {
        nationality nullable:true
    }
}
