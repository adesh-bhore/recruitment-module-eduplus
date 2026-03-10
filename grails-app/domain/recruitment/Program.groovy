package recruitment

class Program {

    String name
    String abbrivation
    String specialization
    boolean isdeleted
    boolean isfeesprogramwise
    boolean is_subject_preference_applicable
    String examdisplayname
    String icard_color_code
    boolean isapplicabletoadmission
    boolean isapplicabletospotadmission
    int sort_order
    int extracredit
    int year_of_starting
    String displayname                                                //used in passing certificate(VIT)
    String admissiondisplayname                                                //used in passing certificate(VIT)
    String icarddisplayname                                           //used for icard strip displayname(ZEAL)
    String grno_code
    String duration_display_name
    String academiccourseno                                           // used to digital Result
    boolean ismerit_form_open_for_editing

    double provisionaladmissionfirstinstallment
    String description

    String username
    Date creation_date
    Date updation_date
    String creation_ip_address
    String updation_ip_address
    String programcode

    static belongsTo=[
            department:Department,
            organization:Organization,
            programtype:ProgramType,
            erpprogramgroup:ERPProgramGroup,
            erplanguage:ERPLanguage,
            school:School
    ]                                                                  //Desh/Computer

    String toString()
    {
        name
    }
    static mapping = {
        year_of_starting defaultValue: 0
        isapplicabletoadmission defaultValue: true
        is_subject_preference_applicable  defaultValue: false
        ismerit_form_open_for_editing  defaultValue: false
        isapplicabletospotadmission defaultValue: true
        sort_order defaultValue: 0
        extracredit defaultValue: 0
        provisionaladmissionfirstinstallment defaultValue: 0
    }

    static constraints = {
        academiccourseno nullable: true
        school nullable: true
        icard_color_code nullable: true
        displayname nullable: true
        admissiondisplayname nullable: true
        erpprogramgroup nullable: true
        organization nullable: true
        abbrivation nullable:true
        erplanguage nullable:true
        examdisplayname nullable:true
        specialization nullable:true
        icarddisplayname nullable:true
        grno_code nullable:true
        duration_display_name nullable:true
        description nullable:true
        programcode nullable:true
    }
}
