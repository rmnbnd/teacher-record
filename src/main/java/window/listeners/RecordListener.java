package window.listeners;

import process.Recorder;

import java.awt.AWTException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RecordListener implements ActionListener {

    private Recorder recorder;

    public RecordListener() throws AWTException {
        this.recorder = new Recorder();
    }

    public void actionPerformed(ActionEvent e) {
        recorder.startRecord();
    }

}
