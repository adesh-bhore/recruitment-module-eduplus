package recruitment
class ProgramTypeYear
{
                                            //viit
    int code
    int sequence                            //1:FY,2:SY,3:TY,4:B.Tech
    boolean islast                          //for MCA : TY : Last Year
    boolean isapplicabletoadmission
    boolean isactive
    String examseatnoprefix
    double ncrf_increment

    String username
    Date creation_date
    Date updation_date
    String creation_ip_address
    String updation_ip_address
    static belongsTo=[
            programtype:ProgramType,
            year:Year,
            organization:Organization
    ]
    static constraints = {
        examseatnoprefix nullable:true
    }
    static mapping = {
        islast defaultValue: false
        ncrf_increment defaultValue: 0
        isactive defaultValue: true
        isapplicabletoadmission defaultValue: false
    }
}