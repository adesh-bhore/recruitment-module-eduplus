package recruitment
import grails.plugins.orm.auditable.Auditable

class Salutation implements Auditable{

    String name   //Mr. / Mrs/ Dr. /
    boolean isactive

    String username
    Date creation_date
    Date updation_date
    String creation_ip_address
    String updation_ip_address

    static belongsTo=[organization:Organization]

    static mapping = {
        isactive defaultValue:false
    }

    static constraints = {
        name unique: ['organization']
    }
}
