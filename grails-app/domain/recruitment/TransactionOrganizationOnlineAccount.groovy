package recruitment

class TransactionOrganizationOnlineAccount
{
    String merchant_code
    String merchant_key
    String merchant_iv
    String host // paytm
    boolean isactive
    String accounttype
    String online_payment_account_no
    String statusapihost // ccavenue

    String username
    Date creation_date
    Date updation_date
    String creation_ip_address
    String updation_ip_address
    long orderid //ccavenue requires tid as well as orderid as unique use this as TransactionOrganizationOnlineAccount+orderid
    static belongsTo=[
            organization:Organization,
            erppaymentgatewaymaster:ERPPaymentGatewayMaster,
            programtype:ProgramType,
            organizationgroup:OrganizationGroup,
            program:Program,
    ]

    static constraints = {
        host nullable : true
        programtype nullable : true
        program nullable : true //for SP college Programwise Payment
        accounttype nullable : true //for SP college Grant-NONgrant Payment gateway
        online_payment_account_no nullable : true //for SP college Grant-NONgrant Payment gateway
        organizationgroup nullable : true //for SP college Grant-NONgrant Payment gateway
        statusapihost nullable : true //for SP college Grant-NONgrant Payment gateway
    }

    static mapping = {
        isactive defaultValue: false
        orderid defaultValue:1000
    }
}