public class Ping extends UserlandProcess {
    public void main() throws InterruptedException {
        int pongPid;
        int myPID;
        int count=0;

        pongPid = OS.GetPidByName("Pong");
        myPID = OS.GetPID();

        System.out.println("I am PING, pong = " + pongPid);

        while(true){
            KernelMessage km = new KernelMessage(myPID,pongPid,count, new byte[0]);
            OS.SendMessage(km);

            KernelMessage response = OS.WaitForMessage();
            System.out.println("PING GOT MESSAGE from: "+response.getSenderPid() + " to: "+ response.getTargetPid()+ " what: " + response.getMessageType());

            count++;
            cooperate();
            if (count > 20) {
                System.out.println("finished 20+ ping and pong exchanges, Ping exited and pong cannot run without a send message to wake it up.");
                OS.Exit(); //pong cannot run without ping, idle process will just go forever since pong will never get woken up without
                //a send message call.
                return; // this will never happen
            }
        }
    }
}
