package recruitment

class RecDegree
{
    String name
    boolean isactive
    String required   //blank means optional  and required means compulsory
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
}
