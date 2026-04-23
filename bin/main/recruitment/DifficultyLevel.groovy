package recruitment

class DifficultyLevel {
    
    String name                     // Easy, Moderate, Hard
    String description
    
    static constraints = {
        name nullable: false, unique: true, maxSize: 50
        description nullable: true, maxSize: 255
    }
    
    static mapping = {
        table 'difficulty_level'
        version false
        id generator: 'identity'
    }
}
