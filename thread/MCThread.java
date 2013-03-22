package thread;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MCThread extends Thread
{
    private static int storedMsg = 0;
    private static int getchunkMsg = 1;
    private static int deleteMsg = 2;
    private static int removedMsg = 3;
    
    private MulticastSocket inputSocket;
    public static int multicastPort = 8000;

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
