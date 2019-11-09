package com.rolandbernard.astar.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import com.rolandbernard.astar.AStar;

@SuppressWarnings("serial")
public class AStarWindow extends JFrame {

	public static void main(String[] args) {
		new AStarWindow(); // create gui window
	}
	
	private AStar as = null;
	private AStarGui asGui = null;
	
	public AStarWindow() {
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setTitle("AStern");
		this.setSize(700, 500);
		
		this.as = new AStar();
		
		this.setLayout(new BorderLayout());
		Container contPane = this.getContentPane();
		
		// a* map
		this.asGui = new AStarGui(this.as);
		this.asGui.setEditMode(null);
		contPane.add(this.asGui, BorderLayout.CENTER);
	
		// edit and player tabs
		JTabbedPane controls = new JTabbedPane();
		controls.addTab("Edit", new AStarEditor(this.asGui, this.as));
		controls.addTab("Calculate", new AStarPlayer(this.asGui, this.as));
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
