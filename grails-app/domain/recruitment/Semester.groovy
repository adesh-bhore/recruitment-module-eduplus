package recruitment

class Semester {

	String sem
    String display_name         // used in attempt certificate form for VIT
    String exam_display_name         // used in attempt certificate form for VIT
    int sequence
    boolean is_provision_sem
    boolean donotcarryforwardmarksinreval  //true:if regualar exam marks used as it is at reval exam mark used as it is -- false:Need To enter Marks at every Exam
    boolean donotcarryforwardmarksinreexam   //true:if regualar exam marks used as it is at reexam mark used as it is -- false:Need To enter Marks at every Exam
    boolean donotcarryforwardmarksinbacklog  //true:if regualar exam marks used as it is at backlog exam mark used as it is -- false:Need To enter Marks at every Exam
    boolean donotcarryforwardmarksinreregistration  //true:if regualar exam marks used as it is at Rereg exam mark used as it is -- false:Need To enter Marks at every Exam

    boolean enabletemplatebasedbacklogregistration

    String username
    Date creation_date
    Date updation_date
    String creation_ip_address
    String updation_ip_address

    static belongsTo=[organization:Organization]

    static constraints = {
        display_name nullable: true
        exam_display_name nullable: true
        sem unique: ['organization']
    }

    static mapping = {
        sequence defaultValue: 0
        is_provision_sem defaultValue: false
        donotcarryforwardmarksinreval defaultValue: false
        donotcarryforwardmarksinreexam defaultValue: false
        donotcarryforwardmarksinbacklog defaultValue: false
        donotcarryforwardmarksinreregistration defaultValue: false
        enabletemplatebasedbacklogregistration defaultValue: false
    }

    String toString(){
        sem
    }
}
