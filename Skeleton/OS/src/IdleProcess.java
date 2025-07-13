public class IdleProcess extends UserlandProcess {
    @Override
    public void main() {
        while (true) {
            try {
                //to check whether the OS just froze or if we are still running without any signs of it.
                //System.out.println("hi!");
                OS.Open("file SwapFile"); //Open the swapfile, empty at creation. ---- belongs to init
                cooperate();
                Thread.sleep(50);
            } catch (Exception e) { }
        }
    }
}
