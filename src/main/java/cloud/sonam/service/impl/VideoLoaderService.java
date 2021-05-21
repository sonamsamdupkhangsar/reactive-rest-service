package cloud.sonam.service.impl;

import cloud.sonam.db.entity.Video;
import cloud.sonam.db.repo.VideoRepository;
import cloud.sonam.service.VideoServiceInterface;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
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
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.function.Function;

import net.coobird.thumbnailator.Thumbnails;
import com.madgag.gif.fmsware.AnimatedGifEncoder;

@Service
public class VideoLoaderService implements VideoServiceInterface {
    private static final Logger LOG = LoggerFactory.getLogger(VideoLoaderService.class);

    @Autowired
    private VideoRepository videoRepository;

    @Value("file:/Users/ssamdupk/Documents/bitbucket/reactive-rest-service/videofiles")
    private Resource resource;

    private String video = "video";
    private String thumbnail = "thumbnail";
    private String gif = "gif";

    private File videoFolder;
    private File thumbnailFolder;
    private File gifFolder;

    private File folder;

    @PostConstruct
    public void createFolder() throws IOException {
        LOG.info("public resource: {}", resource);
        if (resource.getFile().exists()) {
            LOG.info("public resource exists");
            folder = resource.getFile();
        } else {
            LOG.error("public resource folder does not exist");
        }
        File file = resource.getFile();
        videoFolder = new File(file, video);
        thumbnailFolder = new File(file, thumbnail);
        gifFolder = new File(file, gif);
    }

    @Override
    public Flux<Video> getVideo() {
        return videoRepository.findAll();
    }

    int counter = 1;


    @Override
    public Mono<String> save(Mono<FilePart> filePartMono) {
        UUID uuid = UUID.randomUUID();

        return filePartMono
                .flatMap(fp -> {
                    String extension = "";
                    if (fp.filename().contains(".")) {
                        extension = fp.filename().substring(fp.filename().lastIndexOf(".") + 1);
                    }
                    java.io.File file = new java.io.File(videoFolder, uuid.toString() + "_" + extension);

                    return fp.transferTo(file)
                            .doOnSuccess(file2 -> createVideo(uuid, file))
                            .thenReturn(uuid.toString());
                });
    }

    /*                   LOG.info("creating animated gif from video");
                        File gif = new File(gifFolder, timeStamp+".gif");
                        createGifFromVideo(file, 5, 50, 10, 2, gif);
                    })*/







    public static void createTnForVideo(File inFile, File outFile, String imageFormat) {
        try {
            LOG.info("videoFile: {}\n, thumbnail path: {}\n, imageFormat: {}",
                    inFile.getAbsolutePath(), outFile.getAbsolutePath(), imageFormat);

            FFmpegFrameGrabber frameGrabber = new FFmpegFrameGrabber(inFile);
            frameGrabber.start();
            Java2DFrameConverter fc = new Java2DFrameConverter();
            Frame frame = frameGrabber.grabKeyFrame();
            LOG.info("frame: {}", frame);

            BufferedImage bufferedImage = fc.convert(frame);
            LOG.info("bufferedImage: {}", bufferedImage);

            int i = 0;
            while (bufferedImage != null) {
                ImageIO.write(bufferedImage, imageFormat, outFile);

                frame = frameGrabber.grabKeyFrame();
                bufferedImage = fc.convert(frame);
                LOG.info("i: {}", i++);
                break;
            }
            frameGrabber.stop();
            frameGrabber.close();

        } catch (Exception e) {
            LOG.error("failed to create thumbnail for video", e);
        }
    }


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


    }

    private Mono<Video> createVideo(UUID uuid, File file) {
        LOG.info("file has been transferred: {}", file);

        var thumbnailFile = new File(thumbnailFolder, uuid.toString() + ".png");
        //File file = new File(videoFolder, fileName);

        VideoLoaderService.createTnForVideo(file, thumbnailFile, "png");
        final String thumbnailPath = "thumbnail/"+thumbnailFile.getName();

        Video video = new Video(uuid.toString(), "video/"+file.getName(), thumbnailPath);

        Mono<Video> videoMono = videoRepository.save(video);
        videoMono.doOnNext(video1 ->
        {
            LOG.info("video1 is {}",video1);
        }).subscribe();

        LOG.info("saved video: {}", videoMono);

        return videoMono;
    }
}