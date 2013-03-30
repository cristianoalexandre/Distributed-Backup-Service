package datatypes;

import java.util.Objects;

public class RemoteIdentifier implements java.io.Serializable
{
    private String filename;
    private String filenameHash;
    private String number;
    private String host;

    public RemoteIdentifier(String filename, String filenameHash, String number, String host)
    {
        this.filename = filename;
        this.filenameHash = filenameHash;
        this.number = number;
        this.host = host;
    }

    public String getFilename()
    {
        return filename;
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
                && r.getFilename().equals(this.getFilename()));
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 83 * hash + Objects.hashCode(this.filename);
        hash = 83 * hash + Objects.hashCode(this.filenameHash);
        hash = 83 * hash + Objects.hashCode(this.number);
        hash = 83 * hash + Objects.hashCode(this.host);
        return hash;
    }
}
