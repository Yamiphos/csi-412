import java.io.*;

public class FakeFileSystem implements Device {
    private RandomAccessFile[] files = new RandomAccessFile[10];
    private int swapFileId = -1;
    private int nextSwapPageNumber = 0;


    @Override
    public int Open(String filename) throws IOException {
        if (filename == null || filename.isEmpty()) {
            throw new IllegalArgumentException("Filename cannot be null or empty");
        }
        for (int i = 0; i < files.length; i++) {
            if (files[i] == null) {
                files[i] = new RandomAccessFile(filename, "rw");
                if (filename.equals("swapfile")) {
                    swapFileId = i;
                    nextSwapPageNumber = 0; // Optionally read the file to find where to continue
                }
                return i;
            }
        }
        return -1;
    }

    @Override
    public void Close(int id) {
        if (id >= 0 && id < files.length && files[id] != null) {
            try {
                files[id].close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                files[id] = null;
            }
        }
    }

    @Override
    public byte[] Read(int id, int size) {
        if (id < 0 || id >= files.length || files[id] == null || size <= 0) {
            return new byte[0];
        }
        try {
            byte[] data = new byte[size];
            int bytesRead = files[id].read(data);
            return bytesRead > 0 ? data : new byte[0];
        } catch (IOException e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    @Override
    public void Seek(int id, int to) {
        if (id >= 0 && id < files.length && files[id] != null) {
            try {
                files[id].seek(to);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int Write(int id, byte[] data) {
        if (id < 0 || id >= files.length || files[id] == null || data == null) {
            return 0;
        }
        try {
            files[id].write(data);
            return data.length;
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public int getNextSwapPage() {
        return nextSwapPageNumber++;
    }

    public int getSwapFileId() {
        return swapFileId;
    }

}
