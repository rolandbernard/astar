package com.rolandbernard.astar.gui;

import javax.swing.JPanel;

import com.rolandbernard.astar.AStar;

import java.awt.GridBagLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.Collection;

import javax.swing.JLabel;
import javax.swing.JOptionPane;

@SuppressWarnings("serial")
public class AStarEditor extends JPanel {
	
	private AStar as = null;
	private AStarGui asGui = null;
	
	public AStarEditor(AStarGui gui, AStar as) {
		this.as = as;
		this.asGui = gui;
		
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 0, 0, 0, 0};
		gbl_panel.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		gbl_panel.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		this.setLayout(gbl_panel);
		
		// save button
		JButton btnSave = new JButton("Save");
		btnSave.addActionListener(e->{
			JFileChooser fc = new JFileChooser();
			if(fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				try {
					this.as.dumpNodesAndConnections(new FileWriter(fc.getSelectedFile().getPath()));
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(this, ex.getMessage());
				}
			}
		});
		GridBagConstraints gbc_btnSave = new GridBagConstraints();
		gbc_btnSave.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnSave.insets = new Insets(0, 0, 5, 5);
		gbc_btnSave.gridx = 1;
		gbc_btnSave.gridy = 1;
		this.add(btnSave, gbc_btnSave);
		
		// load button
		JButton btnLoad = new JButton("Load");
		btnLoad.addActionListener(e->{
			JFileChooser fc = new JFileChooser();
			if(fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				try {
					this.as.clear();
					this.as.readNodesAndConnections(new BufferedReader(new FileReader(fc.getSelectedFile().getPath())));
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(this, ex.getMessage());
				}
			}
		});
		GridBagConstraints gbc_btnLoad = new GridBagConstraints();
		gbc_btnLoad.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnLoad.insets = new Insets(0, 0, 5, 5);
		gbc_btnLoad.gridx = 2;
		gbc_btnLoad.gridy = 1;
		this.add(btnLoad, gbc_btnLoad);
		
		// add button
		JButton btnAddNode = new JButton("Add node");
		btnAddNode.addActionListener(e->this.asGui.setEditMode(AStarGui.EditMode.ADD));
		GridBagConstraints gbc_btnAddNode = new GridBagConstraints();
		gbc_btnAddNode.gridwidth = 2;
		gbc_btnAddNode.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnAddNode.insets = new Insets(0, 0, 5, 5);
		gbc_btnAddNode.gridx = 1;
		gbc_btnAddNode.gridy = 4;
		this.add(btnAddNode, gbc_btnAddNode);
		
		// move button
		JButton btnMoveNode = new JButton("Move node");
		btnMoveNode.addActionListener(e->this.asGui.setEditMode(AStarGui.EditMode.MOVE));
		GridBagConstraints gbc_btnMoveNode = new GridBagConstraints();
		gbc_btnMoveNode.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnMoveNode.gridwidth = 2;
		gbc_btnMoveNode.insets = new Insets(0, 0, 5, 5);
		gbc_btnMoveNode.gridx = 1;
		gbc_btnMoveNode.gridy = 5;
		this.add(btnMoveNode, gbc_btnMoveNode);
		
		// delete button
		JButton btnDeleteNode = new JButton("Delete node");
		btnDeleteNode.addActionListener(e->this.asGui.setEditMode(AStarGui.EditMode.DEL));
		GridBagConstraints gbc_btnDeleteNode = new GridBagConstraints();
		gbc_btnDeleteNode.gridwidth = 2;
		gbc_btnDeleteNode.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnDeleteNode.insets = new Insets(0, 0, 5, 5);
		gbc_btnDeleteNode.gridx = 1;
		gbc_btnDeleteNode.gridy = 6;
		this.add(btnDeleteNode, gbc_btnDeleteNode);
		
		// rename button
		JButton btnRenameNode = new JButton("Rename node");
		btnRenameNode.addActionListener(e->this.asGui.setEditMode(AStarGui.EditMode.RENAME));
		GridBagConstraints gbc_btnRenameNode = new GridBagConstraints();
		gbc_btnRenameNode.gridwidth = 2;
		gbc_btnRenameNode.insets = new Insets(0, 0, 5, 5);
		gbc_btnRenameNode.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnRenameNode.gridx = 1;
		gbc_btnRenameNode.gridy = 7;
		this.add(btnRenameNode, gbc_btnRenameNode);
		
		// add connection label
		JLabel lblSetConnection = new JLabel("Add connection:");
		GridBagConstraints gbc_lblSetConnection = new GridBagConstraints();
		gbc_lblSetConnection.gridwidth = 2;
		gbc_lblSetConnection.insets = new Insets(0, 0, 5, 5);
		gbc_lblSetConnection.fill = GridBagConstraints.BOTH;
		gbc_lblSetConnection.gridx = 1;
		gbc_lblSetConnection.gridy = 8;
		this.add(lblSetConnection, gbc_lblSetConnection);
		
		// add connection button (automatic cost)
		JButton btnAuto = new JButton("Auto");
		btnAuto.addActionListener(e->this.asGui.setEditMode(AStarGui.EditMode.ADDCON));
		GridBagConstraints gbc_btnAuto = new GridBagConstraints();
		gbc_btnAuto.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnAuto.insets = new Insets(0, 0, 5, 5);
		gbc_btnAuto.gridx = 1;
		gbc_btnAuto.gridy = 9;
		this.add(btnAuto, gbc_btnAuto);
		
		// add connection button (manual cost) 
		JButton btnManual = new JButton("Manual");
		btnManual.addActionListener(e->this.asGui.setEditMode(AStarGui.EditMode.ADDCONSPE));
		GridBagConstraints gbc_btnManual = new GridBagConstraints();
		gbc_btnManual.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnManual.insets = new Insets(0, 0, 5, 5);
		gbc_btnManual.gridx = 2;
		gbc_btnManual.gridy = 9;
		this.add(btnManual, gbc_btnManual);
		
		/// remove connection button
		JButton btnRemoveConnection = new JButton("Remove connection");
		btnRemoveConnection.addActionListener(e->this.asGui.setEditMode(AStarGui.EditMode.DELCON));
		GridBagConstraints gbc_btnRemoveConnection = new GridBagConstraints();
		gbc_btnRemoveConnection.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnRemoveConnection.gridwidth = 2;
		gbc_btnRemoveConnection.insets = new Insets(0, 0, 5, 5);
		gbc_btnRemoveConnection.gridx = 1;
		gbc_btnRemoveConnection.gridy = 10;
		this.add(btnRemoveConnection, gbc_btnRemoveConnection);
		
		/// Add a random node
		JButton btnAddRandomNode = new JButton("Add random node");
		btnAddRandomNode.addActionListener(e->{
			this.asGui.setEditMode(null);
			AStar.Node newNode = new AStar.Node(null, Math.random()*10-5, Math.random()*10-5);
			this.as.addNode(newNode);
		});
		GridBagConstraints gbc_btnAddRandomNode = new GridBagConstraints();
		gbc_btnAddRandomNode.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnAddRandomNode.gridwidth = 2;
		gbc_btnAddRandomNode.insets = new Insets(0, 0, 5, 5);
		gbc_btnAddRandomNode.gridx = 1;
		gbc_btnAddRandomNode.gridy = 12;
		this.add(btnAddRandomNode, gbc_btnAddRandomNode);
		
		/// Add a random connection
		JButton btnAddRandCon = new JButton("Add rand. con.");
		btnAddRandCon.addActionListener(e->{
			this.asGui.setEditMode(null);
			Collection<AStar.Node> nodes = this.as.getNodes();
			int rand = (int) (nodes.size()*Math.random());
			AStar.Node start = nodes.stream().skip(rand).findFirst().get();
			AStar.Node[] closest = new AStar.Node[10];
			for(AStar.Node n : nodes)
				if(!n.equals(start))
					for(int i = 0; i < closest.length; i++)
						if(closest[i] == null || closest[i].distFrom(start) > n.distFrom(start)) {
							for(int j = closest.length-1; j > i; j--)
								closest[j] = closest[j-1];
							closest[i] = n;
							break;
						}
			int length = 0;
			while(length < closest.length && closest[length] != null) length++;
			if(length != 0) {
				rand = (int) (length*Math.random());
				AStar.Node end = Arrays.stream(closest).skip(rand).findFirst().get();
				this.as.setConnection(start, end, start.distFrom(end));
				this.as.setConnection(end, start, start.distFrom(end));
			}
		});
		GridBagConstraints gbc_btnAddRandCon = new GridBagConstraints();
		gbc_btnAddRandCon.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnAddRandCon.gridwidth = 2;
		gbc_btnAddRandCon.insets = new Insets(0, 0, 5, 5);
		gbc_btnAddRandCon.gridx = 1;
		gbc_btnAddRandCon.gridy = 13;
		this.add(btnAddRandCon, gbc_btnAddRandCon);
		
		// Build a random graph
		JButton btnBuildRnadGraph = new JButton("Add rand. graph");
		btnBuildRnadGraph.addActionListener(e->{
			this.asGui.setEditMode(null);
			for(int k = 0; k < 100; k++) {
				AStar.Node newNode = new AStar.Node(null, Math.random()*10-5, Math.random()*10-5);
				this.as.addNode(newNode);	
			}
			for(int k = 0; k < 500; k++) {
				Collection<AStar.Node> nodes = this.as.getNodes();
				int rand = (int) (nodes.size()*Math.random());
				AStar.Node start = nodes.stream().skip(rand).findFirst().get();
				AStar.Node[] closest = new AStar.Node[10];
				for(AStar.Node n : nodes)
					if(!n.equals(start))
						for(int i = 0; i < closest.length; i++)
							if(closest[i] == null || closest[i].distFrom(start) > n.distFrom(start)) {
								for(int j = closest.length-1; j > i; j--)
									closest[j] = closest[j-1];
								closest[i] = n;
								break;
							}
				int length = 0;
				while(length < closest.length && closest[length] != null) length++;
				if(length != 0) {
					rand = (int) (length*Math.random());
					AStar.Node end = Arrays.stream(closest).skip(rand).findFirst().get();
					this.as.setConnection(start, end, start.distFrom(end));
					this.as.setConnection(end, start, start.distFrom(end));
				}
			}
		});
		GridBagConstraints gbc_btnBuildRnadGraph = new GridBagConstraints();
		gbc_btnBuildRnadGraph.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnBuildRnadGraph.gridwidth = 2;
		gbc_btnBuildRnadGraph.insets = new Insets(0, 0, 5, 5);
		gbc_btnBuildRnadGraph.gridx = 1;
		gbc_btnBuildRnadGraph.gridy = 14;
		this.add(btnBuildRnadGraph, gbc_btnBuildRnadGraph);
		
		/// delete the graph
		JButton btnDelGraph = new JButton("Delete graph");
		btnDelGraph.addActionListener(e->{
			this.as.clear();
			this.asGui.setEditMode(null);
		});
		GridBagConstraints gbc_btnDelGraph = new GridBagConstraints();
		gbc_btnDelGraph.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnDelGraph.gridwidth = 2;
		gbc_btnDelGraph.insets = new Insets(0, 0, 5, 5);
		gbc_btnDelGraph.gridx = 1;
		gbc_btnDelGraph.gridy = 15;
		this.add(btnDelGraph, gbc_btnDelGraph);
	}
	
}
