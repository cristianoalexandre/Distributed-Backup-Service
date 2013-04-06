package datatypes;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class RemoteIdentifierContainer implements Serializable
{
    private Set<RemoteIdentifier> remoteChunks;

    public RemoteIdentifierContainer()
    {
        remoteChunks = Collections.synchronizedSet(new HashSet<RemoteIdentifier>());
    }

    public int numberOfReplicas(RemoteIdentifier ri)
    {
        int counter = 0;

        for (RemoteIdentifier r : remoteChunks)
        {
            if (r.sameChunkAs(ri))
            {
                counter++;
            }
        }

        System.out.println("Counter: " + counter);
        return counter;
    }

    public void addRemoteIdentifier(RemoteIdentifier ri)
    {
        remoteChunks.add(ri);
    }

    public Set<RemoteIdentifier> getIdentifiersByHash(String hashname)
    {
        Set<RemoteIdentifier> ris = new HashSet<>();

        for (RemoteIdentifier r : remoteChunks)
        {
            if (r.getFilenameHash().equals(hashname))
            {
                ris.add(r);
            }
        }

        return ris;
    }

    public void deleteRemoteIdentifier(String hashName)
    {

        Set<RemoteIdentifier> ris = getIdentifiersByHash(hashName);

        for (RemoteIdentifier ri : ris)
        {
            for (RemoteIdentifier ri2 : remoteChunks)
            {
                if (ri.sameChunkAs(ri2))
                {
                    remoteChunks.remove(ri2);
                }
            }
        }

    }

    public boolean hasIdentifier(RemoteIdentifier ri)
    {
        return remoteChunks.contains(ri);
    }

    public static RemoteIdentifierContainer load() throws IOException, ClassNotFoundException
    {
        RemoteIdentifierContainer ric = null;

        FileInputStream fis = new FileInputStream(FileDescriptor.remoteChunkContainerFile);
        ObjectInputStream ois = new ObjectInputStream(fis);
        ric = (RemoteIdentifierContainer) ois.readObject();
        ois.close();

        return ric;
    }

    public void save() throws IOException
    {
        FileOutputStream fos = new FileOutputStream(FileDescriptor.remoteChunkContainerFile);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(this);
        oos.flush();
        oos.close();
    }

    public static void bubbleSort(HashSet<RemoteIdentifier> r)
    {
        ArrayList<RemoteIdentifier> ris = new ArrayList<>(r);

        boolean sorted = false;
        while (!sorted)
        {
            for (int i = 0; i < ris.size() - 1; i++)
            {
                if (i == 0)
                {
                    sorted = true;
                }

                int num1 = Integer.parseInt(ris.get(i).getNumber(), 10);
                int num2 = Integer.parseInt(ris.get(i + 1).getNumber(), 10);

                if (num1 > num2)
                {
                    RemoteIdentifier tmp = ris.get(i);
                    ris.set(i,ris.get(i + 1));
                    ris.set(i + 1, tmp);
                    sorted = false;
                }
            }
        }
    }
}
