package recruitment

class EmployeeType {
    String type //(Teaching/Non Teaching/class IV)
    String display_name

    boolean isloadadustmentrequired
    int retirementage

    String username
    Date creation_date
    Date updation_date
    String creation_ip_address
    String updation_ip_address
    static belongsTo=[
            organization:Organization,
            employeecategory:EmployeeCategory
    ]
    static constraints = {
        organization nullable: true
        type unique: ['organization']
        employeecategory nullable: true
    }

    static mapping = {
        isloadadustmentrequired  defaultValue: false
        retirementage  defaultValue: 60
    }
}