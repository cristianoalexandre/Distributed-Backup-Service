package thread;

import cli.Menu;
import datatypes.FileDescriptor;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import messages.PutChunk;

public class UserInputThread extends Thread
{
    // Multicast definitions
    private MulticastSocket outputMDBSocket;

    public UserInputThread() throws IOException
    {
        outputMDBSocket = new MulticastSocket(MDBThread.multicastPort);
        outputMDBSocket.setTimeToLive(1);
    }

    @Override
    public void run()
    {
        
         // Asking the user what to do
         for (;;)
         {
         switch (Menu.ask())
         {
         case Menu.backup:
         doBackup();
         break;
         default:
         System.exit(-1);
         }
         }
    }

    private void doBackup()
    {
        try
        {
            // Example Backup File...
            FileDescriptor toBackup = new FileDescriptor(FileDescriptor.backupDir + "/multicast.pdf");
            System.out.println(toBackup.exists());

            // Getting the file id - hashing through SHA-256
            System.out.println(toBackup.getSHA256filenameHash());

            // Dividing the file into chunks
            toBackup.breakToChunks();

            // ======================= //
            PutChunk pc = new PutChunk(FileDescriptor.sentChunkDir + "/" + toBackup.getSHA256filenameHash() + "/" + toBackup.getSHA256filenameHash() + "_000001");

            // Open Socket to start Multicasting the PUTCHUNK requests, one per chunk.
            InetAddress MDBAddress = InetAddress.getByName(MDBThread.multicastAddress);
            outputMDBSocket.send(new DatagramPacket(pc.toString().getBytes(), pc.toString().length(), MDBAddress, MDBThread.multicastPort));
        }
        catch (NoSuchAlgorithmException | IOException ex)
        {
            Logger.getLogger(UserInputThread.class.getName()).log(Level.SEVERE, null, ex);

        }
    }
}