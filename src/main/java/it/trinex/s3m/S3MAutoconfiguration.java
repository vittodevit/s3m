package it.trinex.s3m;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

/**
 * Spring Boot auto-configuration for the s3m library.
 *
 * <p>When the AWS S3 SDK v2 is on the classpath and no user-defined beans of the same
 * type exist, this configuration creates:
 * - an S3Client
 * - an S3Presigner
 * - an S3MService (built on the S3Presigner)
 *
 * <p>Configuration properties (prefix: s3m):
 * - s3m.accessKeyId
 * - s3m.secretAccessKey
 * - s3m.s3.bucketName
 * - s3m.s3.region
 * - s3m.s3.endpointsPrefix (optional; default: "/direct/")
 *
 * <p>Startup validation: a HeadBucket call is executed to verify the configured
 * bucket can be accessed with the provided credentials and region. If the check fails
 * the application startup fails with BeanCreationException.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(S3Client.class)
@EnableConfigurationProperties(S3MProperties.class)
public class S3MAutoconfiguration {
    private final S3MProperties s3MProperties;

    public S3MAutoconfiguration(S3MProperties s3MProperties) {
        this.s3MProperties = s3MProperties;
    }

    /**
     * Creates a singleton AWS S3Client using static credentials from S3MProperties.
     *
     * <p>After creation, it performs a lightweight self-test (HeadBucket) against the
     * configured bucket to fail fast when credentials, region, or bucket are wrong.
     *
     * @return configured S3Client
     * @throws org.springframework.beans.factory.BeanCreationException if the bucket is not accessible
     */
    @Bean
    @ConditionalOnMissingBean
    public S3Client s3Client() {
        String region = s3MProperties.getS3().getRegion();
        String accessKey = s3MProperties.getAccessKeyId();
        String secretKey = s3MProperties.getSecretAccessKey();


        S3Client client = S3Client.builder()
            .region(Region.of(region))
            .credentialsProvider(StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKey, secretKey)))
            .build();

        // self-test: ensure bucket exists and is accessible
        try {
            HeadBucketRequest head = HeadBucketRequest.builder()
                .bucket(s3MProperties.getS3().getBucketName())
                .build();
            client.headBucket(head);
        } catch (Exception ex) {
            throw new BeanCreationException("Unable to access S3 bucket '" + s3MProperties.getS3().getBucketName() + "': " + ex.getMessage(), ex);
        }

        return client;
    }

    /**
     * Creates a singleton S3Presigner using static credentials from S3MProperties.
     *
     * @return configured S3Presigner
     */
    @Bean
    @ConditionalOnMissingBean
    public S3Presigner s3Presigner() {
        String region = s3MProperties.getS3().getRegion();
        String accessKey = s3MProperties.getAccessKeyId();
        String secretKey = s3MProperties.getSecretAccessKey();


        return S3Presigner.builder()
            .region(Region.of(region))
            .credentialsProvider(StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKey, secretKey)))
            .build();
    }


    /**
     * Exposes S3MService, a small helper around S3Presigner that generates pre-signed
     * URLs for uploads and downloads.
     *
     * @param presigner the S3Presigner bean
     * @return S3MService configured with bucket and endpointsPrefix from properties
     */
    @Bean
    @ConditionalOnMissingBean
    public S3MService autosignedS3Service(S3Presigner presigner) {
        return new S3MService(presigner,
            s3MProperties.getS3().getBucketName(),
            s3MProperties.getS3().getEndpointsPrefix());
    }
}