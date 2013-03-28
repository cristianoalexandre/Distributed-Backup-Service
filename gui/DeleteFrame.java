package gui;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.FlowLayout;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.JLabel;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class DeleteFrame extends JFrame {

	private JPanel contentPane;
	private JTextField textField;

	/**
	 * Create the frame.
	 */
	public DeleteFrame() {
		this.setTitle("Delete File");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 431, 93);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		textField = new JTextField();
		
		contentPane.add(textField);
		textField.setColumns(20);
		
		JTextArea textArea = new JTextArea();
		contentPane.add(textArea);
		
		JButton btnNewButton = new JButton("Delete it!");
		btnNewButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				
				/*TO DO - Action taken when the filename to restore is submited*/
				System.out.println(textField.getText()); // for test only
			}
		});
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
			}
		});
		contentPane.add(btnNewButton);
	}

}
