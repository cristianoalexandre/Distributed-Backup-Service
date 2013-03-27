package messages;

public class Stored
{
    private String fileID;
    private String chunkNo;
    private String protocolVersion;
    
    public Stored(String fileID, String chunkNo, String protocolVersion)
    {
        this.fileID = fileID;
        this.chunkNo = chunkNo;
        this.protocolVersion = protocolVersion;
    }
    
    public Stored(String fileID, String chunkNo)
    {
        this(fileID,chunkNo,"1.0");
    }
    
    @Override
    public String toString()
    {
        return "STORED "+protocolVersion+" "+fileID+" "+chunkNo+"\r\n\r\n";
    }
}
