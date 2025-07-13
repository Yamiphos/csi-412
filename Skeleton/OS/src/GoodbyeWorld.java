public class GoodbyeWorld extends UserlandProcess {
    public void main() {
        int i=0;
        //device testing
//        try{
//            Thread.sleep(200);
//            OS.Open("random");
//            OS.Read(0,10);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }

//        OS.Open("random 100");
//        try{
//            Thread.sleep(1000);
//            OS.Read(0,10);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
        while (true) {

            System.out.println("Goodbye World times: " + i);
            try {
                Thread.sleep(50); // Sleep to make it more readable
                //testing sleep
                //OS.Sleep(500);
            } catch (Exception e) {
                e.printStackTrace();
            }
            cooperate();
            i++;

        }
    }

}