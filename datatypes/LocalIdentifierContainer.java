package datatypes;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class LocalIdentifierContainer implements java.io.Serializable
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
    
    public LocalIdentifier getIdentifierByFilehash(String filehash)
    {
        for (LocalIdentifier l: localFiles)
        {
            if (l.getFilenameHash().equals(filehash))
                return l;
        }
        
        return null;
    }
    
    public static LocalIdentifierContainer load(){

    	LocalIdentifierContainer ric = new LocalIdentifierContainer();

		try { 
			FileInputStream fis = new FileInputStream("serial2"); 
			ObjectInputStream ois = new ObjectInputStream(fis); 
			ric = (LocalIdentifierContainer)ois.readObject();
			ois.close(); 
		} 
		catch(Exception e) { 
			System.exit(0); 
		} 
		return ric;

	}

	public void save(){

		try { 
			FileOutputStream fos = new FileOutputStream("serial2"); 
			ObjectOutputStream oos = new ObjectOutputStream(fos); 
			oos.writeObject(this); 
			oos.flush(); 
			oos.close(); 
		} 
		catch(Exception e) {  
			System.exit(0); 
		} 

	}
}
