import javax.swing.plaf.TableHeaderUI;
import java.io.IOException;


public class Kernel extends Process implements Device {
    public Scheduler scheduler;
    private VFS vfs;
    public static int nextSwapPage ;
    public Kernel() {
        nextSwapPage =0;
        this.scheduler =  new Scheduler();
        this.vfs = new VFS();
    }

    @Override
    public void main() throws IOException, InterruptedException {
            while (true) { // Warning on infinite loop is OK...
                switch (OS.currentCall) { // get a job from OS, do it
                    case CreateProcess ->  // Note how we get parameters from OS and set the return value
                            OS.retVal = CreateProcess((UserlandProcess) OS.parameters.get(0), (OS.PriorityType) OS.parameters.get(1));
                    case SwitchProcess -> SwitchProcess();

                    // Priority Schduler
                    case Sleep -> Sleep((int) OS.parameters.get(0));
                    case GetPID -> OS.retVal = GetPid();
                    case Exit -> Exit();
                    // Devices
                    case Open -> OS.retVal =Open((String) OS.parameters.get(0));
                    case Close -> Close((int) OS.parameters.get(0));
                    case Read -> OS.retVal = Read((int) OS.parameters.get(0), (int) OS.parameters.get(1));
                    case Seek -> Seek((int) OS.parameters.get(0), (int) OS.parameters.get(1));
                    case Write -> Write((int) OS.parameters.get(0),(byte[]) OS.parameters.get(0));
                    // Messages
                    case GetPIDByName -> OS.retVal = GetPidByName((String) OS.parameters.get(0));
                    case SendMessage -> SendMessage((KernelMessage) OS.parameters.get(0));
                    case WaitForMessage -> OS.retVal = WaitForMessage();
                    // Memory
                    case GetMapping -> GetMapping((int)OS.parameters.get(0));
                    case AllocateMemory -> OS.retVal = AllocateMemory((int)OS.parameters.get(0));
                    case FreeMemory -> OS.retVal = FreeMemory((int)OS.parameters.get(0),(int)OS.parameters.get(1));
                     /**/
                }
                // TODO: Now that we have done the work asked of us, start some process then go to sleep.
                //after work is done in kernel, currently running is set, start it
                scheduler.currentlyRunning.start();
                this.stop();
            }
    }

    private void SwitchProcess() {
        try{
            Thread.sleep(70);
            scheduler.currentlyRunning.increaseTimesQuantumExpired(); //will increase by 1
            if(scheduler.currentlyRunning != null && scheduler.currentlyRunning.isDone()){
                closeAllDevices(scheduler.currentlyRunning);
            }
            scheduler.SwitchProcess();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
//            scheduler.currentlyRunning.increaseTimesQuantumExpired(); //will increase by 1
//            scheduler.SwitchProcess();

    }

    // For assignment 1, you can ignore the priority. We will use that in assignment 2
    private int CreateProcess(UserlandProcess up, OS.PriorityType priority) {
        return scheduler.CreateProcess(up,priority);

    }

    private void Sleep(int mills) {
        //call scheduler sleep
        scheduler.currentlyRunning.resetTimesQuantumExpired(); //reset quantum counting
        scheduler.Sleep(mills);
    }

    private void Exit() {
        //already stopped A and started kernel so now we remove the currently running thing from the queue and call switch process
        try {
            Thread.sleep(100);
            FreeAllMemory(scheduler.currentlyRunning); //frees all memory and flushes tlb
            closeAllDevices(scheduler.currentlyRunning); //close all devices when a process is exited
            scheduler.ExitProcess(scheduler.currentlyRunning);
            //all this is done in method above
//            scheduler.currentlyRunning = null; //so the null check in switch process won't add it to the list
//            scheduler.SwitchProcess();
        } catch (InterruptedException e) {
            //nothing
        }
    }

    private int GetPid() throws InterruptedException {
        Thread.sleep(100);
        return scheduler.currentlyRunning.pid;
    }

    @Override
    public int Open(String s) throws IOException {
        PCB currentProcess = scheduler.getCurrentlyRunning(); // Get the running process
        if (currentProcess == null) {
            return -1; // No running process
        }

        // Find an empty (-1) entry in the PCB's array
        int[] pcbArray = currentProcess.getProcessData();

        for(int i=0; i< pcbArray.length; i++){
            if(pcbArray[i] ==-1){
                int tempInt = vfs.Open(s);

                if(tempInt == -1){ //if vfs open failed
                    System.out.println("failed");
                    throw new IOException("vfs open failed");
                }
                else{ //put into pcb array
                    pcbArray[i] =  tempInt;
                    return i; //array index at the moment is i
                }
            }
        }

        //we couldnt fulfil the condition(find an empty entry in the array), fail.
        return -1;

    }

    @Override
    public void Close(int id) {
        //get the currently running thing and its array
        PCB currentProcess = scheduler.getCurrentlyRunning();
        if (currentProcess == null) {
            return; // No running process
        }
        int[] pcbArray = currentProcess.getProcessData();

       int vfsid = pcbArray[id];
       vfs.Close(vfsid); //closing that processes device at the given index
        pcbArray[id] = -1; //setting the closed device to -1 to indicate there isnt a device there in the array.
    }

    @Override
    public byte[] Read(int id, int size) {
        PCB currentProcess = scheduler.getCurrentlyRunning();
        if (currentProcess == null) {
            return new byte[0]; // No running process, return empty array
        }

        int[] pcbArray = currentProcess.getProcessData();
        int vfsid = pcbArray[id];

        //byte[] data = vfs.Read(vfsid, size);

        //System.out.println("Kernel.Read: Returning " + (data != null ? data.length : "null") + " bytes");
        //System.out.println("Kernel.Read: retVal is " + OS.retVal + ", type: " + OS.retVal.getClass().getName());

        //ensure retval is properly set
        //OS.retVal = data;


        return vfs.Read(vfsid, size);
    }



    @Override
    public void Seek(int id, int to) {
        //get the currently running thing and its array
        PCB currentProcess = scheduler.getCurrentlyRunning();
        if (currentProcess == null) {
            return; // No running process
        }
        int[] pcbArray = currentProcess.getProcessData();

        int vfsid = pcbArray[id];
        vfs.Seek(vfsid, to);
    }

    @Override
    public int Write(int id, byte[] data) {
        //get the currently running thing and its array
        PCB currentProcess = scheduler.getCurrentlyRunning();
        if (currentProcess == null) {
            return -1; // No running process
        }
        int[] pcbArray = currentProcess.getProcessData();

        int vfsid = pcbArray[id];
        return vfs.Write(vfsid,data);
    }

    private void SendMessage(KernelMessage km) throws InterruptedException {
        scheduler.sendMessage(km);
    }

    private KernelMessage WaitForMessage() throws InterruptedException {
        KernelMessage km = scheduler.waitForMessage();

        if(scheduler.currentlyRunning.checkWaitingFlag()){
            scheduler.currentlyRunning.resetWaitingFlag();
            km= (KernelMessage) OS.retVal;
        }

        return km;
    }

    private int GetPidByName(String name) throws InterruptedException {
        //Thread.sleep(40);
        return scheduler.GetPidByName(name);
    }

    private void GetMapping(int virtualPage) {
        //calls schedulers version to make it easier to interact with the queues
        scheduler.GetMapping(virtualPage);
    }

    private int AllocateMemory(int size) {
        return scheduler.AllocateMemory(size);
    }

    private boolean FreeMemory(int pointer, int size) {
        return scheduler.FreeMemory(pointer,size);
    }

    private void FreeAllMemory(PCB currentlyRunning) {
        scheduler.FreeAllMemory(currentlyRunning);
    }

    // To close all the devices easier
    public void closeAllDevices(PCB process) {
        for (int i = 0; i < process.getProcessData().length; i++) {
            if (process.getProcessData()[i] != -1) {
                vfs.Close(process.getProcessData()[i]); // Close the device
                process.getProcessData()[i] = -1; // Mark as closed
            }
        }
    }

}