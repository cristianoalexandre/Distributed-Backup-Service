import datatypes.FileDescriptor;
import java.io.File;
import java.io.IOException;
import thread.UserInputThread;
import thread.MCThread;
import thread.MDBThread;
import thread.MDRThread;

public class MainClass
{    
    public static void main(String args[]) throws IOException
    {
        // Guarantee that all the needed folders exist
        File backupFolder = new File(FileDescriptor.backupDir);
        File receivedChunkFolder = new File(FileDescriptor.receivedChunkDir);
        File sentChunkFolder = new File(FileDescriptor.sentChunkDir);
        
        if (!backupFolder.exists()) backupFolder.mkdir();
        if (!receivedChunkFolder.exists()) receivedChunkFolder.mkdir();
        if (!sentChunkFolder.exists()) sentChunkFolder.mkdir();
        
        // Initiating multicast channel threads

        MCThread mc = new MCThread();
        MDBThread mdb = new MDBThread();
        MDRThread mdr = new MDRThread();
        UserInputThread input = new UserInputThread();

        mc.start();
        mdb.start();
        mdr.start();
        input.start();
    }
}