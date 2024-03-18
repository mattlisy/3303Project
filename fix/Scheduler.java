import java.io.IOException;
import java.net.*;


public class Scheduler {
	
	private DatagramSocket floorSocket, elevatorsocket;

	public Scheduler() {

		try {
			floorSocket = new DatagramSocket(8888);
			elevatorsocket = new DatagramSocket(9999);
		} catch (SocketException se) {
			se.printStackTrace();
			System.exit(1);
		}

	}

	private void floorHandler() {
		DatagramPacket floorPacket;
		byte data[] = new byte[100];
		Structure received;
		while (true) {
			floorPacket = new DatagramPacket(data, data.length);
			
			try {
				floorSocket.receive(floorPacket);

			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}	

			received = Structure.fromByteArray(data);
			
			System.out.println(received);
			
		}
	}

	public void schedulerService() {
		floorHandler();
	}




	public static void main(String[] args) { Scheduler s = new Scheduler(); s.schedulerService();

	}


}



