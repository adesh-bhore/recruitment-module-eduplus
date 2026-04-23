package recruitment

class HierarchyInstructor {

    String creation_username
    String updation_username
    Date creation_date
    Date updation_date
    String creation_ip_address
    String updation_ip_address

    static belongsTo=[organization:Organization, instructor:Instructor,
                      hierarchy: Hierarchy, hierarchytype:HierarchyType]

    static constraints = {
    }
}
