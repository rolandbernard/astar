package net.tfobz.astern.gui;

import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JOptionPane;

import net.tfobz.astern.AStern;
import net.tfobz.astern.AStern.Node;

/**
 * Implements a Java Swing Component to display and edit a AStern object
 */
@SuppressWarnings("serial")
public class ASternGui extends JComponent {

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
	
	private AStern as = null;
	private EditMode editMode = null;
	private AStern.Node[] selection = null;
	private int xoff = 0;
	private int yoff = 0;
	
	public ASternGui() {
		super();
		this.selection = new AStern.Node[0];
	
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
				ASternGui.this.xoff += e.getX() - prevx;
				ASternGui.this.yoff += e.getY() - prevy;
				prevx = e.getX();
				prevy = e.getY();
			}
		};
		this.addMouseListener(mouseAdapter);
		this.addMouseMotionListener(mouseAdapter);
		
		this.addMouseWheelListener(e->{
			double factor = Math.pow(2, -e.getPreciseWheelRotation()/10);
			double delta = AStern.GRAPHICS_SCALE*factor - AStern.GRAPHICS_SCALE;
			if(delta >= 0 && delta < 1)
				delta = 1; // prevent getting stuck
			if(AStern.GRAPHICS_SCALE + delta < 1)
				delta = -AStern.GRAPHICS_SCALE+1;
			double x = (e.getX() - this.xoff) / (double)AStern.GRAPHICS_SCALE;
			double y = (e.getY() - this.yoff) / (double)AStern.GRAPHICS_SCALE;
			this.xoff -= x*delta;
			this.yoff -= y*delta;
			AStern.GRAPHICS_SCALE += delta;
		});
		
		// execute action of current edit mode
		this.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(editMode != null) {
					double x = (e.getX() - ASternGui.this.xoff) / (double)AStern.GRAPHICS_SCALE;
					double y = (e.getY() - ASternGui.this.yoff) / (double)AStern.GRAPHICS_SCALE;
					
					if(as != null) {
						try {
							switch (ASternGui.this.editMode) {
							case ADD: {
								AStern.Node n = ASternGui.this.as.getNodeClosestTo(x, y);
								if(n.distFrom(x, y) >= 3/(double)AStern.GRAPHICS_SCALE) // Don't place them on top of each other
									if(ASternGui.this.as != null)
										ASternGui.this.as.addNode(new Node("", x, y));
								break; }
							case RENAME: {
								AStern.Node n = ASternGui.this.as.getNodeClosestTo(x, y);
								if(n.distFrom(x, y) < 5/(double)AStern.GRAPHICS_SCALE) {
									String name = JOptionPane.showInputDialog("New name:");
									if(name != null)
										n.setName(name);
								}
								break; }
							case DEL: {
								AStern.Node n = ASternGui.this.as.getNodeClosestTo(x, y);
								if(n.distFrom(x, y) < 5/(double)AStern.GRAPHICS_SCALE)
									ASternGui.this.as.delNode(n);
								break; }
							case ADDCON:
								if(ASternGui.this.selection[0] == null) {
									AStern.Node n = ASternGui.this.as.getNodeClosestTo(x, y);
									if(n.distFrom(x, y) < 5/(double)AStern.GRAPHICS_SCALE) {
										n.setHighlight(true);
										ASternGui.this.selection[0] = n;
									}
								} else {
									AStern.Node n = ASternGui.this.as.getNodeClosestTo(x, y);
									if(n.distFrom(x, y) < 5/(double)AStern.GRAPHICS_SCALE) {
										ASternGui.this.as.setConnection(ASternGui.this.selection[0], n, n.distFrom(ASternGui.this.selection[0]));
										ASternGui.this.clearSelection();
									} else 
										ASternGui.this.clearSelection(); 
								}
								break;
							case ADDCONSPE:
								if(ASternGui.this.selection[0] == null) {
									AStern.Node n = ASternGui.this.as.getNodeClosestTo(x, y);
									if(n.distFrom(x, y) < 5/(double)AStern.GRAPHICS_SCALE) {
										n.setHighlight(true);
										ASternGui.this.selection[0] = n;
									}
								} else {
									AStern.Node n = ASternGui.this.as.getNodeClosestTo(x, y);
									if(n.distFrom(x, y) < 5/(double)AStern.GRAPHICS_SCALE) {
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
											ASternGui.this.as.setConnection(ASternGui.this.selection[0], n, weight);
										ASternGui.this.clearSelection();
									} else 
										ASternGui.this.clearSelection();
								}
								break;
							case DELCON:
								if(ASternGui.this.selection[0] == null) {
									AStern.Node n = ASternGui.this.as.getNodeClosestTo(x, y);
									if(n.distFrom(x, y) < 5/(double)AStern.GRAPHICS_SCALE) {
										n.setHighlight(true);
										ASternGui.this.selection[0] = n;
									}
								} else {
									AStern.Node n = ASternGui.this.as.getNodeClosestTo(x, y);
									if(n.distFrom(x, y) < 5/(double)AStern.GRAPHICS_SCALE) {
										try {
											ASternGui.this.as.delConnection(ASternGui.this.selection[0], n);
											ASternGui.this.clearSelection();
										} catch (IllegalArgumentException ex) { 
											// Connection doesn't exist
											ASternGui.this.clearSelection(); 
										}
									} else 
										ASternGui.this.clearSelection();
								}
								break;
							case MOVE:
								if(ASternGui.this.selection[0] == null) {
									AStern.Node n = ASternGui.this.as.getNodeClosestTo(x, y);
									if(n.distFrom(x, y) < 5/(double)AStern.GRAPHICS_SCALE) {
										n.setHighlight(true);
										ASternGui.this.selection[0] = n;
									}
								} else {
									AStern.Node n = ASternGui.this.as.getNodeClosestTo(x, y);
									if(n.distFrom(x, y) >= 3/(double)AStern.GRAPHICS_SCALE) { // Don't place them on top of each other
										ASternGui.this.selection[0].setX(x);
										ASternGui.this.selection[0].setY(y);
										ASternGui.this.clearSelection();
									}
								}
								break;
							case SELECT:
								AStern.Node n = ASternGui.this.as.getNodeClosestTo(x, y);
								if(n.distFrom(x, y) < 5/(double)AStern.GRAPHICS_SCALE) {
									if(ASternGui.this.selection.length != 0) { 
										if(ASternGui.this.selection[ASternGui.this.selection.length-1] != null)
											ASternGui.this.clearSelection();
										int size = 0;
										while(ASternGui.this.selection[size] != null) size++;
										n.setHighlight(true);
										ASternGui.this.selection[size] = n;	
									}
								} else
									ASternGui.this.clearSelection();
							}
						} catch (IllegalArgumentException ex) { /*happens if no nodes are present*/; }
					}
				}
			}
		});
	}
	
	public ASternGui(AStern as) {
		this();
		this.setAStern(as);
	}
	
	public void setAStern(AStern as) {
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
				this.selection = new AStern.Node[1];
				break;
			case DEL:
			case ADD:
			case RENAME:
			case SELECT:
				this.selection = new AStern.Node[0];
				break;
			}
	}
	
	public void setSelection(int numToSelect) {
		this.editMode = EditMode.SELECT;
		this.clearSelection();
		this.selection = new AStern.Node[numToSelect];
	}
	
	public AStern.Node[] getSelection() throws IllegalStateException {
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
		g.drawString(Double.toString(((int)(this.getWidth()*100/AStern.GRAPHICS_SCALE))/100.0), 10, this.getHeight()-10);
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
