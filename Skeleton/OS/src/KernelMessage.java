import java.util.Arrays;

public class KernelMessage {
    private int senderPid;
    private int targetPid;
    private int messageType; // Indicates what the message is for
    private byte[] data; // Can be any application-specific data
    public static final KernelMessage NO_MESSAGE = new KernelMessage(-1, -1, -1, new byte[0]);


    // Constructor to initialize a KernelMessage
    public KernelMessage(int senderPid, int targetPid, int messageType, byte[] data) {
        this.senderPid = senderPid;
        this.targetPid = targetPid;
        this.messageType = messageType;
        this.data = (data != null) ? Arrays.copyOf(data, data.length) : new byte[0];
    }

    // Copy constructor to create a separate copy of the message
    public KernelMessage(KernelMessage other) {
        this.senderPid = other.senderPid;
        this.targetPid = other.targetPid;
        this.messageType = other.messageType;
        this.data = Arrays.copyOf(other.data, other.data.length);
    }

    // Getters
    public int getSenderPid() {
        return senderPid;
    }

    public int getTargetPid() {
        return targetPid;
    }

    public int getMessageType() {
        return messageType;
    }

    public byte[] getData() {
        return Arrays.copyOf(data, data.length); // Return a copy to maintain encapsulation
    }

    // ToString method for debugging
    @Override
    public String toString() {
        return "KernelMessage{" +
                "senderPid=" + senderPid +
                ", targetPid=" + targetPid +
                ", messageType=" + messageType +
                ", data=" + Arrays.toString(data) +
                '}';
    }
}
