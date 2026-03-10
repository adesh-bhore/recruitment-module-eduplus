package recruitment

import grails.plugins.orm.auditable.Auditable

class PersonDetails implements Auditable{
    String library_id
    String creation_username
    String updation_username
    Date creation_date
    Date updation_date
    String creation_ip_address
    String updation_ip_address

    static belongsTo=[
            person:Person,
    ]

    static constraints = {
        library_id nullable : true
    }
}
