import java.util.Comparator;
import java.util.stream.Collectors;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.net.*;



public class ElevatorCar implements Runnable {

	private DatagramSocket socket;

	private final int elevatorID;
	private int currentFloor;	
	private int currentDirection; 
	private int currentDestination;

	private UniqueStack<KeyPair> workStack;
	private HashMap<KeyPair, ArrayList<Structure>> jobs;
	private HashMap<Integer, ArrayList<Structure>> destinations;


	public ElevatorCar(int elevatorID) {
		try {
			socket = new DatagramSocket();
		} catch(SocketException se) {
			se.printStackTrace();
		}

		this.elevatorID = elevatorID;
		this.currentFloor = 1;
		this.currentDirection = 1;
		this.workStack = new UniqueStack<>();
		this.jobs = new HashMap<>();
		this.destinations = new HashMap<>();
	}

	private static byte[] intToBytes(int value) {
		ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
		buffer.putInt(value);
		return buffer.array();

	}

	private int requestWork() {
		int request = elevatorID * 100000 + 0 * 10000 + currentFloor * 1000 + currentDirection * 100 + currentDestination*10 + 1;
		byte[] data = intToBytes(request);
		DatagramPacket requestPacket = null;
		DatagramPacket receivePacket = null;
		int isWork = -1;

		try {
			requestPacket = new DatagramPacket(data, data.length, InetAddress.getLocalHost(), 9999);
		} catch(UnknownHostException e) {

			e.printStackTrace();
		}

		try {
			socket.send(requestPacket);
		} catch (IOException e) {
			e.printStackTrace();
		}

		data = new byte[100];
		int flag = 1;
		while(flag > 0) {

			receivePacket = new DatagramPacket(data, data.length);

			try {
				socket.receive(receivePacket);
			} catch(IOException e) {
				e.printStackTrace();
			}

			try {
				Structure received = Structure.fromByteArray(data);
				isWork++;
				// add floor source to sort array list
				KeyPair key = new KeyPair(received.getfloorButton(), received.getfloorSource());
				if (workStack.isEmpty() || workStack.peek().getsecondKey() != received.getfloorSource()) {

					workStack.push(key); // add new work

				} 
				jobs.computeIfAbsent(key, k -> new ArrayList<>()).add(received);

			} catch (Exception e) {
				ByteBuffer wrapped = ByteBuffer.wrap(data);
				flag = wrapped.getInt();
			}
		}
		return isWork;

	}


	private void updateFloorPosition() {

		int request = elevatorID * 100000 + 0 * 10000 + currentFloor * 1000 + currentDirection * 100 + 0*10 + 2;
		byte[] data = intToBytes(request);
		DatagramPacket requestPacket = null;

		try {
			requestPacket = new DatagramPacket(data, data.length, InetAddress.getLocalHost(), 9999);
		} catch(UnknownHostException e) {
			e.printStackTrace();
		}

		try {
			socket.send(requestPacket);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}	

	private void arrivalSignal() {
		// Inform scheduler of pick-up/drop-off
		int request = elevatorID * 100000 + 0 * 10000 + currentFloor * 1000 + currentDirection * 100 + 0* 10 + 3;
		byte[] data = intToBytes(request);
		DatagramPacket requestPacket = null;

		try {
			requestPacket = new DatagramPacket(data, data.length, InetAddress.getLocalHost(), 9999);
		} catch(UnknownHostException e) {
			e.printStackTrace();
		}

		try {
			socket.send(requestPacket);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}	


	private void moveElevator() {
		switch(currentDirection) {

			case 0:
				currentFloor--;
				break;
			case 1:
				currentFloor++;
				break;
		}	
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}


	private void getfloorDestinations(KeyPair work) {

		List<Structure> removedWork = jobs.remove(work); // Remove the item
		if (removedWork == null) {
			return;
		}
		List<Structure> addWork;
		if (work.getfirstKey()) { // up
			addWork = removedWork.stream()
				.sorted(Comparator.nullsLast(Comparator.comparing(Structure::getfloorDestination).reversed()))
				.collect(Collectors.toList()); // Sort the list from greatest to least
		} else { // down
			addWork = removedWork.stream()
				.sorted(Comparator.nullsLast(Comparator.comparing(Structure::getfloorDestination)))
				.collect(Collectors.toList()); // Sort the list from least to greatest
		}

		for (Structure s : addWork) {
			KeyPair push = new KeyPair(s.getfloorButton(), s.getfloorDestination());
			workStack.push(push);
			destinations.computeIfAbsent(s.getfloorDestination(), k -> new ArrayList<>()).add(s);

		}


	}

	private void offloadPassengers() {
		List<Structure> completedJobs = destinations.remove(currentFloor);
		if (completedJobs != null) {
			for (Structure s : completedJobs) {
				System.out.println("Job completed: " + s);
			}
		}
	}

	public void run() {
		int checkifNewWork;
		int i = 0;
		while (currentFloor > 0)
		{

			if (!workStack.isEmpty()) {


				// gets work
				KeyPair work = workStack.peek(); // direction that is requested, and floor
								 // update elevator	
				
				currentDestination = work.getsecondKey();	
				currentDirection = (currentFloor < currentDestination ? 1 : 0);

				// go to that floor source each floor should request for work with direction
				if (currentFloor != currentDestination) {
					checkifNewWork = requestWork();
					if (checkifNewWork != -1) {
						System.out.println("\niteration: " + i + " Elevator ID: " + elevatorID +
								"\ncurrentFloor: " + currentFloor +
								"\ncurrentDestination: " + currentDestination +
								"\ncurrentDirection: " + currentDirection +
								"\nwork stack: " + workStack +
								"\njobs: " + jobs +
								"\ndestinations: " + destinations);

						i++;
						continue;
					} 
				}

				if (currentFloor != currentDestination) {
					moveElevator(); 		
				}

				if (currentFloor == currentDestination) {
					System.out.println("elevator: " + elevatorID + " has arrived at " + currentDestination);
					offloadPassengers();
					workStack.pop();	
					arrivalSignal();
					getfloorDestinations(work);
					work = null;

				}



			} else {

				currentDirection = 2; // direction does not matter elevator in static state
				checkifNewWork = requestWork();
				// when work to be done
				if (checkifNewWork == -1) {
					try {
						Thread.sleep(1000);
					} catch(InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			System.out.println("\niteration: " + i + " Elevator ID: " + elevatorID +
								"\ncurrentFloor: " + currentFloor +
								"\ncurrentDestination: " + currentDestination +
								"\ncurrentDirection: " + currentDirection +
								"\nwork stack: " + workStack +
								"\njobs: " + jobs +
								"\ndestinations: " + destinations);


			i++;


		}
		System.out.println("\nELEVATOR FAULT -> iteration: " + i + " Elevator ID: " + elevatorID +
								"\ncurrentFloor: " + currentFloor +
								"\ncurrentDestination: " + currentDestination +
								"\ncurrentDirection: " + currentDirection +
								"\nwork stack: " + workStack +
								"\njobs: " + jobs +
								"\ndestinations: " + destinations);


			i++;


	}	


	public static void main(String[] args) {
		ElevatorCar e1 = new ElevatorCar(1);
		ElevatorCar e2 = new ElevatorCar(2);

		Thread thread1 = new Thread(e1);
		Thread thread2 = new Thread(e2);

		thread1.start();
		thread2.start();

	}
}
