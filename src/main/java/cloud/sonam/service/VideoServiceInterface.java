package cloud.sonam.service;

import cloud.sonam.db.entity.Video;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface VideoServiceInterface {
    Flux<Video> getVideo();
    Mono<String> save(Mono<FilePart> filePartMono);
}
