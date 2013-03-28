package messages;

import exceptions.InvalidMessageArguments;

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
        this(fileID, chunkNo, "1.0");
    }

    @Override
    public String toString()
    {
        return "STORED " + protocolVersion + " " + fileID + " " + chunkNo + "\r\n\r\n";
    }

    public static Stored parseMsg(String msg) throws InvalidMessageArguments
    {
        String[] splittedMsg = msg.split(" ");

        String protocolVersion = splittedMsg[1];
        String fileID = splittedMsg[2];
        String chunkNo = splittedMsg[3];

        boolean legalCRLFseq = false;
        for (int i = 4; i < splittedMsg.length - 1; i++)
        {
            if (splittedMsg[i].equals("\r\n\r\n"))
            {
                legalCRLFseq = true;
            }
        }

        if (!legalCRLFseq)
        {
            throw new InvalidMessageArguments();
        }

        return new Stored(fileID, chunkNo, protocolVersion);
    }

    public String getFileID()
    {
        return fileID;
    }

    public String getChunkNo()
    {
        return chunkNo;
    }

    public String getProtocolVersion()
    {
        return protocolVersion;
    }
}