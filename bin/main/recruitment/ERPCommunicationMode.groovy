package recruitment

class ERPCommunicationMode
{
    String mode      //SMS/Email/Whatsapp
    int sort_order

    boolean isactive
    String username
    Date creation_date
    Date updation_date
    String creation_ip_address
    String updation_ip_address
    static belongsTo=[organization:Organization]
    static constraints = {
        mode unique: ['organization']
    }
}
