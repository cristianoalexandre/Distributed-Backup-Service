package threads;

import gui.FileChooserFrame;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MCThread extends Thread
{
    // Message types
    private final static int storedMsg = 0;
    private final static int getchunkMsg = 1;
    private final static int deleteMsg = 2;
    private final static int removedMsg = 3;
    // Multicast definitions
    public static final String multicastAddress = "237.1.7.4";
    public static final int multicastPort = 4006;
    private MulticastSocket inputSocket;
    // Random number generator
    private Random rgen;

    public MCThread() throws IOException
    {
        rgen = new Random();
        
        inputSocket = new MulticastSocket(MCThread.multicastPort);
        inputSocket.setTimeToLive(1);
        inputSocket.joinGroup(InetAddress.getByName(multicastAddress));
    }

    @Override
    public void run()
    {
        for (;;)
        {
            byte[] receiveData = new byte[1024];

            // Receives a Message
            DatagramPacket receivedPacket = new DatagramPacket(receiveData, receiveData.length);
            try
            {
                inputSocket.receive(receivedPacket);
            }
            catch (IOException ex)
            {
                Logger.getLogger(UserInputThread.class.getName()).log(Level.SEVERE, null, ex);
            }
            String msgReceived = new String(receivedPacket.getData());

            FileChooserFrame.log.append("MC - Received: " + msgReceived + "\n");
        }
    }

    /**
     * Parses a Message
     *
     * @param receivedMsg
     * @return
     */
    private int parseMsg(String receivedMsg)
    {
        String[] msgArray = receivedMsg.split(" ");

        switch (msgArray[0])
        {
            case "STORED":

                break;
            case "GETCHUNK":

                break;
            case "DELETE":

                break;
            case "REMOVE":

                break;
        }

        return 0;
    }
}
