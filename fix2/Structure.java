import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.LocalTime;
import java.time.Duration;
import java.time.format.DateTimeFormatter;



public class Structure implements Comparable<Structure>{

	private final LocalTime time;	
	private final int floorSource;
	private final boolean floorButton; // 0 - DOWN, 1 - UP
	private final int floorDestination;
	private final int MAXFLOOR = 9;

	public Structure (String time, String floorSource, String floorButton, String FloorDestination) {
		this.time = istimeValid(time);	
		this.floorSource = isfloorSourceValid(floorSource);
		this.floorButton = isfloorButtonValid(floorButton);
		this.floorDestination = isfloorDestinatonValid(FloorDestination);

	}	

	private LocalTime istimeValid(String time) {
		try {
			return LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm:ss:SS"));
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Invalid time format: " + time);
		}
	}

	private int isfloorSourceValid(String floorSource) {
		int floorValue = Integer.parseInt(floorSource);

		if (floorValue > MAXFLOOR || floorValue < 1) {
			throw new IllegalArgumentException("Invalid floorSource: " + floorSource);
		} else {
			return floorValue;
		}	
	}

	private boolean isfloorButtonValid(String floorButton) {
		boolean buttonValue;

		if (floorButton.equalsIgnoreCase("up")) {
			buttonValue = true;
		} else if (floorButton.equalsIgnoreCase("down")) {
			buttonValue = false;		
		} else {
			throw new IllegalArgumentException("Invalid floorButton: " + floorButton);
		}

		int floorSource = getfloorSource();		

		if ((buttonValue && floorSource == MAXFLOOR) || (!buttonValue && floorSource == 1)) {
			throw new IllegalArgumentException("Invalid floorButton: " + floorButton);
		}

		return buttonValue;
	}

	private int isfloorDestinatonValid(String floorDestination) {
		int floorDestinationValue = Integer.parseInt(floorDestination);
		int floorSource = getfloorSource();
		boolean floorButton = getfloorButton();


		if (floorDestinationValue > MAXFLOOR || floorDestinationValue < 1) {
			throw new IllegalArgumentException("Invalid FloorDestination: " + floorDestinationValue);
		} else if ((floorButton && floorSource >= floorDestinationValue) || (!floorButton && floorSource <= floorDestinationValue)) {
			throw new IllegalArgumentException("Invalid FloorDestination: " + floorDestinationValue);
		}

		return floorDestinationValue;

	}


	public LocalTime getTime() {
		return time;

	}

	public int getfloorSource() {
		return floorSource;
	}	

	public boolean getfloorButton() {
		return floorButton;
	}

	public int getfloorDestination() {
		return floorDestination;
	}


	public byte[] toByteArray() {
		ByteBuffer buffer = ByteBuffer.allocate(20); // Adjust buffer size for milliseconds
		long timeMillis = time.toNanoOfDay() / 1_000_000; // Convert nanoseconds to milliseconds
		buffer.putLong(timeMillis);
		buffer.putInt(floorSource);
		buffer.put((byte) (floorButton ? 1 : 0));
		buffer.putInt(floorDestination);
		return buffer.array();
	}



	public static Structure fromByteArray(byte[] bytes) {
		ByteBuffer buffer = ByteBuffer.wrap(bytes);
		buffer.order(ByteOrder.BIG_ENDIAN); // Setting byte order to BIG_ENDIAN

		// Check if the buffer has enough remaining bytes to read all fields
		if (buffer.remaining() < 20) { // Adjust this size based on the actual size of your serialized data
			throw new IllegalArgumentException("Invalid byte array size");
		}

		// Read the fields from the buffer
		long timeMillis = buffer.getLong(); // Read milliseconds as long
		int floorSource = buffer.getInt();
		boolean floorButton = buffer.get() == 1;
		int floorDestination = buffer.getInt();

		// Convert milliseconds to LocalTime with format "HH:mm:ss:SS"
		long seconds = timeMillis / 1000;
		long milliseconds = timeMillis % 1000;

		// Create LocalTime object with milliseconds set to zero
		if (milliseconds < 0 || milliseconds > 999) {
			milliseconds = 0;
		}
		LocalTime time = LocalTime.ofSecondOfDay(seconds).withNano((int) (milliseconds * 1_000_000));

		// Format the LocalTime object with milliseconds
		String formattedTime = String.format("%02d:%02d:%02d:%02d", time.getHour(), time.getMinute(), time.getSecond(), time.getNano() / 10_000_000);


		// Create and return a new Structure object
		return new Structure(formattedTime, String.valueOf(floorSource), floorButton ? "up" : "down", String.valueOf(floorDestination));
	}

	@Override
	public String toString() {
		return "Structure{" +
			"time=" + time.format(DateTimeFormatter.ofPattern("HH:mm:ss:SS")) +
			", floorSource=" + floorSource +
			", floorButton=" + (floorButton ? "UP" : "DOWN") +
			", floorDestination=" + floorDestination +
			'}';
	}

	@Override
	public int compareTo(Structure o) {
		LocalTime currentTime = LocalTime.now();
		long difference1 = Math.abs(Duration.between(currentTime, this.time).toMillis());
		long difference2 = Math.abs(Duration.between(currentTime, o.time).toMillis());
		return Long.compare(difference1, difference2);
	}
}

