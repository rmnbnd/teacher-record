package process;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.JFrame;
import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Recorder extends JFrame {

    private Boolean started;
    private Robot robot;

    public Recorder() throws AWTException {
        this.robot = new Robot();
    }

    public void startRecord() {
        Thread tScreenCapture = new Thread() {
            Dimension ds = getToolkit().getScreenSize();

            public void run() {
                started = true;
                File tempDir = new File("tempDir");
                if (!tempDir.exists())
                    tempDir.mkdir();
                try {
                    while (started) {
                        BufferedImage bi = robot.createScreenCapture(new Rectangle(ds));
                        makeCompression(bi, tempDir);
                        Thread.sleep(200);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
        tScreenCapture.start();
    }

    private void makeCompression(BufferedImage bi, File tempDir) throws IOException {
        ImageWriter imgWriter = ImageIO.getImageWritersByFormatName("jpg").next();

        ImageOutputStream imgOutStrm =
                ImageIO.createImageOutputStream(new File(tempDir.getAbsolutePath()
                        + "/" + System.currentTimeMillis() + ".jpg"));
        imgWriter.setOutput(imgOutStrm);

        IIOImage iioImg = new IIOImage(bi, null, null);
        ImageWriteParam jpgWriterParam = imgWriter.getDefaultWriteParam();
        jpgWriterParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        jpgWriterParam.setCompressionQuality(0.7f);
        imgWriter.write(null, iioImg, jpgWriterParam);

        imgOutStrm.close();
        imgWriter.dispose();
    }

}
