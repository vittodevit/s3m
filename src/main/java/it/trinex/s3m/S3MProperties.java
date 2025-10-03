package it.trinex.s3m;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration properties for the s3m library (prefix: <code>s3m</code>).
 *
 * <p>Example (application.yml):
 * s3m:
 *   accessKeyId: AKIA...
 *   secretAccessKey: ...
 *   s3:
 *     bucketName: my-bucket
 *     endpoint: https://minio.mycompany.local
 *     # Optional: region used for signing; defaults to us-east-1. For Cloudflare R2, use "auto".
 *     region: us-east-1
 *   autoendpoint: false
 *
 * <p>Note: The built-in HTTP endpoints are controlled by
 * <code>s3m.autoendpoint.enabled</code>.
 */
@ConfigurationProperties(prefix = "s3m")
@Validated
@Setter
@Getter
public class S3MProperties {
    /**
     * AWS Access Key ID used by the auto-configured S3Client and S3Presigner.
     * Property: <code>s3m.accessKeyId</code>
     */
    @NotBlank
    private String accessKeyId;

    /**
     * AWS Secret Access Key used by the auto-configured S3Client and S3Presigner.
     * Property: <code>s3m.secretAccessKey</code>
     */
    @NotBlank
    private String secretAccessKey;

    private boolean autoendpoint = false;

    private final S3 s3 = new S3();

    /**
     * Nested S3-specific settings under <code>s3m.s3</code>.
     */
    @Setter
    @Getter
    public static class S3 {
        /**
         * Name of the target S3 bucket.
         * Property: <code>s3m.s3.bucketName</code>
         */
        @NotBlank
        private String bucketName;

        /**
         * Custom S3-compatible endpoint, e.g. MinIO, Cloudflare R2, or AWS S3 URL.
         * Property: <code>s3m.s3.endpoint</code>
         */
        @NotBlank
        private String endpoint;

        /**
         * Optional region for request signing. If not set, defaults to <code>us-east-1</code>.
         * For Cloudflare R2, set to <code>auto</code>.
         * Property: <code>s3m.s3.region</code>
         */
        private String region;
    }
}