package recruitment

class Address {

    String address
    String taluka
    String street
    String pin
    String file_name
    String file_path

    String username
    Date creation_date
    Date updation_date
    String creation_ip_address
    String updation_ip_address

    static belongsTo=[
            country:ERPCountry,
            state:ERPState,
            person:Person,
            district:ERPDistrict,
            city:ERPCity,
            addresstype:AddressType,
            organization:Organization,
            recapplicant:RecApplicant
    ]

    static constraints =
            {
                file_name nullable:true
                file_path nullable:true
                pin nullable:true
                street nullable:true
                taluka nullable:true
                organization nullable:true
                address nullable:true
                person nullable:true
                addresstype nullable:true
                country nullable:true
                state nullable:true
                district nullable:true
                recapplicant nullable:true
                city nullable:true
            }
}
