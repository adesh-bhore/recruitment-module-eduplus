package recruitment

class MapData {
    String type
    String value
    static belongsTo=[organization:Organization]
    static constraints = {
    }
}
