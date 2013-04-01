package datatypes;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class LocalIdentifierContainer
{
    private Set<LocalIdentifier> localFiles;

    public LocalIdentifierContainer()
    {
        localFiles = Collections.synchronizedSet(new HashSet<LocalIdentifier>());
    }

    public void addLocalIdentifier(LocalIdentifier l)
    {
        localFiles.add(l);
    }
    
    public LocalIdentifier getIdentifierByFilename(String filename)
    {
        for (LocalIdentifier l: localFiles)
        {
            if (l.getFilenameReal().equals(filename))
                return l;
        }
        
        return null;
    }
}
