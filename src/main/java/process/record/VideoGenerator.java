package process.record;

import javax.media.ConfigureCompleteEvent;
import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.DataSink;
import javax.media.EndOfMediaEvent;
import javax.media.Format;
import javax.media.Manager;
import javax.media.MediaLocator;
import javax.media.PrefetchCompleteEvent;
import javax.media.Processor;
import javax.media.RealizeCompleteEvent;
import javax.media.ResourceUnavailableEvent;
import javax.media.control.TrackControl;
import javax.media.datasink.DataSinkErrorEvent;
import javax.media.datasink.DataSinkEvent;
import javax.media.datasink.DataSinkListener;
import javax.media.datasink.EndOfStreamEvent;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.DataSource;
import javax.media.protocol.FileTypeDescriptor;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class VideoGenerator implements ControllerListener, DataSinkListener {

    private static final String STORE = "tmp";
    private static final int FRAME_RATE = 2;

    private boolean fileDone = false;
    private boolean fileSuccess = true;
    private boolean stateTransitionOK = true;
    private Object waitSync = new Object();
    private Object waitFileSync = new Object();

    private int width;
    private int height;

    public VideoGenerator(Dimension dimension) {
        this.width = (int) dimension.getWidth();
        this.height = (int) dimension.getHeight();
    }

    public void generate() {
        makeVideo(readImageFiles());

        deleteImageFiles();
    }

    private void makeVideo(List<String> imageFiles) {
        String videoFileName = "./records" + System.currentTimeMillis() + ".mov";
        MediaLocator oml = createMediaLocator(videoFileName);

        ImageDataSource ids = new ImageDataSource(width, height, FRAME_RATE, imageFiles);
        Processor processor;
        try {
            processor = Manager.createProcessor(ids);
        } catch (Exception e) {
            System.err.println("Cannot create a processor from the data source.");
            return;
        }
        processor.addControllerListener(this);
        processor.configure();
        if (!waitForState(processor, Processor.Configured)) {
            System.err.println("Failed to configure the processor.");
            return;
        }
        processor.setContentDescriptor(new ContentDescriptor(FileTypeDescriptor.QUICKTIME));

        TrackControl tcs[] = processor.getTrackControls();
        Format f[] = tcs[0].getSupportedFormats();
        if (f == null || f.length <= 0) {
            System.err.println("The mux does not support the input format: " + tcs[0].getFormat());
            return;
        }
        tcs[0].setFormat(f[0]);

        processor.realize();
        if (!waitForState(processor, Processor.Realized)) {
            System.err.println("Failed to realize the processor.");
            return;
        }

        DataSink dsink = createDataSink(processor, oml);
        if (dsink == null) {
            System.err.println("Failed to create a DataSink for the given output MediaLocator.");
            return;
        }
        dsink.addDataSinkListener(this);
        fileDone = false;

        try {
            processor.start();
            dsink.start();
        } catch (IOException e) {
            System.err.println("IO error during processing");
            return;
        }

        waitForFileDone();

        try {
            dsink.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        processor.removeControllerListener(this);
    }

    @Override
    public void controllerUpdate(ControllerEvent controllerEvent) {
        if (controllerEvent instanceof ConfigureCompleteEvent
                || controllerEvent instanceof RealizeCompleteEvent
                || controllerEvent instanceof PrefetchCompleteEvent) {
            synchronized (waitSync) {
                stateTransitionOK = true;
                waitSync.notifyAll();
            }
        } else if (controllerEvent instanceof ResourceUnavailableEvent) {
            synchronized (waitSync) {
                stateTransitionOK = false;
                waitSync.notifyAll();
            }
        } else if (controllerEvent instanceof EndOfMediaEvent) {
            controllerEvent.getSourceController().stop();
            controllerEvent.getSourceController().close();
        }
    }

    @Override
    public void dataSinkUpdate(DataSinkEvent dataSinkEvent) {
        if (dataSinkEvent instanceof EndOfStreamEvent) {
            synchronized (waitFileSync) {
                fileDone = true;
                waitFileSync.notifyAll();
            }
        } else if (dataSinkEvent instanceof DataSinkErrorEvent) {
            synchronized (waitFileSync) {
                fileDone = true;
                fileSuccess = false;
                waitFileSync.notifyAll();
            }
        }
    }

    private List<String> readImageFiles() {
        File f = new File(STORE);
        File[] fileLst = f.listFiles();
        if (fileLst == null) {
            return new ArrayList<>();
        }
        return Arrays.stream(fileLst)
                .map(File::getAbsolutePath)
                .sorted()
                .collect(Collectors.toList());
    }

    private void deleteImageFiles() {
        File f = new File(STORE);
        File[] files = f.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            file.delete();
        }
    }

    private MediaLocator createMediaLocator(String url) {
        MediaLocator ml;

        if (url.indexOf(":") > 0 && (ml = new MediaLocator(url)) != null)
            return ml;
        if (url.startsWith(File.separator)) {
            if ((ml = new MediaLocator("file:" + url)) != null)
                return ml;
        } else {
            String file = "file:" + System.getProperty("user.dir")
                    + File.separator + url;
            if ((ml = new MediaLocator(file)) != null)
                return ml;
        }
        return null;
    }

    private boolean waitForState(Processor p, int state) {
        synchronized (waitSync) {
            try {
                while (p.getState() < state && stateTransitionOK)
                    waitSync.wait();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return stateTransitionOK;
    }

    private DataSink createDataSink(Processor p, MediaLocator outML) {
        DataSource ds = p.getDataOutput();
        if (ds == null) {
            System.err.println("Something is really wrong: the processor does not have an output DataSource");
            return null;
        }

        DataSink dsink;
        try {
            dsink = Manager.createDataSink(ds, outML);
            dsink.open();
        } catch (Exception e) {
            System.err.println("Cannot create the DataSink: " + e);
            return null;
        }
        return dsink;
    }

    private boolean waitForFileDone() {
        synchronized (waitFileSync) {
            try {
                while (!fileDone)
                    waitFileSync.wait();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return fileSuccess;
    }

}
