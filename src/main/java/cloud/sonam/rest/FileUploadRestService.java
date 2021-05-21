package cloud.sonam.rest;

import cloud.sonam.service.FileUploadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/file")
public class FileUploadRestService {

    private static final Logger LOG = LoggerFactory.getLogger(FileUploadRestService.class);

    @Autowired
    private FileUploadService fileUploadService;

    @PostMapping(value="/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<String> upload( @RequestPart("file") Mono<FilePart> filePartFlux) {
        LOG.info("upload got called");
       return fileUploadService.save(filePartFlux);
    }
}
