package recruitment

class RecExperienceType
{
    String type //Teaching, Industrial/Research
    boolean isactive
    String username
    Date creation_date
    Date updation_date
    String creation_ip_address
    String updation_ip_address
    static constraints = {
        recexperiencecategory nullable : true
    }
    static mapping = {
        isactive defaultValue: true
    }
    static belongsTo=[organization:Organization, recexperiencecategory : RecExperienceCategory]
}
