package recruitment

class ERPClassOfDegree {

    double min
    double max
    String classofdegree
    boolean isdisplayingradecard

    String creation_username
    String updation_username
    Date creation_date
    Date updation_date
    String creation_ip_address
    String updation_ip_address

    static belongsTo=[organization:Organization, programtype:ProgramType]

    static constraints = {
        classofdegree unique: ['organization','programtype']
    }

    static mapping = {
        min defaultValue: 0
        max defaultValue: 0
        max defaultValue: false
    }
}
