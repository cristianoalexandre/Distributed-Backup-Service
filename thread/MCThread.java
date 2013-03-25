package thread;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MCThread extends Thread
{
    private final static int storedMsg = 0;
    private final static int getchunkMsg = 1;
    private final static int deleteMsg = 2;
    private final static int removedMsg = 3;
    
    private MulticastSocket inputSocket;
    public static int multicastPort = 8000;

    public MCThread() throws IOException
    {
        inputSocket = new MulticastSocket(MCThread.multicastPort);
        inputSocket.setTimeToLive(1);
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
            int msgType = parseMsg(msgReceived);
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
        String[] msgArray = receivedMsg.trim().split(" ");    
        
        switch(msgArray[0])
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
