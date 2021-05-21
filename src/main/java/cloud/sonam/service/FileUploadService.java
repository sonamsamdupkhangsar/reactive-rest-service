package cloud.sonam.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.File;

@Service
public class FileUploadService {
    private static final Logger LOG = LoggerFactory.getLogger(FileUploadService.class);

    public Mono<String> save(Mono<FilePart> filePartMono) {
        Mono<String> monoString = filePartMono.flatMap(filePart -> {
            File file = new File(filePart.filename());
            if (file.exists()) {
                file.delete();
                LOG.info("existing file deleted: {}", file.getAbsolutePath());
            }
            Mono<Void> mono = filePart.transferTo(file);
            LOG.info("file saved: {}", file.getAbsolutePath());
            return Mono.just(file.getAbsolutePath());
        }).thenReturn("hello");
        return monoString;
    }
}
