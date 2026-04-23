package recruitment

class RecOnlineTransaction
{
    String erp_transaction_id   //gettime
    String transaction_message
    String transaction_error_message
    String request_details //First_amount_commission
    double amount  //amount sent from erp
    double received_amount  //amount from techprocess
    String bank_name
    String payment_remark
    Date request_transaction_date
    String response_transaction_date
    String paymentgateway_transaction_id
    String transaction_response_entire_url
    String bank_transaction_id
    String card_id
    String customer_id
    String customer_name
    String mobile_number
    String account_number
    String username
    Date creation_date
    Date updation_date
    String creation_ip_address
    String updation_ip_address
    static belongsTo=[recversion:RecVersion,
                      organization:Organization,
                      recapplicant:RecApplicant,
                      recapplication:RecApplication,
                      academicyear:AcademicYear,
                      transactionrequesttype:TransactionRequestType,
                      transactioncurrencycode:TransactionCurrencyCode,
                      transactionorganizationonlineaccount:TransactionOrganizationOnlineAccount,
                      transactionstatus:TransactionStatus]
    static constraints = {
        bank_transaction_id nullable:true
        card_id nullable:true
        customer_id nullable:true
        customer_name nullable:true
        mobile_number nullable:true
        account_number nullable:true
        transaction_error_message nullable:true
        response_transaction_date nullable:true
        paymentgateway_transaction_id nullable:true
        transaction_response_entire_url nullable:true
        bank_name nullable:true
        payment_remark nullable:true
    }
}
