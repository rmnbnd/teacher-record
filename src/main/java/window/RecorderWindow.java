package window;

import process.Recorder;

import javax.swing.*;
import java.awt.*;
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

        RecordListener() throws AWTException {
            this.recorder = new Recorder();
        }

        public void actionPerformed(ActionEvent e) {
            if ("Record".equals(record.getText())) {
                record.setText("Stop Record");
                recorder.startRecord();
            } else if ("Stop Record".equals(record.getText())) {
                recorder.stopRecord();
            }
        }

    }
}
