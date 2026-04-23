package recruitment

class DepartmentType {
    String name    //academics, administrative
    String displayname
    String username
    Date creation_date
    Date updation_date
    String creation_ip_address
    String updation_ip_address
    static constraints = {
        displayname nullable: true
    }

}
