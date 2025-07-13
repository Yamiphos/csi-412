import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.io.IOException;

public class VFS implements Device {

    private static class DeviceMapping {
        Device device;
        int id;

        DeviceMapping(Device device, int id) {
            this.device = device;
            this.id = id;
        }
    }

    private Map<Integer, DeviceMapping> vfsMap = new HashMap<>();
    private int nextVFSId = 0;
    private RandomDevice randomDevice = new RandomDevice();
    private FakeFileSystem fakeFileSystem = new FakeFileSystem();

    @Override
     public int Open(String input) throws IOException {
        if (input == null || input.isEmpty()) {
            throw new IllegalArgumentException("Input cannot be null or empty");
        }

        StringTokenizer tokenizer = new StringTokenizer(input);
        if (!tokenizer.hasMoreTokens()) {
            throw new IllegalArgumentException("Invalid format for open command");
        }

        String deviceName = tokenizer.nextToken();
        String argument = tokenizer.hasMoreTokens() ? input.substring(deviceName.length()).trim() : "";

        Device device;
        if ("random".equalsIgnoreCase(deviceName)) {
            device = randomDevice;
        } else if ("file".equalsIgnoreCase(deviceName)) {
            device = fakeFileSystem;
        } else {
            throw new IllegalArgumentException("Unknown device: " + deviceName);
        }

        int deviceId = device.Open(argument);
        if (deviceId == -1) {
            return -1;
        }

        vfsMap.put(nextVFSId, new DeviceMapping(device, deviceId));
        //System.out.println("VFS.Open: Assigning vfsId " + nextVFSId);
        return nextVFSId++;
    }

    @Override
    public void Close(int vfsId) {
        DeviceMapping mapping = vfsMap.remove(vfsId);
        if (mapping != null) {
            mapping.device.Close(mapping.id);
        }
    }

    @Override
    public byte[] Read(int vfsId, int size) {
        DeviceMapping mapping = vfsMap.get(vfsId);
        if (mapping != null) {
            return mapping.device.Read(mapping.id, size);
        }
        //System.out.println("VFS.Read: Returning " + size + " bytes for VFS ID " + vfsId); //debugging
        System.out.println("No device to read was found at the given spot, returning 0");
        return new byte[0];
    }

    @Override
    public void Seek(int vfsId, int to) {
        DeviceMapping mapping = vfsMap.get(vfsId);
        if (mapping != null) {
            mapping.device.Seek(mapping.id, to);
        }
        else{
            System.out.println("No device to seek was found at the given spot");
        }
    }

    @Override
    public int Write(int vfsId, byte[] data) {
        DeviceMapping mapping = vfsMap.get(vfsId);
        if (mapping != null) {
            return mapping.device.Write(mapping.id, data);
        }
        System.out.println("No device to write was found at the given spot, returning 0");
        return 0;
    }
}
