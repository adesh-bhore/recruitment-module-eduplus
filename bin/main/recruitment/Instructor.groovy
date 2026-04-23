package recruitment

import grails.plugins.orm.auditable.Auditable

class Instructor implements Auditable{
    String uid                          //This should be simmilar to login.username
    String official_email_id            //for payroll
    String pfno                         //payroll
    String uan_no                       //payroll
    String lastpfno                     //payroll
    String gratuatynumber               //payroll
    String reasonfornotactive           //payroll
    String bankaccountnumber            //payroll
    String ifsccode                     //payroll
    String micrcode                     //payroll
    Date consolidateddate               //payroll
    Date scaledate                      //payroll
    String employee_code
    String payroll_employee_code
    String employeeabbr
    String static_link // for website profile
    String username
    Date creation_date
    Date updation_date
    String creation_ip_address
    String updation_ip_address
    double rating
    Date dateofjoining
    Date dateofrejoining
    Date dateofreleving
    String external_organization_name
    double current_basic_salary
    boolean iscurrentlyworking
    boolean ismanagementroleapplicable
    boolean is_eligible_for_increment          //payroll
    boolean is_handicap                        //payroll
    boolean is_house_provided                  //payroll
    boolean is_car_provided                    //payroll
    String mobile_no                           //primary mobile no
    String subject_expertise
    String hindex
    String aditionalinfo1
    String payscale                            //GradePay or AGP
    String payband
    Date salaryblockeddate

    double estimatedsalary
    int displayorder
    int website_sort_order
    boolean is_blocked_on_website
    boolean apply_backdated_leave
    boolean iseligiblefordeptbypasslogin
    boolean iseligibleforbypasslogin
    boolean iseligiblefororggroupbypasslogin
    boolean isbypassloginblocked

    boolean isloadadustmentrequired
    boolean is_salary_blocked
    boolean is_salary_sleep_blocked
    boolean markattendanceautomatically

    String coa_number
    String meta_tags

    Date dateofretirement
    String personalfilenumer
    String nomineename
    String releavingordernumber
    Date releavingorederdate
    String remark
    int retirementage
    boolean is_offical_email_available
    Date ordertilldate
    Date probation_date
    boolean isuntilfurtherorder
    boolean isservicebookavailable
    String posthistory
    boolean isdeleted
    String releavingformname
    String releavingusername
    String releaving_updation_ip_address
    Date releaving_updation_date
    boolean isunverisityapproved
    boolean showcauseblock_attendance
    String university_details
    boolean isdgreefromiit
    boolean isdegreefromforeign
    String salary_blocked_formname
    String salary_blocked_username
    String salary_blocked__ip_address
    String additional_post
    String nominee_address
    String nominee_mobile
    String nominee_aadhar_no
    Date nominee_date_of_birth
    String nominee_amount_share
    Date salary_blocked__date
    boolean isedit_fees_recipt
    double citations
    double h_index
    double i10_index
    double current_year_citations
    String termination_order_number
    Date termination_order_date
    String nominee_filename
    String nominee_filepath


    static hasMany = [
            erpfacultypost: ERPFacultyPost
    ]

    static belongsTo=[
            person:Person,
            organization:Organization,
            reportingdepartment:Department,
            department:Department,
            reportingorganization:Organization,
            reporttingauthority:Instructor,
            reporttingdean:Instructor,
            salaryblockedby : Instructor,
            erpservicetype:ERPServiceType,
            employeetype:EmployeeType,
            employeegroup:EmployeeGroup,
            designation:Designation,
            gender:Gender,
            program:Program,
    ]

    static constraints = {
        erpservicetype nullable: true
        program nullable: true
        employeetype nullable: true
        employeegroup nullable: true
        gender nullable: true
        nominee_filename nullable:true
        nominee_filepath nullable:true
        termination_order_number nullable:true
        designation nullable: true
        termination_order_date nullable:true
        salaryblockeddate nullable: true
        additional_post nullable: true
        probation_date nullable: true
        nominee_address nullable: true
        nominee_mobile nullable: true
        nominee_aadhar_no nullable: true
        nominee_date_of_birth nullable: true
        nominee_amount_share nullable: true
        meta_tags nullable: true
        salaryblockedby nullable: true
        uan_no nullable: true
        scaledate nullable: true
        consolidateddate nullable: true
        micrcode nullable: true
        ifsccode nullable: true
        bankaccountnumber nullable: true
        reasonfornotactive nullable: true
        gratuatynumber nullable: true
        lastpfno nullable: true
        pfno nullable: true
        mobile_no nullable: true
        employee_code nullable: true
        rating defaultValue: 0
        organization nullable: true
        reporttingdean nullable: true
        dateofjoining nullable: true
        dateofrejoining nullable: true
        dateofreleving nullable: true
        department nullable: true
        reportingdepartment nullable: true
        reportingorganization nullable: true
        reporttingauthority nullable: true
        uid nullable: true
        external_organization_name nullable: true
        employeeabbr nullable: true
        subject_expertise nullable: true
        official_email_id nullable: true
        hindex nullable: true
        current_basic_salary defaultValue: 0
        payscale nullable: true//payroll
        payroll_employee_code nullable: true//payroll
        aditionalinfo1 nullable: true//payroll

        dateofretirement nullable: true
        personalfilenumer nullable: true
        nomineename nullable: true
        releavingordernumber nullable: true
        releavingorederdate nullable: true
        remark nullable: true
        payband nullable: true
        ordertilldate nullable: true
        posthistory nullable:true
        releavingformname nullable:true
        releavingusername nullable:true
        releaving_updation_ip_address nullable:true
        releaving_updation_date nullable:true
        university_details nullable:true
        salary_blocked_formname nullable:true
        salary_blocked_username nullable:true
        salary_blocked__ip_address nullable:true
        salary_blocked__date nullable :true
        static_link nullable:true
        coa_number nullable :true
    }

    static mapping = {
        i10_index defaultValue: 0
        h_index defaultValue: 0
        citations defaultValue: 0
        current_year_citations defaultValue: 0
        ismanagementroleapplicable defaultValue: false
        is_blocked_on_website defaultValue: false
        estimatedsalary defaultValue: 0
        iscurrentlyworking defaultValue: true
        iseligibleforbypasslogin defaultValue: false
        iseligiblefordeptbypasslogin defaultValue: false
        isbypassloginblocked defaultValue: false
        iseligiblefororggroupbypasslogin defaultValue: false
        is_eligible_for_increment defaultValue: true
        is_handicap defaultValue: false
        is_house_provided defaultValue: false
        is_car_provided defaultValue: false
        isloadadustmentrequired  defaultValue: false
        is_salary_blocked defaultValue: false
        is_salary_sleep_blocked  defaultValue: false
        apply_backdated_leave  defaultValue: false
        isservicebookavailable  defaultValue: false
        retirementage  defaultValue: 60
        is_offical_email_available defaultValue: false
        markattendanceautomatically defaultValue: false
        isuntilfurtherorder defaultValue: false
        isunverisityapproved defaultValue: false
        isdgreefromiit defaultValue: false
        isdegreefromforeign defaultValue: false
        isedit_fees_recipt defaultValue: false
        isdeleted defaultValue: false
        showcauseblock_attendance defaultValue: false
        website_sort_order defaultValue: 0
    }

}
