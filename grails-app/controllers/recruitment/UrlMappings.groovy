package recruitment

class UrlMappings {

    static mappings = {

        "/(?i:home)/$action?"(controller: "home")
        "/(?i:login)/$action?"(controller: "login")
        "/(?i:health)/$action?"(controller: "Health")
        "/(?i:fetchDomain)/$action?"(controller: "FetchDomain")
        "/(?i:recVersion)/$action?"(controller: "RecVersion")
        "/(?i:recApplicationLogin)/$action?"(controller: "RecApplicationLogin")
        
        // Test API endpoints
        "/(?i:hello)/$action?"(controller: "hello")

        "/"(view: "/index")
        "500"(view: '/error')
        "404"(view: '/notFound')
    }
}
