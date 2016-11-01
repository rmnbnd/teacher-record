package process;

import javax.media.Buffer;
import javax.media.Format;
import javax.media.format.VideoFormat;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.PullBufferStream;
import java.awt.Dimension;
import java.io.IOException;
import java.util.List;

class ImageSourceStream implements PullBufferStream {

    private int width;
    private int height;
    private List<String> images;
    private VideoFormat format;

    ImageSourceStream(int width, int height, float frameRate, List<String> images) {
        this.width = width;
        this.height = height;
        this.images = images;

        format = new VideoFormat(VideoFormat.JPEG, new Dimension(width,
                height), Format.NOT_SPECIFIED, Format.byteArray,
                frameRate);
    }


    @Override
    public boolean willReadBlock() {
        return false;
    }

    @Override
    public void read(Buffer buffer) throws IOException {

    }

    @Override
    public Format getFormat() {
        return null;
    }

    @Override
    public ContentDescriptor getContentDescriptor() {
        return null;
    }

    @Override
    public long getContentLength() {
        return 0;
    }

    @Override
    public boolean endOfStream() {
        return false;
    }

    @Override
    public Object[] getControls() {
        return new Object[0];
    }

    @Override
    public Object getControl(String s) {
        return null;
    }
}
