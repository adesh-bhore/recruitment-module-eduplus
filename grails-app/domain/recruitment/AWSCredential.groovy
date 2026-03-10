package recruitment

class AWSCredential {
    String name //for
    String accesskey
    String secretkey
    String region
    String host   //for ses mail
    int port      // for ses mail
    boolean isactive //to disable it
    static constraints = {
        region nullable: true
        host nullable: true
    }
    static mapping = {
        isactive defaultValue: true
        port defaultValue: 587
    }
}
