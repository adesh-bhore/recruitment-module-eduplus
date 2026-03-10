package recruitment

class ERPNotificationFrom
{
    String notificationfrom
    String password
    boolean isactive
    String prefix     //for SMS example: VI-Accounts

    String username
    Date creation_date
    Date updation_date
    String creation_ip_address
    String updation_ip_address
    static belongsTo=[organization:Organization,roletype:RoleType,erpcommunicationmode:ERPCommunicationMode]
    static constraints = {
        roletype nullable: true
        prefix nullable: true
    }
}
