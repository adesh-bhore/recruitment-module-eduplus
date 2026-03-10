package recruitment

class AuditLogEvent {

    String actor
    String uri
    String className
    String persistedObjectId
    String eventName
    String propertyName
    String oldValue
    String newValue
    Long orgid
    Date dateCreated
    Date lastUpdated

    static mapping = {
        table 'audit_log'
        autoTimestamp true
        sort dateCreated: "desc"
    }

    static constraints = {
        actor nullable: true
        orgid nullable: true
        uri nullable: true
        className nullable: false
        persistedObjectId nullable: false
        eventName nullable: false
        propertyName nullable: true
        oldValue nullable: true
        newValue nullable: true
    }

    def beforeInsert() {
        this.actor = subjectregistration.AuditUserContext.getCurrentUser() ?: 'unknown'
        this.uri = subjectregistration.AuditUserContext.getIP()
        this.orgid = subjectregistration.AuditUserContext.getCurrentOrg()?:0
        // 🔒 Mask sensitive fields manually
        if (this.propertyName in ['password']) {
            this.oldValue = '*****'
            this.newValue = '*****'
        }
    }

}
