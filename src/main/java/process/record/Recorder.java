package process.record;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.File;

public class Recorder extends JFrame {

    private static final String STORE = "tmp";
    private static int CAPTURE_INTERVAL = 1;

    private static boolean record = false;

    private int width;
    private int height;

    private SoundRecorder soundRecorder;

    public Recorder(Dimension dimension) throws AWTException {
        this.width = (int) dimension.getWidth();
        this.height = (int) dimension.getHeight();
        this.soundRecorder = new SoundRecorder();
    }

    public void startRecord() {
        checkStore();

        Thread recordThread = new Thread() {
            public void run() {
                Robot robot;
                int cnt = 0;
                try {
                    robot = new Robot();
                    while (cnt == 0 || record) {
                        BufferedImage img = robot.createScreenCapture(new Rectangle(width, height));
                        ImageIO.write(img, "jpeg", new File("./" + STORE + "/" + System.currentTimeMillis() + ".jpeg"));
                        if (cnt == 0) {
                            record = true;
                            cnt = 1;
                        }
                        Thread.sleep(CAPTURE_INTERVAL);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        Thread audioRecordThread = new Thread() {
            public void run() {
                soundRecorder.start();
            }
        };

        audioRecordThread.start();
        recordThread.start();
    }

    public void stopRecord() {
        soundRecorder.finish();
        record = false;
    }

    private void checkStore() {
        File f = new File(STORE);
        if (!f.exists()) {
            f.mkdir();
        }
    }

}
