package process;

import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.Processor;

class StateHelper implements ControllerListener {

    private Processor processor = null;
    private boolean failed = false;
    private boolean configured = false;
    private boolean realized = false;
    private boolean closed = false;

    StateHelper(Processor p) {
        processor = p;
        processor.addControllerListener(this);
    }

    @Override
    public void controllerUpdate(ControllerEvent controllerEvent) {

    }

    boolean configure(int timeOutMillis) throws InterruptedException {
        long startTime = System.currentTimeMillis();
        synchronized (this) {
            processor.configure();

            while (!configured && !failed) {
                wait(timeOutMillis);
                if (System.currentTimeMillis() - startTime > timeOutMillis) {
                    break;
                }
            }

        }
        return configured;
    }

    boolean waitToEndOfMedia(int timeOutMillis) throws InterruptedException {
        boolean eom = false;
        synchronized (this) {
            while (!eom && !failed) {
                wait(timeOutMillis);
            }
        }
        return eom && !failed;
    }

    boolean realize(int timeOutMillis) throws InterruptedException {
        long startTime = System.currentTimeMillis();
        synchronized (this) {
            processor.realize();
            while (!realized && !failed) {
                wait(timeOutMillis);
                if (System.currentTimeMillis() - startTime > timeOutMillis) {
                    break;
                }
            }
        }
        return realized;
    }

    void close() throws InterruptedException {
        synchronized (this) {
            processor.close();
            while (!closed) {
                wait(100);
            }
        }
        processor.removeControllerListener(this);
    }


}
