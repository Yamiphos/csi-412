import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OS {
    private static Kernel ki; // The one and only one instance of the kernel.

    public static List<Object> parameters = new ArrayList<>();
    public static Object retVal;

    public enum CallType {SwitchProcess,SendMessage, Open, Close, Read, Seek, Write, GetMapping, CreateProcess, Sleep, GetPID, AllocateMemory, FreeMemory, GetPIDByName, WaitForMessage, Exit}
    public static CallType currentCall;

    private static void startTheKernel() {
        //first call start
        ki.start();

        //then check if there is a currently running if so, stop it
        if(ki.scheduler.currentlyRunning != null ){
            //System.out.println("the currently running process was stopped after kernel call.");
            ki.scheduler.currentlyRunning.stop();
        }

        //wait until retval is set
        while(currentCall == CallType.CreateProcess && retVal == null){
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                //nothing!
            }
        }

        //System.out.println("OS.startTheKernel: retVal is of type " + retVal.getClass().getName());
//        if(currentCall == CallType.Open){
//            System.out.println("got the open call done");
//        }
//        if(currentCall == CallType.Read){
//            System.out.println("got the Read call done");
//        }
    }

    public static void switchProcess() {
        parameters.clear();
        currentCall = CallType.SwitchProcess;
        startTheKernel();
    }

    public static void Startup(UserlandProcess init) {
            ki = new Kernel();
            CreateProcess(init, PriorityType.interactive);
            CreateProcess(new IdleProcess(), PriorityType.background);
            //Open("file SwapFile"); //Open the swapfile, empty at creation. ---- moved to idle process, because init exits
    }

    public enum PriorityType {realtime, interactive, background}
    public static int CreateProcess(UserlandProcess up) {
        return  CreateProcess(up,PriorityType.interactive);
    }

    // For assignment 1, you can ignore the priority. We will use that in assignment 2
    public static int CreateProcess(UserlandProcess up, PriorityType priority) {
        parameters.clear();
        parameters.add(up);
        parameters.add(priority);
        currentCall = CallType.CreateProcess;
        startTheKernel();
        return (int) retVal;
    }

    public static int GetPID() {
        parameters.clear();
        currentCall = CallType.GetPID;
        startTheKernel();
        return (int) retVal;
    }

    public static void Exit() {
        System.out.println("process: "+ ki.scheduler.currentlyRunning.getName() + " Exited.");
        parameters.clear();
        currentCall = CallType.Exit;
        startTheKernel();
    }

    public static void Sleep(int mills) {
        parameters.clear();
        parameters.add(mills);
        currentCall = CallType.Sleep;
        startTheKernel();
    }

    // Devices
    public static int Open(String s) {
        parameters.clear();
        parameters.add(s); // Add file/device name
        currentCall = CallType.Open;
        startTheKernel();
        //System.out.println("opened successfully!");
        //System.out.println("OS.Open: Process " + ki.scheduler.getCurrentlyRunning().pid + " opening random");
        return (int) retVal; // The kernel should return a device ID
    }

    public static void Close(int id) {
        parameters.clear();
        parameters.add(id); // Add device ID
        currentCall = CallType.Close;
        startTheKernel();
    }

    public static byte[] Read(int id, int size) {
        parameters.clear();
        parameters.add(id); // Device ID
        parameters.add(size); // Number of bytes to read
        currentCall = CallType.Read;
        startTheKernel();

        //System.out.println("OS.Read: retVal is of type " + retVal.getClass().getName()); // Debugging line
        //System.out.println("retval: "+ retVal);
        //System.out.println("retval: " + Arrays.toString((byte[]) retVal));
        //System.out.println("OS.Read: Process " + ki.scheduler.getCurrentlyRunning().pid + " reading random");

        return (byte[]) retVal; // Kernel should return the read data
    }

    public static void Seek(int id, int to) {
        parameters.clear();
        parameters.add(id); // Device ID
        parameters.add(to); // Position to seek to
        currentCall = CallType.Seek;
        startTheKernel();
    }

    public static int Write(int id, byte[] data) {
        parameters.clear();
        parameters.add(id); // Device ID
        parameters.add(data); // Data to write
        currentCall = CallType.Write;
        startTheKernel();
        return (int) retVal; // Kernel should return number of bytes written
    }

    // Messages
    public static void SendMessage(KernelMessage km) {
        parameters.clear();
        parameters.add(km);
        currentCall = CallType.SendMessage;
        startTheKernel();
    }

    public static KernelMessage WaitForMessage() {
        parameters.clear();
        currentCall = CallType.WaitForMessage;
        startTheKernel();
        return (KernelMessage) retVal;
    }

    public static int GetPidByName(String name) {
        parameters.clear();
        parameters.add(name);
        currentCall = CallType.GetPIDByName;
        startTheKernel();
        return (int) retVal;
    }

    // Memory
    public static void GetMapping(int virtualPage) {
        parameters.clear();
        parameters.add(virtualPage);
        currentCall = CallType.GetMapping;
        startTheKernel();
    }

    public static int AllocateMemory(int size ) {
        if(size % 1024 != 0){
            System.out.println("Allocate memory failed, invalid multiple");
            return -1;
        }
        parameters.clear();
        parameters.add(size);
        currentCall = CallType.AllocateMemory;
        startTheKernel();
        return (int) retVal;
    }

    public static boolean FreeMemory(int pointer, int size) {
        if(size % 1024 != 0 && pointer % 1024 != 0){
            System.out.println("free memory failed, invalid multiples");
            return false;
        }
        parameters.clear();
        parameters.add(pointer);
        parameters.add(size);
        currentCall = CallType.FreeMemory;
        startTheKernel();
        return (boolean) retVal;
    }
}
