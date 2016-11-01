package process;

import javax.media.DataSink;
import javax.media.Format;
import javax.media.Manager;
import javax.media.MediaLocator;
import javax.media.NoDataSinkException;
import javax.media.NoProcessorException;
import javax.media.Processor;
import javax.media.control.TrackControl;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.DataSource;
import javax.media.protocol.FileTypeDescriptor;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class VideoGenerator {

    private List<String> imgList;
    private File tempDir;
    private Dimension ds;
    String vFileName;
    String aFileName;

    public VideoGenerator(Dimension dimension) {
        imgList = new ArrayList<String>();
        tempDir = new File("tempDir");
        this.ds = dimension;
        readImageFiles();
    }

    public void generate() throws IOException, NoProcessorException, NoDataSinkException, InterruptedException {
        makeMovie((int) ds.getWidth(), (int) ds.getHeight(), 2.2f, imgList);
    }

    private void readImageFiles() {
        File[] fileLst = tempDir.listFiles();
        if (fileLst == null) {
            return;
        }
        for (File aFileLst : fileLst) {
            imgList.add(aFileLst.getAbsolutePath());

        }
    }

    private void makeMovie(int width, int height, float frameRate, List<String> imgFiles) throws IOException, NoProcessorException, NoDataSinkException, InterruptedException {
        String videoFileName = System.currentTimeMillis() + ".mov";
        File out = new File(videoFileName);

        MediaLocator oml = new MediaLocator(out.toURI().toURL());
        ImageDataSource ids = new ImageDataSource(width, height, frameRate, imgFiles);
        Processor processor = Manager.createProcessor(ids);
        StateHelper statehelper = new StateHelper(processor);
        if (!statehelper.configure(10000)) {
            System.exit(100);
        }

        processor.setContentDescriptor(new ContentDescriptor(FileTypeDescriptor.QUICKTIME));

        TrackControl tcs[] = processor.getTrackControls();
        Format f[] = tcs[0].getSupportedFormats();
        if (f == null || f.length <= 0) {
            System.exit(100);
        }

        tcs[0].setFormat(f[0]);
        if (!statehelper.realize(10000)) {
            System.exit(100);
        }

        DataSource source = processor.getDataOutput();
        DataSink filewriter = Manager.createDataSink(source, oml);
        filewriter.open();

        processor.start();
        filewriter.start();


        statehelper.waitToEndOfMedia(5000);
        statehelper.close();
        filewriter.close();

    }

}
