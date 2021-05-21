package cloud.sonam.reactiverestservice;


import cloud.sonam.db.entity.Employee;
import cloud.sonam.db.entity.Video;
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
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;


@AutoConfigureWebTestClient
@Log
@RunWith(SpringRunner.class)
@SpringBootTest
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
    public void uploadImageFile() throws IOException {
        var builder = new MultipartBodyBuilder();
        Assert.assertNotNull(image);

        LOG.info("image: {}, video: {}", image, video);
        Assert.assertTrue(image.getFile().exists());


        builder.part("file", image);
        MultiValueMap<String, HttpEntity<?>> multipartBody = builder.build();

        client.post().uri("/videos/upload")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(multipartBody))
                .exchange().expectStatus().isOk()
                .expectBodyList(String.class)
                .consumeWith(
                        result -> {
                            LOG.info("file upload done: {}", result.getResponseBody());
                        }
                );
    }
    @Test
    public void videoExists() throws IOException {
        Assert.assertNotNull(video);
        LOG.info("video: {}", video);
        Assert.assertTrue(video.getFile().exists());
    }

    @Test
    public void uploadVideoFile() throws IOException {
        Assert.assertNotNull(video);
        LOG.info("video: {}", video);
        Assert.assertTrue(video.getFile().exists());

        var builder = new MultipartBodyBuilder();
        builder.part("file", video);
        MultiValueMap<String, HttpEntity<?>> multipartBody = builder.build();

        client.post().uri("/videos/upload")

                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(multipartBody))
                .exchange().expectStatus().isOk()
                .expectBodyList(String.class)
                .consumeWith(
                        result -> {
                            LOG.info("file upload done: {}", result.getResponseBody());
                            getVideos();
                        }
                );
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
