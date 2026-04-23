package recruitment

import grails.plugins.orm.auditable.Auditable

class RoleLink implements Auditable
{
    String vue_js_name
    String controller_name
    String action_name
    String link_name
    String link_displayname
    String link_description
    int sort_order
    boolean isrolelinkactive
    boolean isquicklink
    boolean isblockedforbypass
    String linkiconimagefilename
    String linkiconimagepath
    String youtubelink
    String pdflink
    String creation_username
    String updation_username
    Date creation_date
    Date updation_date
    String creation_ip_address
    String updation_ip_address

    String formname
    static belongsTo=[role:Role,organization:Organization, instituterolegroup:RoleGroup,linkcategory:LinkCategory,
                      departmentrolegroup:RoleGroup, individualrolegroup:RoleGroup]
    static constraints = {
        organization nullable: true
        linkiconimagefilename nullable: true
        linkiconimagepath nullable: true
        link_description nullable: true
        link_displayname nullable: true
        vue_js_name nullable: true
        controller_name nullable: true
        action_name nullable: true
        youtubelink nullable: true
        instituterolegroup nullable: true
        departmentrolegroup nullable: true
        individualrolegroup nullable: true
        pdflink nullable: true
        creation_username nullable: true
        updation_username nullable: true
        creation_date nullable: true
        updation_date nullable: true
        creation_ip_address nullable: true
        updation_ip_address nullable: true
        formname nullable: true
        linkcategory nullable: true

    }
    static mapping = {
        isblockedforbypass defaultValue: false
        isquicklink defaultValue: false

    }

}
