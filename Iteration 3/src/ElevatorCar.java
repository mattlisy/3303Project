/**
 * Elevator client class that checks for new jobs
 * and controls the elevator mechanisms.
 *
 * @author Matthew Lisy
 * @version 3
 */
import java.io.*;
import java.nio.ByteBuffer;
import java.net.*;
import java.util.Arrays;
public class ElevatorCar
{

    DatagramPacket sendElevatorPacket, receiveServerPacket;
    DatagramSocket sendSocket, receiveSocket;


    /**
     * ElevatorCar constructor.
     */
    public ElevatorCar() throws SocketException {
        sendSocket = new DatagramSocket();
        receiveSocket = new DatagramSocket();
    }

    private static byte[] intToBytes(int value) {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.putInt(value);
        return buffer.array();
    }

    public void requestWork() {


        try {
            InetAddress serverAddress = InetAddress.getByName("martas-MacBook-Air.local"); // Use the actual hostname of the server

            byte sendfloor[] = intToBytes(1);
           sendElevatorPacket = new DatagramPacket(sendfloor, sendfloor.length, serverAddress, 69);


            System.out.println("\nElevator: Sending packet");
            System.out.println("To host: " + sendElevatorPacket.getAddress());
            System.out.println("Destination host port: " + sendElevatorPacket.getPort());
            int len = sendElevatorPacket.getLength();
            String packetdata = new String(sendElevatorPacket.getData(),0,len, java.nio.charset.StandardCharsets.ISO_8859_1);
            System.out.println("String representation: " + packetdata);
            System.out.print("Byte Representation:\n");
            for (byte b : packetdata.getBytes()) {
                System.out.print(b + "-");
            }
            System.out.println();
            try {
                sendSocket.send(sendElevatorPacket);
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }

            System.out.println("ElevatorCar: packet sent");


        } catch (UnknownHostException e) {
            System.out.println("UnknownHostException: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }


    }


    /**
     * Performs elevator service, serving floor requests and notifying the scheduler.
     * It simulates elevator movement by sleeping for a fixed duration.
     */
    public synchronized void elevatorService() {
        requestWork();
        // get data from scheduler
        byte data[] = new byte[100];
        receiveServerPacket = new DatagramPacket(data, data.length);
        System.out.println("ElevatorCar: waiting for packet");
        try {
            receiveSocket.receive(receiveServerPacket);
        } catch (IOException e) {
            System.out.print("IO Exception: Receive Socket Timed Out.\n" + e);
            e.printStackTrace();
            System.exit(1);
        }


        System.out.println("\nElevatorCar: Packet received:");
        System.out.println("From host: " + receiveServerPacket.getAddress());
        System.out.println("Host port: " + receiveServerPacket.getPort());

        Structure receivedData = Structure.fromByteArray(data);


        System.out.println(receivedData.getTime()); //time
        System.out.println(receivedData.getFloorSource()); //floor source
        System.out.println(receivedData.getFloorButton()); //floor button (up/down)
        System.out.println(receivedData.getFloorDestination()); //floor destination
        System.out.println("----------------");

        // simulate elevator moving
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // msg scheduler elevator served floor
        byte sendfloor[] = intToBytes(receivedData.getFloorSource());


        sendElevatorPacket = new DatagramPacket(sendfloor, sendfloor.length, receiveServerPacket.getAddress(), receiveServerPacket.getPort());


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
            sendSocket.send(sendElevatorPacket);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        System.out.println("ElevatorCar: packet sent");
    }
    /**
     * Main method to create a Server object and invoke the ReceiveAndSend method.
     *
     * @param args Command-line arguments (not used)
     */
    public static void main(String args[]) throws SocketException {
        ElevatorCar elevatorCar = new ElevatorCar();
        elevatorCar.elevatorService();
    }
}
