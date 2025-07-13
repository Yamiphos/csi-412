import java.io.IOException;
import java.util.Random;

public class RandomDevice implements Device {
    private Random[] devices = new Random[10];

    @Override
    public int Open(String s) throws IOException {
        //go through the device array
        for (int i = 0; i < devices.length; i++) {
            if (devices[i] == null) {  //look for an empty spot
                if (s != null && !s.isEmpty()) {
                    try { // When an empty spot is found get the random seed from s
                        int seed = Integer.parseInt(s);
                        devices[i] = new Random(seed);
                    } catch (NumberFormatException e) { //handle exceptions
                        devices[i] = new Random();
                    }
                } else { // in case s is null or is empty
                    devices[i] = new Random();
                }
                return i;
            }
        }
        return -1; // No available slots
    }

    @Override
    public void Close(int id) {
        if (id >= 0 && id < devices.length) { //length checking and then setting to null
            devices[id] = null;
        }
    }

    @Override
    public byte[] Read(int id, int size) {
        if (id < 0 || id >= devices.length || devices[id] == null || size <= 0) {
            return new byte[0];
        }
        byte[] data = new byte[size];
        devices[id].nextBytes(data);
        //System.out.println("Random.Read: Returning " + data.length + " bytes");
        return data;
    }


    @Override
    public void Seek(int id, int to) {
        if (id >= 0 && id < devices.length && devices[id] != null) { // Id validation
            byte[] temp = new byte[to];
            devices[id].nextBytes(temp); // Read random bytes but do nothing with them
        }
    }

    @Override
    public int Write(int id, byte[] data) {
        return 0; // Writing doesn't make sense for a Random device
    }
}
