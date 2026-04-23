package recruitment

import grails.plugins.orm.auditable.Auditable

class Person implements Auditable{
    String last_school_attended               //For TC/LC
    String telegram_chat_id                   //Telegram Chat ID
    String email                              //primary email
    String institute_email
    String grno
    String firstName
    String middleName
    String lastName
    String name_in_hindi
    String highest_qualification
    String skills
    String short_description

    Date date_of_birth
    boolean isphysicallyhandicapped                //Yes/No
    String father_full_name
    String mother_full_name
    String birth_place
    String birth_taluka
    String native_place
    String aadhar_no
    String father_first_name
    String mother_first_name
    String pan_no
    String fullname_as_per_previous_marksheet
    String nameasperaadhar
    boolean iscreamylayer                          //Yes/No
    boolean is_exserviceman                        //Yes/No
    boolean isbelongtogovernmentscheme             //Yes/No
    boolean is_having_gap_in_academic_year         //Yes/No
    String gap_in_academic_years                   //comma separated academic years
    boolean iseducation_loan_availed               //Yes/No
    boolean is_sponsored_candidate                 //Yes/No
    String strength
    String weakness
    String hobbies
    String describe_yourself
    String technical_interest
//    String opportunity
//    String threats
//    String career_objectives
//    String improve_weakness
//    boolean isfatheralumnus
//    boolean ismotheralumnus
//    boolean isfamilybusiness
//    String familybusinessdetails

    String username
    Date creation_date
    Date updation_date
    String creation_ip_address
    String updation_ip_address

    String driving_license_no
    Date driving_license_valid_upto

    String passport_no
    Date passport_valid_upto
    String visa_no
    Date visa_valid_upto
    String resedential_permit_no
    Date resedential_permit_issue_date
    Date resedential_permit_valid_upto_date
    String fsisnumber
    String abcid
    String antiraggingnumber
    String height
    String identity_mark
    String maidan_full_name
//    String physical_requirements_for_discharge_of_duty
    boolean isemailvalidated
    boolean isemailverified
    double percentage_of_disability

    static belongsTo=[
            gender:Gender,
//            birthdistrict:ERPDistrict,
//            birthstate:ERPState,
            birthcountry:ERPCountry,
//            nativedistrict:ERPDistrict,
//            nativestate:ERPState,
            nativecountry:ERPCountry,
//            nativeareatype:NativeAreaType,
            erpbloodgroup:ERPBloodGroup,
            mothertounge:ERPLanguage,
            organization:Organization,
//            handicapType:HandicapType,
//            minorityType:MinorityType,
//            minoritytypedetails:MinorityTypeDetails,
//            salutation:Salutation,
    ]
//    static hasMany = [logos : Logo,social:Social]
    static mapping = {
        isphysicallyhandicapped defaultValue: false
        iscreamylayer defaultValue: false
        is_exserviceman defaultValue: false
        isbelongtogovernmentscheme defaultValue: false
        is_having_gap_in_academic_year defaultValue: false
        iseducation_loan_availed defaultValue: false
        is_sponsored_candidate defaultValue: false
        isemailvalidated defaultValue: false
        isemailverified defaultValue: false
        percentage_of_disability defaultValue: 0.0
        isfatheralumnus defaultValue: false
        ismotheralumnus defaultValue: false
        isfamilybusiness defaultValue: false
    }
    static constraints = {
     //   physical_requirements_for_discharge_of_duty nullable:true
        nameasperaadhar nullable:true
        height nullable:true
        identity_mark nullable:true
        maidan_full_name nullable:true
        resedential_permit_issue_date nullable:true
        resedential_permit_valid_upto_date nullable:true
        fsisnumber nullable: true
        firstName nullable: true
        middleName nullable: true
        lastName nullable: true
        date_of_birth nullable: true
        gender nullable: true
       // social nullable: true
        highest_qualification nullable: true
        short_description nullable: true
        skills nullable: true
        grno nullable:true
        email nullable:true
        name_in_hindi nullable:true
        father_full_name nullable:true
        mother_full_name nullable:true
        father_first_name nullable:true
        mother_first_name nullable:true
        birth_place nullable:true
        birth_taluka nullable:true
      //  birthdistrict nullable:true
    //    birthstate nullable:true
        birthcountry nullable:true
        native_place nullable:true
     //   nativedistrict nullable:true
      //  nativestate nullable:true
        nativecountry nullable:true
//        nativeareatype nullable:true
        erpbloodgroup nullable:true
        aadhar_no nullable:true
        mothertounge nullable:true
        fullname_as_per_previous_marksheet nullable:true
        gap_in_academic_years nullable:true
        pan_no nullable:true
        institute_email nullable:true
        strength nullable:true
        weakness nullable:true
        hobbies nullable:true
        describe_yourself nullable:true
        technical_interest nullable:true

        driving_license_no nullable:true
        driving_license_valid_upto nullable:true
        passport_no nullable:true
        passport_valid_upto nullable:true
        visa_no nullable:true
        visa_valid_upto nullable:true
        resedential_permit_no nullable:true
        resedential_permit_issue_date nullable:true
        resedential_permit_valid_upto_date nullable:true
        organization nullable:true
        antiraggingnumber nullable: true
        telegram_chat_id nullable: true
        last_school_attended nullable: true
//        handicapType nullable: true
//        minorityType nullable: true
//        minoritytypedetails nullable: true
        abcid nullable: true
//        salutation nullable: true
//        opportunity nullable:true
//        threats nullable : true
//        career_objectives nullable : true
//        improve_weakness nullable : true
//        familybusinessdetails nullable :true
    }
}
