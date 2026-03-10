package recruitment

class RecLogin
{
    String username
    String password
    boolean isblocked
    Date creation_date
    Date updation_date
    String creation_ip_address
    String updation_ip_address
    static mapping = {
        isblocked defaultValue: false
    }
    static constraints = {
    }
}
