package datatypes;

public class ChunkIdentifier
{
    private static int nrCounter = 0;
    
    private int fileID;
    private int nr;
    private String hostMachine;
    
    /**
     * Default constructor for the class.
     * @param fileID id of file of which the chunk belongs (decimal base)
     * @param chunkNr number of the chunk [0-64] (decimal base)
     * @param hostMachine hostname of the machine which stores the chunk
     */
    public ChunkIdentifier(int fileID, String hostMachine)
    {
        this.fileID = fileID;
        this.nr = nrCounter;
        this.hostMachine = hostMachine;
        
        nrCounter++;
    }
    
    public int getFileID()
    {
        return fileID;
    }
    
    public String getFileIDHex()
    {
        return Integer.toHexString(fileID);
    }
    
    public int getNr()
    {
        return nr;
    }
    
    public String whoHostsMe()
    {
        return hostMachine;
    }
}