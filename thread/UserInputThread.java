package thread;

import cli.Menu;
import datatypes.FileDescriptor;
import java.io.File;
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
            File toBackup = new File("./BackupFolder/multicast.pdf");
            System.out.println(toBackup.getName());

            // Getting the file id - hashing through SHA-256
            System.out.println(FileDescriptor.filename2Hash(toBackup.getName()));

            // Dividing the file into chunks

            // Open Socket to start Multicasting the PUTCHUNK requests, one per chunk.

        }
        catch (NoSuchAlgorithmException ex)
        {
            Logger.getLogger(UserInputThread.class.getName()).log(Level.SEVERE, null, ex);

        }
    }
}