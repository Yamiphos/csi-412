public class Pong extends UserlandProcess {
    public void main() throws InterruptedException {
        int pingPid;
        int myPID;

        pingPid = OS.GetPidByName("Ping");
        myPID = OS.GetPID();
        System.out.println("I am PONG, ping = " + pingPid);

        while(true){
            KernelMessage msg = OS.WaitForMessage();

            System.out.println("PONG: GOT MESSAGE from: " + msg.getSenderPid() + " to: " + msg.getTargetPid() + " what: " + msg.getMessageType());

            KernelMessage response = new KernelMessage(myPID,pingPid,msg.getMessageType()+1,new byte[0]);
            OS.SendMessage(response);

            cooperate();
        }
    }
}
