package common

import recruitment.AWSBucket
import com.amazonaws.AmazonClientException
import com.amazonaws.AmazonServiceException
import com.amazonaws.ClientConfiguration
import com.amazonaws.HttpMethod
import com.amazonaws.SdkClientException
import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.Bucket
import com.amazonaws.services.s3.model.CannedAccessControlList
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest
import com.amazonaws.services.s3.model.GetObjectRequest
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.model.PutObjectRequest
import com.amazonaws.services.s3.model.RestoreObjectRequest
import com.amazonaws.services.s3.model.S3Object
import com.amazonaws.services.s3.transfer.MultipleFileDownload
import com.amazonaws.services.s3.transfer.TransferManager
import com.amazonaws.services.s3.transfer.TransferManagerBuilder
import grails.gorm.transactions.Transactional

@Transactional
class AWSBucketService {

    def serviceMethod() {}

    //AWS s3 credentials
    AmazonS3 getCredential(String clientRegion)
    {
        AmazonS3 s3
        AWSBucket awsBucket = AWSBucket.findByContent("documents")
        String access_key = awsBucket.accesskey
        String secret_key = awsBucket.screatekey

        try {
            ClientConfiguration clientConf = new ClientConfiguration();
            clientConf.setConnectionTimeout(60 * 10000);
            //credentials = new ProfileCredentialsProvider().getCredentials()
            BasicAWSCredentials creds = new BasicAWSCredentials(access_key, secret_key);
            s3 = AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(creds)).withRegion("ap-south-1").withClientConfiguration(clientConf).build();
        }
        catch (Exception e) {
            throw new AmazonClientException(
                    "Cannot load the credentials from the credential profiles file. " +
                            "Please make sure that your credentials file is at the correct " +
                            "location (~/.aws/credentials), and is in valid format.",
                    e)
        }
        return s3;
    }

    //creating new AWS bucket
    def createBucket(String bucket,String region)
    {
        final BUCKET_NAME = bucket
        AmazonS3 s3=getCredential(region)
        String bucketName = BUCKET_NAME + UUID.randomUUID()
        String key = "MyObjectKey"

        try {
            s3.createBucket(bucketName)
        }
        catch (AmazonServiceException ase) {
            // Handle service exception
        }
        catch (AmazonClientException ace) {
            // Handle client exception
        }
    }

    //creating folder
    boolean createFolder(String bucket,String region,String key)
    {
        final BUCKET_NAME = bucket
        boolean created=false
        AmazonS3 s3=getCredential(region)

        String bucketName = BUCKET_NAME

        try {
            InputStream input = new ByteArrayInputStream(new byte[0]);
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(0);
            s3.putObject(new PutObjectRequest(bucketName, key, input, metadata));
            s3.setObjectAcl(bucketName, key, CannedAccessControlList.Private);
            created=true
        }
        catch (AmazonServiceException ase) {
            // Handle service exception
        } catch (AmazonClientException ace) {
            // Handle client exception
        }
        return created
    }

    //upload one file
    boolean putObjectToBucket(String bucket,String region,String key,InputStream is,String contentType)
    {
        String bucketName = bucket;
        String fileObjKeyName =key;
        boolean putobj=false

        try {
            AmazonS3 s3Client=getCredential(region)

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(contentType);
            metadata.addUserMetadata("x-amz-meta-title", "eduplusnow");

            PutObjectRequest request = new PutObjectRequest(bucketName, fileObjKeyName,is,metadata);
            s3Client.putObject(request);
            s3Client.setObjectAcl(bucketName, fileObjKeyName, CannedAccessControlList.Private);
            putobj=true
        }
        catch(AmazonServiceException e) {
            e.printStackTrace();
        }
        catch(SdkClientException e) {
            e.printStackTrace();
        }
        return putobj
    }

    //listing all buckets list
    def listBuckets(String region)
    {
        AmazonS3 s3=getCredential(region)
        for (Bucket bucket : s3.listBuckets()) {
            System.out.println(" - " + bucket.getName())
        }
    }

    //to check whether folder or file is exist
    boolean doesObjectExist(String bucket,String region,String key)
    {
        final BUCKET_NAME = bucket
        boolean exist=false
        AmazonS3 s3=getCredential(region)

        String bucketName = BUCKET_NAME

        try {
            exist=s3.doesObjectExist(bucketName,key)
        }
        catch (AmazonServiceException ase) {
            // Handle service exception
        } catch (AmazonClientException ace) {
            // Handle client exception
        }
        return exist
    }

    //delete file or folder
    def deleteObject(String bucket,String region,String key)
    {
        final BUCKET_NAME = bucket
        String clientRegion = region
        AWSCredentials credentials = null
        AmazonS3 s3=getCredential(region)

        String bucketName = BUCKET_NAME

        try {
            s3.deleteObject(bucketName, key);
        }
        catch (AmazonServiceException ase) {
            // Handle service exception
        } catch (AmazonClientException ace) {
            // Handle client exception
        }
    }

    //downloading file from AWS
    InputStream downloadContentFromBucket(String bucket,String region,String key)
    {
        final BUCKET_NAME = bucket

        AmazonS3 s3=getCredential(region)
        String bucketName = BUCKET_NAME
        S3Object object = s3.getObject(new GetObjectRequest(bucketName, key))
        return object.getObjectContent()
    }

    boolean putPDFToBucket(String bucket,String region,String key,String file,File video,String contentType)
    {
        String clientRegion = region;
        String bucketName = bucket;
        String fileObjKeyName = key+file;
        String fileName = key+file;
        boolean putobj=false

        AmazonS3 s3Client
        try {
            s3Client=getCredential(region)
            println("PATH -->>"+fileName)
            ObjectMetadata metadata = new ObjectMetadata()
            metadata.setContentType(contentType)
            metadata.addUserMetadata("x-amz-meta-title", "eduplusnow");

            PutObjectRequest request = new PutObjectRequest(bucketName, fileObjKeyName,video)

            s3Client.putObject(request)
            s3Client.setObjectAcl(bucketName, fileObjKeyName, CannedAccessControlList.Private)
            putobj=true
        }
        catch(AmazonServiceException e) {
            e.printStackTrace();
        }
        catch(SdkClientException e) {
            e.printStackTrace();
        }
        return putobj
    }

    def downloaddirectory(String key, String dir_path)
    {
        TransferManager xfer_mgr = TransferManagerBuilder.standard().withS3Client(getCredential("ap-south-1")).build();
        AWSBucket awsBucket = AWSBucket.findByContent("documents")
        try {
            MultipleFileDownload xfer = xfer_mgr.downloadDirectory(
                    awsBucket.bucketname, key, new File(dir_path));
            XferMgrProgress.showTransferProgress(xfer);
            XferMgrProgress.waitForCompletion(xfer);
        } catch (AmazonServiceException e) {
            System.err.println(e.getErrorMessage());
            System.exit(1);
        }
        xfer_mgr.shutdownNow();
    }

    String getPresignedUrl(String bucket, String key,String region)
    {
        AmazonS3 s3=getCredential(region)
        def bucketName = bucket
        try {
            AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                    .withRegion(region)
                    .withCredentials(new ProfileCredentialsProvider())
                    .build();

            java.util.Date expiration = new java.util.Date();
            long expTimeMillis = expiration.getTime();
            expTimeMillis += 1000 * 60 * 60;
            expiration.setTime(expTimeMillis);

            GeneratePresignedUrlRequest generatePresignedUrlRequest =
                    new GeneratePresignedUrlRequest(bucketName, key)
                            .withMethod(HttpMethod.GET)
                            .withExpiration(expiration);
            URL url = s3.generatePresignedUrl(generatePresignedUrlRequest);
            return url
        } catch (AmazonServiceException e) {
            e.printStackTrace();
        } catch (SdkClientException e) {
            e.printStackTrace();
        }
    }

    boolean CopyObjectToBucket(String bucket,String region, String keynew, String keyold)
    {
        String bucketName = bucket;
        boolean putobj=false
        try {
            AmazonS3 s3Client=getCredential(region)
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.addUserMetadata("x-amz-meta-title", "eduplusnow");
            s3Client.copyObject(bucketName, keyold, bucketName, keynew);
            s3Client.setObjectAcl(bucketName, keynew, CannedAccessControlList.Private);
            putobj=true
        }
        catch(AmazonServiceException e) {
            e.printStackTrace();
        }
        catch(SdkClientException e) {
            e.printStackTrace();
        }
        return putobj
    }

    def moveGlacierToStandard()
    {
        String region = "ap-south-1";
        String bucketName = "vgespl-easycheck";
        String keyName = "viit/1/answersheet/2021-22/1/Written/10/ES21201CV/1_1_5885_438_1_96_52543.pdf";
        boolean putobj=false
        AmazonS3 s3Client
        try {
            s3Client=getCredential(region)
            RestoreObjectRequest requestRestore = new RestoreObjectRequest(bucketName, keyName, 30);
            s3Client.restoreObjectV2(requestRestore);

            ObjectMetadata response = s3Client.getObjectMetadata(bucketName, keyName);
            Boolean restoreFlag = response.getOngoingRestore();
            System.out.format("Restoration status: %s.\n",
                    restoreFlag ? "in progress" : "not in progress (finished or failed)");
        }
        catch(AmazonServiceException e) {
            e.printStackTrace();
        }
        catch(SdkClientException e) {
            e.printStackTrace();
        }
        return putobj
    }

    def objectLastModificationDate(String bucket,String region,String key){
        final BUCKET_NAME = bucket
        AmazonS3 s3=getCredential(region)
        String bucketName = BUCKET_NAME
        S3Object object = s3.getObject(new GetObjectRequest(bucketName, key))
        ObjectMetadata objectMetadata = object.getObjectMetadata();
        Date lastModified = objectMetadata.getLastModified();
        return lastModified
    }
}