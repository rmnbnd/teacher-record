package process;

import javax.media.Time;
import javax.media.protocol.PullBufferDataSource;
import javax.media.protocol.PullBufferStream;
import java.io.IOException;
import java.util.List;

class ImageDataSource extends PullBufferDataSource {

    private ImageSourceStream streams[];

    ImageDataSource(int width, int height, float frameRate, List<String> images) {
        streams = new ImageSourceStream[1];
        streams[0] = new ImageSourceStream(width, height, frameRate, images);
    }

    @Override
    public PullBufferStream[] getStreams() {
        return new PullBufferStream[0];
    }

    @Override
    public String getContentType() {
        return null;
    }

    @Override
    public void connect() throws IOException {

    }

    @Override
    public void disconnect() {

    }

    @Override
    public void start() throws IOException {

    }

    @Override
    public void stop() throws IOException {

    }

    @Override
    public Object getControl(String s) {
        return null;
    }

    @Override
    public Object[] getControls() {
        return new Object[0];
    }

    @Override
    public Time getDuration() {
        return null;
    }
}
