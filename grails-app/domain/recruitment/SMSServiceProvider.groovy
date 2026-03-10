package recruitment

class SMSServiceProvider {

    String sms_provider_name
    String username
    String password
    String sender
    String header
    boolean is_active

    String api_base_url

    static belongsTo=[organization:Organization]

    static constraints = {
        api_base_url nullable:true
        username nullable:true
        password nullable:true
        sender nullable:true
        header nullable:true
        sms_provider_name unique: ['organization']
    }
    static mapping = {
        is_active defaultValue: false
    }
}
