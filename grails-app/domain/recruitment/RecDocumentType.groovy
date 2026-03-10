package recruitment

class RecDocumentType
{
    String type //Cast Certificate, UG Degree Certificate etc.
    boolean isactive
    String username
    String size
    String extension
    String info
    String resolution
    Date creation_date
    Date updation_date
    String creation_ip_address
    String updation_ip_address
    boolean iscompulsory
    static constraints = {
        size nullable: true
        extension nullable: true
        info nullable: true
    }
    static mapping = {
        isactive defaultValue: true
        iscompulsory defaultValue: true

    }
    String toString()
    {
        type
    }
}
