package recruitment

import grails.plugins.orm.auditable.Auditable

class ApplicationAcademicYear implements Auditable
{
    boolean isActive   //which ay is active
    boolean isDeleted     //row is considered or deleted

    Date aystartdate
    Date ayenddate

    String username
    Date creation_date
    Date updation_date
    String creation_ip_address
    String updation_ip_address
    //roletype means modules for. ex. admission, registration,tt,fb
    boolean dhemispullenabled
    static belongsTo=[
            roletype:RoleType,
            academicyear:AcademicYear,
            semester:Semester,
            organization:Organization
    ]

    static mapping =
    {
        isActive defaultValue:false
        isDeleted defaultValue:false
        dhemispullenabled defaultValue:false
    }

    static constraints = {
        semester nullable: true
        aystartdate nullable:true
        ayenddate nullable:true
    }
}
