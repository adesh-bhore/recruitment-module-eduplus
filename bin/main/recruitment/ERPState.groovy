package recruitment

class ERPState {

    String state
    String username
    Date creation_date
    Date updation_date
    String creation_ip_address
    String updation_ip_address
    static belongsTo=[erpcountry:ERPCountry]
    static constraints = {
        erpcountry nullable :true
    }
    String toString(){
        state
    }
}
