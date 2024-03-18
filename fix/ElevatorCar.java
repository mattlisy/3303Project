import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;


public class ElevatorCar {

	private DatagramSocket socket;
	
	public ElevatorCar() {
		try {
			socket = new DatagramSocket();	

		} catch (SocketException se) {
			se.printStackTrace();
			System.exit(1);

		}
	}	

	private byte[] intToBytes(int value) {
		ByteBuffer buffer = ByteBuffer.allocate(value);
		buffer.putInt(value);
		return buffer.array();
		

	}

	private void requstWork() {
		DatagramPacket reqPacket = null; 		
		
		try {
			System.out.println("Sending request for work to Server");
			byte[] reqMsg = intToBytes(1111);
			reqPacket = new DatagramPacket(reqMsg, reqMsg.length, InetAddress.getLocalHost(), 9999);

		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.exit(1); 
		}

		try {
			socket.send(reqPacket);
	       	} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}			
	}

	public void elevatorService() {
		requstWork();
	}

	public static void main(String[] args) {
		ElevatorCar e = new ElevatorCar();
		e.elevatorService();
	}
}
