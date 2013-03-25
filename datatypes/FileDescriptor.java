package datatypes;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FileDescriptor extends File
{
    public FileDescriptor(String filename)
    {
        super(filename);
    }

    /**
     * Hashes a filename using SHA-256 algorithm.
     *
     * @param filename String to hash.
     * @return Hash (in String format)
     */
    public static String filename2Hash(String filename) throws NoSuchAlgorithmException
    {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(filename.getBytes());
        byte[] digest = md.digest();

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < digest.length; i++)
        {
            sb.append(Integer.toString((digest[i] & 0xff) + 0x100, 16).substring(1));
        }

        return sb.toString();
    }
}