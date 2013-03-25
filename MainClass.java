import java.io.IOException;
import java.util.HashMap;
import thread.UserInputThread;
import thread.MCThread;
import thread.MDBThread;
import thread.MDRThread;

public class MainClass
{
    private HashMap<String, String> localChunks = new HashMap<>();

    public static void main(String args[]) throws IOException
    {
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