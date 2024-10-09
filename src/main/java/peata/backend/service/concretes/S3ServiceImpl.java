package peata.backend.service.concretes;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import peata.backend.service.abstracts.S3Service;
import peata.backend.utils.FileData;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
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
            } catch (SdkException e) {
                // Handle the SDK exception (e.g., log it)
                e.printStackTrace();
                // Optionally, handle error logic (e.g., return an error response)
            }
        }

        return imageUrls; // Return the list of image URLs
    }

     public byte[] downloadFileFromFolder(String fileName,String folderName) throws IOException {
        // The key is the path to the file inside the S3 bucket
        String fullFilePath = folderName + fileName;  // E.g. folderName/my-image.png

        // Create a GetObjectRequest
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(fullFilePath)  // Specify the file path inside S3
                .build();

        // Get the file as a byte array
        ResponseBytes<?> objectBytes = s3Client.getObjectAsBytes(getObjectRequest);
        return objectBytes.asByteArray();  
    }

    private String generateFileUrl(String filePath) {
        return "https://" + bucketName + ".s3." + region + ".amazonaws.com/" + filePath;
    }

    public void deleteFolder(String folderName) {

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

            System.out.println("Folder deleted successfully: " + folderName);
        } catch (SdkException e) {
            System.err.println("Failed to delete folder: " + e.getMessage());
        }
    }
    
    public void deleteFile(String filename) {
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(filename)
                .build();

        try {
            s3Client.deleteObject(deleteObjectRequest);
            System.out.println("File deleted: " + filename);
        } catch (SdkException e) {
            System.err.println("Failed to delete file: " + filename + " - " + e.getMessage());
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
