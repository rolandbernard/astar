package net.tfobz.astern.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import net.tfobz.astern.AStern;

@SuppressWarnings("serial")
public class ASternWindow extends JFrame {

	public static void main(String[] args) {
		new ASternWindow(); // create gui window
	}
	
	private AStern as = null;
	private ASternGui asGui = null;
	
	public ASternWindow() {
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setTitle("AStern");
		this.setSize(700, 500);
		
		this.as = new AStern();
		
		this.setLayout(new BorderLayout());
		Container contPane = this.getContentPane();
		
		// a* map
		this.asGui = new ASternGui(this.as);
		this.asGui.setEditMode(null);
		contPane.add(this.asGui, BorderLayout.CENTER);
	
		// edit and player tabs
		JTabbedPane controls = new JTabbedPane();
		controls.addTab("Edit", new ASternEditor(this.asGui, this.as));
		controls.addTab("Calculate", new ASternPlayer(this.asGui, this.as));
		controls.addChangeListener(e->{
			if(((JTabbedPane)e.getSource()).getSelectedIndex() == 1)
				this.asGui.setSelection(2);
			else
				this.asGui.setEditMode(null);
			this.as.delSavedPath();
		});
		contPane.add(controls, BorderLayout.EAST);
		
		this.setVisible(true);
	}
}
