package threads;

import cli.Menu;
import datatypes.FileDescriptor;
import gui.FileChooserFrame;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

import messages.PutChunk;

public class UserInputThread extends Thread {
	// Multicast definitions
	private MulticastSocket outputMDBSocket;

	public UserInputThread() throws IOException {
		outputMDBSocket = new MulticastSocket(MDBThread.multicastPort);
		outputMDBSocket.setTimeToLive(1);
	}

	@Override
	public void run() {

		// Asking the user what to do
		/*
		 * for (;;) { switch (Menu.ask()) { case Menu.backup: doBackup(); break;
		 * default: System.exit(-1); } }
		 */
	}

	public void doBackup(String filename, String path) {
		// System.out.println(path);
		String completeFilename = "/" + filename;

		try {
			// Example Backup File...
			FileDescriptor toBackup = new FileDescriptor(path);
			// FileDescriptor toBackup = new
			// FileDescriptor(FileDescriptor.backupDir + "/multicast.pdf");
			// System.out.println(toBackup.exists());

			// Getting the file id - hashing through SHA-256
			// System.out.println(toBackup.getSHA256filenameHash());
			// FileChooserFrame.log.append(toBackup.getSHA256filenameHash());

			// Dividing the file into chunks
			toBackup.breakToChunks();

			// ======================= //

			FileDescriptor sentChunks = new FileDescriptor(
					FileDescriptor.sentChunkDir + "/"
							+ toBackup.getSHA256filenameHash());

			File[] allfiles = sentChunks.listFiles();

			// Open Socket to start Multicasting the PUTCHUNK requests, one
			// per chunk.
			InetAddress MDBAddress = InetAddress
					.getByName(MDBThread.multicastAddress);
			
			System.out.println("len: " + allfiles.length);
			
			for (int i = 0;i<allfiles.length;i++) {
				
				FileChooserFrame.log.append("Vai enviar chunk " + i);

				PutChunk pc = new PutChunk(allfiles[i].getPath());

				outputMDBSocket.send(new DatagramPacket(pc.toString()
						.getBytes(), pc.toString().length(), MDBAddress,
						MDBThread.multicastPort));
				System.out.println("Enviou Chunk numero " + i);
			}
			
			System.out.println("Terminou o envio!");
			
		} catch (NoSuchAlgorithmException | IOException ex) {
			Logger.getLogger(UserInputThread.class.getName()).log(Level.SEVERE,
					null, ex);

		}
	}
}