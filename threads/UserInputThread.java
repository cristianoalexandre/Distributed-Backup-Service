package threads;

import datatypes.FileDescriptor;
import datatypes.LocalIdentifier;
import datatypes.LocalIdentifierContainer;
import datatypes.RemoteIdentifier;
import datatypes.RemoteIdentifierContainer;
import exceptions.InvalidFile;
import exceptions.InvalidMessageArguments;
import exceptions.MaxAttemptsReached;
import gui.FileChooserFrame;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import messages.Chunk;
import messages.GetChunk;

import messages.PutChunk;
import static threads.MDRThread.multicastAddress;

public class UserInputThread extends Thread
{
    // Multicast definitions
    private MulticastSocket outputMDBSocket;
    private MulticastSocket outputMCSocket;
    // Keep Local Chunks...
    public static LocalIdentifierContainer localFiles;
    private boolean MDRChunkReceived;
    // Which files have I requested?
    public static ArrayList<String> recoveringFiles;
    

    public UserInputThread() throws IOException
    {
        recoveringFiles = new ArrayList<>();
        MDRChunkReceived = false;

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

    public void doRestore(String filename) throws InvalidFile, IOException, InterruptedException
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
        HashSet<RemoteIdentifier> ris = (HashSet<RemoteIdentifier>) MCThread.remoteChunks.getIdentifiersByHash(hashname);
        
        if (ris.isEmpty())
        {
            throw new InvalidFile();
        }        

        // For each identifier, send a GETCHUNK msg to the MC channel
        recoveringFiles.add(hashname);
        for (RemoteIdentifier r : ris)
        {
            while (!MDRChunkReceived)
            {              
                // Send the GetChunk request
                GetChunk msg = new GetChunk(r.getFilenameHash(), r.getNumber());
                outputMCSocket.send(new DatagramPacket(msg.toString().getBytes(), msg.toString().getBytes().length, InetAddress.getByName(MCThread.multicastAddress), MCThread.multicastPort));

                // Did the chunk got to its destination?
                TimeOutThread tmt = new TimeOutThread(msg.getFileID(), msg.getChunkNo());

                tmt.start();
                Thread.sleep(500);
                tmt.finish();
            }

            // Resetting values
            MDRChunkReceived = false;
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
    
    public void removeChunk(String filename, String path) throws IOException{
    	
    	String filenameID = filename.substring(0,64);
    	String chunkNo = filename.substring(65, 71);
    	String version = "1.0";
    	
    	String removed_message = "REMOVED " + version + " " + filenameID + " " + chunkNo + "\r\n\r\n";
    	
    	InetAddress MCAddress = InetAddress.getByName(MCThread.multicastAddress);

        /*send removed_message*/
        outputMCSocket.send(new DatagramPacket(removed_message.getBytes(), removed_message.length(), MCAddress, MCThread.multicastPort));
        
        /*Removes the chunk from the file system*/
    	File f = new File("./stored/" + filenameID + "/" + filename);
    	f.delete();
    }

    @Override
    public void run()
    {
    }

    public class TimeOutThread extends Thread
    {
        private MulticastSocket MDRSocket;
        private boolean stopFlag;
        private String fileID;
        private String chunkNo;

        public TimeOutThread(String fileID, String chunkNo) throws IOException
        {
            stopFlag = false;

            this.fileID = fileID;
            this.chunkNo = chunkNo;

            MDRSocket = new MulticastSocket(MDRThread.multicastPort);
            MDRSocket.setTimeToLive(1);
            MDRSocket.joinGroup(InetAddress.getByName(multicastAddress));
        }

        @Override
        public void run()
        {
            try
            {
                while (!stopFlag)
                {
                    byte[] recvData = new byte[1024];
                    DatagramPacket dp = new DatagramPacket(recvData, recvData.length);
                    MDRSocket.receive(dp);

                    String msg = new String(dp.getData());
                    Chunk c = Chunk.parseMsg(msg);

                    if (c.getChunkNo().equals(chunkNo) && c.getFileID().equals(fileID)
                            && stopFlag == false)
                    {
                        MDRChunkReceived = true;
                    }
                }
            }
            catch (IOException | InvalidMessageArguments ex)
            {
                Logger.getLogger(UserInputThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        public void finish()
        {
            stopFlag = true;
        }
    }
}