package recruitment

class AWSBucket {

    String bucketname
    String pipelineId
    String region//ap-south-1,us-east-2...
    String content//video,assignment,material.....
    String distributiondomainname//d1mpcb8a1usmzf.cloudfront.net for signed url
    String aws_path

    String accesskey
    String screatekey

    static constraints = {
        pipelineId nullable: true
        distributiondomainname nullable: true
        accesskey nullable: true
        screatekey nullable: true
    }

}
