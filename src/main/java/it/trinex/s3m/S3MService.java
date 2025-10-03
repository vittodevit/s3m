package it.trinex.s3m;

import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.Objects;

/**
 * Injectable service for generating AWS S3 pre-signed URLs.
 *
 * <p>Supports creating URLs for PUT (upload) and GET (download) operations using
 * the AWS SDK v2 S3Presigner.
 *
 * <p>Keys can optionally be prefixed with a configured prefix (e.g., "/direct/").
 * The built-in HTTP controller uses the prefix by default; when using this service
 * directly you can choose whether to apply it per call.
 */
public class S3MService {
    private final S3Presigner presigner;
    private final String bucket;
    /**
     * Create a new S3MService.
     *
     * @param presigner AWS S3Presigner to use
     * @param bucket    target S3 bucket name
     */
    public S3MService(S3Presigner presigner, String bucket) {
        this.presigner = Objects.requireNonNull(presigner, "s3Presigner");
        this.bucket = Objects.requireNonNull(bucket, "bucket");
    }

    /**
     * Generate a pre-signed upload URL with optional automatic application of the
     * configured prefix.
     *
     * @param fileKey           object key in the bucket
     * @param expirationMinutes link validity in minutes; values < 1 are coerced to 1
     * @return the signed URL
     */
    public String generateUploadUrl(String fileKey, int expirationMinutes) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
            .bucket(bucket)
            .key(fileKey)
            .build();


        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
            .signatureDuration(Duration.ofMinutes(Math.max(1, expirationMinutes)))
            .putObjectRequest(putObjectRequest)
            .build();


        PresignedPutObjectRequest presignedRequest = presigner.presignPutObject(presignRequest);
        return presignedRequest.url().toString();
    }

    /**
     * Generate a pre-signed download URL with optional automatic application of the
     * configured prefix.
     *
     * @param fileKey           object key in the bucket
     * @param expirationMinutes link validity in minutes; values < 1 are coerced to 1
     * @return the signed URL
     */
    public String generateDownloadUrl(String fileKey, int expirationMinutes) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
            .bucket(bucket)
            .key(fileKey)
            .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
            .signatureDuration(Duration.ofMinutes(Math.max(1, expirationMinutes)))
            .getObjectRequest(getObjectRequest)
            .build();

        PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(presignRequest);
        return presignedRequest.url().toString();
    }
}