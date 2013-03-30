package messages;

public class GetChunk
{
    private String fileID;
    private String chunkNo;
    private String protocolVersion;
    
    public GetChunk(String fileID, String chunkNo, String protocolVersion)
    {
        this.fileID = fileID;
        this.chunkNo = chunkNo;
        this.protocolVersion = protocolVersion;
    }
    
    public GetChunk(String fileID, String chunkNo)
    {
        this(fileID, chunkNo, "1.0");
    }
    
    @Override
    public String toString()
    {
        return "GETCHUNK "+protocolVersion+" "+fileID+" "+chunkNo+"\r\n\r\n";
    }
}