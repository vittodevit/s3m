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
 *     region: eu-central-1
 *     endpointsPrefix: /direct/
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
         * AWS region where the bucket resides (for example, <code>eu-central-1</code>).
         * Property: <code>s3m.s3.region</code>
         */
        @NotBlank
        private String region;

        /**
         * Deprecated: not used by the auto-configuration. Use
         * <code>s3m.autoendpoint.enabled</code> to toggle the built-in HTTP controller instead.
         * Property: <code>s3m.s3.endpointsEnabled</code>
         * @deprecated Use <code>s3m.autoendpoint.enabled</code> to control the HTTP controller instead.
         */
        private boolean endpointsEnabled = false;

        /**
         * Prefix applied to object keys by the built-in HTTP endpoints.
         * You can also apply it yourself when using S3MService directly.
         * Property: <code>s3m.s3.endpointsPrefix</code>
         * Default: <code>"/direct/"</code>
         */
        private String endpointsPrefix = "/direct/";
    }
}