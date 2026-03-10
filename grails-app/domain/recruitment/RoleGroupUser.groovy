package recruitment

import grails.plugins.orm.auditable.Auditable

class RoleGroupUser implements Auditable{

    String creation_username
    String updation_username
    Date creation_date
    Date updation_date
    String creation_ip_address
    String updation_ip_address

    static belongsTo=[employeerolegroup: RoleGroup, instructor:Instructor,
                      organization: Organization, department : Department,
                      program:Program, programyear:ProgramYear,
                      stream : Stream,
                      programtype:ProgramType, school:School
    ]

    static constraints = {
        instructor nullable:true
        program nullable:true
        programyear nullable:true
        department nullable:true
        stream nullable:true
        programtype nullable:true
        school nullable:true
        employeerolegroup nullable:true
    }
}
