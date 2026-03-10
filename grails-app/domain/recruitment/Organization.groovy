package recruitment

class Organization {

    String trust_name
    String icard_organization_name
    String icard_organization_tag                      //An Autonomous Institute to Savitribai Phule Pune University
    String website
    String address
    String uid                                         //This should be simmilar to login.username
    String email
    String organization_name
    String organization_code
    String registration_number
    String display_name
    String organization_detailed_name                  //will be used for office order
    String establishment_email                         //will be used for sending mail
    String establishment_email_credentials
    String account_email                               //will be used for sending mail
    String account_email_credentials
    String admission_email                         //will be used for sending mail
    String admission_email_credentials
    String official_director_email                     //Director
    String official_registrar_email                    //Registrar
    String official_bract_email                        //BRACT
    String official_director_mobileno                  //Director
    String official_registrar_mobileno                 //Registrar
    String official_bract_mobileno
    String studentsection_email
    String studentsection_email_credentials
    String gsuit_credentials_file_path
    String gsuit_credentials_file_name
    String gsuit_org_unit
    String instructor_website
    String learner_website
    String admission_website
    String account_sign
    String examination_sign
    String principal_sign
    String dean_sign
    String account_sign_fees_estimate
    String account_stamp
    String logincode
    String state
    String statecode
    String gst_no
    String sac_code
    String trustmobilenoforfeescollectionmsg
    String organization_number
    String ptrc_number
    String director_name
    String admission_vuejs_page
    String hostel_vuejs_page
    String adhock_vuejs_page
    String tallyserverurl
    String challan_bank_name
    String challan_bank_logo
    String challan_bank_address
    String challan_sign_place
    String org_logo
    String org_exam_code
    String org_university_code
    String org_admission_code
    String logo_file_path
    String logo_file_name
    String payroll_email
    String payroll_watermark
    String communication_access_key
    String examination_email
    String biometricdbstring
    String biometricdbusername
    String biometricdbpassword
    String mobile_number
    String fax_number
    String instructor_android_app_link
    String instructor_ios_app_link
    String learner_android_app_link
    String learner_ios_app_link
    String generalInstructorWebsite

    Date payment_due_date

    int minweeklyhoursperweek
    int gsuit_port_number
    int sort_order

    boolean isactive
    boolean isgsuitapplicable
    boolean enableadmissionemailotp
    boolean enableadmissionmobileotp
    boolean iseducationalinstitute
    boolean ischallanapplicable
    boolean isreceiptapplicable
    boolean autoapproveleavejobon
    boolean autoapprovecertificatejobon
    boolean autoinstallmentemailjobon
    boolean autoattendancesendonemail
    boolean autoabsentattendancesendonemail
    boolean autofreezeandsynchattendance
    boolean autosendfeesduelisttohod
    boolean autosendregistrationduelisttohod
    boolean validateemailwithzerobounce
    boolean isschool
    boolean ishardblockondue

    String username
    Date creation_date
    Date updation_date
    String creation_ip_address
    String updation_ip_address

    static belongsTo=[
            organization:Organization,
            organizationtype:OrganizationType,
            organizationgroup:OrganizationGroup,
            examcoordinator:Instructor,
            timetablecoordinator:Instructor ,
            feedbackcoordinator:Instructor
    ]

    static constraints = {
        organization_name nullable: true
        tallyserverurl nullable: true
        organization_code nullable: true
        registration_number nullable: true
        display_name nullable: true
        organization nullable: true
        organizationtype nullable: true
        organizationgroup nullable: true
        organization_detailed_name nullable: true
        generalInstructorWebsite nullable: true
        establishment_email nullable: true
        establishment_email_credentials nullable: true
        account_email nullable: true
        account_email_credentials nullable: true
        official_director_email nullable: true
        official_registrar_email nullable: true
        official_bract_email nullable: true
        trust_name nullable: true
        icard_organization_name nullable: true
        icard_organization_tag nullable: true
        website nullable: true
        address nullable: true
        organization_number nullable: true
        examcoordinator nullable: true
        timetablecoordinator nullable: true
        feedbackcoordinator nullable: true
        gsuit_credentials_file_path nullable: true
        gsuit_credentials_file_name nullable: true
        gsuit_org_unit nullable: true
        instructor_website nullable: true
        learner_website nullable: true
        admission_website nullable: true
        ptrc_number nullable: true
        director_name nullable:true
        account_sign nullable:true
        examination_sign nullable:true
        account_sign_fees_estimate nullable:true
        account_stamp nullable:true
        adhock_vuejs_page nullable:true
        admission_vuejs_page nullable:true
        hostel_vuejs_page nullable:true
        org_logo nullable:true
        org_exam_code nullable:true
        org_university_code nullable:true
        logo_file_path nullable:true
        logo_file_name nullable:true
        challan_bank_name nullable:true
        challan_bank_logo nullable:true
        challan_bank_address nullable:true
        challan_sign_place nullable:true
        payroll_email nullable:true
        payroll_watermark nullable:true
        logincode nullable:true
        studentsection_email nullable:true
        studentsection_email_credentials nullable:true
        org_admission_code nullable:true
        communication_access_key nullable:true
        payment_due_date nullable:true
        examination_email nullable:true
        biometricdbstring nullable:true
        biometricdbusername nullable:true
        biometricdbpassword nullable:true
        mobile_number nullable : true
        fax_number nullable : true
        state nullable : true
        statecode nullable : true
        gst_no nullable : true
        sac_code nullable : true
        official_director_mobileno nullable : true
        official_registrar_mobileno nullable : true
        official_bract_mobileno nullable : true
        trustmobilenoforfeescollectionmsg nullable : true
        principal_sign nullable : true
        dean_sign nullable : true
        admission_email nullable : true
        admission_email_credentials nullable : true
        instructor_android_app_link nullable : true
        instructor_ios_app_link nullable : true
        learner_android_app_link nullable : true
        learner_ios_app_link nullable : true
    }

    static mapping = {
        isreceiptapplicable defaultValue:true
        sort_order defaultValue:0
        minweeklyhoursperweek defaultValue : 40
        ischallanapplicable defaultValue : false
        enableadmissionemailotp defaultValue : true
        iseducationalinstitute defaultValue : true
        isgsuitapplicable defaultValue : false
        isschool defaultValue : false
        gsuit_port_number defaultValue : 0
        autoapproveleavejobon defaultValue : false
        autoapprovecertificatejobon defaultValue : false
        autoinstallmentemailjobon defaultValue : false
        ishardblockondue defaultValue : false
        autoattendancesendonemail defaultValue : false
        autoabsentattendancesendonemail defaultValue : false
        autofreezeandsynchattendance defaultValue : false
        autosendfeesduelisttohod defaultValue : false
        validateemailwithzerobounce defaultValue : false
        autosendregistrationduelisttohod defaultValue : false
    }
}
