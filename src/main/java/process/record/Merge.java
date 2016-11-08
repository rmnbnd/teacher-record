package process.record;

import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.DataSink;
import javax.media.EndOfMediaEvent;
import javax.media.Format;
import javax.media.Manager;
import javax.media.MediaLocator;
import javax.media.Processor;
import javax.media.ProcessorModel;
import javax.media.datasink.DataSinkErrorEvent;
import javax.media.datasink.DataSinkEvent;
import javax.media.datasink.DataSinkListener;
import javax.media.datasink.EndOfStreamEvent;
import javax.media.format.AudioFormat;
import javax.media.format.VideoFormat;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.DataSource;
import javax.media.protocol.FileTypeDescriptor;
import java.util.ArrayList;
import java.util.List;


class Merge implements ControllerListener, DataSinkListener {

    private List<String> sourcesURLs = new ArrayList<>();
    private String outputFile = null;
    private String videoEncoding = "JPEG";
    private String audioEncoding = "LINEAR";
    private DataSource merger = null;
    private DataSource outputDataSource;
    private Processor outputProcessor;
    private DataSink outputDataSink;
    private boolean done = false;

    private VideoFormat videoFormat = null;
    private AudioFormat audioFormat = null;

    void merge(String videoFileName, String audioFileName) {
        sourcesURLs.add(videoFileName);
        sourcesURLs.add(audioFileName);
        doMerge();
    }

    private void doMerge() {
        Processor[] processors = new Processor[sourcesURLs.size()];
        DataSource[] dataOutputs = new DataSource[sourcesURLs.size()];

        for (int i = 0; i < sourcesURLs.size(); i++) {
            String source = sourcesURLs.get(i);
            MediaLocator ml = new MediaLocator(source);
            ProcessorModel pm = new MyPM(ml);
            try {
                processors[i] = Manager.createRealizedProcessor(pm);
                dataOutputs[i] = processors[i].getDataOutput();
                processors[i].start();
            } catch (Exception e) {
                System.err.println("Failed to create a processor: " + e);
                System.exit(-1);
            }
        }

        // Merge the data sources from the individual processors
        try {
            merger = Manager.createMergingDataSource(dataOutputs);
            merger.connect();
            merger.start();
        } catch (Exception ex) {
            System.err.println("Failed to merge data sources: " + ex);
            System.exit(-1);
        }
        if (merger == null) {
            System.err.println("Failed to merge data sources");
            System.exit(-1);
        }

        // Create the output processor
        ProcessorModel outputPM = new MyPMOut(merger);

        try {
            outputProcessor = Manager.createRealizedProcessor(outputPM);
            outputDataSource = outputProcessor.getDataOutput();
        } catch (Exception exc) {
            System.err.println("Failed to create output processor: " + exc);
            System.exit(-1);
        }

        try {
            MediaLocator outputLocator = new MediaLocator(outputFile);
            outputDataSink = Manager.createDataSink(outputDataSource,
                    outputLocator);
            outputDataSink.open();
        } catch (Exception exce) {
            System.err.println("Failed to create output DataSink: " + exce);
            System.exit(-1);
        }

        outputProcessor.addControllerListener(this);
        outputDataSink.addDataSinkListener(this);
        System.err.println("Merging...");
        try {
            outputDataSink.start();
            outputProcessor.start();
        } catch (Exception excep) {
            System.err.println("Failed to start file writing: " + excep);
            System.exit(-1);
        }
        int count = 0;

        while (!done) {
            try {
                Thread.currentThread().sleep(100);
            } catch (InterruptedException ie) {
            }

            if (outputProcessor != null &&
                    (int) (outputProcessor.getMediaTime().getSeconds()) > count) {
                System.err.print(".");
                count = (int) (outputProcessor.getMediaTime().getSeconds());
            }

        }

        if (outputDataSink != null) {
            outputDataSink.close();
        }
        synchronized (this) {
            if (outputProcessor != null) {
                outputProcessor.close();
            }
        }
        System.err.println("Done!");
    }

    public void controllerUpdate(ControllerEvent ce) {
        if (ce instanceof EndOfMediaEvent) {
            synchronized (this) {
                outputProcessor.close();
                outputProcessor = null;
            }
        }
    }

    public void dataSinkUpdate(DataSinkEvent dse) {
        if (dse instanceof EndOfStreamEvent) {
            done = true;
        } else if (dse instanceof DataSinkErrorEvent) {
            done = true;
        }
    }

    private class MyPM extends ProcessorModel {

        MediaLocator inputLocator;

        MyPM(MediaLocator inputLocator) {
            this.inputLocator = inputLocator;
        }

        public ContentDescriptor getContentDescriptor() {
            return new ContentDescriptor(ContentDescriptor.RAW);
        }

        public DataSource getInputDataSource() {
            return null;
        }

        public MediaLocator getInputLocator() {
            return inputLocator;
        }

        public Format getOutputTrackFormat(int index) {
            return null;
        }

        public int getTrackCount(int n) {
            return n;
        }

        public boolean isFormatAcceptable(int index, Format format) {
            if (videoFormat == null) {
                videoFormat = new VideoFormat(videoEncoding);
            }
            if (audioFormat == null) {
                audioFormat = new AudioFormat(audioEncoding);
            }
            return format.matches(videoFormat) || format.matches(audioFormat);
        }
    }

    private class MyPMOut extends ProcessorModel {

        DataSource inputDataSource;

        MyPMOut(DataSource inputDataSource) {
            this.inputDataSource = inputDataSource;
        }

        public ContentDescriptor getContentDescriptor() {
            String outputType = FileTypeDescriptor.QUICKTIME;
            return new FileTypeDescriptor(outputType);
        }

        public DataSource getInputDataSource() {
            return inputDataSource;
        }

        public MediaLocator getInputLocator() {
            return null;
        }

        public Format getOutputTrackFormat(int index) {
            return null;
        }

        public int getTrackCount(int n) {
            return n;
        }

        public boolean isFormatAcceptable(int index, Format format) {
            if (videoFormat == null) {
                videoFormat = new VideoFormat(videoEncoding);
            }
            if (audioFormat == null) {
                audioFormat = new AudioFormat(audioEncoding);
            }
            return format.matches(videoFormat) || format.matches(audioFormat);
        }
    }

}
