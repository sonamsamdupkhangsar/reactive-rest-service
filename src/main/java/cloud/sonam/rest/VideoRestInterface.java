package cloud.sonam.rest;

import cloud.sonam.db.entity.Video;
import reactor.core.publisher.Flux;

public interface VideoRestInterface {
    Flux<Video> getVideos();
}
