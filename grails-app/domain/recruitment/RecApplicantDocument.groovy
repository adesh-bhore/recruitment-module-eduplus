package recruitment

class RecApplicantDocument
{
    String filepath
    String filename
    String username
    Date creation_date
    Date updation_date
    String creation_ip_address
    String updation_ip_address
    static belongsTo=[recapplicant:RecApplicant,recdocumenttype:RecDocumentType]
    static constraints = {
    }
}
