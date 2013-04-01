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
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import messages.Chunk;

public class MDRThread extends Thread
{
    // Multicast definitions
    public static final String multicastAddress = "237.1.7.3";
    public static final int multicastPort = 4005;
    private MulticastSocket inputSocket;
    private FileDescriptor currentFile;
    
    public MDRThread() throws IOException
    {
        inputSocket = new MulticastSocket(MDRThread.multicastPort);
        inputSocket.setTimeToLive(1);
        inputSocket.joinGroup(InetAddress.getByName(multicastAddress));
        
        currentFile = null;
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
                
                FileChooserFrame.log.append("MDR - Received:" + msgReceived);
                
                Chunk msg = Chunk.parseMsg(msgReceived);
                if (currentFile == null || currentFile.getSHA256filenameHash().equals(msg.getFileID()))
                {
                    if (!new File(FileDescriptor.recoveredDir).exists())
                        new File(FileDescriptor.recoveredDir).mkdir();
                    
                    if (currentFile == null)
                    {
                        currentFile = new FileDescriptor(FileDescriptor.recoveredDir + "/" + UserInputThread.localFiles.getIdentifierByFilehash(msg.getFileID()).getFilenameReal());
                        currentFile.createNewFile();
                    }
                    
                    FileOutputStream out = new FileOutputStream(currentFile,true);
                    out.write(msg.getChunkData().getBytes("UTF-8"));
                    out.close();
                    
                    if (msg.getChunkData().getBytes().length < FileDescriptor.chunkSize)
                    {
                        currentFile = null;
                    }
                }
                
            }
            catch (IOException | InvalidMessageArguments | NoSuchAlgorithmException ex)
            {
                Logger.getLogger(MDRThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}