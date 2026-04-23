package recruitment

import grails.converters.*

class TestController {
	static responseFormats = ['json', 'xml']

    def handleException(Exception e) {
        HashMap hashMap=new HashMap()
        hashMap.put("error_trace",e)
        hashMap.put("error_msg",e.message)
        render hashMap as JSON
        return
    }
    def processRequest(serviceMethod) {
        println("Processing request for : ${serviceMethod}")
        HashMap hm = new HashMap()
        hm.putAll(authData)
        hm.put("msg","Failed!!!")
        hm.put("flag",false)
        TestService ms = new TestService()
        ms."${serviceMethod}"(hm,request,dataresponse) // Dynamically invoke the desired service method
        render hm as JSON
    }
    def processRequestWithoutParams(String methodName) {
        println "In $methodName"

        def responseMap = new LinkedHashMap(authData)
        TestService obj =  new TestService()
        obj."$methodName"(request, responseMap)

        render responseMap as JSON
    }
    ///////////////////////////////////////////////////////////////////////
    def getTest() {
        processRequest("getTest")
    }
    def index() { }
}
