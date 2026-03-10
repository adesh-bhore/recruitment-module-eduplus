package recruitment

class ERPISORevision {

    String system_name// for zeal it will be controller-action
    String issue_number
    String revision_number //Revision No
    Date revision_date    //Revision Date
    String ff_number     //Record No
    String formname      //form description
    String username
    boolean iscurrent
    Date creation_date
    Date updation_date
    String creation_ip_address
    String updation_ip_address
    static belongsTo=[organization:Organization,roletype:RoleType,academicyear:AcademicYear]
    static constraints = {
        academicyear nullable:true
    }


}
