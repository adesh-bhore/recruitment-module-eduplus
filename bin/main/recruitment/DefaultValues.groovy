package recruitment

import grails.plugins.orm.auditable.Auditable

class DefaultValues implements Auditable{

    String value
    boolean isactive
    int sort_order
    String other

    String creation_username
    String updation_username
    Date creation_date
    Date updation_date
    String creation_ip_address
    String updation_ip_address

    static belongsTo=[organization : Organization, defaultvaluetype:DefaultValueType]

    static constraints = {
    }

    static mapping = {
        sort_order defaultValue: 0
        isactive defaultValue: true
    }
}
