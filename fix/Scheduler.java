import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Scheduler {
	
	private DatagramSocket floorSocket, elevatorSocket;

	public Scheduler() {

		try {
			floorSocket = new DatagramSocket(8888);
			elevatorSocket = new DatagramSocket(9999);
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
	
	public void elevatorHandler() {
		DatagramPacket elevatorPacket;
		byte data[] = new byte[100];
		while (true) {

			elevatorPacket = new DatagramPacket(data, data.length);
			
			try {
				elevatorSocket.receive(elevatorPacket);
} catch (IOException e) { e.printStackTrace();
				System.exit(1);
			}	

			ByteBuffer wrapped = ByteBuffer.wrap(data);
			int call = wrapped.getInt();
			System.out.println("Recieved request from Elevator: " +call);




		}

	}

	public void schedulerService() {
		ExecutorService executorService = Executors.newFixedThreadPool(2);
		executorService.submit(() -> elevatorHandler());
		executorService.submit(() -> floorHandler());
		
		Runtime.getRuntime().addShutdownHook(new Thread(executorService::shutdown));

	}




	public static void main(String[] args) { 
		Scheduler s = new Scheduler(); 
		s.schedulerService();

	}
}



