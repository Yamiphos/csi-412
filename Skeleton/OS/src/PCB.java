import java.util.LinkedList;

public class PCB { // Process Control Block
    private static int nextPid = 1;
    public int pid;
    private OS.PriorityType priority;
    private UserlandProcess process;
    public long wakeuptime; // for wake-up time
    private int timesQuantumExpired = 0; // to check how many times it ran to the end of the 250ms
    private int[] processData = new int[10]; // Array of 10 integers
    private String name; // Name of process
    private LinkedList<KernelMessage> messageQueue; // Message queue for IPC
    private boolean waitingFlag;
    //private int[] pageTable = new int[100]; // Page table: virtual page → physical page
    public VirtualToPhysicalMapping[] pageTable = new VirtualToPhysicalMapping[100]; //new page table

    PCB(UserlandProcess up, OS.PriorityType priority) {
        this.process = up;
        this.priority = priority;
        this.pid = nextPid++;
        this.name = up.getClass().getSimpleName();
        this.messageQueue = new LinkedList<>(); // Initialize message queue
        this.waitingFlag = false;

        for (int i = 0; i < pageTable.length; i++) {
            pageTable[i] = new VirtualToPhysicalMapping(); // no mappings initially
        }

        // Initialize array with -1
        for (int i = 0; i < processData.length; i++) {
            processData[i] = -1;
        }
    }

    public String getName() {
        return name;
    }

    OS.PriorityType getPriority() {
        return priority;
    }

    public void requestStop() {
        process.requestStop();
    }

    public void stop() {
        // Calls userlandprocess’ stop. Loops with Thread.sleep() until ulp.isStopped() is true.
        process.stop();
        while (!process.isStopped()) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                // Do nothing
            }
        }
    }

    public boolean isDone() {
        // Calls userlandprocess’ isDone()
        return process.isDone();
    }

    void start() {
        // Calls userlandprocess’ start()
        process.start();
    }

    public void setPriority(OS.PriorityType newPriority) {
        priority = newPriority;
    }

    // Getter and setter for Quantum times
    public int getTimesQuantumExpired() {
        return timesQuantumExpired;
    }

    public void resetTimesQuantumExpired() {
        timesQuantumExpired = 0;
    }

    public void increaseTimesQuantumExpired() {
        timesQuantumExpired++;
    }

    public int[] getProcessData() {
        return processData;
    }

    // ** Message Queue Methods ** 

    public void addMessage(KernelMessage message) {
       if(messageQueue.add(message)) {
           //System.out.println("added a message to: "+name);
       }
       else{
           //System.out.println("could not add a message: "+name);
       }
    }

    public KernelMessage retrieveMessage() {
        //System.out.println("we removed a message from: "+name);
        return messageQueue.poll(); // Retrieves and removes the first message
    }

    public boolean hasMessages() {
        return !messageQueue.isEmpty();
    }

    //waiting flag methods

    public void resetWaitingFlag(){
        waitingFlag = false;
    }

    public void setWaitingFlag(){
        waitingFlag = true;
    }

    public boolean checkWaitingFlag(){
        return waitingFlag;
    }

    public VirtualToPhysicalMapping[] getPageTable() {
        return pageTable;

    }
}
