package window;

import process.record.Recorder;
import process.record.VideoGenerator;
import process.upload.YoutubeVideoUpload;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.AWTException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static java.awt.Toolkit.getDefaultToolkit;

public class RecorderWindow extends JFrame {

    private JButton startRecord;
    private JButton stopRecord;
    private JButton upload;

    private Recorder recorder;
    private VideoGenerator videoGenerator;

    public RecorderWindow() throws AWTException {
        this.recorder = new Recorder(getDefaultToolkit().getScreenSize());
        this.videoGenerator = new VideoGenerator(getDefaultToolkit().getScreenSize());


        // Add main panel
        JPanel panel = new JPanel();

        // Add startRecord button
        startRecord = new JButton("Start Record");
        startRecord.addActionListener(new StartRecordListener());
        panel.add(startRecord);

        // Add stopRecord button
        stopRecord = new JButton("Stop Record");
        stopRecord.addActionListener(new StopRecordListener());
        stopRecord.setEnabled(false);
        panel.add(stopRecord);

        // Add upload button
        upload = new JButton("Upload");
        upload.addActionListener(new UploadListener());
        upload.setEnabled(false);
        panel.add(upload);

        this.getContentPane().add(panel);

        // Setup window
        this.pack();
        this.setVisible(true);
    }

    private class StartRecordListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            startRecord.setEnabled(false);
            stopRecord.setEnabled(true);

            recorder.startRecord();
        }

    }

    private class StopRecordListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            recorder.stopRecord();
            videoGenerator.generate();

            stopRecord.setEnabled(false);
            startRecord.setEnabled(true);
        }

    }

    private class UploadListener implements ActionListener {

        private YoutubeVideoUpload youtubeVideoUpload;

        UploadListener() {
            this.youtubeVideoUpload = new YoutubeVideoUpload();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            youtubeVideoUpload.upload();
        }

    }
}
