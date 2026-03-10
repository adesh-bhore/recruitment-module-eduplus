package recruitment

class ERPRoleTypeSettings {

    String name
    String displayname
    String value
    String type
    String default_value
    String impact
    String urls

    String creation_username
    String updation_username
    Date creation_date
    Date updation_date
    String creation_ip_address
    String updation_ip_address

    static belongsTo=[ organization:Organization, roletype:RoleType,erproletypesettingscategory:ERPRoleTypeSettingsCategory,
                       program:Program,year:Year,programtype:ProgramType]

    static constraints = {
        program nullable : true
        year nullable : true
        programtype nullable : true
        erproletypesettingscategory nullable : true
        default_value nullable : true
        impact nullable : true
        urls nullable : true
    }
}
