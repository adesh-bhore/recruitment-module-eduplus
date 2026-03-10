package recruitment

class ERPCalendarYear {

    String year
    boolean iscurrent

    String username
    Date creation_date
    Date updation_date
    String creation_ip_address
    String updation_ip_address

    static mapping ={
        iscurrent defaultValue: false
    }

    static constraints = {
    }
}
