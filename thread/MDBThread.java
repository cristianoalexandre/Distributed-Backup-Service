package thread;

import gui.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import messages.Stored;

public class MDBThread extends Thread
{
    // Multicast incoming definitions
    public static final String multicastAddress = "237.1.7.2";
    public static final int multicastPort = 4004;
    private final MulticastSocket inputSocket;
    // MC multicast
    private final MulticastSocket mcSocket;
    // Random number generator
    private Random rgen;
    

    public MDBThread() throws IOException
    {
        inputSocket = new MulticastSocket(MDBThread.multicastPort);
        inputSocket.setTimeToLive(1);
        inputSocket.joinGroup(InetAddress.getByName(multicastAddress));

        mcSocket = new MulticastSocket(MCThread.multicastPort);
        mcSocket.setTimeToLive(1);
        mcSocket.joinGroup(InetAddress.getByName(MCThread.multicastAddress));

        rgen = new Random();
    }

    @Override
    public void run()
    {
    	for(;;){
        try
        {
            byte[] receiveData = new byte[1024];

            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            inputSocket.receive(receivePacket);

            String msgReceived = new String(receivePacket.getData());

            //System.out.println("MDB - Received: " + msgReceived);
            FileChooserFrame.log.append("MDB - Received: " + msgReceived + "\n");

            switch (msgReceived.split(" ")[0])
            {
                case "PUTCHUNK":
                    parseStored(msgReceived);
                    break;
            }
        }
        catch (IOException | InterruptedException ex)
        {
            Logger.getLogger(MDBThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    	}
    }

    private void sendStored(String msgReceived) throws IOException
    {
        String[] msgSplitted = msgReceived.split(" ");

        Stored st = new Stored(msgSplitted[2], msgSplitted[3]);
        MulticastSocket outputSocket = new MulticastSocket(MCThread.multicastPort);
        outputSocket.setTimeToLive(1);
        InetAddress MCAddress = InetAddress.getByName(MCThread.multicastAddress);
        outputSocket.send(new DatagramPacket(st.toString().getBytes(), st.toString().length(), MCAddress, MCThread.multicastPort));
        //System.out.println("MDB - Sent: " + st);
        FileChooserFrame.log.append("MDB - Sent: " + st + "\n");
    }

    private void parseStored(String msgReceived) throws IOException, InterruptedException
    {
        // Selecting a random time to sleep - give time for other threads to volunteer!
        int sleepTime = rgen.nextInt(400);
       // System.out.println("MDB - Going to sleep for " + sleepTime + " ms...");
        FileChooserFrame.log.append("MDB - Going to sleep for " + sleepTime + " ms..." + "\n");

        // Before falling asleep, launch a thread to monitor how many STORED have been sent to the network
        final Queue<String> storedHosts = new PriorityQueue<>();
        CounterThread ct = new CounterThread(storedHosts);
        ct.start();

        // Falling asleep...
        Thread.sleep(sleepTime);

        ct.finish();
        //ct.join();

        // Now, time to decide if storing or not!
       // System.out.println(storedHosts.size());
       // System.out.println("Hosts stored: " + storedHosts.element());

        //
        
        sendStored(msgReceived);
    }

    public class CounterThread extends Thread
    {
        private Queue<String> storedHosts;
        private boolean stopFlag;

        public CounterThread(Queue<String> storedHosts)
        {
            this.storedHosts = storedHosts;
            stopFlag = false;
        }

        @Override
        public void run()
        {
            while (!stopFlag)
            {
                try
                {
                    byte[] receiveData = new byte[1024];
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

                    mcSocket.receive(receivePacket);
                    //System.out.println("MDB - Received (MC Channel): " + new String(receivePacket.getData()));
                    FileChooserFrame.log.append("MDB - Received (MC Channel): " + new String(receivePacket.getData()) + "\n");
                    
                    storedHosts.add(receivePacket.getAddress().toString());
                    //System.out.println(storedHosts.size());
                }
                catch (IOException ex)
                {
                    Logger.getLogger(MDBThread.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        public void finish()
        {
            stopFlag = true;
        }
    }
}
