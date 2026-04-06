package recruitment

class UrlMappings {

    static mappings = {

        "/(?i:home)/$action?"(controller: "home")
        "/(?i:login)/$action?"(controller: "login")
        "/(?i:health)/$action?"(controller: "Health")
        "/(?i:fetchDomain)/$action?"(controller: "FetchDomain")
        "/(?i:recVersion)/$action?"(controller: "RecVersion")
        "/(?i:recApplicationLogin)/$action?"(controller: "RecApplicationLogin")
        "/(?i:recInterviewSchedule)/$action?"(controller: "RecInterviewSchedule")
        "/(?i:recApplication)/$action?"(controller: "RecApplication")
        "/(?i:recExam)/$action?"(controller: "RecExam")
        "/(?i:recOnlineMcq)/$action?"(controller: "RecOnlineMcq")
        
        // Test API endpoints
        "/(?i:hello)/$action?"(controller: "hello")

        "/"(controller: "application", action: "index")
        "500"(view: '/error')
        "404"(view: '/notFound')
    }
}
