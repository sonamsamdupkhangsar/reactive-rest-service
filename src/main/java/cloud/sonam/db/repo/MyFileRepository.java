package cloud.sonam.db.repo;

import cloud.sonam.db.entity.Video;
import org.springframework.data.repository.reactive.ReactiveSortingRepository;

import java.util.UUID;

public interface MyFileRepository extends ReactiveSortingRepository<Video, UUID> {

}
