package recruitment

class RecCourse
{
    String course_code
    String course_name
    boolean isDeleted
    int minimum_number_of_questions_in_bank
    int number_of_questions_to_be_picked_for_exam
    int totalmarksinexam
    String username
    Date creation_date
    Date updation_date
    String creation_ip_address
    String updation_ip_address
    static belongsTo=[organization:Organization,erpmcqexamname:ERPMCQExamName,
                      instructor:Instructor,recdeptgroup:RecDeptGroup]
    static constraints = {
        instructor nullable: true
        recdeptgroup nullable: true
    }
}
