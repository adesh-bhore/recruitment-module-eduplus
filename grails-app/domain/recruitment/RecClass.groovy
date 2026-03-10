package recruitment

class RecClass
{
    String name
    String username
    Date creation_date
    Date updation_date
    String creation_ip_address
    String updation_ip_address

    boolean isactive

    static constraints = { }

    static mapping={
        isactive defaultValue:false
    }

    String toString() {
        name
    }

    static belongsTo=[organization:Organization]
}
