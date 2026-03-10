package recruitment

class Year {
	String year
    String display_name
    int sort_oder
    String grno_code

    String username    
    Date creation_date
    Date updation_date
    String creation_ip_address
    String updation_ip_address

    static belongsTo=[organization:Organization, yearmaster: YearMaster]

    static constraints = {
        organization nullable: true
        yearmaster nullable: true
        grno_code nullable: true
        year unique: ['organization']
    }

    static mapping = {
        sort_oder defaultValue: 0
    }
}
