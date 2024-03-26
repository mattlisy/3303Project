/**
 * Elevator client class that checks for new jobs
 * and controls the elevator mechanisms.
 *
 * @author Alain Xu
 * @author Janice
 * @version 3
 */

import java.util.Comparator;
import java.util.stream.Collectors;
import java.io.*;
import java.nio.ByteBuffer;
import java.net.*;
import java.util.Stack;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;



public class ElevatorCar
{
	// Networking
	private DatagramSocket sendSocket, receiveSocket;
	private final InetAddress schedulerAddress;
	private final int schedulerPort;
	// Networking
	private DatagramSocket socket;
	private static InetAddress schedulerAddress;
	private static int schedulerPort;

	// Lamps
	private int directionLamp;  // 0 = off, 1 = up, -1 = down
	private boolean[] buttonLamps;  // false = off, true = on

	// States
	private int currentFloor;   // Floor 1 is assumed to be the lowest floor.
				    // Any negative floors is assumed to be an error
				    // and the elevator will shutdown. Zero is assumed
				    // to either be between floors or an invalid state.
	boolean direction;
	boolean doorState;  // false = door closed, true = door opened
	int doorMotor; // 0 = off, -1 = closing, 1 = opening
	boolean elevatorMotorPower; // false = off, true = on
	int elevatorMotorAcceleration; // 0 = none, 1 = accelerating, -1 = decelerating

	// Flags
	boolean faultDetected;  // Fault detection flag (false = all good, true = WTF BRO)
	boolean elevatorMoving; // false = no, true = yes

	// Elevator Data
	private final Queue<Structure> workQueue;
	private Structure currentJob;
	private final int elevatorId;




	// States
	private int currentFloor;   // Floor 1 is assumed to be the lowest floor.
				    // Any negative floors is assumed to be an error
				    // and the elevator will shutdown. Zero is assumed
				    // to either be between floors or an invalid state.
	boolean doorState;  // false = door closed, true = door opened
	int doorMotor; // 0 = off, -1 = closing, 1 = opening
	boolean elevatorMotorPower; // false = off, true = on
	int elevatorMotorAcceleration; // 0 = none, 1 = accelerating, -1 = decelerating
	boolean faultDetected; // false = all good, true = WTF BRO

	// Elevator Data
	private Stack<KeyPair> workStack;
	private HashMap<KeyPair, ArrayList<Structure>> jobs; 
	private static int elevatorId;


	/**
	 * ElevatorCar constructor.
	 *
	 * @param eId The elevator ID
	 * @param sAddress The IP address of the scheduler.
	 * @param sPort The port number of the scheduler.
	 */
	public ElevatorCar(int eId)
	{
		// Assignments
		// schedulerAddress = sAddress;
		// schedulerPort = sPort;
		elevatorId = eId;

		// Socket building
		try {
			socket = new DatagramSocket(5555);
		} catch(SocketException se) {
			se.printStackTrace();
		}

		workStack = new Stack<>();
		jobs = new HashMap<>();
		// Default state
		currentFloor = 1;   // Assuming the elevator starts at floor 1
		directionLamp = 0;  // Assuming the elevator is at a floor
		buttonLamps = new boolean[] {false, false, false, false};   // Assume no buttons were pressed
		doorMotor = 0;  // Assuming motor is off
		doorState = false;  // Assuming door is closed

		faultDetected = false;  // Assuming no issues at start up
	}


	private static byte[] intToBytes(int value)
	{
		ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
		buffer.putInt(value);
		return buffer.array();
	}



	/**
	 * Receives work requests from the scheduler and adds them to the work queue.
	 */
	private int requestWork(int direction) {
		//  elevatorID - capacity left - what floor is the elevator at? - up/down - special call
		int request = elevatorId * 10000 + 0 * 1000 + currentFloor * 100 + direction * 10 + 1; 
		byte[] data = intToBytes(request);
		DatagramPacket requestPacket = null;
		DatagramPacket receivePacket = null;
		int returnValue  = -1;

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
				returnValue++;
				// add floor source to sort array list 
				KeyPair key = new KeyPair(received.getfloorButton(), received.getfloor
Source());

				workStack.push(key);
				// add structure to map? 
				jobs.computeIfAbsent(key, k -> new ArrayList<>()).add(received);

			} catch (Exception e) {
				ByteBuffer wrapped = ByteBuffer.wrap(data);
				flag = wrapped.getInt();				
			}	
		}
		return returnValue;

	}

	private void updateFloorPosition() {

		int request = elevatorId * 10000 + 0 * 1000 + currentFloor * 100 + 0 * 10 + 2; 
		byte[] data = intToBytes(request);
		DatagramPacket requestPacket = null;
		DatagramPacket receivePacket = null;
		int returnValue  = -1;

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


	/**
	 * Sends pick-up/drop-off notifications to the scheduler.
	 *
	 * @param floor The floor number.
	 */
	private void arrivalSignal(boolean direction)
	{
		// Inform scheduler of pick-up/drop-off	
		int request = elevatorId * 10000 + 0 * 1000 + currentFloor * 100 + (direction ? 1:0) * 10 + 3; 
		byte[] data = intToBytes(request);
		DatagramPacket requestPacket = null;
		DatagramPacket receivePacket = null;
		int returnValue  = -1;

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













	/**
	 * ElevatorCar constructor.
	 *
	 * @param eId The elevator ID
	 * @param ePort The elevator port number
	 * @param sAddress The IP address of the scheduler.
	 * @param sPort The port number of the scheduler.
	 */
	public ElevatorCar(int eId, int ePort, InetAddress sAddress, int sPort) throws SocketException
	{
		// Assignments
		schedulerAddress = sAddress;
		schedulerPort = sPort;
		elevatorId = eId;

		// Socket building
		sendSocket = new DatagramSocket();
		receiveSocket = new DatagramSocket(ePort);

		// Funny queueueueueueueuueueueueueueuueueueueueue
		workQueue = new LinkedList<>();

		// Default state
		currentFloor = 1;   // Assuming the elevator starts at floor 1
		directionLamp = 0;  // Assuming the elevator is at a floor
		buttonLamps = new boolean[] {false, false, false, false};   // Assume no buttons were pressed
									    // (also assuming there are only
									    // four floors)
		doorMotor = 0;  // Assuming motor is off
		doorState = false;  // Assuming door is closed
		direction = true;   // Assuming default up

		// Flags
		faultDetected = false;  // Assuming no issues at start up
		elevatorMoving = false;
	}


	/**
	 * Start the elevator.
	 */
	public void boot()
	{
		System.out.println("Creating elevator " + elevatorId + " UDP receiver and main threads...");
		// Set up the main threads
		Thread requestReciver = new Thread(this::receiveWorkRequests);
		Thread elevatorSimulator = new Thread(this::simulateElevator);

		System.out.println("Running elevator " + elevatorId + " UDP receiver and main threads...");
		// FIRE IN THE HOLE
		requestReciver.start();
		elevatorSimulator.start();
	}


	/**
	 * Receives work requests from the scheduler and request addition to the work queue.
	 */
	private void receiveWorkRequests()
	{
		boolean packetRecevied;

		while (!faultDetected)
		{
			byte[] data = new byte[100];
			DatagramPacket receivePacket = new DatagramPacket(data, data.length);
			packetRecevied = true;

			System.out.println("Elevator " + elevatorId + " waiting for packet...");

			try
			{
				receiveSocket.receive(receivePacket);
			}
			catch (IOException e)
			{
				packetRecevied = false;
				System.out.println("IO Exception: Elevator " + elevatorId + " receive socket timed out. Retrying...");
			}

			if (packetRecevied)
			{
				Structure receivedData = Structure.fromByteArray(data);

				synchronized (workQueue)
				{
					workQueue.offer(receivedData);
					workQueue.notifyAll();
				}

				System.out.println("Elevator " + elevatorId + " received work request: " + receivedData);
			}
		}

		System.out.println("Elevator " + elevatorId + " fault flag detected. Terminating UDP receiver...");
	}


	/**
	 * OVERSIMPLIFIED main elevator controller.
	 */
	private void simulateElevator()
	{
		informScheduler(currentFloor);

		while (!faultDetected)
		{
			synchronized (workQueue)
			{
				while (workQueue.isEmpty())
				{
					try
					{
						wait();
					}
					catch (InterruptedException ignored)
					{
						System.out.println("Interrupt Exception: Elevator " + elevatorId + " fucked up. I don't know what it did, but I'm shutting it down.");
						faultDetected = true;
					}
				}

				currentJob = workQueue.poll();
				workQueue.notifyAll();
			}

			if (!faultDetected)
			{
				int sFloor = currentJob.getfloorSource();
				int dFloor = currentJob.getfloorDestination();
				buttonLamps[sFloor - 1] = true;
				buttonLamps[dFloor - 1] = true;

				if (!elevatorMoving)
				{
					boolean continueDirection = false;

					if (direction)
					{
						for (int i = currentFloor--; i < 3; i++)
						{
							if (buttonLamps[i])
							{
								continueDirection = true;
								Thread moving = new Thread(this::elevatorMoveUp);
								moving.start();
							}
						}

						if (!continueDirection)
						{
							direction = false;
						}
					}
					else
					{
						for (int i = currentFloor--; i > 0; i--)
						{
							if (buttonLamps[i])
							{
								continueDirection = true;
								Thread moving = new Thread(this::elevatorMoveDown);
								moving.start();
							}
						}

						if (!continueDirection)
						{
							direction = true;
						}
					}
				}
			}
		}

		System.out.println("Elevator " + elevatorId + " shutdown.");
	}


	/**
	 * OVERSIMPLIFIED motor up.
	 */
	private void elevatorMoveUp()
	{
		elevatorMoving = true;

		if (!buttonLamps[currentFloor--])
		{
			try
			{
				Thread.sleep(14000);
				currentFloor++;
			}
			catch (InterruptedException e)
			{
				System.out.println("[PLACEHOLDER]");
			}

			if (!buttonLamps[currentFloor--])
			{
				try
				{
					Thread.sleep(5000);
					currentFloor++;
				}
				catch (InterruptedException e)
				{
					System.out.println("[PLACEHOLDER]");
				}

				if (!buttonLamps[currentFloor--])
				{
					try
					{
						Thread.sleep(7000);
						currentFloor++;
					}
					catch (InterruptedException e)
					{
						System.out.println("[PLACEHOLDER]");
					}
				}
			}
		}

		buttonLamps[currentFloor--] = false;
		elevatorMoving = false;
	}


	/**
	 * OVERSIMPLIFIED motor down.
	 */
	private void elevatorMoveDown()
	{
		elevatorMoving = true;

		if (!buttonLamps[currentFloor--])
		{
			try
			{
				Thread.sleep(14000);
				currentFloor--;
			}
			catch (InterruptedException e)
			{
				System.out.println("[PLACEHOLDER]");
			}

			if (!buttonLamps[currentFloor--])
			{
				try
				{
					Thread.sleep(5000);
					currentFloor--;
				}
				catch (InterruptedException e)
				{
					System.out.println("[PLACEHOLDER]");
				}

				if (!buttonLamps[currentFloor--])
				{
					try
					{
						Thread.sleep(7000);
						currentFloor--;
					}
					catch (InterruptedException e)
					{
						System.out.println("[PLACEHOLDER]");
					}
				}
			}
		}

		buttonLamps[currentFloor--] = false;
		elevatorMoving = false;
	}
		/**
		 * Simulates elevator movement and servicing floors.
		 */
		public void simulateElevator()
		{

			int i = 0;
			while (currentFloor > 0)
			{
				System.out.println(workStack);
				System.out.println(jobs);
				System.out.println(i);
				i++;


				if (!workStack.isEmpty())
				{


					// gets work 
					KeyPair floorDestination = workStack.pop(); // direction that is requested, and floor


					// go to that floor source each floor should request for work with direction
					// requestWork(floorDestination.getsecondKey());
					currentFloor++;
					updateFloorPosition();


					// method for moving 



					arrivalSignal(floorDestination.getfirstKey());

					// load passengers 

					// get destinations 

					List<Structure> removedWork = jobs.remove(floorDestination); // Remove the item


					List<Structure> addWork;
					if (floorDestination.getfirstKey()) { // up
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
					}

					// go to closest destination 

					// while going to destination call request for work with direction to see if there are any passenger waiting at floor above or below (dependent on direction) 
					// when arriving at each destination print the structures that a serviced  


				} else {
					int check = requestWork(3);
					// when work to be done
					if (check == -1) {
						try {
							Thread.sleep(1000);
						} catch(InterruptedException e) {
							e.printStackTrace();
						}
					}
				}

			}

			System.out.println("Elevator fault... Emergency shutdown.");
		}

	// <!-----DOOR OPERATIONS-----!>

	/**
	 * Simulates opening the elevator doors.
	 */
	private void doorOpening()
	{
		if ((!doorState) && (doorMotor == 0) && (currentFloor > 0) && (!elevatorMotorPower) && (elevatorMotorAcceleration == 0))
		{
			System.out.println("Doors opening...");
			doorMotor = 1;

			try
			{
				Thread.sleep(2000); // Simulate door opening time
			}
			catch (InterruptedException e)
			{
				faultDetected = true;
				System.out.println("Timer fault...");
			}
		}
		else
		{
			faultDetected = true;
			System.out.println("Invalid state...");
		}

		if (faultDetected)
		{
			doorMotor = 0;
			elevatorMotorPower = false;
			elevatorMotorAcceleration = 0;
			currentFloor = 0;
		}
		else
		{
			doorMotor = 0;
			doorState = true;
			Thread opened = new Thread(this::doorOpened);
			opened.start();
		}
	}

	/**
	 * Simulates opened elevator doors.
	 */
	private void doorOpened()
	{
		/*
		   if ((doorState) && (doorMotor == 0) && (currentFloor > 0) && (!elevatorMotorPower) && (elevatorMotorAcceleration == 0))
		   {
		   System.out.println("Doors opened...");
		   doorMotor = 1;


		   System.out.println("Doors opening...");

		   try
		   {
		//Thread.sleep(putTime); // Simulate door opening time
		   }
		   catch (InterruptedException e)
		   {
		   e.printStackTrace();
		   }
		   System.out.println("Doors closing...");
		   try
		   {
		//Thread.sleep(putTime); // Simulate door closing time
		   }
		   catch (InterruptedException e)
		   {
		   e.printStackTrace();
		   }
		   }
		   else
		   {
		   faultDetected = true;
		   }

		   if (faultDetected)
		   {
		   doorMotor = 0;
		   elevatorMotorPower = false;
		   elevatorMotorAcceleration = 0;
		   currentFloor = 0;
		   System.out.println("Invalid state...");
		   }
		   */
	}




	public static void main(String[] args) {
		ElevatorCar e = new ElevatorCar(1);
		e.simulateElevator();
	}
}
