package recruitment

class AcademicYear
 {
	String ay
    int sort_order
    String shortcut     //2018-19 -> 18
    String username
    boolean isactive
    boolean isapplicabletoadmission
    Date financial_year_start_date
    Date financial_year_end_date
    Date creation_date
    Date updation_date
    String creation_ip_address
    String updation_ip_address
    static belongsTo=[previousyear:AcademicYear,nextyear:AcademicYear]

    static constraints = {
       nextyear nullable:true
       previousyear nullable:true
       financial_year_start_date nullable : true
       financial_year_end_date nullable :true
    }
    static mapping = {
       sort_order defaultValue: 0
       isactive defaultValue: true
       isapplicabletoadmission defaultValue: false
    }
}
