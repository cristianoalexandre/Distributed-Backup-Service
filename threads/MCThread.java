package threads;

import datatypes.FileDescriptor;
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
import messages.Chunk;
import messages.GetChunk;
import messages.Stored;

public class MCThread extends Thread
{
    // Multicast definitions
    public static final String multicastAddress = "237.1.7.4";
    public static final int multicastPort = 4006;
    private MulticastSocket inputSocket;
    private MulticastSocket MDRSocket;
    // Random number generator
    private Random rgen;
    public static RemoteIdentifierContainer remoteChunks;
    // Boolean flag to control Chunk answers
    private boolean neededChunk = false;

    public MCThread() throws IOException
    {
        remoteChunks = new RemoteIdentifierContainer();

        rgen = new Random();

        inputSocket = new MulticastSocket(MCThread.multicastPort);
        inputSocket.setTimeToLive(1);
        inputSocket.joinGroup(InetAddress.getByName(multicastAddress));
        
        MDRSocket = new MulticastSocket(MDRThread.multicastPort);
        MDRSocket.setTimeToLive(1);
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
            catch (InvalidMessageArguments | IOException ex)
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
    private int parseMsg(DatagramPacket receivedPacket) throws InvalidMessageArguments, IOException
    {
        String[] msgArray = new String(receivedPacket.getData()).split(" ");

        switch (msgArray[0])
        {
            case "STORED":
                Stored storedMsg = Stored.parseMsg(new String(receivedPacket.getData()));
                RemoteIdentifier ri = new RemoteIdentifier(storedMsg.getFileID(), storedMsg.getChunkNo(), receivedPacket.getAddress().toString());
                MCThread.addRemoteIdentifier(ri);
                FileChooserFrame.log.append("Added " + ri.toString());
                break;
            case "GETCHUNK":
                GetChunk getChunkMsg = GetChunk.parseMsg(new String(receivedPacket.getData()));

                // Does the message apply to me?
                if (remoteChunks.hasIdentifier(new RemoteIdentifier(getChunkMsg.getFileID(), getChunkMsg.getChunkNo(), receivedPacket.getAddress().toString())))
                {
                    System.out.println(receivedPacket.getAddress());
                    System.out.println(InetAddress.getByAddress(InetAddress.getLocalHost().getAddress()));
                    if (receivedPacket.getAddress().toString().equals(InetAddress.getByAddress(InetAddress.getLocalHost().getAddress()).toString()))
                    {
                        // Someone needs a chunk, start the loop!
                        neededChunk = true;
                        System.out.println("I have a packet!");
                        new MCChunkThread(getChunkMsg).start();
                    }
                    else
                    {
                        // Someone was faster to answer than us, disable the loop.
                        neededChunk = false;
                    }
                }

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

    public class MCChunkThread extends Thread
    {
        private GetChunk chunkMsg;
        private String chunkData;

        public MCChunkThread(GetChunk chunkMsg) throws IOException
        {
            this.chunkMsg = chunkMsg;

            System.out.println(FileDescriptor.receivedChunkDir + "/" + chunkMsg.getFileID() + "/"+ chunkMsg.getFileID() + "_" + chunkMsg.getChunkNo());
            
            // Time to get the chunk contents, then.
            chunkData = FileDescriptor.readFile(FileDescriptor.receivedChunkDir + "/" + chunkMsg.getFileID() + "/"+ chunkMsg.getFileID() + "_" + chunkMsg.getChunkNo());
        }

        @Override
        public void run()
        {
            try
            {
                // Wait random time between 0 and 400ms.
                int waitTime = rgen.nextInt(400);
                Thread.sleep(waitTime);

                // Finally, answer the query, if nobody has answered yet.
                if (neededChunk)
                {
                    Chunk msg = new Chunk(chunkMsg.getFileID(), chunkMsg.getProtocolVersion(), chunkMsg.getChunkNo(), chunkData);

                    MDRSocket.send(new DatagramPacket(msg.toString().getBytes(), msg.toString().getBytes().length, InetAddress.getByName(MDRThread.multicastAddress),MDRThread.multicastPort));

                    neededChunk = false;
                }
            }
            catch (InterruptedException | IOException ex)
            {
                Logger.getLogger(MCThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
