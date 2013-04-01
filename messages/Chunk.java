package messages;

import exceptions.InvalidMessageArguments;

public class Chunk
{
    // Header and Data
    private String protocolVersion;
    private String chunkNo;
    private String fileID;
    private String chunkData;

    public Chunk(String fileID, String protocolVersion, String chunkNo, String chunkData)
    {
        this.chunkData = chunkData;
        this.chunkNo = chunkNo;
        this.fileID = fileID;
        this.protocolVersion = protocolVersion;
    }

    @Override
    public String toString()
    {
        return "CHUNK " + protocolVersion + " " + fileID + " " + chunkNo + "\r\n\r\n" + chunkData;
    }

    public String getChunkData()
    {
        return chunkData;
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
    
    public static Chunk parseMsg(String msg) throws InvalidMessageArguments
    {
        String[] splittedMsg = msg.trim().split(" ");

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

        StringBuilder temp = new StringBuilder("");
        String chunkData = null;

        if (splittedMsg[3].charAt(6) == '\r'
            && splittedMsg[3].charAt(7) == '\n'
            && splittedMsg[3].charAt(8) == '\r'
            && splittedMsg[3].charAt(9) == '\n')
        {
            for (int k = 10; k < splittedMsg[3].length(); k++)
            {
                temp.append(splittedMsg[3].charAt(k));
            }

            for (int i = 4; i < splittedMsg.length; i++)
            {
                {
                    temp.append(" ");
                    temp.append(splittedMsg[i]);
                }
            }

            chunkData = temp.toString();
        }
        
        // Header is invalid - throw an exception!
        if (chunkData == null)
        {
            throw new InvalidMessageArguments();
        }
        
        return new Chunk(fileID, protocolVersion, chunkNo, chunkData);
    }
}