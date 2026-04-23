package recruitment

class ProgramType {

    String name    //UG/PG/PhD
    String displayname
    String abbrivation
    String durationregular
    String durationseda

    boolean isapplicabletoadmission
    boolean ispercentagebasedresult
    boolean isannualpatern
    boolean isintermediategradenotapplicable
    boolean yeardownrule1
    boolean donotdisplayatprovisionaldashboard
    String programtypecode //this is reverse API
    int sort_order

    String username
    Date creation_date
    Date updation_date
    String creation_ip_address
    String updation_ip_address
    static belongsTo=[organization:Organization]

    static constraints = {
        displayname nullable:true
        organization nullable: true
        abbrivation nullable: true
        durationregular nullable: true
        durationseda nullable: true
        programtypecode nullable: true
    }

    static mapping = {
        sort_order defaultValue: 0
        isannualpatern defaultValue: false
        isintermediategradenotapplicable defaultValue: false
        donotdisplayatprovisionaldashboard defaultValue: false
        isapplicabletoadmission defaultValue: false
        ispercentagebasedresult defaultValue: false
        yeardownrule1 defaultValue: false
    }
}
