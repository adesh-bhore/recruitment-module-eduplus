package recruitment

class RecApplicationStatusMaster {

    String status //(inprocess, selected, rejected)
    String display_name
    String creation_username
    String updation_username
    Date creation_date
    Date updation_date
    String creation_ip_address
    String updation_ip_address

    static belongsTo=[organization:Organization]

    static constraints = {
    }
}
