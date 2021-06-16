package cloud.sonam.rest;

import cloud.sonam.db.entity.Video;
import cloud.sonam.img.UploadResult;
import cloud.sonam.service.VideoServiceInterface;
import cloud.sonam.service.impl.VideoLoaderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.s3.model.Part;

import java.nio.ByteBuffer;

import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("videos")
public class VideoRestService implements VideoRestInterface {
    private static final Logger LOG = LoggerFactory.getLogger(VideoRestService.class);

    @Autowired
    private VideoLoaderService videoServiceInterface;


    @GetMapping
    public Flux<Video> getVideos() {
        LOG.info("get videos");

        return videoServiceInterface.getVideo();
    }

    @PostMapping(value="/upload")
    public Mono<ResponseEntity<UploadResult>> upload(@RequestHeader HttpHeaders headers, @RequestBody Flux<ByteBuffer> body) {
        LOG.info("upload got called");
        return videoServiceInterface.save(headers, body);
    }

    //@RequestMapping(value = "/upload/part", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, method = {RequestMethod.POST, RequestMethod.PUT})
    @RequestMapping(value = "/multipartupload", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<UploadResult>> uploadMultiPart(@RequestHeader HttpHeaders headers, @RequestBody Flux<Part> parts) {
        LOG.info("multipartupload got called, header: {}\n, parts: {}", parts);
        return videoServiceInterface.multipartUploadHandler(headers, parts);
        //parts.log().subscribe();
        //return null;

    }

    @PostMapping(value="/part2", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(OK)
    public Mono<ResponseEntity<UploadResult>> uploadMultiPart2(@RequestHeader HttpHeaders headers,  @RequestPart Flux<Part> parts) {
        LOG.info("part2 got called, headers: {}", headers);
        return null;
        // return videoServiceInterface.multipartUploadHandler(headers, parts);
    }
}
