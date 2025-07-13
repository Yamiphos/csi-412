public class HelloWorld extends UserlandProcess {
    public void main() {
        int i=0;

        //device testing
        //OS.Open("random");
        //OS.Read(0,10);

        //open multiple devices
//        for(int x=0; x<5; x++){
//            OS.Open("random");
//        }

        //checking multiple Reads are different and that open is different each time
//        for(int x=0; x<5; x++){
//            OS.Read(x,10);
//        }

        //trying to read a spot that there is no device
        //OS.Read(9,10);

        //testing getpid by name and getpid
        //System.out.println("HelloWorld PID from getpid: "+ OS.GetPID());
        //System.out.println("HelloWorld PID from getpidbyname: "+ OS.GetPidByName("HelloWorld"));




        //Paging testing --------- not commented out to indicate I was testing with it, yeah.
        int startVirtualPage = OS.AllocateMemory(1024);
        byte[] value = new byte[1];
        value[0] = 10;
        Hardware.Write(startVirtualPage,value[0]);
        byte a = Hardware.Read(0);
        System.out.println("byte a: "+ a); //should be 10

        boolean f = OS.FreeMemory(0,1024); //freed in page table still in tlb----- tlb cleared if successful
        if(!f){ //failed free memory
            byte b = Hardware.Read(0);
            System.out.println("byte b: "+b);
        }
        else{
            System.out.println("Successfully cleared");
        }

        //Hardware.flushTLB(); //flushing to clear and update tlb ------- moved to after a successful free memory call

        byte c = Hardware.Read(0);
        System.out.println("byte c: "+c); //should kill process

        while (true) {

            System.out.println("Hello World times: "+ i);
            try {
                Thread.sleep(50); // Sleep to make it more readable
                //testing sleep
                //OS.Sleep(500);

                //OS.Read(0,10); //should print
                //OS.Exit();
                //OS.Read(0,10); //should not print
            } catch (Exception e) {
                e.printStackTrace();
            }

            //device testing
            //OS.Read(1,10); //should not print
            cooperate();
            i++;
        }
    }
}