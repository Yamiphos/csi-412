import java.io.IOException;
import java.util.concurrent.Semaphore;

public abstract class Process implements Runnable{
    private Thread thread;
    private Semaphore semaphore;
    private volatile Boolean quantumExpired;
    public Process() {
        this.thread =  new Thread(this);
        this.semaphore = new Semaphore(0);
        this.quantumExpired = false;
        this.thread.start();
    }

    public void requestStop() {
        quantumExpired = true;
    }

    public abstract void main() throws IOException, InterruptedException; //interrupted exception added for sleep in scheduler wait for message

    public boolean isStopped() {
        return semaphore.availablePermits() == 0;
    }

    public boolean isDone() {
        return !thread.isAlive();
    }

    public void start() {
        semaphore.release();
        //System.out.println("permits:" + semaphore.availablePermits());
    }

    public void stop() {
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void run() { // This is called by the Thread - NEVER CALL THIS!!!
        try {
            semaphore.acquire(); // Block the thread
            main();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void cooperate() {
        if(quantumExpired){
            quantumExpired = false;
            OS.switchProcess();
        }
    }
}
