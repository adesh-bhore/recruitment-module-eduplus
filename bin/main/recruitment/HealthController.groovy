package recruitment


import grails.converters.*
import org.springframework.jdbc.datasource.DriverManagerDataSource
import org.hibernate.SessionFactory

class HealthController {
    SessionFactory sessionFactory

    static responseFormats = ['json', 'xml']
	
    def index() {
        println("hi...")
        def hm = [:]
        hm.put("status","200")
        render hm as JSON
    }
    def switchdb() {
        return
        println("grailsApplication")
        def dataresponse = request.JSON
        String currentDbUrl = dataresponse.url//'jdbc:mysql://localhost:3306/pccoedb?useSSL=false'
        String username = dataresponse.uid//"root"
        String password = dataresponse.pwd//""
        DriverManagerDataSource dataSource = new DriverManagerDataSource()
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver")
        dataSource.setUrl(currentDbUrl)
        dataSource.setUsername(username)
        dataSource.setPassword(password)

        sessionFactory.currentSession.connection().setAutoCommit(false) // Manage transactions if needed
        sessionFactory.currentSession.connection().setCatalog(dataSource.getConnection().getCatalog())
        println("occupation:"+ERPOccupation.list()?.occupation)
        render dataSource
    }

}
