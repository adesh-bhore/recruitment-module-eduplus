package recruitment

class Stream {
    String official_stream_email
    String name
    String username
    Date creation_date
    Date updation_date
    String creation_ip_address
    String updation_ip_address
    static belongsTo=[dean:Instructor,organization:Organization]
    static constraints = {
        name unique: ['organization']
    }

}
