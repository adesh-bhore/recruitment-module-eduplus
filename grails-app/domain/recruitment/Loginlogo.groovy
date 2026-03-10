package recruitment

class Loginlogo {

    String logo_name
    String logo_path
    String button_color
    String text_color
    String url

    String apklink
    String apkicon
    String apktext

    String username
    Date creation_date
    Date updation_date
    String creation_ip_address
    String updation_ip_address
    static constraints = {
        apklink nullable : true
        apkicon nullable : true
        apktext nullable : true
        url nullable : true
    }
}
