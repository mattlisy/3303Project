import java.net.*;
import java.time.Duration;
import java.nio.ByteBuffer;
import java.time.LocalTime;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Floor {

	private final String fileName;
	private DatagramSocket socket;
	private PriorityQueue<Structure> jobQueue;


	public Floor(String fileName) {
		try {
			socket = new DatagramSocket(7777);
		} catch (SocketException se) {
			se.printStackTrace();
			System.exit(1);
		}
		this.fileName = fileName;
		jobQueue = new PriorityQueue<>();
	

	}


	public void floorService() {
		ExecutorService executorService = Executors.newFixedThreadPool(2);
		executorService.submit(() -> floorListen());
		executorService.submit(() -> parseInputFile());
		Runtime.getRuntime().addShutdownHook(new Thread(executorService::shutdown));
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

					 
					//sendtoServer(temp);
					jobQueue.add(temp);
				} catch (Exception e) {
					System.out.println("\nInvalid line: " + line);
					e.printStackTrace();
				}

			}
			sc.close();

		} catch (FileNotFoundException e)  {
			e.printStackTrace();
			System.exit(1);
		}
		
		popQueue();
	}

	private void popQueue() {
		System.out.println(jobQueue);
		/*
		int i = 0;
		while (!jobQueue.isEmpty()) {
			System.out.println(i + " " + jobQueue.poll() );
			i++;
			
		}
		*/

		while (!jobQueue.isEmpty()) {
			Structure nextJob = jobQueue.poll();
			LocalTime currentTime = LocalTime.now();
			Duration timeUntilJob = Duration.between(currentTime, nextJob.getTime());
			System.out.println("\nProcessing job: " + nextJob);

			if (timeUntilJob.isNegative() || timeUntilJob.isZero()) {
				// If the scheduled time has already passed or is now, process the job
				sendtoServer(nextJob);
				// Perform any other necessary actions here
			} else {
				// If the scheduled time is in the future, sleep until then
				try {
					Thread.sleep(timeUntilJob.toMillis());
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				sendtoServer(nextJob);
			}
			
			System.out.println("Processed job: " + nextJob);
		}
		System.out.println("\nall jobs processed :)");
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

	private void floorListen() {

		DatagramPacket arrivalPacket;
		byte[] data = new byte[100];
		int signal;
		int elevatorID;
		int floorSource;

		while (true) {
			arrivalPacket = new DatagramPacket(data, data.length);
			try {
				socket.receive(arrivalPacket);

			} catch (IOException e) {
				e.printStackTrace();
			}

			ByteBuffer wrapped = ByteBuffer.wrap(data);
			signal = wrapped.getInt();
			elevatorID = signal / 100000;
			floorSource = signal / 1000 % 10;

			System.out.println("Elevator: " + elevatorID + " has arrived at " + floorSource);

		}
	}


	public static void main(String[] args) {
		Floor f = new Floor(args[0]);
		f.floorService();
	}
}
