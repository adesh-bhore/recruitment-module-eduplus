package recruitment

class ERPScholarshipType {

    String type
    boolean isapplicabletoadmission
    String username
    Date creation_date
    Date updation_date
    String creation_ip_address
    String updation_ip_address
    static belongsTo=[organization:Organization]
    static constraints = {
    }

    static mapping = {
        isapplicabletoadmission defaultValue: false
    }

    String toString(){
        type
    }

}
