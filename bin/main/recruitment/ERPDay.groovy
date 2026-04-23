package recruitment

class ERPDay {

    int daysequence // 1 , 2
    String day   // Monday , Tuesday .....
    String displayname // MON , TUE
    boolean isactive
    static constraints = {
        daysequence nullable:true
    }

    static mapping = {
        isactive defaultValue: true
    }
}
