package process;

import javax.imageio.ImageIO;
import javax.media.Buffer;
import javax.media.Format;
import javax.media.format.VideoFormat;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.PullBufferStream;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

class ImageSourceStream implements PullBufferStream {

    private List<String> images;
    private int width, height;
    private VideoFormat format;

    private int nextImage = 0;
    private boolean ended = false;

    ImageSourceStream(int width, int height, float frameRate, List<String> images) {
        this.width = width;
        this.height = height;
        this.images = images;

        format = new VideoFormat(VideoFormat.JPEG, new Dimension(width,
                height), Format.NOT_SPECIFIED, Format.byteArray,
                frameRate);
    }


    public boolean willReadBlock() {
        return false;
    }

    public void read(Buffer buf) throws IOException {

        if (nextImage >= images.size()) {
            buf.setEOM(true);
            buf.setOffset(0);
            buf.setLength(0);
            ended = true;
            return;
        }

        String imageFile = images.get(nextImage);
        File fnew = new File(imageFile);
        BufferedImage originalImage = ImageIO.read(fnew);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(originalImage, "jpg", baos);
        byte[] imageInByte = baos.toByteArray();
        buf.setOffset(0);
        buf.setLength(imageInByte.length);
        buf.setFormat(format);
        buf.setFlags(buf.getFlags() | buf.FLAG_KEY_FRAME);
        buf.setData(imageInByte);

        nextImage++;
    }

    public Format getFormat() {
        return format;
    }

    public ContentDescriptor getContentDescriptor() {
        return new ContentDescriptor(ContentDescriptor.RAW);
    }

    public long getContentLength() {
        return 0;
    }

    public boolean endOfStream() {
        return true;
    }

    public Object[] getControls() {
        return null;
    }

    public Object getControl(String type) {
        return null;
    }
}
