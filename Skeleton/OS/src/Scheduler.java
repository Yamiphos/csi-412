import java.util.*;
import java.time.Clock;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.ThreadPoolExecutor;

public class Scheduler {
    private final Timer timer = new Timer(true);
    public PCB currentlyRunning = null;
    private final ConcurrentLinkedQueue<PCB> sleepingProcessQueue = new ConcurrentLinkedQueue<>();
    private final LinkedList<PCB> realtimeProcessQueue = new LinkedList<>();
    private final LinkedList<PCB> interactiveProcessQueue = new LinkedList<>();
    private final LinkedList<PCB> backgroundProcessQueue = new LinkedList<>();
    private final Random random = new Random();
    private static HashMap<Integer, PCB> processMap = new HashMap<>(); //<pid, PCB>
    private static HashMap<Integer, PCB> waitingProcesses = new HashMap<>();//<pid, PCB>
    private static final int PAGE_SIZE = 1024;//paging stuff
    private static final int TOTAL_PAGES = 1024; //paging stuff
    private static final boolean[] physicalPageUsed = new boolean[TOTAL_PAGES]; //paging stuff

    public Scheduler() {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (currentlyRunning != null) {
                    currentlyRunning.requestStop();
                }
                //wakeUpProcesses();
            }
        }, 0, 250);
    }

    public int CreateProcess(UserlandProcess up, OS.PriorityType p) {
        PCB Newprocess = new PCB(up, p);
        addToPriorityQueue(Newprocess);
        if (currentlyRunning == null) {
            SwitchProcess();
        }
        processMap.put(Newprocess.pid, Newprocess);
        return Newprocess.pid;


    }

    public void SwitchProcess() {
        //System.out.println("current call: "+OS.currentCall);
        Hardware.flushTLB(); //clean the tlb
        wakeUpProcesses();
        if(currentlyRunning != null){
            checkAndDemoteProcess(currentlyRunning);
        }

        // Place the currently running process back into its correct queue
        if (currentlyRunning != null && !currentlyRunning.isDone()) {
            addToPriorityQueue(currentlyRunning);
        }

        // Select the next process to run based on priority probabilities
        currentlyRunning = selectNextProcess();
        //System.out.println("picked next process to run : " + currentlyRunning.getName());

        if(currentlyRunning.checkWaitingFlag()){
            OS.retVal = currentlyRunning.retrieveMessage();
        }
    }

    public void Sleep(int milliseconds) {
        //null check just in case
        if (currentlyRunning == null) return;
        //calculate wake up time
        currentlyRunning.wakeuptime = Clock.systemUTC().millis() + milliseconds;
        sleepingProcessQueue.add(currentlyRunning);
        removeFromRunnableQueue(currentlyRunning);

        //set currently running to null so it doesnt get put on any active queue
        SwitchProcess();
        //System.out.println("should not print hopefully!");
    }

    private void wakeUpProcesses() {
        long currentTime = Clock.systemUTC().millis();
        var iterator = sleepingProcessQueue.iterator();
        while (iterator.hasNext()) {
            PCB process = iterator.next();
            if (process.wakeuptime <= currentTime) {
                addToPriorityQueue(process);
                iterator.remove();
            }
        }
    }

    private void addToPriorityQueue(PCB process) {
        OS.PriorityType priority = process.getPriority();
        switch (priority) {
            case realtime -> realtimeProcessQueue.addLast(process);
            case interactive -> interactiveProcessQueue.addLast(process);
            case background -> backgroundProcessQueue.addLast(process);
        }
    }

    private void removeFromRunnableQueue(PCB process){
        OS.PriorityType priority = process.getPriority();
        switch (priority) {
            case realtime -> realtimeProcessQueue.remove(process);
            case interactive -> interactiveProcessQueue.remove(process);
            case background -> backgroundProcessQueue.remove(process);
        }
    }

    private PCB selectNextProcess() {
        //if there are real time processes
        if (!realtimeProcessQueue.isEmpty()) {
            //System.out.println("this shouldn't print");
            int roll = random.nextInt(10); // Random number from 0-9
            if (roll < 6) return realtimeProcessQueue.poll();
            else if (roll < 9 && !interactiveProcessQueue.isEmpty()) return interactiveProcessQueue.poll();
            else if (!backgroundProcessQueue.isEmpty()) return backgroundProcessQueue.poll();
        }

        //if there is no real time processes
        if (!interactiveProcessQueue.isEmpty()) {
            //System.out.println("this should print but eventually stop");
            //System.out.println("this should print and not stop because of helloworld");
            int roll = random.nextInt(4); //number from 0-3
            if(roll<3) return interactiveProcessQueue.poll();
            else{ return backgroundProcessQueue.poll();} //it rolled a background process
        }


        //testing
//        else{ //interactive and realtime are empty
//            //System.out.println("this should print after demotion of hello and goodbye");
//            //System.out.println("this shouldn't print because helloworld will always be IN");
//        }
        //if there is nothing else, we cant return before this
        return backgroundProcessQueue.poll();
    }


    private void checkAndDemoteProcess(PCB process) {
        if (process.getTimesQuantumExpired() >= 5) {
            // Demote the process
            if (process.getPriority() == OS.PriorityType.realtime) {
                process.setPriority(OS.PriorityType.interactive);
                //System.out.println("demoted something, RT to IN, PID:"+ currentlyRunning.pid);
            }
            else if (process.getPriority() == OS.PriorityType.interactive) {
                process.setPriority(OS.PriorityType.background);
                //System.out.println("demoted something, IN to BG, PID:"+ currentlyRunning.pid);
            }
//            else if (process.getPriority() == OS.PriorityType.background) {
//                // further testing to see if they get demoted properly
//                System.out.println("attempted background demotion, PID:"+ currentlyRunning.pid);
//            }
            //and ignore background process demotion

            // Reset expiration count
            process.resetTimesQuantumExpired();
        }
    }

    public PCB getCurrentlyRunning(){
        return currentlyRunning;
    }

    public int GetPid() {
        return currentlyRunning.pid;
    }

    public int GetPidByName(String processName) {
        for (PCB pcb : processMap.values()) {
            if (pcb.getName().equals(processName)) {
                return pcb.pid;  // Found the process, return its PID
            }
        }
        return -1;  // Not found
    }

    public void ExitProcess(PCB process) {
        processMap.remove(process.pid); // Remove from HashMap, could also make own method
        currentlyRunning = null; // Mark the current process as terminated
        SwitchProcess(); // Move to the next process
    }

    public static PCB getProcessByPid(int pid) {
        return processMap.get(pid);
    }


    public void sendMessage(KernelMessage km){
        KernelMessage copy = new KernelMessage(km);
        //System.out.println("SendMessage from:" + copy.getSenderPid() + " TO " + copy.getTargetPid());
        PCB targetPCB = getProcessByPid(copy.getTargetPid());
        if (targetPCB != null) {
            targetPCB.addMessage(copy);

            // If process was waiting, move it back to the runnable queue
            if (waitingProcesses.containsKey(targetPCB.pid)) {
                //System.out.println("found waiting proces");
                waitingProcesses.remove(targetPCB.pid);
                addToPriorityQueue(targetPCB);
                //System.out.println("added waiting process to queue");
            }
        }
        else
            throw new RuntimeException("Hey - send message couldn't find pid " + copy.getTargetPid());
    }

    public KernelMessage waitForMessage() throws InterruptedException {
        //if it has a message get it and return immediately
        PCB currentProcess = currentlyRunning;

        if (currentProcess.hasMessages()) {
            return currentProcess.retrieveMessage();
        }

        //System.out.println("should print multiple times because message queue should be empty multiple times");
        //System.out.println("Putting process " + currentProcess.getName() + " to sleep.");

        //no message yet
        waitingProcesses.put(currentProcess.pid,currentProcess);
        //removeFromRunnableQueue(currentProcess);
        currentProcess.setWaitingFlag();
        currentlyRunning=null;
        SwitchProcess();

        return null;
    }

    public void GetMapping(int virtualPageNumber) {
        PCB pcb = currentlyRunning;

        if (virtualPageNumber < 0 || virtualPageNumber >= pcb.getPageTable().length || pcb.getPageTable()[virtualPageNumber] == null) {
            System.out.println("Invalid virtual page: " + virtualPageNumber);
            killCurrentProcess("Invalid memory access");
            return;
        }

        VirtualToPhysicalMapping mapping = pcb.getPageTable()[virtualPageNumber];

        if (mapping.physicalPageNumber == -1) {
            // Try to find a free physical page
            int freePage = -1;
            for (int i = 0; i < physicalPageUsed.length; i++) {
                if (!physicalPageUsed[i]) {
                    freePage = i;
                    break;
                }
            }

            // No free page: Swap out a page from another process
            if (freePage == -1) {
                PCB victimProcess = getRandomProcess();
                VirtualToPhysicalMapping[] victimTable = victimProcess.getPageTable();

                for (int i = 0; i < victimTable.length; i++) {
                    VirtualToPhysicalMapping victimMapping = victimTable[i];
                    if (victimMapping != null && victimMapping.physicalPageNumber != -1) {
                        int victimPhys = victimMapping.physicalPageNumber;

                        // Assign a swap page if needed
                        if (victimMapping.diskPageNumber == -1) {
                            victimMapping.diskPageNumber = Kernel.nextSwapPage++;
                        }

                        // Write the victim page to swap
                        Hardware.Write(victimPhys, (byte)victimMapping.diskPageNumber);
                        victimMapping.physicalPageNumber = -1;
                        physicalPageUsed[victimPhys] = false;

                        freePage = victimPhys;
                        break;
                    }
                }
            }

            // Still no page (shouldn’t happen)
            if (freePage == -1) {
                System.out.println("No available physical memory, even after swapping.");
                killCurrentProcess("Memory allocation failed");
                return;
            }

            // Assign the page
            mapping.physicalPageNumber = freePage;
            physicalPageUsed[freePage] = true;

            // Load from disk or zero-fill
            if (mapping.diskPageNumber != -1) {
                Hardware.Read(mapping.diskPageNumber);
            } else {
                freePage = 0; //Set the page to 0
            }
        }

        // Add to TLB
        int tlbSlot = random.nextInt(2);
        Hardware.loadTLBEntry(tlbSlot, virtualPageNumber, mapping.physicalPageNumber);
    }



    public void killCurrentProcess(String reason) {
        System.out.println("Killing process: "+ currentlyRunning.getName() + " due to: " + reason);
        Hardware.flushTLB();

        processMap.remove(currentlyRunning.pid);
        currentlyRunning = null;
        SwitchProcess();
    }

    public int AllocateMemory(int size) {
        int pagesNeeded = size / PAGE_SIZE;
        PCB pcb = currentlyRunning;

        VirtualToPhysicalMapping[] pageTable = pcb.getPageTable();
        int startVirtualPage = -1;

        outer:
        for (int i = 0; i <= pageTable.length - pagesNeeded; i++) {
            for (int j = 0; j < pagesNeeded; j++) {
                if (pageTable[i + j].physicalPageNumber != -1) {
                    continue outer;
                }
            }
            startVirtualPage = i;
            break;
        }

        if (startVirtualPage == -1) {
            System.out.println("No contiguous virtual memory found");
            return -1;
        }

        // Reserve the virtual pages (physical/disk pages stay at -1)
        for (int i = 0; i < pagesNeeded; i++) {
            pageTable[startVirtualPage + i] = new VirtualToPhysicalMapping(); // Sets phys/disk to -1
        }

        return startVirtualPage * PAGE_SIZE;
    }



    public boolean FreeMemory(int pointer, int size) {
        PCB pcb = currentlyRunning;
        int startVirtualPage = pointer / PAGE_SIZE;
        int pagesToFree = size / PAGE_SIZE;

        if (startVirtualPage + pagesToFree > pcb.getPageTable().length) return false;

        for (int i = 0; i < pagesToFree; i++) {
            int virtualPage = startVirtualPage + i;
            VirtualToPhysicalMapping mapping = pcb.getPageTable()[virtualPage];


            //changed from last submission to just skip iteration if null.
            if (mapping == null) {
                //System.out.println("Tried to free unallocated virtual page " + virtualPage);
                continue;
            }

            if (mapping.physicalPageNumber != -1) {
                physicalPageUsed[mapping.physicalPageNumber] = false;
            }

            // Free the mapping
            pcb.getPageTable()[virtualPage] = null;
        }

        return true;
    }


    public void FreeAllMemory(PCB currentlyRunning) {
        if (currentlyRunning == null) return;

        VirtualToPhysicalMapping[] pageTable = currentlyRunning.getPageTable();
        for (int i = 0; i < pageTable.length; i++) {
            int physicalPage = pageTable[i].physicalPageNumber;
            if (physicalPage != -1) {
                physicalPageUsed[physicalPage] = false;  // Free the physical page
                pageTable[i].physicalPageNumber = -1;
            }
        }

        Hardware.flushTLB(); //empties tlb
    }

    public PCB getRandomProcess() {
        List<PCB> candidates = new ArrayList<>();

        for (PCB pcb : processMap.values()) {
            if (pcb != currentlyRunning) {
                candidates.add(pcb);
            }
        }

        Collections.shuffle(candidates);

        for (PCB pcb : candidates) {
            VirtualToPhysicalMapping[] pageTable = pcb.getPageTable();
            for (int i = 0; i < pageTable.length; i++) {
                if (pageTable[i] != null && pageTable[i].physicalPageNumber != -1) {
                    return pcb; // Found a process with a page to swap out
                }
            }
        }

        return null; // Should never happen if there's at least one mappable page
    }





}
