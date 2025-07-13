import java.io.*;

public interface Device {
    int Open(String s) throws IOException;

    void Close(int id);

    byte[] Read(int id, int size);

    void Seek(int id, int to);

    int Write(int id, byte[] data);
}