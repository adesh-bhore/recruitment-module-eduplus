package recruitment

class ERPCity {

    String city
    String username
    Date creation_date
    Date updation_date
    String creation_ip_address
    String updation_ip_address
    static belongsTo=[erpdistrict:ERPDistrict, erpstate:ERPState, erpcountry:ERPCountry]
    static constraints = {
        erpdistrict nullable :true
        erpstate nullable :true
        erpcountry nullable :true
    }
    String toString(){
        city
    }
}
