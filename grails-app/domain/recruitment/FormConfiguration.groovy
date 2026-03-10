package recruitment

class FormConfiguration {

    String field_name                                   //This name is exactly same as form field name
    String display_name
    boolean isActive
    boolean isEditable                                  //false:readonly, true:editable
    boolean isRequired                                  //true:required, false:optional

    String creation_username
    String updation_username
    Date creation_date
    Date updation_date
    String creation_ip_address
    String updation_ip_address

    static belongsTo=[organization:Organization,formname: FormName]

    static constraints = {}

}
