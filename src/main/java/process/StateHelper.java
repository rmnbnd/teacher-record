package process;

import javax.media.ConfigureCompleteEvent;
import javax.media.ControllerClosedEvent;
import javax.media.ControllerErrorEvent;
import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.EndOfMediaEvent;
import javax.media.PrefetchCompleteEvent;
import javax.media.Processor;
import javax.media.RealizeCompleteEvent;

class StateHelper implements ControllerListener {

    private Processor processor = null;
    private boolean configured = false;
    private boolean realized = false;
    private boolean prefetched = false;
    private boolean eom = false;
    private boolean failed = false;
    private boolean closed = false;

    public StateHelper(Processor p) {
        processor = p;
        processor.addControllerListener(this);
    }

    public void turnOffRecording() {
        eom = true;
    }

    public boolean configure(int timeOutMillis) {
        long startTime = System.currentTimeMillis();
        synchronized (this) {
            processor.configure();

            while (!configured && !failed) {
                try {
                    wait(timeOutMillis);
                } catch (InterruptedException ie) {
                }
                if (System.currentTimeMillis() - startTime > timeOutMillis)
                    break;
            }

        }
        return configured;
    }

    public boolean realize(int timeOutMillis) {
        long startTime = System.currentTimeMillis();
        synchronized (this) {
            processor.realize();
            while (!realized && !failed) {
                try {
                    wait(timeOutMillis);
                } catch (InterruptedException ie) {
                }
                if (System.currentTimeMillis() - startTime > timeOutMillis)
                    break;
            }
        }
        return realized;
    }

    public boolean prefetch(int timeOutMillis) {
        long startTime = System.currentTimeMillis();
        synchronized (this) {
            processor.prefetch();
            while (!prefetched && !failed) {
                try {
                    wait(timeOutMillis);
                } catch (InterruptedException ie) {
                }
                if (System.currentTimeMillis() - startTime > timeOutMillis)
                    break;
            }
        }
        return prefetched && !failed;
    }

    public boolean waitToEndOfMedia(int timeOutMillis) {
        long startTime = System.currentTimeMillis();
        eom = false;
        synchronized (this) {
            while (!eom && !failed) {
                try {
                    wait(timeOutMillis);
                } catch (InterruptedException ie) {
                }
            }
        }
        return eom && !failed;
    }

    public boolean waitToEndOfMedia(int timeOutMillis, boolean isForMerge) {
        long startTime = System.currentTimeMillis();
        eom = false;
        synchronized (this) {
            while (!eom && !failed) {
                try {
                    wait(timeOutMillis);
                } catch (InterruptedException ie) {
                }
                if (System.currentTimeMillis() - startTime > timeOutMillis)
                    break;
            }
        }
        return eom && !failed;
    }

    public void close() {
        synchronized (this) {
            processor.close();
            while (!closed) {
                try {
                    wait(100);
                } catch (InterruptedException ie) {
                }
            }
        }
        processor.removeControllerListener(this);
    }

    public synchronized void controllerUpdate(ControllerEvent ce) {
        if (ce instanceof RealizeCompleteEvent) {
            realized = true;
        } else if (ce instanceof ConfigureCompleteEvent) {
            configured = true;
        } else if (ce instanceof PrefetchCompleteEvent) {
            prefetched = true;
        } else if (ce instanceof EndOfMediaEvent) {
            eom = true;
        } else if (ce instanceof ControllerErrorEvent) {
            failed = true;
        } else if (ce instanceof ControllerClosedEvent) {
            closed = true;
        } else {
            return;
        }
        notifyAll();
    }


}
