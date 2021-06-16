package cloud.sonam.config;


import cloud.sonam.img.SpacesVideoSaver;
/*
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
*/
/*
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.http.async.SdkAsyncHttpClient;
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3AsyncClientBuilder;
import software.amazon.awssdk.services.s3.S3Configuration;
*/

import java.time.Duration;

/**
 * this config is here to support dev and test implementation only.
 * Should not upload any files to spaces.
 */
//@Profile({"dev"})
//@Configuration
public class SpacesDevConfig {
/*

    private static final Logger LOG = LoggerFactory.getLogger(SpacesDevConfig.class);

    @Value("${spaceBucketEndpoint}")
    private String bucketEndpoint;

    @Value("${spaceBucketRegion}")
    private String bucketRegion;

    @Value("${spaceAccessKey}")
    private String spaceAccessKey;

    @Value("${spaceSecretKey}")
    private String spaceSecretKey;

    public SpacesDevConfig() {
        LOG.info("instantiate dev spaces config");
    }

    @Bean
    public AmazonS3 space() {
        LOG.info("configuring AmazonS3 space object " +
                        "using \n key: {}, secret: {}, endpoint: {}, " +
                        "region: {}",
                spaceAccessKey, spaceSecretKey, bucketEndpoint, bucketRegion);
        SdkAsyncHttpClient httpClient = NettyNioAsyncHttpClient.builder()
                .writeTimeout(Duration.ZERO)
                .maxConcurrency(64)
                .build();
        S3Configuration serviceConfiguration = S3Configuration.builder()
                .checksumValidationEnabled(false)
                .chunkedEncodingEnabled(true)
                .build();
        S3AsyncClientBuilder b = S3AsyncClient.builder().httpClient(httpClient)
                .region(Region.of(bucketRegion))
                .credentialsProvider(credentialsProvider)
                .serviceConfiguration(serviceConfiguration);

        if (s3props.getEndpoint() != null) {
            b = b.endpointOverride(s3props.getEndpoint());
        }
        return b.build();

        */
/*AWSCredentialsProvider awscp = new AWSStaticCredentialsProvider(
                new BasicAWSCredentials(spaceAccessKey, spaceSecretKey)
        );
        AmazonS3 space = AmazonS3ClientBuilder
                .standard()
                .withCredentials(awscp)
                .withEndpointConfiguration(
                        new AwsClientBuilder.EndpointConfiguration(bucketEndpoint, bucketRegion)
                )
                .build();

        LOG.info("AmazonS3 space configuration done");
        return space;*//*

    }

    @Bean
    public SpacesVideoSaver spacesProfilePhoto() {
        return new SpacesVideoSaver();
    }
*/

}
