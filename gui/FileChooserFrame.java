package gui;

import thread.*;
import datatypes.*;
import java.io.*;
import java.util.HashMap;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.filechooser.*;

public class FileChooserFrame extends JPanel
        implements ActionListener
{
    private HashMap<String, String> localChunks = new HashMap<>();
    static private final String newline = "\n";
    JButton openButton, restoreButton;
    JTextArea log;
    JFileChooser fc;
    private JComboBox comboBox;

    public FileChooserFrame()
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

        openButton = new JButton("Backup");
        openButton.addActionListener(this);

        restoreButton = new JButton("Restore");
        restoreButton.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                RestoreFrame restoreFrame = new RestoreFrame();
                restoreFrame.setVisible(true);
                restoreFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            }
        });


        //For layout purposes, put the buttons in a separate panel
        JPanel buttonPanel = new JPanel(); //use FlowLayout


        /*comboBox indicating the number of copies of a file user wants to backup*/
        comboBox = new JComboBox();
        comboBox.addItem("1");
        comboBox.addItem("2");
        comboBox.addItem("3");
        comboBox.addItem("4");
        comboBox.addItem("5");


        buttonPanel.add(comboBox);
        buttonPanel.add(openButton);
        buttonPanel.add(restoreButton);


        //Add the buttons and the log to this panel.
        add(buttonPanel, BorderLayout.PAGE_START);
        add(logScrollPane, BorderLayout.CENTER);
    }

    public void actionPerformed(ActionEvent e)
    {

        //Handle open button action.
        if (e.getSource() == openButton)
        {
            int returnVal = fc.showOpenDialog(FileChooserFrame.this);

            if (returnVal == JFileChooser.APPROVE_OPTION)
            {
                File file = fc.getSelectedFile();
                //This is where a real application would open the file.
                log.append("Uploading: " + file.getName() + "." + newline + "Num of copies: " + comboBox.getSelectedItem() + newline);
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
     */
    private static void createAndShowGUI()
    {
        //Create and set up the window.
        JFrame frame = new JFrame("Backup ME");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

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

        // Initiating multicast channel threads

        MCThread mc = new MCThread();
        MDBThread mdb = new MDBThread();
        MDRThread mdr = new MDRThread();
        UserInputThread input = new UserInputThread();

        mc.start();
        mdb.start();
        mdr.start();
        input.start();

        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                //Turn off metal's use of bold fonts
                UIManager.put("swing.boldMetal", Boolean.FALSE);
                createAndShowGUI();
            }
        });
    }
}