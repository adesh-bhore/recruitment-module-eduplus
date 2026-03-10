package recruitment

class RecExperience
{
    int no_of_years
    int no_of_months
    String username
    Date creation_date
    Date updation_date
    String creation_ip_address
    String updation_ip_address
    static belongsTo=[recexperiencetype:RecExperienceType,recapplicant:RecApplicant]
    static constraints = {
    }
}
