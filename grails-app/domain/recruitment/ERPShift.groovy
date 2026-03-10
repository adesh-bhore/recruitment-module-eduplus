package recruitment

class ERPShift {

    String type

    boolean isapplicabletoadmission

    String username
    Date creation_date
    Date updation_date
    String creation_ip_address
    String updation_ip_address

    static belongsTo=[organization:Organization]

    String toString(){
        type
    }

    static mapping = {
        isapplicabletoadmission defaultValue: false
    }
}
