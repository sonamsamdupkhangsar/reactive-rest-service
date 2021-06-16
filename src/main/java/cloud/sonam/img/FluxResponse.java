package cloud.sonam.img;

import cloud.sonam.rest.VideoDownload;
import reactor.core.publisher.Flux;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

public class FluxResponse {

        public final CompletableFuture<FluxResponse> cf = new CompletableFuture<>();
        public GetObjectResponse sdkResponse;
        public Flux<ByteBuffer> flux;
    }
