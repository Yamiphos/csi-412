public class Hardware {

    public static final int PAGE_SIZE = 1024; // 1KB
    public static final int MEMORY_SIZE = 1024 * PAGE_SIZE; // 1MB

    private static byte[] memory = new byte[MEMORY_SIZE];

    // TLB: [][0] = virtual page, [][1] = physical page
    private static int[][] tlb = new int[2][2];

    static {
        for (int i = 0; i < 2; i++) {
            tlb[i][0] = -1;
            tlb[i][1] = -1;
        }
    }

    // Private helper to translate virtual to physical page
    private static int translateVirtualPageToPhysical(int virtualPage) {
        // First try to find it in the TLB
        for (int i = 0; i < 2; i++) {
            if (tlb[i][0] == virtualPage) {
                return tlb[i][1]; // TLB Hit
            }
        }

        // TLB Miss — call kernel to get mapping
        OS.GetMapping(virtualPage); //will stop here(after getting killed) if nothing is found

        // After GetMapping, try again
        for (int i = 0; i < 2; i++) {
            if (tlb[i][0] == virtualPage) {
                return tlb[i][1]; // TLB filled by OS call
            }
        }

        // Still not found — something went wrong
        throw new RuntimeException("Mapping not found for virtual page " + virtualPage);
    }

    public static byte Read(int address) {
        int virtualPage = address / PAGE_SIZE;
        int offset = address % PAGE_SIZE;

        int physicalPage = translateVirtualPageToPhysical(virtualPage);
        int physicalAddress = physicalPage * PAGE_SIZE + offset;

        return memory[physicalAddress];
    }

    public static void Write(int address, byte value) {
        int virtualPage = address / PAGE_SIZE;
        int offset = address % PAGE_SIZE;

        int physicalPage = translateVirtualPageToPhysical(virtualPage);
        int physicalAddress = physicalPage * PAGE_SIZE + offset;

        memory[physicalAddress] = value;
    }

    public static void loadTLBEntry(int index, int virtualPage, int physicalPage) {
        if (index >= 0 && index < 2) {
            tlb[index][0] = virtualPage;
            tlb[index][1] = physicalPage;
        } else {
            throw new IllegalArgumentException("Invalid TLB index: " + index);
        }
    }

    public static void printTLB() {
        System.out.println("TLB Contents:");
        for (int i = 0; i < 2; i++) {
            System.out.println("  [" + i + "] Virtual Page " + tlb[i][0] + " -> Physical Page " + tlb[i][1]);
        }
    }


    public static void flushTLB(){
        // Clear TLB
        for (int i = 0; i < tlb.length; i++) {
            tlb[i][0] = -1;  // Clear virtual page
            tlb[i][1] = -1;  // Clear physical page
        }
    }
}
