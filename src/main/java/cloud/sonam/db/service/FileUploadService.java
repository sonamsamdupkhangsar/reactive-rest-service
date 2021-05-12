package cloud.sonam.db.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;

@Service
public class FileUploadService {
    private static final Logger LOG = LoggerFactory.getLogger(FileUploadService.class);

    public Mono<String> save(Mono<FilePart> filePartMono) {
        LOG.info("file part mono service");
        filePartMono.flatMap(filePart -> {
            File file = new File(filePart.filename());
            if (file.exists()) {
                file.delete();
                LOG.info("existing file deleted: {}", file.getAbsolutePath());
            }
            Mono<Void> mono = filePart.transferTo(file);
            LOG.info("file upload done: {}", file.getAbsolutePath());
           return Mono.just("file upload done: "+ file.getAbsolutePath());
        }).subscribe();

        return Mono.just("returning after subscribe");
    }
}
