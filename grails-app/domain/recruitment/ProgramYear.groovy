package recruitment

class ProgramYear {

    boolean isactive
    String examdisplayname
    String icarddisplayname
    boolean issubjectregistrationon

    boolean isregistrationonallowedwithoutfees
    boolean isapplicabletoadmission
    boolean isapplicableforcoursetrack

    boolean is_daily_load_monitoring_notification_is_on
    boolean isregistrationotaowedwithbacklog

    static belongsTo=[program:Program,year:Year,organization:Organization]

    static constraints = {
        organization nullable:true
        examdisplayname nullable:true
        icarddisplayname nullable:true
        isregistrationonallowedwithoutfees nullable: false
        isregistrationotaowedwithbacklog nullable: false
    }


    static mapping = {
        is_daily_load_monitoring_notification_is_on defaultValue: false
        isactive defaultValue: true
        issubjectregistrationon defaultValue: false
        isapplicabletoadmission defaultValue: false
        isapplicableforcoursetrack defaultValue: false
    }
}
