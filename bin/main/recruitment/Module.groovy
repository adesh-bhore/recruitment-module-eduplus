package recruitment

class Module {

	String module
    String username    
    Date creation_date
    Date updation_date
    String creation_ip_address
    String updation_ip_address

    static belongsTo=[
            organization:Organization,
            regularmodule:Module //regular module mapping to honor minor module
    ]

    static constraints = {
        regularmodule nullable:true
        module unique: ['organization']
    }

}
