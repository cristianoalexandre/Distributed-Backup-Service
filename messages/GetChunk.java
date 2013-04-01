package messages;

import exceptions.InvalidMessageArguments;

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

    public String getChunkNo()
    {
        return chunkNo;
    }

    public String getFileID()
    {
        return fileID;
    }

    public String getProtocolVersion()
    {
        return protocolVersion;
    }
    
    public static GetChunk parseMsg(String msg) throws InvalidMessageArguments
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

        return new GetChunk(fileID, chunkNo, protocolVersion);
    }
}