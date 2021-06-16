package cloud.sonam.config;

import java.net.URI;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;


@Configuration
@ConfigurationProperties(prefix = "aws.s3")
@Data
public class S3ClientConfigurationProperties {

    private Region region = Region.of("https://sfo2.digitaloceanspaces.com");
    private URI endpoint = null;

    private String accessKeyId;
    private String secretAccessKey;
    private String subdomain;

    // Bucket name we'll be using as our backend storage
    private String bucket;

    private String videoPath;

    // AWS S3 requires that file parts must have at least 5MB, except
    // for the last part. This may change for other S3-compatible services, so let't
    // define a configuration property for that
    private int multipartMinPartSize = 5*1024*1024;

}
