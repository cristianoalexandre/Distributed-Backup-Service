package thread;

import java.net.MulticastSocket;

public class MDRThread extends Thread
{
    // Multicast definitions
    public static final String multicastAddress = "237.1.7.3";
    public static final int multicastPort = 4005;
    private MulticastSocket inputSocket;
    
    @Override
    public void run()
    {
    }
}