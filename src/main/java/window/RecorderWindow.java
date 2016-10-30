package window;

import window.listeners.RecordListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import java.awt.AWTException;

public class RecorderWindow extends JFrame {

    public RecorderWindow() throws AWTException {
        // Add record button
        JButton control = new JButton("Record");
        control.addActionListener(new RecordListener());
        this.getContentPane().add(control);

        // Setup window
        this.pack();
        this.setVisible(true);
    }
}
