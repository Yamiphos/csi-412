public class VirtualToPhysicalMapping {
    public int physicalPageNumber;
    public int diskPageNumber;

    public VirtualToPhysicalMapping() {
        this.physicalPageNumber = -1;
        this.diskPageNumber = -1;
    }
}
