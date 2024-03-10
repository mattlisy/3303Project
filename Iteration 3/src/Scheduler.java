import java.io.*;
import java.net.*;
import java.util.PriorityQueue;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
public class Scheduler {

    private PriorityQueue<Structure> floorRequests;
    DatagramSocket floorSocket, elevatorSocket;


    public Scheduler(){
        floorRequests = new PriorityQueue<Structure>();
        try {
            elevatorSocket = new DatagramSocket(69);
            floorSocket = new DatagramSocket(23);
        } catch (SocketException se) {
            se.printStackTrace();
            System.exit(1);
        }
    }

    public void receiveFloorData(){
        DatagramPacket receiveFloorPacket;
        byte data[];
        Structure temp;
        while(true) {
            data = new byte[100];
            receiveFloorPacket = new DatagramPacket(data, data.length);

            // get the packet
            try {
                System.out.println("Waiting..."); // so we know we're waiting
                floorSocket.receive(receiveFloorPacket);

            } catch (IOException e) {
                System.out.print("IO Exception: likely:");
                System.out.println("Receive Socket Timed Out.\n" + e);
                e.printStackTrace();
                System.exit(1);
            }

            // put packet in Structure
            temp = Structure.fromByteArray(data);

            // add Structure to pQ
            floorRequests.add(temp);
            System.out.println("recieved floor data");

        }
    }

    public void serveElevator() {
        DatagramPacket receiveREQPacket,sendElevatorPacket,receiveCMPLPacket;
        byte data[] = new byte[100];
        Structure temp;
        while(true) {
            // see if there are elevators to send work to
            int elevatorsREQ = 0;
            receiveREQPacket = new DatagramPacket(data, data.length);
            while(elevatorsREQ == 0) {
                // get the packet
                try {
                    System.out.println("Elevator Waiting..."); // so we know we're waiting
                    floorSocket.receive(receiveREQPacket);
                    System.out.println("get something");
                    elevatorsREQ += 1;
                } catch (IOException e) {
                    System.out.print("IO Exception: likely:");
                    System.out.println("Receive Socket Timed Out.\n" + e);
                    e.printStackTrace();
                    System.exit(1);
                }
                System.out.println("looping");
            }

            // send work to elevator
            System.out.println("outside of loop");
            temp = floorRequests.poll();
            System.out.println("1");
            data = temp.toByteArray();

            System.out.println("2");
            sendElevatorPacket = new DatagramPacket(data, data.length, receiveREQPacket.getAddress(), receiveREQPacket.getPort());

            System.out.println("\nElevator: Sending packet");
            System.out.println("To host: " + sendElevatorPacket.getAddress());
            System.out.println("Destination host port: " + sendElevatorPacket.getPort());
            int len = sendElevatorPacket.getLength();
            String packetdata = new String(sendElevatorPacket.getData(),0,len, java.nio.charset.StandardCharsets.ISO_8859_1);
            System.out.print("Byte Representation:\n");
            for (byte b : packetdata.getBytes()) {
                System.out.print(b + "-");
            }
            System.out.println();
            try {
                elevatorSocket.send(sendElevatorPacket);
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }

            System.out.println("ElevatorCar: packet sent");


            // get job complete by elevator

            receiveCMPLPacket = new DatagramPacket(data, data.length);

            // get the packet
            try {
                System.out.println("Waiting..."); // so we know we're waiting
                floorSocket.receive(receiveCMPLPacket);

            } catch (IOException e) {
                System.out.print("IO Exception: likely:");
                System.out.println("Receive Socket Timed Out.\n" + e);
                e.printStackTrace();
                System.exit(1);
            }

            System.out.println("Elevator has Serviced Floor" + ByteBuffer.wrap(data).getInt());


        }
    }


    public void Server() {
        // need 2 client handlers one for floor other for elevator
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        executorService.submit(() -> receiveFloorData());
        executorService.submit(() -> serveElevator());

        Runtime.getRuntime().addShutdownHook(new Thread(executorService::shutdown));
    }

    public static void main(String args[]) {
        Scheduler scheduler = new Scheduler();
        scheduler.Server();
    }
}
