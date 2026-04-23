package recruitment

import grails.gorm.transactions.Transactional

@Transactional
class HelloService {

    def getGreeting(String name) {
        return "Hello, ${name}! Welcome to Recruitment API."
    }
    
    def getCurrentTime() {
        return new Date()
    }
    
    def processData(Map data) {
        // Simulate some business logic
        return [
            processed: true,
            data: data,
            processedAt: new Date()
        ]
    }
}
