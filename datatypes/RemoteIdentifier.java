package datatypes;

import java.util.Objects;

public class RemoteIdentifier implements java.io.Serializable
{
    private String filenameHash;
    private String number;
    private String host;

    public RemoteIdentifier(String filenameHash, String number, String host)
    {
        this.filenameHash = filenameHash;
        this.number = number;
        this.host = host;
    }

    public String getFilenameHash()
    {
        return filenameHash;
    }

    public String getHost()
    {
        return host;
    }

    public String getNumber()
    {
        return number;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj.getClass() != this.getClass()) return false;
        
        RemoteIdentifier r = (RemoteIdentifier) obj;
        
        return (r.getNumber().equals(this.getNumber())
                && r.getHost().equals(this.getHost())
                && r.getFilenameHash().equals(this.getFilenameHash()));
    }
    
    public boolean sameChunkAs(RemoteIdentifier r)
    {
        return (r.getNumber().equals(this.getNumber())
                && r.getFilenameHash().equals(this.getFilenameHash())); 
    }

    @Override
    public String toString()
    {
        return "RI: "+getFilenameHash()+" - "+getNumber()+" - "+getHost();
    }

    
    
    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 83 * hash + Objects.hashCode(this.filenameHash);
        hash = 83 * hash + Objects.hashCode(this.number);
        hash = 83 * hash + Objects.hashCode(this.host);
        return hash;
    }
}
