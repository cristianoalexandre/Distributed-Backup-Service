package thread;

import cli.Menu;
import datatypes.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserInputThread extends Thread
{
    public UserInputThread()
    {
    }

    @Override
    public void run()
    {
        try
        {
            // Asking the user what to do
            switch (Menu.ask())
            {
                case Menu.backup:
                    break;
                default:
                    System.exit(-1);
            }

            // Example Backup File...
            FileDescriptor toBackup = new FileDescriptor("./BackupFolder/multicast.pdf");
            System.out.println(toBackup.exists());

            // Getting the file id - hashing through SHA-256
            System.out.println(toBackup.getSHA256filenameHash());

            // Dividing the file into chunks
            toBackup.breakToChunks();
            
            // Open Socket to start Multicasting the PUTCHUNK requests, one per chunk.

        }
        catch (NoSuchAlgorithmException | IOException ex)
        {
            Logger.getLogger(UserInputThread.class.getName()).log(Level.SEVERE, null, ex);

        }
    }
}