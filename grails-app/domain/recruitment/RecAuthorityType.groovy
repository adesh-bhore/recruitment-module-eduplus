package recruitment

class RecAuthorityType
{
    String type   //HOD/Registrar/Management
    boolean islastauthority
    int serial_no
    boolean is_programwise_authority // HOD:true(Department)
    boolean is_streamwise_authority  // Dean:True(Stream)
    String username
    Date creation_date
    Date updation_date
    String creation_ip_address
    String updation_ip_address
    static belongsTo=[organization:Organization,erpfacultypost:ERPFacultyPost]
    static constraints = {
        erpfacultypost nullable:true
    }
    static mapping = {
        islastauthority defaultValue: false
        is_streamwise_authority defaultValue: false
        is_programwise_authority defaultValue: false
        serial_no defaultValue: 0
    }
}
