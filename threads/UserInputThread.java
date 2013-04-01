package threads;

import datatypes.FileDescriptor;
import datatypes.LocalIdentifier;
import datatypes.LocalIdentifierContainer;
import datatypes.RemoteIdentifier;
import exceptions.InvalidFile;
import exceptions.MaxAttemptsReached;
import gui.FileChooserFrame;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.security.NoSuchAlgorithmException;
import java.util.Set;
import messages.GetChunk;

import messages.PutChunk;

public class UserInputThread extends Thread
{
    // Multicast definitions
    private MulticastSocket outputMDBSocket;
    private MulticastSocket outputMCSocket;
    // Keep Local Chunks...
    public static LocalIdentifierContainer localFiles;

    public UserInputThread() throws IOException
    {
        outputMDBSocket = new MulticastSocket(MDBThread.multicastPort);
        outputMDBSocket.setTimeToLive(1);

        outputMCSocket = new MulticastSocket(MCThread.multicastPort);
        outputMCSocket.setTimeToLive(1);

        localFiles = new LocalIdentifierContainer();
    }

    public void doBackup(String filename, String path, int replicationDegree) throws InterruptedException, IOException, NoSuchAlgorithmException, MaxAttemptsReached
    {
        // Example Backup File...
        FileDescriptor toBackup = new FileDescriptor(path);

        System.out.println(toBackup.getName());
        // Dividing the file into chunks
        toBackup.breakToChunks();

        // ======================= //

        FileDescriptor sentChunks = new FileDescriptor(FileDescriptor.sentChunkDir + "/" + toBackup.getSHA256filenameHash());

        File[] allfiles = sentChunks.listFiles();

        System.out.println("Number of chunks:" + allfiles.length);
        // Open Socket to start Multicasting the PUTCHUNK requests, one per chunk.
        InetAddress MDBAddress = InetAddress.getByName(MDBThread.multicastAddress);

        PutChunk pc = null;
        for (int i = 0; i < allfiles.length; i++)
        {
            // Vars for retrying, if needed...
            int putChunkAttempt = 0;
            int interval = 500;

            FileChooserFrame.log.append("Sending ChunkNo " + i + " - " + allfiles[i]);
            pc = new PutChunk(allfiles[i].getPath());
            while (MCThread.remoteChunks.numberOfReplicas(new RemoteIdentifier(pc.getFileId(), pc.getChunkNo(), null)) < pc.getReplicationDegree())
            {
                outputMDBSocket.send(new DatagramPacket(pc.toString().getBytes(), pc.toString().length(), MDBAddress, MDBThread.multicastPort));
                Thread.sleep(interval);

                if (putChunkAttempt >= 5)
                {
                    throw new MaxAttemptsReached();
                }
                else
                {
                    putChunkAttempt++;
                    interval *= 2;
                }
            }
        }

        localFiles.addLocalIdentifier(new LocalIdentifier(pc.getFileId(), filename, Integer.toString(pc.getReplicationDegree())));
        FileChooserFrame.log.append("==== Finished. ====");
    }

    public void doRestore(String filename) throws InvalidFile, IOException
    {
        // First, search the filename on local files
        LocalIdentifier lfile = localFiles.getIdentifierByFilename(filename);
        String hashname;

        if (lfile != null)
        {
            hashname = lfile.getFilenameHash();
        }
        else
        {
            throw new InvalidFile();
        }

        // Now, get the related chunk identifiers
        Set<RemoteIdentifier> ris = MCThread.remoteChunks.getIdentifiersByHash(hashname);

        if (ris.isEmpty())
        {
            throw new InvalidFile();
        }

        // For each identifier, send a GETCHUNK msg to the MC channel
        for (RemoteIdentifier r : ris)
        {
            GetChunk msg = new GetChunk(r.getFilenameHash(), r.getNumber());
            outputMCSocket.send(new DatagramPacket(msg.toString().getBytes(),msg.toString().getBytes().length));
            
        }
        
        
    }

    /*to send Delete Messages*/
    public void sendDeleteMessage(String filename_encoded) throws IOException
    {
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