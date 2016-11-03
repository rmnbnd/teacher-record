package window;

import process.record.Recorder;
import process.record.VideoGenerator;
import process.upload.YoutubeVideoUpload;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.AWTException;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RecorderWindow extends JFrame {

    private JButton record;
    private JButton upload;

    public RecorderWindow() throws AWTException {
        // Add main panel
        JPanel panel = new JPanel();

        // Add record button
        record = new JButton("Record");
        record.addActionListener(new RecordListener());
        panel.add(record);

        // Add upload button
        upload = new JButton("Upload");
        upload.addActionListener(new UploadListener());
        panel.add(upload);

        this.getContentPane().add(panel);

        // Setup window
        this.pack();
        this.setVisible(true);
    }

    private class RecordListener implements ActionListener {

        private Recorder recorder;
        private VideoGenerator videoGenerator;

        RecordListener() throws AWTException {
            this.recorder = new Recorder(Toolkit.getDefaultToolkit().getScreenSize());
            this.videoGenerator = new VideoGenerator(getToolkit().getScreenSize());
        }

        public void actionPerformed(ActionEvent e) {
            if ("Record".equals(record.getText())) {
                record.setText("Stop Record");
                recorder.startRecord();
            } else if ("Stop Record".equals(record.getText())) {
                recorder.stopRecord();

                record.setText("Generating video");
                videoGenerator.generate();

                record.setText("Record");
            }
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
