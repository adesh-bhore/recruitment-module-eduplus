package recruitment

class RecCategory
{
    String name
    boolean isactive
    String username
    Date creation_date
    Date updation_date
    String creation_ip_address
    String updation_ip_address
    static constraints = {
    }
    static mapping = {
        isactive defaultValue: true
    }
    String toString()
    {
        name
    }
}
