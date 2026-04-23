package recruitment

class RoleType {
    String icon_path
    String type  //modules
    String type_displayname  //modules
    boolean isroletypeset
    boolean isactive
    int sort_order
    boolean isapplicabletolearnercurrentyear
    boolean isapplicableforpushingtonextyear



    String username
    Date creation_date
    Date updation_date
    String creation_ip_address
    String updation_ip_address

    boolean generalIsactive
    String generalIcon
    static belongsTo=[applicationtype:ApplicationType, organization:Organization, instructor:Instructor]
    static constraints = {
        icon_path nullable:true
        type nullable:false
        isapplicabletolearnercurrentyear nullable:false
        type_displayname nullable:true
        isactive defaultValue: true
        generalIsactive defaultValue: false
        generalIcon nullable:true
        instructor nullable:true
        type unique: ['organization','applicationtype']
    }

    static mapping = {
        isapplicableforpushingtonextyear defaultValue: false

    }
}
