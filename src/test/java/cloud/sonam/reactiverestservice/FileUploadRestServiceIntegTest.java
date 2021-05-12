package cloud.sonam.reactiverestservice;


import cloud.sonam.db.entity.Employee;
import lombok.extern.java.Log;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.http.client.reactive.ClientHttpRequest;
import org.springframework.http.codec.multipart.MultipartHttpMessageReader;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;


@AutoConfigureWebTestClient
@Log
@RunWith(SpringRunner.class)
@SpringBootTest
public class FileUploadRestServiceIntegTest {

    @Autowired
    private WebTestClient client;

    private static final Logger LOG = LoggerFactory.getLogger(FileUploadRestServiceIntegTest.class);

    @Value("classpath:sampleimage.png")
    private Resource image;

    @Test
    public void uploadFile() throws IOException {
        var builder = new MultipartBodyBuilder();
        builder.part("file", image);
        MultiValueMap<String, HttpEntity<?>> multipartBody = builder.build();

        client.post().uri("/file/upload")
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

    private MultiValueMap<String, HttpEntity<?>> generateBody() {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("fileParts", image);
        return builder.build();
    }
}
