package recruitment

class ERPMCQExamName {

    String name   //Ex.Recruitment Portal
    int duration_in_minutes
    double max_score
    static belongsTo=[organization:Organization]
    static constraints = {
    }
}
