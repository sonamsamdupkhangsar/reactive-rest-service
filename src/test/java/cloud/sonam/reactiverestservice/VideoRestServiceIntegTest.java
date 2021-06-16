package cloud.sonam.reactiverestservice;


import cloud.sonam.db.entity.Employee;
import cloud.sonam.db.entity.Video;
import cloud.sonam.img.UploadResult;
import lombok.extern.java.Log;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.*;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import javax.print.attribute.standard.Media;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;


@Log
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureWebTestClient(timeout = "3600000")
public class VideoRestServiceIntegTest {

    @Autowired
    private WebTestClient client;

    private static final Logger LOG = LoggerFactory.getLogger(VideoRestServiceIntegTest.class);

    @Value("classpath:sampleimage.png")
    private Resource image;

    @Value("classpath:shortmov.mp4")
    private Resource video;

    @Autowired
    DatabaseClient database;

    @Before
    public void setUp() {

        Hooks.onOperatorDebug();
//        "CREATE TABLE video ( id SERIAL PRIMARY KEY, name varchar(255), path VARCHAR(255) NOT NULL, stored datetime NOT NULL);");

        List<String> statements = Arrays.asList(//
                "DROP TABLE IF EXISTS video;",
                "CREATE TABLE video ( id SERIAL PRIMARY KEY, name varchar(255),  thumb varchar(255), path VARCHAR(255) NOT NULL, stored datetime NOT NULL);");



        statements.forEach(it -> database.sql(it) //
                .fetch() //
                .rowsUpdated() //
                .as(StepVerifier::create) //
                .expectNextCount(1) //
                .verifyComplete());
    }
    @Test
    public void videoExists() throws IOException {
        Assert.assertNotNull(video);
        LOG.info("video: {}", video);
        Assert.assertTrue(video.getFile().exists());
    }

    @Test
    public void uploadImageFile() throws Exception {
        var builder = new MultipartBodyBuilder();
        Assert.assertNotNull(image);

        LOG.info("image.contentLength: {}, image: {}", image.contentLength(), image);
        Assert.assertTrue(image.getFile().exists());


        builder.part("file", Files.readAllBytes(image.getFile().toPath()));
        MultiValueMap<String, HttpEntity<?>> multipartBody = builder.build();



        client.post().uri("/videos/upload")
                .header("filename", image.getFilename())
                .header("format", "image/png")
                .header(HttpHeaders.CONTENT_LENGTH, ""+image.contentLength())
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(multipartBody))
                .exchange().expectStatus().isCreated();
    }

    @Test
    public void uploadVideoFile() throws IOException, InterruptedException {
        Assert.assertNotNull(video);
        LOG.info("video contentLength: {}, video: {}", video.contentLength(), video);
        Assert.assertTrue(video.getFile().exists());

        var builder = new MultipartBodyBuilder();

        builder.part("file", Files.readAllBytes(video.getFile().toPath()))
                .header("Content-Disposition", "form-data; name=myHomeVideo; filename=video.mp4");

        builder.part("content-length", video.contentLength());

        client.post().uri("/videos/upload")
                .header("filename", video.getFilename())
                .header("format", "video/mp4")
                .header(HttpHeaders.CONTENT_LENGTH, ""+video.contentLength())
                .bodyValue(video)
                .exchange().expectStatus().isCreated()

               .expectBody(UploadResult.class)
                .consumeWith(
                        result -> {
                            LOG.info("file upload done: {}", result.getResponseBody());
                            getVideos();
                        }
                );
        Thread.sleep(1000*6);

    }

    //@Test
    public void uploadVideoFileMultipart() throws IOException {
        Assert.assertNotNull(video);
        LOG.info("video contentLength: {}, video: {}", video.contentLength(), video);
        Assert.assertTrue(video.getFile().exists());

        var builder = new MultipartBodyBuilder();

       builder.part("file",video.getFile());
        //MultiValueMap<String, HttpEntity<?>> multipartBody = builder.build();


       client.post().uri("/videos/multipartupload")
                .contentType(MediaType.MULTIPART_FORM_DATA)

               .body(BodyInserters.fromMultipartData(builder.build()))
                .exchange()
                .expectStatus()
                .is2xxSuccessful();
    }

    @Test
    public void uploadVideoFileMultipart2() throws IOException {
        Assert.assertNotNull(video);
        LOG.info("video contentLength: {}, video: {}", video.contentLength(), video);
        Assert.assertTrue(video.getFile().exists());

        var builder = new MultipartBodyBuilder();

        //builder.part("file", new FileSystemResource(video.getFile()));
        builder.part("file", Files.readAllBytes(video.getFile().toPath()))
                .header("Content-Disposition", "form-data; name=myHomeVideo; filename=video.mp4");


        //builder.part("content-length", video.contentLength());
        //MultiValueMap<String, HttpEntity<?>> multipartBody = builder.build();

        Path path = video.getFile().toPath();

        client.post().uri("/videos/part2")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .header("filename", video.getFilename())
                .header("format", "video/mp4")
                .header(HttpHeaders.CONTENT_LENGTH, ""+video.contentLength())
                .body(BodyInserters.fromMultipartData(builder.build()))
                .exchange()
                .expectStatus()
                .is2xxSuccessful();
    }

    @Test
    public void getFile() {
        client.get().uri("/videos?sort=localDateTime,asc")
                .accept(MediaType.APPLICATION_JSON)
                .exchange().expectStatus().isOk()
                .expectBodyList(Employee.class)
                .consumeWith(
                        result -> {
                            LOG.info("got employee by first name: {}", result.getResponseBody());
                        }
                );
    }

    public void getVideos() {
        client.get().uri("/videos")
                .accept(MediaType.APPLICATION_JSON)
                .exchange().expectStatus().isOk()
                .expectBodyList(Video.class)
                .consumeWith(
                        result -> {
                            LOG.info("videos: {}", result.getResponseBody());
                        }
                );
    }


    private MultiValueMap<String, HttpEntity<?>> generateBody() {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("fileParts", image);
        return builder.build();
    }


}
