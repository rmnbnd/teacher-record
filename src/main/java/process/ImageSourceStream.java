package process;

import javax.media.Buffer;
import javax.media.Format;
import javax.media.format.VideoFormat;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.PullBufferStream;
import java.awt.Dimension;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;

class ImageSourceStream implements PullBufferStream {

    private List<String> images;
    private VideoFormat format;

    private int nextImage = 0;
    private boolean ended = false;

    ImageSourceStream(int width, int height, float frameRate, List<String> images) {
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
        nextImage++;

        RandomAccessFile raFile = new RandomAccessFile(imageFile, "r");
        byte data[] = null;

        if (buf.getData() instanceof byte[])
            data = (byte[]) buf.getData();

        if (data == null || data.length < raFile.length()) {
            data = new byte[(int) raFile.length()];
            buf.setData(data);
        }

        raFile.readFully(data, 0, (int) raFile.length());

        buf.setOffset(0);
        buf.setLength((int) raFile.length());
        buf.setFormat(format);
        buf.setFlags(buf.getFlags() | buf.FLAG_KEY_FRAME);

        raFile.close();
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
        return ended;
    }

    public Object[] getControls() {
        return new Object[0];
    }

    public Object getControl(String type) {
        return null;
    }
}
