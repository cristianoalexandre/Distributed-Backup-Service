package messages;

import datatypes.FileDescriptor;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class PutChunk
{
    private byte[] chunkData;
    private String protocolVersion;
    private String fileId;
    private int replicationDegree;
    private String chunkNo;

    public PutChunk(String pathToChunk, int replicationDegree, String protocolVersion) throws NoSuchAlgorithmException, FileNotFoundException, IOException
    {
        // Getting the fileId and chunkNo through the filename
        FileDescriptor chunk = new FileDescriptor(pathToChunk);
        System.out.println(chunk.getName());
         
        String[] chunkFileNameSplitted = (chunk.getName()).split("_");
        System.out.println(chunkFileNameSplitted.length);
        
        fileId = chunkFileNameSplitted[0];
        chunkNo = chunkFileNameSplitted[1];
                
        // Getting the contents of the chunk to include in the message
        chunkData = new byte[FileDescriptor.chunkSize];
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(chunk));
        bis.read(chunkData);
        
        // Setting the protocol version and replication degree used
        this.protocolVersion = protocolVersion;
        this.replicationDegree = Math.max(replicationDegree,9); // maximum replication degree is 9
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
    
    @Override
    public String toString()
    {
        return "PUTCHUNK "+protocolVersion+" "+fileId+" "
                + chunkNo + " " + replicationDegree + " "
                + "\r\n" + "\r\n" + chunkData.toString();

    }
}