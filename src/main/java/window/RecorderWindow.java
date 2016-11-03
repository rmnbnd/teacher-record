package window;

import process.Recorder;
import process.VideoGenerator;

import javax.swing.JButton;
import javax.swing.JFrame;
import java.awt.AWTException;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RecorderWindow extends JFrame {

    private JButton record;

    public RecorderWindow() throws AWTException {
        // Add record button
        record = new JButton("Record");
        record.addActionListener(new RecordListener());
        this.getContentPane().add(record);

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
}
