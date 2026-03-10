package recruitment

class RecExpertType {
    String type
    String username
    Date creation_date
    Date updation_date
    String creation_ip_address
    String updation_ip_address
    static belongsTo=[oranization:Organization]
    static constraints = {
    }
    String toString(){type}
}
