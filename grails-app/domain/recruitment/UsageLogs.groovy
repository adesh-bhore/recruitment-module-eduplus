package recruitment

class UsageLogs {
    String pagename
    String controller
    String action
    String description
    String ipaddress
    Date accesstime
    String devicename
    String usagetype
    String username

    static belongsTo=[person: Person, useraccessed: Person, organization: Organization]

    static constraints = {
        person nullable: true
        useraccessed nullable: true
        usagetype nullable: true
        username nullable: true
    }
}
