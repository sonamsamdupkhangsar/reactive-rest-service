package cloud.sonam.img;
/*

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
*/
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * @author sonamwangyalsamdupkhangsar
 */
//@Service
public class SpacesVideoSaver {

    private static final Logger LOG = LoggerFactory.getLogger(SpacesVideoSaver.class);

    @Value("${spaceBucketEndpoint}")
    private String bucketEndpoint;

    @Value("${bucket}")
    private String bucket;

    @Value("${imageAclHeader}")
    private String imageAclHeader;

    @Value("${imageAclValue}")
    private String imageAclValue;

   /* @Autowired
    private AmazonS3 amazonS3;

    private DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_a_hh_mm_ss_S");


    public String getVideoUrl(String path) {
        URL url = amazonS3.getUrl("sonam", path);
        if (url != null) {
            LOG.info("this path exists: {}", url.toString());
            return url.toString();
        } else {
            LOG.error("this path does not exist: {}", path);
            return null;
        }
    }

    *//**
     * this will save the imagebyte to a AWS Space bucket and return
     * the url.
     * example url (https://sonam.sfo2.digitaloceanspaces.com/profilephotos/1234/profilephoto/1.jpg)
     *
     * @param inputStream

     * @return returns the filename
     */
/*
    public String save(InputStream inputStream, int streamLength, String format, String filePathName) {
       // InputStream is = new ByteArrayInputStream(byteImage);
        ObjectMetadata om = new ObjectMetadata();
        //om.setContentLength(byteImage.length);
        om.setContentLength(streamLength);
        //om.setContentType("image/" + imageType);
        om.setContentType(format);
        om.setHeader(imageAclHeader, imageAclValue);

        LOG.info("saving format {} to bucket: {}, filepath: {}", format, bucket, filePathName);
        PutObjectResult putObjectResult = amazonS3.putObject(bucket, filePathName, inputStream, om);

        LOG.info("saved file to bucket: {}", putObjectResult);
        return filePathName;
    }*/
}