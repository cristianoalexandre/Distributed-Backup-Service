package threads;

import datatypes.FileDescriptor;
import datatypes.RemoteIdentifier;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

import messages.PutChunk;

public class UserInputThread extends Thread
{
    // Multicast definitions
    private MulticastSocket outputMDBSocket;
    private MulticastSocket outputMCSocket;

    public UserInputThread() throws IOException
    {
        outputMDBSocket = new MulticastSocket(MDBThread.multicastPort);
        outputMDBSocket.setTimeToLive(1);
        
        outputMCSocket = new MulticastSocket(MCThread.multicastPort);
        outputMCSocket.setTimeToLive(1);
    }

    public void doBackup(String filename, String path, int replicationDegree)
    {
        try
        {
            // Example Backup File...
            FileDescriptor toBackup = new FileDescriptor(path);

            // Dividing the file into chunks
            toBackup.breakToChunks();

            // ======================= //

            FileDescriptor sentChunks = new FileDescriptor(FileDescriptor.sentChunkDir + "/" + toBackup.getSHA256filenameHash());

            File[] allfiles = sentChunks.listFiles();

            // Open Socket to start Multicasting the PUTCHUNK requests, one per chunk.
            InetAddress MDBAddress = InetAddress.getByName(MDBThread.multicastAddress);

            // Repeat the cycle as many times as needed (replication degree!).
            for (int k = 0; k < replicationDegree; k++)
            {
                for (int i = 0; i < allfiles.length; i++)
                {
                    PutChunk pc = new PutChunk(allfiles[i].getPath());

                    outputMDBSocket.send(new DatagramPacket(pc.toString().getBytes(), pc.toString().length(), MDBAddress, MDBThread.multicastPort));
                }
            }

        }
        catch (NoSuchAlgorithmException | IOException ex)
        {
            Logger.getLogger(UserInputThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /*to send Delete Messages*/
    public void sendDeleteMessage(String filename_encoded) throws IOException{
    	
    	String toDelete_message = "DELETE " + filename_encoded + "\r\n\r\n";
    	
    	InetAddress MCAddress = InetAddress.getByName(MCThread.multicastAddress);
    	
    	/*send toDelete_message*/
    	outputMCSocket.send(new DatagramPacket(toDelete_message.getBytes(), toDelete_message.length(), MCAddress, MCThread.multicastPort));
    	
    	
    }

    @Override
    public void run()
    {
    }
}