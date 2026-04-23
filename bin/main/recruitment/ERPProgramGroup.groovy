package recruitment

class ERPProgramGroup {
    String groupname // MCIP and ECIT
    static belongsTo=[organization:Organization]
    static constraints = {
        groupname unique: ['organization']
    }

}
