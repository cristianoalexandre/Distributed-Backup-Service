package messages;

import datatypes.FileDescriptor;
import exceptions.InvalidMessageArguments;
import gui.FileChooserFrame;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class PutChunk
{
    // Header and Data
    private byte[] chunkData;
    private String protocolVersion;
    private String fileId;
    private int replicationDegree;
    private String chunkNo;
    
    public PutChunk(String pathToChunk, int replicationDegree, String protocolVersion) throws NoSuchAlgorithmException, FileNotFoundException, IOException
    {
        // Getting the fileId and chunkNo through the filename
        FileDescriptor chunk = new FileDescriptor(pathToChunk);
        //System.out.println(chunk.getName());
        FileChooserFrame.log.append("Chunk Name: " + chunk.getName() + "\n");

        String[] chunkFileNameSplitted = (chunk.getName()).split("_");
        //System.out.println(chunkFileNameSplitted.length);

        fileId = chunkFileNameSplitted[0];
        chunkNo = chunkFileNameSplitted[1];

        // Getting the contents of the chunk to include in the message
        chunkData = new byte[FileDescriptor.chunkSize];
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(chunk));
        bis.read(chunkData);

        // Setting the protocol version and replication degree used
        this.protocolVersion = protocolVersion;
        this.replicationDegree = Math.max(replicationDegree, 9); // maximum replication degree is 9
    }

    public PutChunk(String pathToChunk, int replicationDegree) throws NoSuchAlgorithmException, FileNotFoundException, IOException
    {
        // Assumes only version 1.0 of protocol
        this(pathToChunk, replicationDegree, "1.0");
    }

    public PutChunk(String pathToChunk) throws NoSuchAlgorithmException, FileNotFoundException, IOException
    {
        // Assumes replication degree of 1, with only version 1.0 of protocol
        this(pathToChunk, 1, "1.0");
    }
    
    public PutChunk(String fileId, int replicationDegree, String protocolVersion, String chunkNo, byte[] chunkData)
    {
        this.chunkData = chunkData;
        this.protocolVersion = protocolVersion;
        this.fileId = fileId;
        this.replicationDegree = replicationDegree;
        this.chunkNo = chunkNo;
    }

    @Override
    public String toString()
    {
        return "PUTCHUNK " + protocolVersion + " " + fileId + " "
                + chunkNo + " " + replicationDegree + " "
                + "\r\n" + "\r\n" + chunkData.toString();

    }

    public static PutChunk parseMsg(String msg) throws InvalidMessageArguments
    {
        String[] splittedMsg = msg.split(" ");

        String protocolVersion = splittedMsg[1];
        String fileID = splittedMsg[2];
        String chunkNo = splittedMsg[3];
        int replicationDegree = Integer.decode(splittedMsg[4]);

        byte[] chunkData = null;

        // Cycle to ignore unknown header stuff
        for (int i = 4; i < splittedMsg.length - 1; i++)
        {
            if (splittedMsg[i].equals("\r\n\r\n"))
            {
                chunkData = splittedMsg[i + 1].getBytes();
            }
        }

        // Header is invalid - throw an exception!
        if (chunkData == null)
        {
            throw new InvalidMessageArguments();
        }

        return new PutChunk(fileID, replicationDegree, protocolVersion, chunkNo, chunkData);
    }

    public byte[] getChunkData()
    {
        return chunkData;
    }

    public String getChunkNo()
    {
        return chunkNo;
    }

    public String getProtocolVersion()
    {
        return protocolVersion;
    }

    public String getFileId()
    {
        return fileId;
    }

    public int getReplicationDegree()
    {
        return replicationDegree;
    }
}