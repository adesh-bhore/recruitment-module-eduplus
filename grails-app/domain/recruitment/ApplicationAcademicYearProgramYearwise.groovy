package recruitment

class ApplicationAcademicYearProgramYearwise {

    Date startdate
    Date enddate

    String creation_username
    String updation_username
    Date creation_date
    Date updation_date
    String creation_ip_address
    String updation_ip_address

    static belongsTo=[
            applicationacademicyear:ApplicationAcademicYear,
            semester               :Semester,
            organization           :Organization, program:Program, year:Year
    ]

    static constraints = {
        startdate nullable:true
        enddate nullable:true
    }
}
