import java.net.*;
import java.util.Scanner;
import java.io.*;

public class Floor {

	private final String fileName = "test.csv";
	private DatagramSocket socket;
		
	public Floor() {
		try { 
			socket = new DatagramSocket();
		} catch (SocketException se) {
			se.printStackTrace();
			System.exit(1);

		}
		
	}


	public void floorService() {
		parseInputFile();

	}

	private void parseInputFile() {
		try {
			Scanner sc = new Scanner(new File(fileName)); 
			sc.nextLine();
			while(sc.hasNextLine()) {
				String line = sc.nextLine();
				String[] values = line.split(",");	
				try {
					Structure temp = new Structure(values[0], values[1], values[2], values[3]);
					sendtoServer(temp);
				} catch (IllegalArgumentException e) {
					System.out.println("\nInvalid line: " + line);
					e.printStackTrace();
				}

			}
			sc.close();

		} catch (FileNotFoundException e)  {
			e.printStackTrace();
			System.exit(1);
		}	
	}

	private void sendtoServer(Structure input) {
		DatagramPacket sendPacket = null; 
		System.out.println("\n==========================================================================================================\nSending "+ input + " to Scheduler");
		byte[] data = input.toByteArray();

		try {
			sendPacket = new DatagramPacket(data, data.length, InetAddress.getLocalHost(), 8888);

		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.exit(1);
		}

		try {
			System.out.println("Sending...\n==========================================================================================================");
			socket.send(sendPacket);

		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}		
		
	}

		

	public static void main(String[] args) {
		Floor f = new Floor();	
		f.floorService();
	}
}
