package recruitment

import grails.plugins.orm.auditable.Auditable

class Login implements Auditable{
    String username   //email
    String password
    String grno_empid
    Date creation_date
    Date updation_date
    String creation_ip_address
    String updation_ip_address
    boolean isloginblocked
    boolean otpbasedlogin
    String loginthroughips
    String access_token
    boolean bypassed_by_admin //true -> rolelink check isblockedforbypass flag
    String token
    Date token_creation_date
    Date token_expiry_date
    String mobileHashKey
    boolean isMobileHashKeyApproved // Mobile Change status
    int mobileChangeCount // Mobile Change Count

    static belongsTo=[organization:Organization, person:Person]
    static hasMany = [roles: Role,usertype:UserType]
    static constraints = {
        //username unique : true, blank: false
        username nullable:true
        grno_empid nullable:true
        organization nullable:true
        access_token nullable:true
        person nullable:true
        token nullable:true
        token_creation_date nullable:true
        token_expiry_date nullable:true
        otpbasedlogin nullable:false
        loginthroughips nullable:true
        mobileHashKey nullable:true
    }
    static mapping= {
        isMobileHashKeyApproved defaultValue: false
        bypassed_by_admin defaultValue: false
        mobileChangeCount defaultValue: 0
    }
}
