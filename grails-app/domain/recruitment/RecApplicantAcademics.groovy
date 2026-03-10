package recruitment

class RecApplicantAcademics
{
    String name_of_degree
    String yearofpassing
    String university  //university or Board Name
    String branch
    double cpi_marks   // CPI/Marks
    String username
    Date creation_date
    Date updation_date
    String creation_ip_address
    String updation_ip_address
    static belongsTo=[recapplicant:RecApplicant,recdegree:RecDegree,recclass:RecClass,recdegreestatus:RecDegreeStatus,recdegreename:RecDegreeName]
    static constraints = {
        recclass nullable:true
        recdegree nullable:true
        name_of_degree nullable:true
        recdegreename nullable:true
        recdegreestatus nullable:true
        cpi_marks nullable:true
    }
}
