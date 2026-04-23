package recruitment

class ERPFacultyPost {

    String name   //HOD,DeanQuality,COE  
    String username
    Date creation_date
    Date updation_date
    String creation_ip_address
    String updation_ip_address
    static belongsTo=[organization:Organization, erpfacultyposttype:ERPFacultyPostType]
    static constraints = {
        erpfacultyposttype nullable:true
    }
}
