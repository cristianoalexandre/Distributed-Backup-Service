package threads;

import datatypes.FileDescriptor;
import exceptions.InvalidMessageArguments;
import gui.FileChooserFrame;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.logging.Level;
import java.util.logging.Logger;
import messages.Chunk;

public class MDRThread extends Thread
{
    // Multicast definitions
    public static final String multicastAddress = "239.3.3.3";
    public static final int multicastPort = 3333;
    private MulticastSocket inputSocket;
    private MulticastSocket mcSocket;
    // Chunk-related definitions
    private int numberOfChunksFromFile;

    public MDRThread() throws IOException
    {
        inputSocket = new MulticastSocket(MDRThread.multicastPort);
        inputSocket.setTimeToLive(1);
        inputSocket.joinGroup(InetAddress.getByName(multicastAddress));

        mcSocket = new MulticastSocket(MCThread.multicastPort);
        mcSocket.setTimeToLive(1);
        mcSocket.joinGroup(InetAddress.getByName(MCThread.multicastAddress));

        numberOfChunksFromFile = -1;
    }

    @Override
    public void run()
    {
        for (;;)
        {
            try
            {
                byte[] receiveData = new byte[65535];

                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                inputSocket.receive(receivePacket);

                String msgReceived = new String(receivePacket.getData());

                new MsgParsingThread(msgReceived).start();
            }
            catch (IOException ex)
            {
                Logger.getLogger(MDRThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public class MsgParsingThread extends Thread
    {
        private String msgReceived;

        public MsgParsingThread(String msg)
        {
            this.msgReceived = msg;
        }

        @Override
        public void run()
        {
            try
            {
                FileChooserFrame.log.append("MDR - Received:" + msgReceived);

                Chunk msg = Chunk.parseMsg(msgReceived);

                // are we interested in this chunk?
                if (UserInputThread.recoveringFiles.contains(msg.getFileID()))
                {
                    // if last chunk, determine the number of chunks
                    if (msg.getChunkData().getBytes().length < FileDescriptor.chunkSize
                            && numberOfChunksFromFile < 0)
                    {
                        numberOfChunksFromFile = Integer.parseInt(msg.getChunkNo(), 10);
                        System.out.println("Number of chunks (determined): "+numberOfChunksFromFile);
                    }

                    // store the current chunk in disk
                    File chunkDir = new File(FileDescriptor.recoveredDir + "/" + msg.getFileID());
                    if (!chunkDir.exists())
                        chunkDir.mkdir();

                    FileOutputStream out = new FileOutputStream(chunkDir + "/" + msg.getFileID() + "_" + msg.getChunkNo());
                    out.write(msg.getChunkData().getBytes("UTF-8"));
                    out.close();
                    System.out.println("Stored Chunk: "+msg.getChunkNo());

                    int nChunksRecv = chunkDir.list().length;
                    System.out.println("Chunks Received:" + nChunksRecv);

                    // everything received, writing to final file!
                    if (numberOfChunksFromFile > 0 && nChunksRecv >= numberOfChunksFromFile)
                    {
                        System.out.println("Merging Chunks...");
                        File recoveredFile = new File(FileDescriptor.recoveredDir + "/" + UserInputThread.localFiles.getIdentifierByFilehash(msg.getFileID()).getFilenameReal());

                        if (recoveredFile.exists() == false)
                            recoveredFile.createNewFile();

                        String[] chunks = chunkDir.list();
                        FileOutputStream finalOut = new FileOutputStream(recoveredFile,true);

                        for (int i = 0; i < chunks.length; i++)
                        {
                            File currentChunk = new File(chunkDir+"/"+chunks[i]);
                            String buf = FileDescriptor.readFile(currentChunk.getPath());

                            finalOut.write(buf.getBytes("UTF-8"));
                        }

                        finalOut.close();
                        System.out.println("Finished.");
                        
                        // Setting the vars for the next recovered file...
                        UserInputThread.recoveringFiles.remove(msg.getFileID());
                        numberOfChunksFromFile = -1;
                    }
                }
            }
            catch (InvalidMessageArguments | IOException ex)
            {
                Logger.getLogger(MDRThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
