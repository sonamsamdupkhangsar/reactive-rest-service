package cloud.sonam.service.impl;

import cloud.sonam.config.S3ClientConfigurationProperties;
import cloud.sonam.db.entity.Video;
import cloud.sonam.db.repo.VideoRepository;
import cloud.sonam.exception.DownloadFailedException;
import cloud.sonam.img.FluxResponse;
import cloud.sonam.img.FluxResponseProvider;
import cloud.sonam.img.UploadFailedException;
import cloud.sonam.img.UploadResult;
import cloud.sonam.rest.VideoDownload;
import com.fasterxml.jackson.core.util.ByteArrayBuilder;
import com.madgag.gif.fmsware.AnimatedGifEncoder;
//import com.sun.prism.impl.BufferUtil;
import net.coobird.thumbnailator.Thumbnails;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.BodyExtractor;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.BodyInserter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.netty.ByteBufFlux;
import software.amazon.awssdk.core.SdkResponse;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.http.SdkHttpResponse;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class VideoLoaderService /*implements VideoServiceInterface */{
    private static final Logger LOG = LoggerFactory.getLogger(VideoLoaderService.class);

    @Autowired
    private VideoRepository videoRepository;

    @Value("file:/Users/ssamdupk/Documents/bitbucket/reactive-rest-service/videofiles")
    private Resource resource;

    @Value("${videoPath}")
    private String videoPath;

    @Value("${imageAclHeader}")
    private String imageAclHeader;

    @Value("${imageAclValue}")
    private String imageAclValue;

    @Autowired
    private S3AsyncClient s3client;

    @Autowired
    private S3ClientConfigurationProperties s3config;


    public VideoLoaderService() {
    }

   // @Override
    public Flux<Video> getVideo() {
        return videoRepository.findAll();
    }

    public Mono<ResponseEntity<UploadResult>> save(@RequestHeader HttpHeaders headers,
                             @RequestBody Flux<ByteBuffer> body) {
        LocalDateTime localDateTime = LocalDateTime.now();
        String extension = "";

        final String fileName = headers.getFirst("filename");
        if (headers.getFirst("filename").contains(".")) {
            extension = headers.getFirst("filename").substring(headers.getFirst("filename").lastIndexOf(".") + 1);
        }
        LOG.info("header.filename: {}", headers.getFirst("filename"));

        String fileKey = videoPath+"video/"+localDateTime + "." + extension;

        LOG.info("accessKeyId: {}, secretAccessKey: {}, endpoint: {}, region: {}, bucket: {}",
                s3config.getAccessKeyId(), s3config.getSecretAccessKey(),
                s3config.getEndpoint(), s3config.getRegion(), s3config.getBucket());
        long length = headers.getContentLength();
        LOG.info("length: {}", length);


        Map<String, String> metadata = new HashMap<>();
        metadata.put("Content-Length", ""+length);
        metadata.put("Content-Type", headers.getFirst("format"));
        metadata.put("x-amz-acl", "public-read");

        MediaType mediaType = headers.getContentType();

        LOG.info("mediaType: {}", mediaType.getType());

        CompletableFuture future = s3client
                .putObject(PutObjectRequest.builder()
                                .bucket(s3config.getBucket())
                                .contentLength(length)
                                .key(fileKey)
                                .contentType(headers.getFirst("format"))
                                .metadata(metadata)
                                .acl(ObjectCannedACL.PUBLIC_READ)
                                .build(),
                        AsyncRequestBody.fromPublisher(body));

        LOG.info("checking future");

        return Mono.fromFuture(future)
                .map(response -> {
                    checkResult(response);

                    //https://sonam.sfo2.digitaloceanspaces.com/videoapp/1/video/2021-06-16T12:11:46.036369.mp4
                    ByteArrayOutputStream baos = createThumbnail(fileKey, "png");
                    byte[] bytes = baos.toByteArray();
                    ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
                    Flux<ByteBuffer> byteBufferFlux = Flux.just(byteBuffer);
                    Mono<ByteBuffer> mono = Mono.just(byteBuffer);

                    String thumbKey = videoPath+"thumbnail/"+localDateTime + "." + "png";
                    Map<String, String> metadata2 = new HashMap<>();
                    metadata2.put("Content-Length", ""+bytes.length);
                    metadata2.put("Content-Type", "image/png");
                    metadata2.put("x-amz-acl", "public-read");


                    CompletableFuture future2 =  s3client
                            .putObject(PutObjectRequest.builder()
                                            .bucket(s3config.getBucket())
                                            .contentLength((long)bytes.length)
                                            .key(thumbKey)
                                            .contentType("image/png")
                                            .metadata(metadata2)
                                            .acl(ObjectCannedACL.PUBLIC_READ)
                                            .build(),
                                    AsyncRequestBody.fromPublisher(byteBufferFlux));

                    save(fileName, fileKey, thumbKey);

                    return ResponseEntity
                            .status(HttpStatus.CREATED)
                            .body(new UploadResult(HttpStatus.CREATED, new String[] {fileKey}));
                });
    }

    private Mono<Video> save(String fileName, String fileKey, String thumbKey) {
        Video video = new Video(fileName, fileKey, thumbKey);
        var videoMono = videoRepository.save(video);
        LOG.info("saved video: {}", videoMono);
        videoMono.log();
        return videoMono;
    }

    // Helper used to check return codes from an API call
    private static void checkResult(GetObjectResponse response) {
        SdkHttpResponse sdkResponse = response.sdkHttpResponse();
        if ( sdkResponse != null && sdkResponse.isSuccessful()) {
            return;
        }

        throw new DownloadFailedException(response);
    }



    private PutObjectResponse checkResult(Object result1) {
        PutObjectResponse result = (PutObjectResponse) result1;
        LOG.info("response.sdkHttpResponse: {}", result.sdkHttpResponse().isSuccessful());

        if (result.sdkHttpResponse() == null || !result.sdkHttpResponse().isSuccessful()) {
            LOG.error("response is un successful");
            throw new UploadFailedException(result);
        }
        return result;
    }

    /**
     * Multipart file upload
     * @param
     * @param parts
     * @param headers
     * @return
     */
   // @RequestMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, method = {RequestMethod.POST, RequestMethod.PUT})
    public Mono<ResponseEntity<UploadResult>> multipartUploadHandler( HttpHeaders headers, Flux<Part> parts  ) {

        return parts
                .ofType(FilePart.class) // We'll ignore other data for now
                .flatMap((part) -> saveFile(headers, s3config.getBucket(), part))
                .collect(Collectors.toList())
                .map((keys) -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(new UploadResult(HttpStatus.CREATED,keys)));
    }


    /**
     * Save file using a multipart upload. This method does not require any temporary
     * storage at the REST service
     * @param headers
     * @param bucket Bucket name
     * @param part Uploaded file
     * @return
     */
    protected Mono<String> saveFile(HttpHeaders headers,String bucket, FilePart part) {

        // Generate a filekey for this upload
        String filekey = UUID.randomUUID().toString();

        LOG.info("[I137] saveFile: filekey={}, filename={}", filekey, part.filename());

        // Gather metadata
        Map<String, String> metadata = new HashMap<>();
        String filename = part.filename();
        if ( filename == null ) {
            filename = filekey;
        }

        metadata.put("filename", filename);

        MediaType mt = part.headers().getContentType();
        if ( mt == null ) {
            mt = MediaType.APPLICATION_OCTET_STREAM;
        }

        // Create multipart upload request
        CompletableFuture<CreateMultipartUploadResponse> uploadRequest = s3client
                .createMultipartUpload(CreateMultipartUploadRequest.builder()
                        .contentType(mt.toString())
                        .key(filekey)
                        .metadata(metadata)
                        .bucket(bucket)
                        .build());

        // This variable will hold the upload state that we must keep
        // around until all uploads complete
        final UploadState uploadState = new UploadState(bucket,filekey);

        return Mono
                .fromFuture(uploadRequest)
                .flatMapMany((response) -> {
                    checkResult(response);
                    uploadState.uploadId = response.uploadId();
                    LOG.info("[I183] uploadId={}", response.uploadId());
                    return part.content();
                })
                .bufferUntil((buffer) -> {
                    uploadState.buffered += buffer.readableByteCount();
                    if ( uploadState.buffered >= s3config.getMultipartMinPartSize() ) {
                        LOG.info("[I173] bufferUntil: returning true, bufferedBytes={}, partCounter={}, uploadId={}", uploadState.buffered, uploadState.partCounter, uploadState.uploadId);
                        uploadState.buffered = 0;
                        return true;
                    }
                    else {
                        return false;
                    }
                })
                .map((buffers) -> concatBuffers(buffers))
                .flatMap((buffer) -> uploadPart(uploadState,buffer))
                .onBackpressureBuffer()
                .reduce(uploadState,(state,completedPart) -> {
                    LOG.info("[I188] completed: partNumber={}, etag={}", completedPart.partNumber(), completedPart.eTag());
                    state.completedParts.put(completedPart.partNumber(), completedPart);
                    return state;
                })
                .flatMap((state) -> completeUpload(state))
                .map((response) -> {
                    checkResult(response);
                    return  uploadState.filekey;
                });
    }

    private static ByteBuffer concatBuffers(List<DataBuffer> buffers) {
        LOG.info("[I198] creating BytBuffer from {} chunks", buffers.size());

        int partSize = 0;
        for( DataBuffer b : buffers) {
            partSize += b.readableByteCount();
        }

        ByteBuffer partData = ByteBuffer.allocate(partSize);
        buffers.forEach((buffer) -> {
            partData.put(buffer.asByteBuffer());
        });

        // Reset read pointer to first byte
        partData.rewind();

        LOG.info("[I208] partData: size={}", partData.capacity());
        return partData;

    }

    /**
     * Upload a single file part to the requested bucket
     * @param uploadState
     * @param buffer
     * @return
     */
    private Mono<CompletedPart> uploadPart(UploadState uploadState, ByteBuffer buffer) {
        final int partNumber = ++uploadState.partCounter;
        LOG.info("[I218] uploadPart: partNumber={}, contentLength={}",partNumber, buffer.capacity());

        CompletableFuture<UploadPartResponse> request = s3client.uploadPart(UploadPartRequest.builder()
                        .bucket(uploadState.bucket)
                        .key(uploadState.filekey)
                        .partNumber(partNumber)
                        .uploadId(uploadState.uploadId)
                        .contentLength((long) buffer.capacity())
                        .build(),
                AsyncRequestBody.fromPublisher(Mono.just(buffer)));

        return Mono
                .fromFuture(request)
                .map((uploadPartResult) -> {
                    checkResult(uploadPartResult);
                    LOG.info("[I230] uploadPart complete: part={}, etag={}",partNumber,uploadPartResult.eTag());
                    return CompletedPart.builder()
                            .eTag(uploadPartResult.eTag())
                            .partNumber(partNumber)
                            .build();
                });
    }

    private Mono<CompleteMultipartUploadResponse> completeUpload(UploadState state) {
        LOG.info("[I202] completeUpload: bucket={}, filekey={}, completedParts.size={}", state.bucket, state.filekey, state.completedParts.size());

        CompletedMultipartUpload multipartUpload = CompletedMultipartUpload.builder()
                .parts(state.completedParts.values())
                .build();

        return Mono.fromFuture(s3client.completeMultipartUpload(CompleteMultipartUploadRequest.builder()
                .bucket(state.bucket)
                .uploadId(state.uploadId)
                .multipartUpload(multipartUpload)
                .key(state.filekey)
                .build()));
    }



    /**
     * Holds upload state during a multipart upload
     */
    static class UploadState {
        final String bucket;
        final String filekey;

        String uploadId;
        int partCounter;
        Map<Integer, CompletedPart> completedParts = new HashMap<>();
        int buffered = 0;

        UploadState(String bucket, String filekey) {
            this.bucket = bucket;
            this.filekey = filekey;
        }
    }

    class SpaceObj {
        String thumbnailFullPath;
        String videoFullPath;
        LocalDateTime localDateTime;
        PipedInputStream pipedInputStream;
        String name;

        public SpaceObj(String name, PipedInputStream pipedInputStream, String videoFullPath, LocalDateTime localDateTime) {
            this.name = name;
            this.pipedInputStream = pipedInputStream;
            this.videoFullPath = videoFullPath;
            this.localDateTime = localDateTime;
        }
    }

    private byte[] getBytes(DataBuffer dataBuffer) {
        byte[] bytes = new byte[dataBuffer.readableByteCount()];
        dataBuffer.read(bytes);
        DataBufferUtils.release(dataBuffer);
        return bytes;
    }

    /*
        public Mono<String> saveRefactor(Mono<FilePart> filePartMono) {
            return filePartMono.flatMap(fp -> {
               return fp.content().map(dataBuffer -> getBytes(dataBuffer))
                       .then(Mono.just(dataB));

            }).map(o -> oh);

        }
    */
    class FileStream {
        PipedOutputStream pipedOutputStream;
        PipedInputStream pipedInputStream;
        String fileName;
        int size;

        public FileStream(String fileName, PipedOutputStream pipedOutputStream, PipedInputStream pipedInputStream, int size) {
            this.fileName = fileName;
            this.pipedOutputStream = pipedOutputStream;
            this.pipedInputStream = pipedInputStream;
            this.size = size;
        }

        @Override
        public String toString() {
            return "FileStream{" +
                    "pipedOutputStream=" + pipedOutputStream +
                    ", pipedInputStream=" + pipedInputStream +
                    ", fileName='" + fileName + '\'' +
                    ", size=" + size +
                    '}';
        }
    }

   /* @Override
    public Mono<String> save(Mono<FilePart> filePartMono, int contentLength) {
        return filePartMono
                .flatMap(fp -> {
                    LocalDateTime localDateTime = LocalDateTime.now();
                    String extension = "";

                    if (fp.filename().contains(".")) {
                        extension = fp.filename().substring(fp.filename().lastIndexOf(".") + 1);
                    }

                    String keyName = videoPath+"video/"+localDateTime + "." + extension;

                    // Initiate the multipart upload.
                    InitiateMultipartUploadRequest initRequest = new InitiateMultipartUploadRequest(bucketName, keyName);
                    InitiateMultipartUploadResult initResponse = s3Client.initiateMultipartUpload(initRequest);
                    long filePosition = 0;


                    for (int i = 1; filePosition < contentLength; i++) {
                        // Because the last part could be less than 5 MB, adjust the part size as needed.
                      //  partSize = Math.min(partSize, (contentLength - filePosition));

                        // Create the request to upload a part.


                        //filePosition += partSize;
                    }
*/

                    /*try {
                        File file = File.createTempFile("tmp", fp.filename());
                        fp.transferTo(file).
                                doOnSuccess(unused -> LOG.info("file written: {}", file))
                                .thenReturn(file);
                    } catch (IOException e) {
                        LOG.error("failed to create temp file", e);
                    }
                    return null;

                })

                           try {
                                Flux<DataBuffer> dataBufferFlux = fp.content();

                               // dataBufferFlux.map(dataBuffer -> getBytes(dataBuffer));
                                PipedOutputStream osPipe = new PipedOutputStream();
                                PipedInputStream isPipe = new PipedInputStream(osPipe);
                                Runnable runnable1 = new Runnable() {
                                    @Override
                                    public void run() {
                                        LOG.info("done writing to PipedOutputstream");
                                    }
                                };

                                DataBufferUtils.write(dataBufferFlux, osPipe).doOnComplete(runnable1)
                                        .subscribe(DataBufferUtils.releaseConsumer());
                                var fs = new FileStream(fp.filename(), osPipe, isPipe, isPipe.available());
                                LOG.info("fileStream: {}", fs);
                                return Mono.just(fs);
                            } catch (Exception e) {
                                LOG.error("error occured", e);
                            }
                            return null;
                        }).map(fs -> {
                            LocalDateTime localDateTime = LocalDateTime.now();
                            String extension = "";

                            if (fs.fileName.contains(".")) {
                                extension = fs.fileName.substring(fs.fileName.lastIndexOf(".") + 1);
                            }
                            String videoFullPath = videoPath+"video/"+localDateTime + "." + extension;

                                saveToSpaces(videoFullPath, fs.pipedInputStream, fs.size, "video/mp4");

                            var spaceObj = new SpaceObj(fs.fileName, fs.pipedInputStream, videoFullPath, localDateTime);
                            return spaceObj;
                     }).map(spaceObj -> {
                         LOG.info("create thumbnail");
                        var localDateTime = spaceObj.localDateTime;

                        ByteArrayOutputStream byteArrayOutputStream = createThumbnail(spaceObj.pipedInputStream, "png");
                        String thumbnailFullpath =  videoPath+"thumbnail/"+localDateTime+".png";
                        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
                        saveToSpaces(thumbnailFullpath, byteArrayInputStream, byteArrayOutputStream.size(), "image/png");
                        spaceObj.thumbnailFullPath = thumbnailFullpath;
                        return spaceObj;
                    }).flatMap(spaceObj -> {
                        Video video = new Video(spaceObj.name, spaceObj.videoFullPath, spaceObj.thumbnailFullPath);
                        var videoMono = videoRepository.save(video);
                        return videoMono;
                    }).map(video -> {return Mono.just(video.getId());})
                    .then(Mono.just(""));*/
    //  }

   /* @Override
    public Mono<String> save(Mono<FilePart> filePartMono) {

        return filePartMono
                .flatMap(fp -> {
                    try {
                        Flux<DataBuffer> dataBufferFlux = fp.content();
                        dataBufferFlux.map(dataBuffer -> getBytes(dataBuffer));
                        PipedOutputStream osPipe = new PipedOutputStream();
                        PipedInputStream isPipe = new PipedInputStream(osPipe);
                        DataBufferUtils.write(dataBufferFlux, osPipe).subscribe(DataBufferUtils.releaseConsumer());

                    }
                    catch (Exception e) {
                        LOG.error("error occured", e);
                    }
                        return fp.content().map(dataBuffer -> {
                            LOG.info("readable count: {}", dataBuffer.readableByteCount());
                            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

                            byte[] bytes = new byte[dataBuffer.readableByteCount()];
                            dataBuffer.read(bytes);
                        DataBufferUtils.release(dataBuffer);

                        LOG.info("bytes.length: {}", bytes.length);
                        return bytes;
                    }).map(bytes -> {
                        LocalDateTime localDateTime = LocalDateTime.now();
                        String extension = "";
                        LOG.info("bytes.length: {}", bytes.length);
                        if (fp.filename().contains(".")) {
                            extension = fp.filename().substring(fp.filename().lastIndexOf(".") + 1);
                        }
                        String videoFullPath = videoPath+"video/"+localDateTime + "." + extension;

                        saveToSpaces(videoFullPath, bytes, "video/mp4");

                        var spaceObj = new SpaceObj(fp.filename(), bytes, videoFullPath, localDateTime);
                        return spaceObj;
                        
                    }).map(spaceObj -> {
                        byte[] bytes = spaceObj.bytes;
                        var localDateTime = spaceObj.localDateTime;

                          ByteArrayOutputStream byteArrayOutputStream = createThumbnail(new ByteArrayInputStream(bytes), "png");
                          String thumbnailFullpath =  videoPath+"thumbnail/"+localDateTime+".png";
                          ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
                          saveToSpaces(thumbnailFullpath, byteArrayInputStream, byteArrayOutputStream.size(), "image/png");
                          spaceObj.thumbnailFullPath = thumbnailFullpath;
                         return spaceObj;
                      }).flatMap(spaceObj -> {
                           Video video = new Video(spaceObj.name, spaceObj.videoFullPath, spaceObj.thumbnailFullPath);
                            return videoRepository.save(video);
                       }).map(video -> Mono.just(video.getId()))
                        .then(Mono.just("I want to return that id from line above"));

                      //  return Mono.just("hello");
                });


    }*/

   /* private String saveToSpaces(String filePathName, byte[] bytes, String format) {
        String key = spacesVideoSaver.save(bytes, format, filePathName);
        return key;
    }*/

   /* private String saveToSpaces(String filePathName, InputStream inputStream, int streamLength, String format) {
        String key = spacesVideoSaver.save(inputStream, streamLength, format, filePathName);
        return key;
    }*/


    public ByteArrayOutputStream createThumbnail(String fileKey, String imageFormat) {
 /*       GetObjectRequest request = GetObjectRequest.builder()
                .bucket(s3config.getBucket())
                .key(fileKey)
                .build();
        Mono.fromFuture(s3client.getObject(request,new FluxResponseProvider()))
                .map( (response) -> {
                    checkResult(response.sdkResponse);

                    String filename = getMetadataItem(response.sdkResponse,"filename",fileKey);

                    LOG.info("[I65] filename={}, length={}",filename, response.sdkResponse.contentLength() );
                    response.flux.map(byteBuffer -> ByteArrayInputStream::new)
                    return ResponseEntity.ok()
                            .header(HttpHeaders.CONTENT_TYPE, response.sdkResponse.contentType())
                            .header(HttpHeaders.CONTENT_LENGTH, Long.toString(response.sdkResponse.contentLength()))
                            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                            .body(response.flux);
                });
*/


                    try {

                        LOG.info("fileKey: {}, endpoint: {}", fileKey, s3config.getSubdomain());
                        InputStream inputStream = new URL(s3config.getSubdomain()+fileKey).openStream();

            LOG.info("imageFormat: {}", imageFormat);

            FFmpegFrameGrabber frameGrabber = new FFmpegFrameGrabber(inputStream);
            frameGrabber.start();
            Java2DFrameConverter fc = new Java2DFrameConverter();
            Frame frame = frameGrabber.grabKeyFrame();
            LOG.info("frame: {}", frame);

            BufferedImage bufferedImage = fc.convert(frame);
            LOG.info("bufferedImage: {}", bufferedImage);

            int i = 0;
            while (bufferedImage != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(bufferedImage, imageFormat, baos);

                frame = frameGrabber.grabKeyFrame();
                bufferedImage = fc.convert(frame);

                frameGrabber.stop();
                frameGrabber.close();

                LOG.info("i: {}, bytearray.length: {}", i++, baos.toByteArray().length);
                return baos;

            }
            frameGrabber.stop();
            frameGrabber.close();

            LOG.info("thumbnail done");
        } catch (Exception e) {
            LOG.error("failed to create thumbnail for video", e);
        }
        return null;

    }

    private String getMetadataItem(GetObjectResponse sdkResponse, String key, String defaultValue) {
        for( Map.Entry<String, String> entry : sdkResponse.metadata().entrySet()) {
            if ( entry.getKey().equalsIgnoreCase(key)) {
                return entry.getValue();
            }
        }
        return defaultValue;
    }


  /*                   LOG.info("creating animated gif from video");
                        File gif = new File(gifFolder, timeStamp+".gif");
                        createGifFromVideo(file, 5, 50, 10, 2, gif);
                    })*/

    private void createGifFromVideo(File inFile, int startFrame, int frameCount, Integer frameRate, Integer margin, File outFile) {
        try {
            final String imageFormat = "gif";

            LOG.info("videoFile: {}\n, gif path: {}\n, imageFormat: {}",
                    inFile.getAbsolutePath(), outFile.getAbsolutePath(), imageFormat);

            FileOutputStream targetFile = new FileOutputStream(outFile);
            FFmpegFrameGrabber frameGrabber = new FFmpegFrameGrabber(inFile);
            frameGrabber.start();
            Java2DFrameConverter fc = new Java2DFrameConverter();

            Integer videoLength = frameGrabber.getLengthInFrames();
            // If the user uploads the video to be extremely short and does not meet the value interval defined by the user, the acquisition starts at 1/5 and ends at 1/2
            if (startFrame > videoLength || (startFrame + frameCount) > videoLength) {
                startFrame = videoLength / 5;
                frameCount = videoLength / 2;
            }
            LOG.info("startFrame: {}, frameRate: {}", startFrame, frameRate);

            frameGrabber.setFrameNumber(startFrame);
            AnimatedGifEncoder en = new AnimatedGifEncoder();
            en.setFrameRate(frameRate);
            en.start(targetFile);

            for (int i = 0; i < frameCount; i++) {
                Frame frame = frameGrabber.grabFrame(false, true, true, false);
                LOG.info("frame: {}", frame);

                BufferedImage bufferedImage = fc.convert(frame);
                LOG.info("bufferedImage: {}", bufferedImage);
                while (bufferedImage != null) {
                    en.addFrame(bufferedImage);
                    frameGrabber.setFrameNumber(frameGrabber.getFrameNumber() + margin);
                }
            }
            en.finish();


            frameGrabber.stop();
            frameGrabber.close();

        } catch (Exception e) {
            LOG.error("failed to create gif for video", e);
        }
    }

    private static void createFromImageFile(File inputFile, File outputPath) {
        try {
            Thumbnails.of(inputFile)
                    .size(500, 500)
                    .toFile(outputPath);
            LOG.info("created thumbnail to path: {}", outputPath.getAbsolutePath());
        } catch (IOException e) {
            LOG.error("failed to create thumbnail", e);
        }
    }
/*
    public Mono<String> save3(Mono<FilePart> filePartMono) {
        LOG.info("save3 called");
        counter++;
        String timeStamp = LocalDateTime.now().toString();

         return filePartMono
                .flatMap(fp -> {
                    java.io.File file = new java.io.File(videoFolder, timeStamp + "_" + fp.filename());

                     return fp.transferTo(file)//.thenReturn(file.getName())

                             .thenReturn(file.getName());
                        //.then(createVideo(file, timeStamp));
                });


    }*/
   /* private Mono<Video> createVideo(UUID uuid, InputStream inputStream, String fileName) {
        LOG.info("file has been transferred: {}", inputStream);

        var thumbnailFile = new File(thumbnailFolder, uuid.toString() + ".png");
        //File file = new File(videoFolder, fileName);

        VideoLoaderService.createTnForVideo(inputStream, thumbnailFile, "png");
        final String thumbnailPath = "thumbnail/"+thumbnailFile.getName();

        Video video = new Video(uuid.toString(), "video/"+file.getName(), thumbnailPath);

        Mono<Video> videoMono = videoRepository.save(video);
        videoMono.doOnNext(video1 ->
        {
            LOG.info("video1 is {}",video1);
        }).subscribe();

        LOG.info("saved video: {}", videoMono);

        return videoMono;
    }*/

   /* private Mono<Video> createVideo(UUID uuid, InputStream inputStream, String fileName) {
        LOG.info("file has been transferred: {}");

        var thumbnailFile = new File(thumbnailFolder, uuid.toString() + ".png");
        //File file = new File(videoFolder, fileName);

        ByteArrayOutputStream baos = createTnForVideo(inputStream, "png");
        String pathKey = saveBytes(baos.toByteArray(), fileName, "video/mp4");
        //final String thumbnailPath = "thumbnail/"+thumbnailFile.getName();

        Video video = new Video(uuid.toString(), f, thumbnailPath);

        Mono<Video> videoMono = videoRepository.save(video);
        videoMono.doOnNext(video1 ->
        {
            LOG.info("video1 is {}",video1);
        }).subscribe();

        LOG.info("saved video: {}", videoMono);

        return videoMono;
    }*/
}