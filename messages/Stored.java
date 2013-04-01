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

        if (splittedMsg.length < 4)
        {
            throw new InvalidMessageArguments();
        }

        String protocolVersion = splittedMsg[1];
        String fileID = splittedMsg[2];

        StringBuilder chunkNoBuilder = new StringBuilder();
        for (int i = 0; i < 6; i++)
        {
            chunkNoBuilder.append(splittedMsg[3].charAt(i));
        }
        String chunkNo = chunkNoBuilder.toString();

        if (Float.parseFloat(protocolVersion) < 1.0 || fileID.length() != 64 || Integer.parseInt(chunkNo) < 0 || Integer.parseInt(chunkNo) > 999999)
        {
            throw new InvalidMessageArguments();
        }

        boolean legalCRLFseq = false;
        if (splittedMsg[3].charAt(6) == '\r'
            && splittedMsg[3].charAt(7) == '\n'
            && splittedMsg[3].charAt(8) == '\r'
            && splittedMsg[3].charAt(9) == '\n')
        {
            legalCRLFseq = true;
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