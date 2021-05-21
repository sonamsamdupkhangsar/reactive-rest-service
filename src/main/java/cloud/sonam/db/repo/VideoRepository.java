package cloud.sonam.db.repo;

import cloud.sonam.db.entity.Video;
import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface VideoRepository extends ReactiveSortingRepository<Video, UUID> {
    Video findByName(String name);
}
