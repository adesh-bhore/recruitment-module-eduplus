package recruitment

class OrganizationGroup {

    String name
    String communication_api_url                       //For Telegram Sever url
    String telegram_access_key                         //For Telegram Access Key
    String organization_group_code                     //use for Group specific setting
    String group_featured_logo                         //use for group specific logo(25 yrs,50 yrs...)

    String hostel_account_sign
    String hostel_account_sign_fees_estimate
    String hostel_account_stamp
    String hostel_challan_sign_place

    String username
    Date creation_date
    Date updation_date
    String creation_ip_address
    String updation_ip_address

    static constraints = {
        organization_group_code nullable: true
        group_featured_logo nullable: true
        hostel_account_sign nullable:true
        hostel_account_sign_fees_estimate nullable:true
        hostel_account_stamp nullable:true
        hostel_challan_sign_place nullable:true
        communication_api_url nullable:true
    }
}
