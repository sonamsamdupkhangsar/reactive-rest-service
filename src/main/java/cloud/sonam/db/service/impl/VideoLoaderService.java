package cloud.sonam.db.service.impl;

import cloud.sonam.db.entity.Video;
import cloud.sonam.db.repo.MyFileRepository;
import cloud.sonam.db.service.VideoServiceInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;

@Service
public class VideoLoaderService implements VideoServiceInterface {
    private static final Logger LOG = LoggerFactory.getLogger(VideoLoaderService.class);

    @Autowired
    private MyFileRepository myFileRepository;

    @Value("file:/Users/ssamdupk/Documents/bitbucket/reactive-rest-service/videofiles")
    private Resource resource;

    private File folder;

    @PostConstruct
    public void createFolder() throws IOException {
        LOG.info("public resource: {}", resource);
        if (resource.getFile().exists()) {
            LOG.info("public resource exists");
            folder = resource.getFile();
        }
        else {
            LOG.error("public resource folder does not exist");
        }
    }

    @Override
    public Flux<Video> getVideo() {
        return myFileRepository.findAll();
    }

    @Override
    public Mono<String> save(Mono<FilePart> filePartMono) {
        return filePartMono.flatMap(filePart -> {
            java.io.File file = new java.io.File(folder, LocalDateTime.now() + "_"+filePart.filename());
            if (file.exists()) {
                file.delete();
                LOG.info("existing file deleted: {}", file.getAbsolutePath());
            }
            Mono<Void> mono = filePart.transferTo(file);
            var myFile = new Video(file.getName());
            Mono<Video> myFileMono = myFileRepository.save(myFile);
            myFileMono.subscribe(System.out::println);
            LOG.info("file saved");

            return filePart.transferTo(file)
                    .doOnNext(v -> {
                        LOG.info("file saved: {}", file.getAbsolutePath());
                    }).thenReturn(file.getAbsolutePath());
        });
    }
}
