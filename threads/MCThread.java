package threads;

import datatypes.RemoteIdentifierContainer;
import datatypes.RemoteIdentifier;
import exceptions.InvalidMessageArguments;
import gui.FileChooserFrame;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import messages.Stored;

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
    public static RemoteIdentifierContainer remoteChunks;

    public MCThread() throws IOException
    {
        remoteChunks = new RemoteIdentifierContainer();
        
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
            try
            {
                parseMsg(receivedPacket);
            }
            catch (InvalidMessageArguments ex)
            {
                Logger.getLogger(MCThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Parses a Message
     *
     * @param receivedMsg
     * @return
     */
    private int parseMsg(DatagramPacket receivedPacket) throws InvalidMessageArguments
    {
        String[] msgArray = new String(receivedPacket.getData()).split(" ");

        switch (msgArray[0])
        {
            case "STORED":
                Stored msg = Stored.parseMsg(new String(receivedPacket.getData()));
                RemoteIdentifier ri = new RemoteIdentifier(msg.getFileID(), msg.getChunkNo(), receivedPacket.getAddress().toString());
                MCThread.addRemoteIdentifier(ri);
                FileChooserFrame.log.append("Added " + ri.toString());
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

    public static void addRemoteIdentifier(RemoteIdentifier ri)
    {
        remoteChunks.addRemoteIdentifier(ri);
    }
}
