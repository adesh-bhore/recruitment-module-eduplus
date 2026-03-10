package recruitment

class RecApplication
{
    String applicaitionid    //Ex.120190517XXXXX       Organization Number:1 + Date:yyyymmdd + 5 digit application tracking number
    String feesreceiptid  //Ex. R120190517XXXXX       Organization Number:1 + Date:yyyymmdd + 5 digit application tracking number
    Date applicationdate
    String place   //Ex. Pune
    boolean isfeespaid
    boolean isrejected //true : rejected
    double amount
    Date receiptdate
    String username
    Date creation_date
    Date updation_date
    String creation_ip_address
    String updation_ip_address
    static belongsTo=[recapplicationrejectionreason:RecApplicationRejectionReason,
                      organization:Organization,
                      recapplicant:RecApplicant,
                      recversion:RecVersion,
                      reconlinetransaction:RecOnlineTransaction]
    static hasMany = [recpost: RecPost,recbranch:RecBranch]
    static constraints = {
        reconlinetransaction nullable:true
        recapplicationrejectionreason nullable:true
        feesreceiptid nullable:true
        receiptdate nullable:true
    }
    static mapping = {
        isfeespaid defaultValue: false
        isrejected defaultValue: false
    }
}
