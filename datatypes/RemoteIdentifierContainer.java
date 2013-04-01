package datatypes;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
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

        for (RemoteIdentifier r: remoteChunks)
        {
            if (r.sameChunkAs(ri))
                counter++;
        }

        System.out.println("Counter: "+counter);
        return counter;
    }

    public void addRemoteIdentifier(RemoteIdentifier ri)
    {
        remoteChunks.add(ri);
    }
}