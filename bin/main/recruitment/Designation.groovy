package recruitment

class Designation {

    String name
    String abbreavation
    int displayorder

    String username
    Date creation_date
    Date updation_date
    String creation_ip_address
    String updation_ip_address
    static belongsTo=[employeetype:EmployeeType , organization:Organization]

    static constraints = {
        employeetype nullable:true
        organization nullable:true
        abbreavation nullable:true
    }
    static hasMany = [defaultroles: Role]



    String toString(){
        name
    }
}
