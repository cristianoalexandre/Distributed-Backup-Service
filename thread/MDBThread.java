package thread;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.logging.Level;
import java.util.logging.Logger;
import messages.Stored;

public class MDBThread extends Thread
{
    // Multicast definitions
    public static final String multicastAddress = "237.1.7.2";
    public static final int multicastPort = 4004;
    private MulticastSocket inputSocket;

    public MDBThread() throws IOException
    {
        inputSocket = new MulticastSocket(MDBThread.multicastPort);
        inputSocket.setTimeToLive(1);
        inputSocket.joinGroup(InetAddress.getByName(multicastAddress));
    }

    @Override
    public void run()
    {
        try
        {
            byte[] receiveData = new byte[1024];

            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            inputSocket.receive(receivePacket);

            String msgReceived = new String(receivePacket.getData());

            System.out.println("MDB - Received: " + msgReceived);

            switch (msgReceived.split(" ")[0])
            {
                case "PUTCHUNK":
                    sendStored(msgReceived);
                    break;
            }
        }
        catch (IOException ex)
        {
            Logger.getLogger(MDBThread.class.getName()).log(Level.SEVERE, null, ex);
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
    }
}
