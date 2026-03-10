package recruitment

class RecApplicant
{
    String email  //same as user name
    String fullname
    String cast
    String pancardno
    String aadhaarcardno
    Date dateofbirth
    String mobilenumber
    String area_of_specialization
    String any_other_info_related_to_post
    String present_salary
    String photopath
    String photoname
    boolean ishandicapped
    String username
    Date creation_date
    Date updation_date
    String creation_ip_address
    String updation_ip_address
    static belongsTo=[reccategory:RecCategory,maritalstatus:MaritalStatus,salutation:Salutation,minoritytypedetails:MinorityTypeDetails,gender:Gender]
    static constraints = {
        cast nullable:true
        pancardno nullable:true
        aadhaarcardno nullable:true
        any_other_info_related_to_post nullable:true
        present_salary nullable:true
        photopath nullable:true
        photoname nullable:true
        area_of_specialization nullable:true
        maritalstatus nullable: true
        salutation nullable:true
        gender nullable:true
        minoritytypedetails nullable:true
    }
    static mapping= {
        ishandicapped defaultValue:false
    }
}
