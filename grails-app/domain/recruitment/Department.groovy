package recruitment

class Department {
    String official_department_email
    boolean is_applicable_to_maintenance
    String name
    String abbrivation
    String static_link                      //for website faculty profile display
    String hod_sign_filepath
    String hod_sign_filename
    String username
    Date creation_date
    Date updation_date
    String creation_ip_address
    String updation_ip_address
    static belongsTo=[
            hod:Instructor,
            organization:Organization,
            departmenttype:DepartmentType,
            stream:Stream,
    ]
    static constraints = {
        hod nullable: true
        abbrivation nullable: true
        stream nullable: true
        official_department_email nullable: true
        static_link nullable: true
        hod_sign_filepath nullable :true
        hod_sign_filename nullable : true
    }

    static mapping = {
        is_applicable_to_maintenance defaultValue: false
    }
}
