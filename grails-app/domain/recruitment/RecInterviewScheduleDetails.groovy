package recruitment

class RecInterviewScheduleDetails
{
    Date interview_date
    String interview_venue
    String interview_time
    String username
    Date creation_date
    Date updation_date
    String creation_ip_address
    String updation_ip_address
    static belongsTo=[organization:Organization,department:Department,recpost:RecPost, recversion:RecVersion]
    static constraints = {
    }
}
