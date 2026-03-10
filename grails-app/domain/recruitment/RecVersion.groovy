package recruitment

class RecVersion
{
    int version_number
    Date version_date
    double feesamountgeneral
    double feesamountcategory
    boolean iscurrent //if true link is live
    boolean iscurrentforbackendprocessing    //if true then organization is active for backendprocessing
    int applicationtrack   //by default, insert 0, then increment by 1
    int receipttrack  //by default, insert 0, then increment by 1

    Date from_date
    Date to_date

    String username
    Date creation_date
    Date updation_date
    String creation_ip_address
    String updation_ip_address
    static belongsTo=[organization:Organization,academicyear:AcademicYear]
    static constraints = {
        from_date nullable: true
        to_date nullable: true
    }
}
