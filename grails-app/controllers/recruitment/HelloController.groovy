package recruitment

class HelloController {
    
    HelloService helloService
    
    static responseFormats = ['json']
    
    // Test endpoint 1: Simple hello
    def index() {
        respond([message: "Hello from Recruitment API", status: "success"])
    }
    
    // Test endpoint 2: With service
    def greet() {
        def greeting = helloService.getGreeting(params.name ?: "Guest")
        respond([greeting: greeting, timestamp: new Date()])
    }
    
    // Test endpoint 3: With path variable
    def show(Long id) {
        respond([
            id: id,
            message: "You requested ID: ${id}",
            status: "success"
        ])
    }
    
    // Test endpoint 4: POST request
    def save() {
        def data = request.JSON
        respond([
            received: data,
            message: "Data received successfully",
            status: "success"
        ])
    }
    
    // Database Query 1: Get all academic years
    def listAcademicYears() {
        def academicYears = AcademicYear.list()
        respond([
            total: academicYears.size(),
            data: academicYears,
            status: "success"
        ])
    }
    
    // Database Query 2: Get active academic years only
    def activeAcademicYears() {
        def activeYears = AcademicYear.findAllByIsactive(true)
        respond([
            total: activeYears.size(),
            data: activeYears,
            status: "success"
        ])
    }
    
    // Database Query 3: Get academic year by ID
    def getAcademicYear() {
        def id = params.long('id')
        def academicYear = AcademicYear.get(id)
        
        if (academicYear) {
            respond([
                data: academicYear,
                status: "success"
            ])
        } else {
            respond([
                message: "Academic Year not found with ID: ${id}",
                status: "error"
            ], status: 404)
        }
    }
    
    // Database Query 4: Search academic year by name
    def searchAcademicYear() {
        def searchTerm = params.ay
        
        if (!searchTerm) {
            respond([
                message: "Please provide 'ay' parameter to search",
                status: "error"
            ], status: 400)
            return
        }
        
        def results = AcademicYear.findAllByAyLike("%${searchTerm}%")
        respond([
            total: results.size(),
            searchTerm: searchTerm,
            data: results,
            status: "success"
        ])
    }
    
    // Database Query 5: Get academic years sorted by sort_order
    def sortedAcademicYears() {
        def sortedYears = AcademicYear.list(sort: 'sort_order', order: 'asc')
        respond([
            total: sortedYears.size(),
            data: sortedYears,
            status: "success"
        ])
    }
    
    // Database Query 6: Count total academic years
    def countAcademicYears() {
        def total = AcademicYear.count()
        def activeCount = AcademicYear.countByIsactive(true)
        def inactiveCount = AcademicYear.countByIsactive(false)
        
        respond([
            total: total,
            active: activeCount,
            inactive: inactiveCount,
            status: "success"
        ])
    }
    
    // Database Query 7: Create new academic year (POST)
    def createAcademicYear() {
        def data = request.JSON
        
        def academicYear = new AcademicYear()
        academicYear.ay = data.ay
        academicYear.shortcut = data.shortcut
        academicYear.username = data.username ?: "system"
        academicYear.isactive = data.isactive != null ? data.isactive : true
        academicYear.isapplicabletoadmission = data.isapplicabletoadmission != null ? data.isapplicabletoadmission : false
        academicYear.sort_order = data.sort_order ?: 0
        academicYear.creation_date = new Date()
        academicYear.creation_ip_address = request.remoteAddr
        
        if (academicYear.save(flush: true)) {
            respond([
                message: "Academic Year created successfully",
                data: academicYear,
                status: "success"
            ])
        } else {
            respond([
                message: "Failed to create Academic Year",
                errors: academicYear.errors.allErrors.collect { it.defaultMessage },
                status: "error"
            ], status: 400)
        }
    }
}
