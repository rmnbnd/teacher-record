package window;

import process.record.Recorder;
import process.record.VideoGenerator;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import static java.awt.Toolkit.getDefaultToolkit;

public class RecorderWindow extends JFrame {

    private JButton startRecord;
    private JButton stopRecord;

    private Recorder recorder;
    private VideoGenerator videoGenerator;

    public RecorderWindow() throws AWTException {
        this.recorder = new Recorder(getDefaultToolkit().getScreenSize());
        this.videoGenerator = new VideoGenerator(getDefaultToolkit().getScreenSize());


        // Add main panel
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        // Add startRecord button
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        startRecord = new JButton("Start Record");
        startRecord.addActionListener(new StartRecordListener());
        panel.add(startRecord, c);

        // Add stopRecord button
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.gridy = 0;
        stopRecord = new JButton("Stop Record");
        stopRecord.addActionListener(new StopRecordListener());
        stopRecord.setEnabled(false);
        panel.add(stopRecord, c);

        // Add records list
        renderRecordsList(panel, c);

        // Add scroll page for all content
        JScrollPane pane = new JScrollPane();
        pane.setSize(new Dimension(200, 200));
        pane.setViewportView(panel);

        this.getContentPane().add(pane);

        // Setup window
        this.pack();
        this.setVisible(true);
    }

    private void renderRecordsList(JPanel panel, GridBagConstraints c) {
        File f = new File("records");
        File[] records = f.listFiles();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = 2;
        c.gridx = 0;
        if (records == null) {
            return;
        }
        for (File record : records) {
            c.gridy = ++c.gridy;

            JButton button = new JButton("Upload");

            JLabel label = new JLabel(record.getName());
            JPanel jPanel = new JPanel();
            jPanel.add(label);
            jPanel.add(button);
            panel.add(jPanel, c);
        }
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

}
