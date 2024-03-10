import java.nio.ByteBuffer;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Structure implements Comparable<Structure> {
    private final LocalTime time;
    private final int floorSource;
    private final FloorButton floorButton;
    private final int floorDestination;

    /*
     * Allows integer style comparison by time
     * for priority queue
     * */
    public int compareTo(Structure o){
        return time.compareTo(o.getTime());
    }

    public Structure (int h, int m, int s, int ms, int floorSource, FloorButton floorButton, int floorDestination) {
        this.time = LocalTime.of(h,m,s,ms);
        this.floorSource = floorSource;
        this.floorButton = floorButton;
        this.floorDestination = floorDestination;
    }

    public LocalTime getTime() {
        return time;
    }

    public int getFloorSource() {
        return floorSource;
    }

    public FloorButton getFloorButton() {
        return floorButton;
    }

    public int getFloorDestination() {
        return floorDestination;
    }

    // "Serialize" to a Bytes array for sending over UDP
    public byte[] toByteArray() {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.put((byte) time.getHour());
        buffer.put((byte) time.getMinute());
        buffer.put((byte) time.getSecond());
        buffer.putShort((short) floorSource);
        buffer.put((byte) (floorButton == FloorButton.UP ? 1 : 0));
        buffer.putShort((short) floorDestination);
        return buffer.array();
    }

    // Deserialize a structure from Bytes into
    public static Structure fromByteArray(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        int hour = buffer.get();
        int minute = buffer.get();
        int second = buffer.get();
        int floorSource = buffer.getShort();
        byte floorButtonByte = buffer.get();
        FloorButton floorButton = (floorButtonByte == 1) ? FloorButton.UP : FloorButton.DOWN;
        int floorDestination = buffer.getShort();
        return new Structure(hour, minute, second, 0, floorSource, floorButton, floorDestination);
    }

    public enum FloorButton {
        UP, DOWN
    }
}
