package com.rolandbernard.astar.gui;

import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JOptionPane;

import com.rolandbernard.astar.AStar;
import com.rolandbernard.astar.AStar.Node;

/**
 * Implements a Java Swing Component to display and edit a AStern object
 */
@SuppressWarnings("serial")
public class AStarGui extends JComponent {

	public static enum EditMode {
		MOVE,
		ADD,
		DEL,
		RENAME,
		ADDCON,
		ADDCONSPE,
		DELCON,
		SELECT
	}
	
	private AStar as = null;
	private EditMode editMode = null;
	private AStar.Node[] selection = null;
	private int xoff = 0;
	private int yoff = 0;
	
	public AStarGui() {
		super();
		this.selection = new AStar.Node[0];
	
		// setup listeners for editing and moving the map
		MouseAdapter mouseAdapter = new MouseAdapter() {
			int prevx;
			int prevy;

		    @Override
		    public void mousePressed(MouseEvent e) {
		    	prevx = e.getX();
		        prevy = e.getY();
		    }
		    
			@Override
			public void mouseDragged(MouseEvent e) {
				AStarGui.this.xoff += e.getX() - prevx;
				AStarGui.this.yoff += e.getY() - prevy;
				prevx = e.getX();
				prevy = e.getY();
			}
		};
		this.addMouseListener(mouseAdapter);
		this.addMouseMotionListener(mouseAdapter);
		
		this.addMouseWheelListener(e->{
			double factor = Math.pow(2, -e.getPreciseWheelRotation()/10);
			double delta = AStar.GRAPHICS_SCALE*factor - AStar.GRAPHICS_SCALE;
			if(delta >= 0 && delta < 1)
				delta = 1; // prevent getting stuck
			if(AStar.GRAPHICS_SCALE + delta < 1)
				delta = -AStar.GRAPHICS_SCALE+1;
			double x = (e.getX() - this.xoff) / (double)AStar.GRAPHICS_SCALE;
			double y = (e.getY() - this.yoff) / (double)AStar.GRAPHICS_SCALE;
			this.xoff -= x*delta;
			this.yoff -= y*delta;
			AStar.GRAPHICS_SCALE += delta;
		});
		
		// execute action of current edit mode
		this.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(editMode != null) {
					double x = (e.getX() - AStarGui.this.xoff) / (double)AStar.GRAPHICS_SCALE;
					double y = (e.getY() - AStarGui.this.yoff) / (double)AStar.GRAPHICS_SCALE;
					
					if(as != null) {
						try {
							switch (AStarGui.this.editMode) {
							case ADD: {
								AStar.Node n = AStarGui.this.as.getNodeClosestTo(x, y);
								if(n.distFrom(x, y) >= 3/(double)AStar.GRAPHICS_SCALE) // Don't place them on top of each other
									if(AStarGui.this.as != null)
										AStarGui.this.as.addNode(new Node("", x, y));
								break; }
							case RENAME: {
								AStar.Node n = AStarGui.this.as.getNodeClosestTo(x, y);
								if(n.distFrom(x, y) < 5/(double)AStar.GRAPHICS_SCALE) {
									String name = JOptionPane.showInputDialog("New name:");
									if(name != null)
										n.setName(name);
								}
								break; }
							case DEL: {
								AStar.Node n = AStarGui.this.as.getNodeClosestTo(x, y);
								if(n.distFrom(x, y) < 5/(double)AStar.GRAPHICS_SCALE)
									AStarGui.this.as.delNode(n);
								break; }
							case ADDCON:
								if(AStarGui.this.selection[0] == null) {
									AStar.Node n = AStarGui.this.as.getNodeClosestTo(x, y);
									if(n.distFrom(x, y) < 5/(double)AStar.GRAPHICS_SCALE) {
										n.setHighlight(true);
										AStarGui.this.selection[0] = n;
									}
								} else {
									AStar.Node n = AStarGui.this.as.getNodeClosestTo(x, y);
									if(n.distFrom(x, y) < 5/(double)AStar.GRAPHICS_SCALE) {
										AStarGui.this.as.setConnection(AStarGui.this.selection[0], n, n.distFrom(AStarGui.this.selection[0]));
										AStarGui.this.clearSelection();
									} else 
										AStarGui.this.clearSelection(); 
								}
								break;
							case ADDCONSPE:
								if(AStarGui.this.selection[0] == null) {
									AStar.Node n = AStarGui.this.as.getNodeClosestTo(x, y);
									if(n.distFrom(x, y) < 5/(double)AStar.GRAPHICS_SCALE) {
										n.setHighlight(true);
										AStarGui.this.selection[0] = n;
									}
								} else {
									AStar.Node n = AStarGui.this.as.getNodeClosestTo(x, y);
									if(n.distFrom(x, y) < 5/(double)AStar.GRAPHICS_SCALE) {
										String input = null;
										double weight = -1;
										do {
											input = JOptionPane.showInputDialog("Cost:");
											if(input != null) 
												try {
													weight = Double.parseDouble(input);
												} catch (NumberFormatException ex) { ; }
										} while (input != null && weight < 0);
										if(weight >= 0)
											AStarGui.this.as.setConnection(AStarGui.this.selection[0], n, weight);
										AStarGui.this.clearSelection();
									} else 
										AStarGui.this.clearSelection();
								}
								break;
							case DELCON:
								if(AStarGui.this.selection[0] == null) {
									AStar.Node n = AStarGui.this.as.getNodeClosestTo(x, y);
									if(n.distFrom(x, y) < 5/(double)AStar.GRAPHICS_SCALE) {
										n.setHighlight(true);
										AStarGui.this.selection[0] = n;
									}
								} else {
									AStar.Node n = AStarGui.this.as.getNodeClosestTo(x, y);
									if(n.distFrom(x, y) < 5/(double)AStar.GRAPHICS_SCALE) {
										try {
											AStarGui.this.as.delConnection(AStarGui.this.selection[0], n);
											AStarGui.this.clearSelection();
										} catch (IllegalArgumentException ex) { 
											// Connection doesn't exist
											AStarGui.this.clearSelection(); 
										}
									} else 
										AStarGui.this.clearSelection();
								}
								break;
							case MOVE:
								if(AStarGui.this.selection[0] == null) {
									AStar.Node n = AStarGui.this.as.getNodeClosestTo(x, y);
									if(n.distFrom(x, y) < 5/(double)AStar.GRAPHICS_SCALE) {
										n.setHighlight(true);
										AStarGui.this.selection[0] = n;
									}
								} else {
									AStar.Node n = AStarGui.this.as.getNodeClosestTo(x, y);
									if(n.distFrom(x, y) >= 3/(double)AStar.GRAPHICS_SCALE) { // Don't place them on top of each other
										AStarGui.this.selection[0].setX(x);
										AStarGui.this.selection[0].setY(y);
										AStarGui.this.clearSelection();
									}
								}
								break;
							case SELECT:
								AStar.Node n = AStarGui.this.as.getNodeClosestTo(x, y);
								if(n.distFrom(x, y) < 5/(double)AStar.GRAPHICS_SCALE) {
									if(AStarGui.this.selection.length != 0) { 
										if(AStarGui.this.selection[AStarGui.this.selection.length-1] != null)
											AStarGui.this.clearSelection();
										int size = 0;
										while(AStarGui.this.selection[size] != null) size++;
										n.setHighlight(true);
										AStarGui.this.selection[size] = n;	
									}
								} else
									AStarGui.this.clearSelection();
							}
						} catch (IllegalArgumentException ex) { /*happens if no nodes are present*/; }
					}
				}
			}
		});
	}
	
	public AStarGui(AStar as) {
		this();
		this.setAStern(as);
	}
	
	public void setAStern(AStar as) {
		this.as = as;
	}
	
	public void setEditMode(EditMode editMode) {
		this.editMode = editMode;
		this.clearSelection();
		if(editMode != null)
			switch (editMode) {
			case MOVE:
			case ADDCON:
			case ADDCONSPE:
			case DELCON:
				this.selection = new AStar.Node[1];
				break;
			case DEL:
			case ADD:
			case RENAME:
			case SELECT:
				this.selection = new AStar.Node[0];
				break;
			}
	}
	
	public void setSelection(int numToSelect) {
		this.editMode = EditMode.SELECT;
		this.clearSelection();
		this.selection = new AStar.Node[numToSelect];
	}
	
	public AStar.Node[] getSelection() throws IllegalStateException {
		if(this.selection[this.selection.length-1] == null)
			throw new IllegalStateException("not enought selections made");
		return this.selection.clone();
	}
	
	public void clearSelection() {
		for(int i = 0; i < this.selection.length; i++) {
			if(this.selection[i] != null)
				this.selection[i].setHighlight(false);
			this.selection[i] = null;
		}
	}
	
	public void paint(Graphics g) {
		super.paint(g);
		// draw info about current edit mode
		if(this.editMode != null)
			g.drawString(this.editMode.toString(), 10, 20);
		// draw current scale
		g.drawString(Double.toString(((int)(this.getWidth()*100/AStar.GRAPHICS_SCALE))/100.0), 10, this.getHeight()-10);
		g.translate(xoff, yoff);
		
		// draw the A* map
		if(this.as != null)
			this.as.drawGraph(g);
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) { ; }
		
		this.repaint();
	}
}
