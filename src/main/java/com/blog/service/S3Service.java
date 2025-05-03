package com.blog.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class S3Service {

    @Autowired(required = false)
    private AmazonS3 amazonS3Client;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    /**
     * Get the configured S3 bucket name
     * @return The bucket name
     */
    public String getBucketName() {
        return bucketName;
    }

    /**
     * Check if S3 is configured and available
     * @return true if S3 client is available
     */
    public boolean isS3Available() {
        return amazonS3Client != null;
    }

    /**
     * Check if AWS S3 is properly configured
     * @return true if configured, false otherwise
     */
    public boolean isConfigured() {
        return amazonS3Client != null;
    }

    /**
     * Upload a file to S3 bucket
     *
     * @param file The file to upload
     * @return The URL of the uploaded file
     * @throws IOException If file cannot be processed
     * @throws IllegalStateException If S3 client is not configured
     */
    public String uploadFile(MultipartFile file) throws IOException {
        return uploadFile(file, "uploads");
    }

    /**
     * Upload a file to S3 bucket with a specific folder path
     *
     * @param file The file to upload
     * @param folderPath The folder path to place the file in (e.g., "profiles", "uploads")
     * @return The URL of the uploaded file
     * @throws IOException If file cannot be processed
     * @throws IllegalStateException If S3 client is not configured
     */
    public String uploadFile(MultipartFile file, String folderPath) throws IOException {
        // Check if S3 client is available
        if (amazonS3Client == null) {
            throw new IllegalStateException("AWS S3 is not configured. Please provide AWS credentials.");
        }
        // Generate a unique filename
        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String timestamp = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now());
        String uniqueFilename = folderPath + "/" + timestamp + "-" + UUID.randomUUID().toString() + fileExtension;

        // Set object metadata
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());

        // Upload file to S3
        amazonS3Client.putObject(
                new PutObjectRequest(bucketName, uniqueFilename, file.getInputStream(), metadata)
                        .withCannedAcl(CannedAccessControlList.PublicRead)
        );

        // Return the public URL
        return amazonS3Client.getUrl(bucketName, uniqueFilename).toString();
    }

    /**
     * Delete a file from S3 bucket
     *
     * @param fileUrl The complete URL of the file to delete
     * @throws IllegalStateException If S3 client is not configured
     */
    public void deleteFile(String fileUrl) {
        // Check if S3 client is available
        if (amazonS3Client == null) {
            throw new IllegalStateException("AWS S3 is not configured. Please provide AWS credentials.");
        }

        // Extract the key from the URL
        String key = fileUrl.substring(fileUrl.indexOf(bucketName) + bucketName.length() + 1);

        // Delete the file
        amazonS3Client.deleteObject(new DeleteObjectRequest(bucketName, key));
    }
}
