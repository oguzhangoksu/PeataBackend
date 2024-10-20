package peata.backend.service.concretes;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import peata.backend.service.abstracts.S3Service;
import peata.backend.utils.FileData;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@Service
public class S3ServiceImpl implements S3Service{

    private static final Logger logger = LoggerFactory.getLogger(S3ServiceImpl.class);
    
    @Autowired
    private S3Client s3Client;
    
    @Value("${aws.region}")
    private String region;

    @Value("${aws.bucket.name}")
    private String bucketName;




    public List<String> uploadFilesToFolder(String folderName, List<FileData> files) throws IOException {
      List<String> imageUrls = new ArrayList<>();

        for (FileData file : files) {
            String fileName = file.getFileName();
            byte[] fileData = file.getFileData();

            // Create the full file path in S3
            String fullFilePath = folderName + "/" + fileName;
            String contentType = determineContentType(fileName);

            logger.info("Uploading file: {} to folder: {}", fileName, folderName);

            // Prepare the S3 PutObjectRequest
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fullFilePath)
                    .contentType(contentType) 
                    .build();

            try {
                // Upload the file
                s3Client.putObject(request, RequestBody.fromBytes(fileData));

                // Generate the public URL for the uploaded file
                String imageUrl = generateFileUrl(fullFilePath);
                imageUrls.add(imageUrl);

                logger.info("File uploaded successfully: {}", fileName);
            } catch (SdkException e) {
                logger.error("Failed to upload file: {} to folder: {}. Error: {}", fileName, folderName, e.getMessage());
            }
        }

        return imageUrls; // Return the list of image URLs
    }

     public byte[] downloadFileFromFolder(String fileName,String folderName) throws IOException {
        // The key is the path to the file inside the S3 bucket
        String fullFilePath = folderName + fileName;  // E.g. folderName/my-image.png

        logger.info("Downloading file: {} from folder: {}", fileName, folderName);

        // Create a GetObjectRequest
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(fullFilePath)  // Specify the file path inside S3
                .build();

        // Get the file as a byte array
          try {
            ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(getObjectRequest);
            logger.info("File downloaded successfully: {}", fileName);
            return objectBytes.asByteArray();
        } catch (SdkException e) {
            logger.error("Failed to download file: {}. Error: {}", fileName, e.getMessage());
            throw new IOException("Failed to download file from S3", e);
        }
    }

    private String generateFileUrl(String filePath) {

        String fileUrl = "https://" + bucketName + ".s3." + region + ".amazonaws.com/" + filePath;
        logger.debug("Generated file URL: {}", fileUrl);
        return fileUrl;
    }

    public void deleteFolder(String folderName) {

        logger.info("Deleting folder: {}", folderName);

        // Initial request to list objects with the folder prefix
        ListObjectsV2Request listObjectsRequest = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix(folderName)
                .build();

        ListObjectsV2Response listObjectsResponse;
        try {
            do {
                // Fetch the objects in the folder
                listObjectsResponse = s3Client.listObjectsV2(listObjectsRequest);

                // Delete each object found
                for (S3Object s3Object : listObjectsResponse.contents()) {
                    deleteFile(s3Object.key());
                }

                // Update the request to continue with the next batch (pagination)
                String nextContinuationToken = listObjectsResponse.nextContinuationToken();
                listObjectsRequest = listObjectsRequest.toBuilder()
                        .continuationToken(nextContinuationToken)
                        .build();

            } while (listObjectsResponse.isTruncated());

            logger.info("Folder deleted successfully: {}", folderName);
        } catch (SdkException e) {
            logger.error("Failed to delete folder: {}. Error: {}", folderName, e.getMessage());
        }
    }
    
    public void deleteFile(String filename) {
        logger.info("Deleting file: {}", filename);
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(filename)
                .build();

        try {
            s3Client.deleteObject(deleteObjectRequest);
            logger.info("File deleted successfully: {}", filename);
        } catch (SdkException e) {
            logger.error("Failed to delete file: {}. Error: {}", filename, e.getMessage());
        }
    }

    private String determineContentType(String fileName) {
        if (fileName.endsWith(".png")) {
            return "image/png";
        } else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (fileName.endsWith(".gif")) {
            return "image/gif";
        }
        // Add more file types if needed
        return "application/octet-stream"; // Default for unknown file types
    }

    
    

}
