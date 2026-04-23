package recruitment

class RecPostDocument {

    boolean isCompulsory
    String creation_username
    String updation_username
    Date creation_date
    Date updation_date
    String creation_ip_address
    String updation_ip_address

    static belongsTo=[recDocumentType:RecDocumentType,recPost:RecPost]
    static mapping={
        isCompulsory defaultValue:false
    }
}
