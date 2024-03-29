package threads;

import datatypes.FileDescriptor;
import datatypes.LocalIdentifier;
import datatypes.LocalIdentifierContainer;
import datatypes.RemoteIdentifierContainer;
import datatypes.RemoteIdentifier;
import exceptions.InvalidMessageArguments;
import gui.FileChooserFrame;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import messages.Chunk;
import messages.GetChunk;
import messages.PutChunk;
import messages.Stored;

public class MCThread extends Thread
{
    // Multicast definitions
    public static final String multicastAddress = "239.1.1.1";
    public static final int multicastPort = 1111;
    private MulticastSocket inputSocket;
    private MulticastSocket MDRSocket;
    private MulticastSocket outputMDBSocket;
    // Random number generator
    private Random rgen;
    public static RemoteIdentifierContainer remoteChunks;
    // Boolean flag to control Chunk answers
    private boolean neededChunk = false;

    public MCThread(RemoteIdentifierContainer ric) throws IOException
    {
    	
    	outputMDBSocket = new MulticastSocket(MDBThread.multicastPort);
        outputMDBSocket.setTimeToLive(1);
        
        remoteChunks = ric;

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
            } catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
    }

    /**
     * Parses a Message
     *
     * @param receivedMsg
     * @return
     * @throws NoSuchAlgorithmException 
     */
    private int parseMsg(DatagramPacket receivedPacket) throws InvalidMessageArguments, IOException, NoSuchAlgorithmException
    {
        String[] msgArray = new String(receivedPacket.getData()).split(" ");

        switch (msgArray[0])
        {
            case "STORED":
                Stored storedMsg = Stored.parseMsg(new String(receivedPacket.getData()));
                RemoteIdentifier ri = new RemoteIdentifier(storedMsg.getFileID(), storedMsg.getChunkNo(), receivedPacket.getAddress().toString());
                appendToRemoteIdentifiers(storedMsg.getFileID(), storedMsg.getChunkNo(), receivedPacket.getAddress().toString());
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
                String filenameHash = msgArray[1].trim();
                FileDescriptor path_to_remove = new FileDescriptor("./stored/" + filenameHash);

                /*Get all files*/
                File[] allfiles = path_to_remove.listFiles();
                
                /*Deletes them, one by one*/
                for(int i=0;i<allfiles.length;i++){
                    System.out.println(allfiles[i].delete());
                }
                
                /*And then, directory must be deleted*/
                if(path_to_remove.delete()){
                    FileChooserFrame.log.append("Deleted chunks from file " + filenameHash + "\n");
                }else{
                    FileChooserFrame.log.append("Failed on deleting attempt" + "\n");
                }
                
                /*TO DO - Deleting remote identifier from the container*/
                remoteChunks.deleteRemoteIdentifier(filenameHash);
                

                break;
            case "REMOVED":
            	
            	String fileID = msgArray[2].trim();
            	String chunkNo = fileID + "_" + msgArray[3].trim();
            	
            	String path = "./backup/sent/" + fileID + "/" + chunkNo;
            	
            	System.out.println(path);
            	
            	File f = new File("./backup/sent/" + fileID);
            	
            	/*If is the initiator-peer*/
            	if(f.exists()){
            		
            		/*Number of Replications that are supposed to exist on the network*/
            		LocalIdentifier li = UserInputThread.localFiles.getIdentifierByFilehash(fileID);
            		String repDegree = li.getReplicationDegree();
            		System.out.println(repDegree);
            		
            		/*Number o Replications that really exist on the network*/
            		int totalRepDegree = remoteChunks.getIdentifiersByHash(fileID).size();
            		String totalRep = Integer.toString(totalRepDegree-1);
            		System.out.println(totalRepDegree);
            		
            		/*If RepDegrees are not the same*/
            		if(!totalRep.equals(repDegree)){
            			
            			//String path = "./backup/sent/" + fileID + "/" + chunkNo;
            			
            			PutChunk pc = null;
            			pc = new PutChunk(path);
            			
            			/*Send PUTCHUNK*/
            			InetAddress MDBAddress = InetAddress.getByName(MDBThread.multicastAddress);
            			outputMDBSocket.send(new DatagramPacket(pc.toString().getBytes(), pc.toString().length(), MDBAddress, MDBThread.multicastPort));
            			
            			
            		}
            		
            		/*Otherwise ignores the message...*/
            		
            	}
            	

                break;
        }

        return 0;
    }

    public static void addRemoteIdentifier(RemoteIdentifier ri)
    {
        remoteChunks.addRemoteIdentifier(ri);
    }

    public static void appendToRemoteIdentifiers(String fileID, String chunkNo, String host){
        try
        {
            String filename= "./config/remoteIdentifiers.txt";
            FileWriter fw = new FileWriter(filename,true); //the true will append the new data
            fw.write(fileID);
            fw.write(System.getProperty("line.separator"));
            fw.write(chunkNo);
            fw.write(System.getProperty("line.separator"));
            fw.write(host);
            fw.write(System.getProperty("line.separator"));
            fw.close();
        }
        catch(IOException ioe)
        {
            System.err.println("IOException: " + ioe.getMessage());
        }
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
