package recruitment

class Remark {

    String name   //Document
    Boolean isactive
    String creation_username
    String updation_username
    Date creation_date
    Date updation_date
    String creation_ip_address
    String updation_ip_address
    static belongsTo=[organization:Organization,
    remarktype:RemarkType]

    static constraints = {

    }
}
