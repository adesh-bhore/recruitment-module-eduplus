package recruitment

class OrganizationDetails {

    String defaultintime
    String defaultouttime
    String feesduemailcc
    String registrationduemailcc
    String examination_stamp
    String examination_sign
    String examination_head
    String udise_number
    String college_index_no
    String spot_admission_email                         //will be used for sending mail
    String spot_admission_email_credentials
    String admin_instructor_website

    static belongsTo=[organization:Organization]

    static constraints = {
        organization unique: true
        defaultintime nullable: true
        spot_admission_email nullable: true
        spot_admission_email_credentials nullable: true
        defaultouttime nullable: true
        feesduemailcc nullable: true
        registrationduemailcc nullable: true
        examination_stamp nullable: true
        examination_sign nullable: true
        examination_head nullable: true
        college_index_no nullable: true
        udise_number nullable: true
        admin_instructor_website nullable: true
    }
}
