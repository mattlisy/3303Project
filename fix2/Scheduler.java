import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.List;
import java.util.Optional;
import java.util.Comparator;
import java.util.stream.Stream;
import java.util.ArrayList;



public class Scheduler {

	private DatagramSocket floorSocket, elevatorSocket;
	private ConcurrentHashMap<KeyPair, CopyOnWriteArrayList<Structure>> map;
	private InetAddress floorAddress;

	public Scheduler() {

		map = new ConcurrentHashMap<>();
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

			map.computeIfAbsent(new KeyPair(received.getfloorButton(), received.getfloorSource()), k -> new CopyOnWriteArrayList<>()).add(received);
			System.out.println();
			System.out.println(map);
			floorAddress = floorPacket.getAddress(); 
				
			
		}
	}
	
	private KeyPair calculateKey(int floor, int direction, int elevatorDestination){
		

		Optional<KeyPair> closestKeyPair;
		// directions 
		switch (direction) {

			case 0: // down

				closestKeyPair = map.keySet().stream()
					.filter(keyPair -> !keyPair.getfirstKey() && keyPair.getsecondKey() < floor && keyPair.getsecondKey() >= elevatorDestination)
					.max(Comparator.comparingInt(KeyPair::getsecondKey));

				// Check if the optional contains a value, and if so, retrieve it
				if (closestKeyPair.isPresent()) {
					KeyPair closestElement = closestKeyPair.get();
					System.out.println("Closest element less than " + floor + ": " + closestElement);
					return closestElement;
				} else {
					System.out.println("No element less than " + floor + " found.");
					return new KeyPair(false,-1);
				}

			case 1: // up
				
				closestKeyPair = map.keySet().stream()
					.filter(keyPair -> keyPair.getfirstKey() && keyPair.getsecondKey() > floor && keyPair.getsecondKey() <= elevatorDestination)
					.min(Comparator.comparingInt(KeyPair::getsecondKey));

				// Check if the optional contains a value, and if so, retrieve it
				if (closestKeyPair.isPresent()) {
					KeyPair closestElement = closestKeyPair.get();
					System.out.println("Closest element greater than " + floor + ": " + closestElement);
					return closestElement;
				} else {
					System.out.println("No element greater than " + floor + " found.");
					return new KeyPair(false, -1);
				}



			default: // dont care 
					
				if (map.isEmpty()) {

					System.out.println("No available work");
					return new KeyPair(false, -1);
				}
			
				// filter key that closeest then biggest size
				closestKeyPair = map.keySet().stream()
					.collect(Collectors.groupingBy(
								keyPair -> Math.abs(keyPair.getsecondKey() - floor), // Group by distance to floor
								Collectors.maxBy(Comparator.comparingInt(keyPair -> map.get(keyPair).size())))) // Find key pair with largest list size in each group
					.values().stream()
					.flatMap(opt -> opt.map(Stream::of).orElseGet(Stream::empty)) // Convert Optional to Stream<KeyPair>
					.min(Comparator.comparingInt(keyPair -> Math.abs(keyPair.getsecondKey() - floor))); // Find closest key pair
				



				KeyPair closestElement = closestKeyPair.get();
				System.out.println("Closest element to " + floor + ": " + closestElement);


				return closestElement;


		}


	}

	private byte[] intToBytes(int value) {
		ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
		buffer.putInt(value);
		return buffer.array();
	}

	public void elevatorHandler() {
		DatagramPacket elevatorPacket, responsePacket;
		byte data[] = new byte[100];
		ArrayList<Integer> elevatorFloor = new ArrayList<>();
		int elevatorID, currentFloor, elevatorDirection, elevatorDestination;

		while (true) {
			System.out.println("\nmap: " + map + "\n");

			elevatorPacket = new DatagramPacket(data, data.length);

			try {
				elevatorSocket.receive(elevatorPacket);

			} catch (IOException e) { e.printStackTrace();
				e.printStackTrace();

			}	


			ByteBuffer wrapped = ByteBuffer.wrap(data);

			int call = wrapped.getInt();
			
			/*
			 * how do we want to encode 
			 * elevatorID - capacity left - what floor is the elevator at? - up/down - floor destination - special call 
			 */
			System.out.println("Recieved request from Elevator: " + call);
			switch(call % 10) {


				// request work
				case 1:

					elevatorID = call/100000;

					if (elevatorID >= 0 && elevatorID < elevatorFloor.size() && elevatorFloor.get(elevatorID) <= 0) {
						System.out.println("Elevator "+ elevatorID + "no longer operational");
						break;
					}	

					currentFloor = call/1000 %  10;
					elevatorDirection = call/100 % 10;
					elevatorDestination = call/10 % 10;

					// update elevator postion
					if (elevatorID < elevatorFloor.size()) {
						// If the index is within the bounds of the list, update the existing element
						elevatorFloor.set(elevatorID, currentFloor);
					} else {
						// If the index is beyond the current size of the list, add a new element
						elevatorFloor.add(currentFloor);
					}
					
					System.out.println("Elevator: " + elevatorID + " updated to " + currentFloor);
		
					if (currentFloor <= 0) {
						System.out.println("Elevator " + elevatorID + "no longer operational");
						break;
					}	



					KeyPair serviceFloor = calculateKey(currentFloor, elevatorDirection, elevatorDestination);
					if (serviceFloor.getsecondKey() != -1) {

						List<Structure> sendingFloorStrucutures = map.remove(serviceFloor);

						for (Structure s : sendingFloorStrucutures) {
							data = s.toByteArray(); 
							// send get structure to elevatorcar
							responsePacket = new DatagramPacket(data, data.length, elevatorPacket.getAddress(), elevatorPacket.getPort());		

							try {
								System.out.println("sending jobs");
								elevatorSocket.send(responsePacket);
							} catch (IOException e) {
								e.printStackTrace();
							}	

						}		
					}


					// send end signal 
					data = intToBytes(-1);
					responsePacket = new DatagramPacket(data, data.length, elevatorPacket.getAddress(), elevatorPacket.getPort());		
					try {
						elevatorSocket.send(responsePacket);
					} catch (IOException e) {
						e.printStackTrace();
					}	
					break;

					// update elevatorFloor
				case 2: 

					elevatorID = call/100000;


					if (elevatorID >= 0 && elevatorID < elevatorFloor.size() && elevatorFloor.get(elevatorID) <= 0) {
						System.out.println("Elevator "+ elevatorID + "no longer operational");
						break;
					}	

					currentFloor = call/1000 %  10;
					// update elevator positon
					if (elevatorID < elevatorFloor.size()) {
						// If the index is within the bounds of the list, update the existing element
						elevatorFloor.set(elevatorID, currentFloor);
					} else {
						// If the index is beyond the current size of the list, add a new element
						elevatorFloor.add(currentFloor);
					}

					System.out.println("Elevator: " + elevatorID + " updated to " + currentFloor);

					if (currentFloor <= 0) {
						System.out.println("Elevator "+ elevatorID + "no longer operational");
						break;
					}
					break;
				// elevators arrival at a floor 
				case 3: 

					elevatorID = call/100000;
					if (elevatorID >= 0 && elevatorID < elevatorFloor.size() && elevatorFloor.get(elevatorID) <= 0) {
						System.out.println("Elevator "+ elevatorID + "no longer operational");
						break;
					}

					currentFloor = call/1000 %  10;
					elevatorDirection = call/100 % 10; 

					// update elevator positon	
					if (elevatorID < elevatorFloor.size()) {
						// If the index is within the bounds of the list, update the existing element
						elevatorFloor.set(elevatorID, currentFloor);
					} else {
						// If the index is beyond the current size of the list, add a new element
						elevatorFloor.add(currentFloor);
					}

					System.out.println("Elevator: " + elevatorID + " updated to " + currentFloor);

					if (currentFloor <= 0) {
						System.out.println("Elevator "+ elevatorID + "no longer operational");
						break;
					}


					//send to floor 

					responsePacket = new DatagramPacket(data, data.length, floorAddress, 7777); // floor listens on port 7777

					try {
						System.out.println("Sending to Floor");
						elevatorSocket.send(responsePacket);
					} catch (IOException e) {
						e.printStackTrace();
					}


					/*
					 * this iteration we are skipping floor manual input so we already have all data needed from floor
					 * next iteration we must ask the floor what floor destination passengers want to goto
					 * so we are not recieving data from floor just informing arrival 
					 */
					break;

				default:
					System.out.println("not an implemented call");

			}

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



