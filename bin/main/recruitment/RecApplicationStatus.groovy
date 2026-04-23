package recruitment

class RecApplicationStatus {

    String remark
    boolean iscalledforinterview  //True if called otherwise false
    Date approve_date
    boolean ismailsent   //True if mail sent otherwise false
    Date mailsentdate
    String username
    Date creation_date
    Date updation_date
    String creation_ip_address
    String updation_ip_address
    static belongsTo=[organization:Organization,recapplication:RecApplication,
                      recauthoritytype:RecAuthorityType,approvedby:Instructor,recbranch:RecBranch,recversion:RecVersion,
                      recapplicationstatusmaster:RecApplicationStatusMaster]
    static constraints = {
        approve_date nullable:true
        mailsentdate nullable:true
        approvedby nullable:true
        remark nullable:true
        recbranch nullable:true
        recversion nullable:true
    }
    static mapping = {
        iscalledforinterview defaultValue: false
        ismailsent defaultValue: false
    }
}