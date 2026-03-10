package common

import recruitment.AWSBucket
import grails.gorm.transactions.Transactional

import javax.servlet.http.Part
@Transactional
class AWSUploadDocumentsService {

    AWSBucketService awsBucketService = new AWSBucketService()

    boolean uploadDocument(Part filePart, def folderPath, def fileName, def existsFilePath) {
        AWSBucket awsBucket = AWSBucket.findByContent("documents")
        InputStream fs = filePart.getInputStream()
        String contentType = filePart.getContentType()
        def filePath = folderPath + fileName
        boolean filePathIsExists = existsFilePath != null
        boolean isUploadSuccess = false
        // Check if the folder exists
        if (!awsBucketService.doesObjectExist(awsBucket.bucketname, awsBucket.region, folderPath)) {
            boolean folderCreated = awsBucketService.createFolder(awsBucket.bucketname, awsBucket.region, folderPath)
            if (!folderCreated) return false // Exit early if folder creation fails
        }
        // If file already exists and needs to be replaced
        if (filePathIsExists && awsBucketService.doesObjectExist(awsBucket.bucketname, awsBucket.region, existsFilePath)) {
            boolean deleteSuccess = awsBucketService.deleteObject(awsBucket.bucketname, awsBucket.region, existsFilePath)
            if (!deleteSuccess) return false // Exit early if deletion fails
        }
        // Upload the file
        isUploadSuccess = awsBucketService.putObjectToBucket(awsBucket.bucketname, awsBucket.region, filePath, fs, contentType)
        return isUploadSuccess
    }

}

