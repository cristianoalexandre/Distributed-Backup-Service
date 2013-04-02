package datatypes;

public class LocalIdentifier implements java.io.Serializable
{
    private String filenameHash;
    private String filenameReal;
    private String replicationDegree;

    public LocalIdentifier(String filenameHash, String filenameReal, String replicationDegree)
    {
        this.filenameHash = filenameHash;
        this.filenameReal = filenameReal;
        this.replicationDegree = replicationDegree;
    }

    public String getFilenameHash()
    {
        return filenameHash;
    }

    public String getFilenameReal()
    {
        return filenameReal;
    }

    public String getReplicationDegree()
    {
        return replicationDegree;
    }
}
