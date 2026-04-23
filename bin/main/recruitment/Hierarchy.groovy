package recruitment

class Hierarchy {

    String name  ////HOD/Registrar/Accounts
    int level
    boolean custom_flag // example for admission cancel treat registrar as student section to disable div and sub for student
    boolean islast
    boolean isactive
    String creation_username
    String updation_username
    Date creation_date
    Date updation_date
    String creation_ip_address
    String updation_ip_address

    static belongsTo=[organization:Organization,hierarchytype:HierarchyType]
    static mapping = {
        isactive defaultValue:true
        islast defaultValue:false
        custom_flag defaultValue:false
    }
    static constraints = {
    }
}
