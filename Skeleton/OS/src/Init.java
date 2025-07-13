public class Init extends UserlandProcess{
    public void main(){

                //replaced with ping and pong for messages testing
        OS.CreateProcess(new HelloWorld(), OS.PriorityType.interactive);
        OS.CreateProcess(new GoodbyeWorld(), OS.PriorityType.interactive);

        //ping and pong testing
        //OS.CreateProcess(new Ping(), OS.PriorityType.interactive);
        //OS.CreateProcess(new Pong(), OS.PriorityType.interactive);

        try {
            Thread.sleep(100);
            OS.Exit();
        } catch (InterruptedException e) {
            //nothing
        }
    }

}
