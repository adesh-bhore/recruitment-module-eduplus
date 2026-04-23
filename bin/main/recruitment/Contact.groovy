package recruitment

class Contact {
    String email                                        //primary email
	String mobile_no                                    //primary mobile no
    String ulternate_mobile_no                          //secondary mobile number
	String alternate_email                              //secondary email
	String telephone_no                                 //phone number
    String fax
    String website_url
    String emergency_contact_name
    String emergency_contact_address
    String emergency_contact_primary_mobile
    String emergency_contact_secondary_mobile
    String emergency_contact_phone
    int emergency_contact_age
    String emergency_contact_remark
    String local_guardian_name
    String local_guardian_address
    String local_guardian_primary_mobile
    String local_guardian_secondary_mobile
    String local_guardian_phone
    String hostel_name
    String hostel_address
    int local_guardian_age
    String local_guardian_remark
    String whatsappnumber
    String username    
    Date creation_date
    Date updation_date
    String creation_ip_address
    String updation_ip_address
    String family_full_name
    String family_address
    String family_mobile
    String family_adhar
    String filename
    String filepath
    Date family_date_of_birth
    boolean ismobileverfied

    static belongsTo=[
            person:Person,
            organization:Organization,
    ]

    static constraints = {
    	mobile_no size : 10 .. 15
        organization nullable:true
        filename nullable:true
        filepath nullable:true
        person nullable:true
        emergency_contact_name nullable:true
        family_full_name nullable:true
        family_address nullable:true
        family_mobile nullable:true
        family_adhar nullable:true
        family_date_of_birth nullable:true
        emergency_contact_address nullable:true
        emergency_contact_primary_mobile nullable:true
        emergency_contact_secondary_mobile nullable:true
        emergency_contact_phone nullable:true
        emergency_contact_remark nullable:true
        local_guardian_name nullable:true
        local_guardian_address nullable:true
        local_guardian_primary_mobile nullable:true
        local_guardian_secondary_mobile nullable:true
        local_guardian_phone nullable:true
        local_guardian_remark nullable:true
        whatsappnumber nullable:true
        email nullable:true
        mobile_no nullable:true
        ulternate_mobile_no nullable:true
        alternate_email nullable:true
        telephone_no nullable:true
        fax nullable:true
        website_url nullable:true
        hostel_name nullable:true
        hostel_address nullable:true
    }

    static mapping = {
        emergency_contact_age defaultValue: 0
        ismobileverfied defaultValue:false
    }
}
