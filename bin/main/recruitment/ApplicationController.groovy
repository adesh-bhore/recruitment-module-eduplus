package recruitment

import grails.core.GrailsApplication
import grails.plugins.*

class ApplicationController implements PluginManagerAware {

    GrailsApplication grailsApplication
    GrailsPluginManager pluginManager

    def index() {
        render(contentType: "application/json") {
            [
                application: grailsApplication.metadata['info.app.name'],
                version: grailsApplication.metadata['info.app.version'],
                grailsVersion: grailsApplication.metadata['info.app.grailsVersion'],
                status: "running",
                message: "Recruitment API is running. Use /health for health check."
            ]
        }
    }
}
