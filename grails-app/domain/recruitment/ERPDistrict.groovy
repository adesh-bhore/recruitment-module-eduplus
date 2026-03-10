package recruitment

class ERPDistrict {

    String district
    String username
    Date creation_date
    Date updation_date
    String creation_ip_address
    String updation_ip_address
    static belongsTo=[erpstate:ERPState, erpcountry:ERPCountry]
    static constraints = {
        erpstate nullable :true
        erpcountry nullable :true
    }
    String toString(){
        district
    }
}
