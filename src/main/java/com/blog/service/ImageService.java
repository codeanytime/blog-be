package com.blog.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
public class ImageService {

    @Autowired(required = false)
    private AmazonS3 s3Client;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    /**
     * Check if S3 is configured and available
     * @return true if S3 client is available
     */
    public boolean isS3Available() {
        return s3Client != null;
    }

    public String uploadImage(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        // Check if S3 client is available
        if (s3Client == null) {
            throw new IllegalStateException("AWS S3 is not configured. Please provide AWS credentials.");
        }

        // Generate unique file name
        String fileName = UUID.randomUUID().toString() + "-" + file.getOriginalFilename();

        // Set metadata
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(file.getContentType());
        metadata.setContentLength(file.getSize());

        // Upload file to S3
        PutObjectRequest request = new PutObjectRequest(
                bucketName,
                fileName,
                file.getInputStream(),
                metadata
        ).withCannedAcl(CannedAccessControlList.PublicRead);

        s3Client.putObject(request);

        // Return the URL of the uploaded file
        return s3Client.getUrl(bucketName, fileName).toString();
    }
}
