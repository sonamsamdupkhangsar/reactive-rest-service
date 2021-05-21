package cloud.sonam.img;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.opencv.opencv_core.IplImage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

public class VideoThumbnail {

    protected String ffmpegApp;

    public VideoThumbnail(String ffmpegApp)
    {
        this.ffmpegApp = ffmpegApp;
    }


}