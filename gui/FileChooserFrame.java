package gui;

import exceptions.InvalidFile;
import exceptions.MaxAttemptsReached;
import threads.MDRThread;
import threads.MDBThread;
import threads.MCThread;
import threads.UserInputThread;
import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.awt.*;
import java.awt.event.*;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;

import datatypes.LocalIdentifierContainer;
import datatypes.RemoteIdentifier;
import datatypes.RemoteIdentifierContainer;
import datatypes.FileDescriptor;

public class FileChooserFrame extends JPanel implements ActionListener
{
    private MCThread mc;
    private MDBThread mdb;
    private MDRThread mdr;
    private UserInputThread input;
    private JButton openButton, restoreButton, removeButton;
    public static JTextArea log;
    private JFileChooser fc;
    private JComboBox comboBox;
    private JLabel lblNumberOfCopies;
    private JButton btnDelete;
    static private final String newline = "\n";

    public FileChooserFrame() throws IOException, ClassNotFoundException
    {
        super(new BorderLayout());

        //Create the log first, because the action listeners
        //need to refer to it.
        log = new JTextArea(5, 20);
        log.setMargin(new Insets(5, 5, 5, 5));
        log.setEditable(false);
        JScrollPane logScrollPane = new JScrollPane(log);

        //Create a file chooser
        fc = new JFileChooser();

        openButton = new JButton("Backup (Select File)");
        openButton.addActionListener(this);
        
        removeButton = new JButton("Remove Chunks");
        removeButton.addActionListener(this);

        restoreButton = new JButton("Restore");
        restoreButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent arg0)
            {
            }
        });
        restoreButton.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                openRestoreDialog();
            }
        });

        //For layout purposes, put the buttons in a separate panel
        JPanel buttonPanel = new JPanel(); //use FlowLayout


        /*comboBox indicating the number of copies of a file user wants to backup*/

        lblNumberOfCopies = new JLabel("Number of Copies");
        buttonPanel.add(lblNumberOfCopies);
        comboBox = new JComboBox();
        comboBox.addItem("1");
        comboBox.addItem("2");
        comboBox.addItem("3");
        comboBox.addItem("4");
        comboBox.addItem("5");
        comboBox.addItem("6");
        comboBox.addItem("7");
        comboBox.addItem("8");
        comboBox.addItem("9");


        buttonPanel.add(comboBox);
        buttonPanel.add(openButton);
        buttonPanel.add(restoreButton);


        //Add the buttons and the log to this panel.
        add(buttonPanel, BorderLayout.PAGE_START);

        btnDelete = new JButton("Delete");
        btnDelete.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                openDeleteDialog();
            }
        });
        buttonPanel.add(btnDelete);
        buttonPanel.add(removeButton);
        add(logScrollPane, BorderLayout.CENTER);

        RemoteIdentifierContainer r;
        LocalIdentifierContainer l;

        if (new File(FileDescriptor.remoteChunkContainerFile).exists())
        {
            r = RemoteIdentifierContainer.load();
        }
        else
        {
            r = new RemoteIdentifierContainer();
        }

        if (new File(FileDescriptor.localChunkContainerFile).exists())
        {
            l = LocalIdentifierContainer.load();
        }
        else
        {
            l = new LocalIdentifierContainer();
        }

        mc = new MCThread(r);
        mdb = new MDBThread();
        mdr = new MDRThread();
        input = new UserInputThread();

        // Initiating multicast channel threads
        mc.start();
        mdb.start();
        mdr.start();
        input.start();
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {

        //Handle open button action.
        if (e.getSource() == openButton)
        {
            int returnVal = fc.showOpenDialog(FileChooserFrame.this);

            if (returnVal == JFileChooser.APPROVE_OPTION)
            {
                final File file = fc.getSelectedFile();
                //This is where a real application would open the file.
                log.append("Uploading: " + file.getName() + "." + newline + "Num of copies: " + comboBox.getSelectedItem() + newline);


                new Thread()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            input.doBackup(file.getName(), file.getPath(), Integer.decode((String) comboBox.getSelectedItem()));
                        }
                        catch (InterruptedException | IOException | NoSuchAlgorithmException | MaxAttemptsReached ex)
                        {
                            Logger.getLogger(FileChooserFrame.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }.start();


            }
            log.setCaretPosition(log.getDocument().getLength());
        }
        else if (e.getSource() == removeButton)
        {
            int returnVal = fc.showOpenDialog(FileChooserFrame.this);

            if (returnVal == JFileChooser.APPROVE_OPTION)
            {
                final File file = fc.getSelectedFile();
                //This is where a real application would open the file.
                log.append("Removing chunks from: " + file.getName() + newline);


                new Thread()
                {
                    @Override
                    public void run()
                    {
                            try {
								input.removeChunk(file.getName(), file.getPath());
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
                       
                    }
                }.start();


            }
            log.setCaretPosition(log.getDocument().getLength());
        }
        
    }

    /**
     * Returns an ImageIcon, or null if the path was invalid.
     */
    protected static ImageIcon createImageIcon(String path)
    {
        java.net.URL imgURL = FileChooserFrame.class.getResource(path);
        if (imgURL != null)
        {
            return new ImageIcon(imgURL);
        }
        else
        {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }

    /**
     * Create the GUI and show it. For thread safety, this method should be invoked from the event dispatch thread.
     *
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private static void createAndShowGUI() throws IOException, ClassNotFoundException
    {
        //Create and set up the window.
        JFrame frame = new JFrame("Backup ME");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent ev)
            {
                try
                {
                    UserInputThread.localFiles.save();
                    MCThread.remoteChunks.save();
                }
                catch (IOException ex)
                {
                    Logger.getLogger(FileChooserFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

        //Add content to the window.
        frame.getContentPane().add(new FileChooserFrame());

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) throws IOException
    {
        //Schedule a job for the event dispatch thread:
        //creating and showing this application's GUI.

        // Guarantee that all the needed folders exist
        File backupFolder = new File(datatypes.FileDescriptor.backupDir);
        File receivedChunkFolder = new File(datatypes.FileDescriptor.receivedChunkDir);
        File sentChunkFolder = new File(datatypes.FileDescriptor.sentChunkDir);
        File configFolder = new File(datatypes.FileDescriptor.configDir);
        File recoveryFolder = new File(datatypes.FileDescriptor.recoveredDir);

        if (!backupFolder.exists())
        {
            backupFolder.mkdir();
        }
        if (!receivedChunkFolder.exists())
        {
            receivedChunkFolder.mkdir();
        }
        if (!sentChunkFolder.exists())
        {
            sentChunkFolder.mkdir();
        }
        if (!configFolder.exists())
        {
            configFolder.mkdir();
        }
        if (!recoveryFolder.exists())
        {
            recoveryFolder.mkdir();
        }

        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                //Turn off metal's use of bold fonts
                UIManager.put("swing.boldMetal", Boolean.FALSE);
                try
                {
                    createAndShowGUI();
                }
                catch (IOException | ClassNotFoundException e)
                {
                    e.printStackTrace();
                }
            }
        });
    }

    /*Deletes a File on network, if it exists*/
    public void deleteFile(String filename) throws NoSuchAlgorithmException, IOException
    {
        RemoteIdentifierContainer ric = MCThread.remoteChunks;

        String encoded_filename = FileDescriptor.filename2Hash(filename);
        Set<RemoteIdentifier> set = ric.getIdentifiersByHash(encoded_filename);

        /*Only sends Delete Message if we really are the initiator-peer*/
        if (set.isEmpty())
        {
            FileChooserFrame.log.append("Unnable to Delete File. It doesn't exist or you don't have permission to Delete it.\n");
        }
        else
        {
            input.sendDeleteMessage(encoded_filename);
        }
    }

    private void openDeleteDialog()
    {
        JDialog dialog = new JDialog();
        dialog.setTitle("Delete File");
        dialog.setResizable(true);

        dialog.getContentPane().setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        dialog.getContentPane().add(new JLabel("Filename:"));
        final JTextField textField = new JTextField();

        JButton btnNewButton = new JButton("Delete it!");
        btnNewButton.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent arg0)
            {
                try
                {
                    deleteFile(textField.getText());
                }
                catch (NoSuchAlgorithmException | IOException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });

        dialog.getContentPane().add(textField);
        dialog.getContentPane().add(btnNewButton);
        textField.setColumns(20);

        JTextArea textArea = new JTextArea();
        dialog.getContentPane().add(textArea);


        dialog.setModal(true);
        dialog.pack();
        dialog.setVisible(true);
    }

    private void openRestoreDialog()
    {
        JDialog dialog = new JDialog();
        dialog.setTitle("Restore File");
        dialog.setResizable(true);

        dialog.getContentPane().setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        dialog.getContentPane().add(new JLabel("Filename:"));
        final JTextField textField = new JTextField();

        JButton btnNewButton = new JButton("Get it!");
        btnNewButton.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent arg0)
            {
                try
                {
                    input.doRestore(textField.getText());
                }
                catch (InvalidFile | IOException | InterruptedException e )
                {
                    Logger.getLogger(FileChooserFrame.class.getName()).log(Level.SEVERE, null, e);
                }
            }
        });

        dialog.getContentPane().add(textField);
        dialog.getContentPane().add(btnNewButton);
        textField.setColumns(20);

        JTextArea textArea = new JTextArea();
        dialog.getContentPane().add(textArea);


        dialog.setModal(true);
        dialog.pack();
        dialog.setVisible(true);
    }
}