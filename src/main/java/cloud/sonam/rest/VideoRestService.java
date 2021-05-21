package cloud.sonam.rest;

import cloud.sonam.db.entity.Video;
import cloud.sonam.service.VideoServiceInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("videos")
public class VideoRestService implements VideoRestInterface {
    private static final Logger LOG = LoggerFactory.getLogger(VideoRestService.class);

    @Autowired
    private VideoServiceInterface videoServiceInterface;

    @Override

    @GetMapping
    public Flux<Video> getVideos() {
        LOG.info("get videos");

        return videoServiceInterface.getVideo();
    }

    @PostMapping(value="/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<String> upload(@RequestPart("file") Mono<FilePart> filePartFlux) {
        LOG.info("upload got called");
        return videoServiceInterface.save(filePartFlux);
    }
}
