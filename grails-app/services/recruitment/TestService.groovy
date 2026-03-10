package recruitment

import grails.gorm.transactions.Transactional

@Transactional
class TestService {

    def getTest(hm,request,data) {
        println("In getTest ")
        hm.remove("instructor")
        def org = hm.remove("org")

        return
    }
}
