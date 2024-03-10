/**
 * Floor class that has the following functionalities:
 * Receives input on time, floor source, floor button, and floor destination
 * Puts those input values in a data structure
 * Sends the structure to the scheduler
 *
 * @author Alizée Drolet
 * @version 2
 */
import java.io.*;
import java.net.*;
import java.text.ParseException;
import java.util.Scanner;

public class Floor {

    DatagramPacket sendPacket;

    DatagramSocket sendSocket;
    private final String fileName = "test.csv";

    public Floor(){
        try {
            sendSocket = new DatagramSocket();
        } catch (SocketException se) {
            se.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * executed on thread creation
     */
    public void floorService(){
        try{
            Scanner sc = new Scanner(new File(fileName));
            sc.nextLine(); //skips first header line
            while(sc.hasNextLine()){
                Structure struct = processInput(sc.nextLine());
                if(struct != null) {
                    //send values to scheduler
                    floorDataEcho(struct);
                }
            }
            sc.close();
        }catch(FileNotFoundException | ParseException e){
            Thread.currentThread().interrupt();
        }
    }

    /**
     * processInput processes a line in the input file to split it in terms of:
     * time, floor source, floor button, and floor destination
     *
     * @param line - current line in input file
     * @throws ParseException
     */
    private Structure processInput(String line) throws ParseException {
        String[] values = line.split(","); //split each line by commas
        String[] times = values[0].split(":"); //split time into hours, minutes, seconds, milliseconds

        //boolean that tests if destination floor is lower than source floor when Up is pressed

        boolean lowDestFloor = values[2].equalsIgnoreCase("Up") && (Integer.parseInt(values[3]) < Integer.parseInt(values[1]));
        //boolean that tests if destination floor is higher than source floor when Down is pressed
        boolean highDestFloor = values[2].equalsIgnoreCase("Down") && (Integer.parseInt(values[3]) > Integer.parseInt(values[1]));
        //boolean that tests that there is a Down button on bottom floor and Up button on top floor
        boolean badFloorButton = (Integer.parseInt(values[1]) == 1 && values[2].equalsIgnoreCase("Down")) || (Integer.parseInt(values[1]) == 4 && values[2].equalsIgnoreCase("Up"));
        //boolean that tests if floors are from 1-4
        boolean extremeFloors = Integer.parseInt(values[1]) < 1 || Integer.parseInt(values[1]) > 4 || Integer.parseInt(values[3]) < 1 || Integer.parseInt(values[3]) > 4;

        //if input is not valid, return null
        if (lowDestFloor || highDestFloor || badFloorButton || extremeFloors) {
            return null;
        } else {
            // Convert floor button (values[2]) to FloorButton type
            Structure.FloorButton floorButton = Structure.FloorButton.valueOf(values[2].toUpperCase());
            //retrieve and print data
            System.out.println(Integer.parseInt(times[0]) + ":" + Integer.parseInt(times[1]) + ":" + Integer.parseInt(times[2]) + ":" + Integer.parseInt(times[3])); //time
            System.out.println(values[1]); //floor source
            System.out.println(values[2]); //floor button (up/down)
            System.out.println(values[3]); //floor destination
            System.out.println("----------------");
            //put values in data struct
            return new Structure(Integer.parseInt(times[0]), Integer.parseInt(times[1]), Integer.parseInt(times[2]), Integer.parseInt(times[3]), Integer.parseInt(values[1]), floorButton, Integer.parseInt(values[3]));
        }
    }

    /**
     * Sends the input data to the scheduler and prints the data received from the scheduler
     * @param struct - input data to be sent to the scheduler
     */
    public void floorDataEcho(Structure struct){

        byte msg[] = struct.toByteArray();

        try {

            sendPacket = new DatagramPacket(msg, msg.length, InetAddress.getLocalHost(), 23); // inetaddress should be replaced with server ip.
            System.out.println("\nElevator: Sending packet");
            System.out.println("To host: " + sendPacket.getAddress());
            System.out.println("Destination host port: " + sendPacket.getPort());

        } catch (UnknownHostException e) {
            e.printStackTrace();
            System.exit(1);
        }


        // send packet
        try {
            sendSocket.send(sendPacket);
            System.out.println("Sending...");
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }


    }

    public static void main(String args[]) throws SocketException {
        Floor floor = new Floor();
        floor.floorService();
    }
}
