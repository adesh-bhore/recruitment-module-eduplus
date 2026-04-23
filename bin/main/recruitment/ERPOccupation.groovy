package recruitment

import grails.plugins.orm.auditable.Auditable

class ERPOccupation implements Auditable{

    String occupation
    String username
    Date creation_date
    Date updation_date
    String creation_ip_address
    String updation_ip_address

    static belongsTo=[organization:Organization]
    static auditable = [
            maskedFields: ['occupation']
    ]
    static constraints = {
        occupation unique: ['organization']
    }

}
