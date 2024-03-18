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

	public void requstWork() {
		DatagramPacket reqPacket = null; 		
		
		try {
			byte[] reqMsg = intToBytes(1111);
			reqPacket = new DatagramPacket(reqMsg, reqMsg.length, InetAddress.getLocalHost(), 9999);

		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.exit(1); 
		}

		try 
	


		


	}


}
