package com.rolandbernard.astar;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;

/**
 * @author roland
 * 
 * Die Klasse ermöglicht es Punkte einzulesen und die minimale distanz zwischen zwei Punktn mittels des
 * A*-Algorithmus zu errechnen
 */
public class AStar {
	// Variables for saving the graph
	private Map<Node, Map<Node, Double>> connections;
	private Collection<Node> nodes;

	// Working variables of the Algorithm
	private Map<Node, Node> prev = null;
	private Map<Node, Double> dist = null;
	private PriorityQueue<Node> openl = null;
	private List<Node> ret = null;
	private boolean inCalc = false;

	// Config variables
	private int waitTime = 0;
	private double distBias = 1;

	// Decides thow big one is when drawing to a Graphics element
	public static int GRAPHICS_SCALE = 500;
	
	/**
	 * @author roland
	 * 
	 * The class contains all the data for a node.
	 */
	public static class Node {
		private String name;
		private double x = 0.0;
		private double y = 0.0;
		private boolean highligt = false;

		/**
		 * Constructor that takes all important information about a node
		 * @param name
		 * @param x
		 * @param y
		 * @throws IllegalArgumentException
		 */
		public Node(String name, double x, double y) {
			this.setName(name);
			this.setX(x);
			this.setY(y);
		}

		// Getters and setters
		
		public void setHighlight(boolean highligt) {
			this.highligt = highligt;
		}
		
		public boolean getHighlight() {
			return this.highligt;
		}
		
		public void setName(String name) {
			if(name == null || name.equals(""))
				this.name = null;
			else
				this.name = name;
		}
		
		public String getName() {
			return this.name;
		}

		public void setX(double x) {
			this.x = x;
		}
		
		public void setY(double y) {
			this.y = y;
		}
		
		public double getX() {
			return this.x;
		}

		public double getY() {
			return this.y;
		}

		/**
		 * Retrns true is the given node is the same as the Node. Two nodes are the same if they have the
		 * same name and the same position.
		 * @param n
		 * @return true if they are the same, false otherwise
		 */
		public boolean equals(Node n) {
			if(this.name == null)
				return this.x == n.x && this.y == n.y && this.name == n.name;
			else
				return this.x == n.x && this.y == n.y && this.name.equals(n.name);
		}

		public String toString() {
			if(this.name == null)
				return ": " +  this.x + ";" + this.y;
			else
				return this.name + ": " + this.x + ";" + this.y;
		}
		
		/**
		 * Computes the euclidean distance between this node and the point (x,y).
		 * @param x
		 * @param y
		 * @return the euclidean distance
		 */
		public double distFrom(double x, double y) {
			double dx = this.x - x;
			double dy = this.y - y;
			return Math.sqrt(dx*dx + dy*dy);
		}
		
		/**
		 * Computes the euclidean distance between this node and another.
		 * @param n
		 * @return the euclidean distance
		 */
		public double distFrom(Node n) {
			return distFrom(n.x, n.y);
		}
	}

	/**
	 * Initialises an empty graph.
	 */
	public AStar() {
		this.nodes = new HashSet<>();
		this.connections = new HashMap<>();
	}

	/**
	 * Loads the graph from the BufferedReader in.
	 * @param in
	 * @throws IOException
	 * @throws IllegalArgumentException
	 */
	public AStar(BufferedReader in) throws IOException, IllegalArgumentException {
		this();
		readNodesAndConnections(in);
	}

	/**
	 * Loads the graph from a file.
	 * @param filename
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws IllegalArgumentException
	 */
	public AStar(String filename) throws FileNotFoundException, IOException, IllegalArgumentException {
		this();
		FileInputStream in = new FileInputStream(filename);
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		readNodesAndConnections(reader);
	}

	/**
	 * Reads the graph from the file and adds it to the current one.
	 * @param in
	 * @throws IOException
	 * @throws IllegalArgumentException
	 */
	public synchronized void readNodesAndConnections(BufferedReader in) throws IOException, IllegalArgumentException {
		String line;
		int l = 1;
		while((line = in.readLine()) != null) {
			line = line.trim();
			if(line.length() > 0) {
				if(line.charAt(0) == 'n') /* new node */ {
					try {
						String[] st = line.substring(1).split(":");
						if(st == null || st.length != 2)
							throw new IllegalArgumentException("line " + Integer.toString(l) + ": syntax error");	
						String name = st[0].trim();
						st = st[1].split(";");
						if(st == null || st.length != 2)
							throw new IllegalArgumentException("line " + Integer.toString(l) + ": syntax error");
						double x = Double.parseDouble(st[0].trim());
						double y = Double.parseDouble(st[1].trim());
						this.addNode(new Node(name, x, y));
					} catch (NumberFormatException e) {
						throw new IllegalArgumentException("line " + Integer.toString(l) + ": couldn't parse coords");
					} catch (IllegalArgumentException e) {
						throw new IllegalArgumentException("line " + Integer.toString(l) + ": " + e.getMessage());
					}
				} else if(line.charAt(0) == 'c') /* new connection */ {
					try {
						Boolean strict = false;
						if(line.charAt(1) == '!') /* make only one connection */ {
							strict = true;
							line = line.substring(1);
						}
						if(line.charAt(line.length()-1) == '=') // Allows `c == ` to connect every unnamed node with every unnamed node
							line += " ";
						String[] st = line.substring(1).split("=");
						if(st == null || st.length != 3)
							throw new IllegalArgumentException("line " + Integer.toString(l) + ": syntax error");
						int dir = 0;
						if(st[0].charAt(st[0].length()-1) == '<') {
							dir -= 1;
							st[0] = st[0].substring(0, st[0].length()-1);
						}
						if(st[2].charAt(0) == '>') {
							dir += 1;
							st[2] = st[2].substring(1);
						}
						String name1;
						String name2;
						// determine the direction of the connection
						if(dir < 0) {
							dir = 1;
							name1 = st[2].trim();
							name2 = st[0].trim();
						} else {
							name1 = st[0].trim();
							name2 = st[2].trim();
						}
						// find matching objects
						Object[] n1;
						Object[] n2;
						if(name1.contains(":")) {
							String[] strs = name1.split(":");
							if(strs == null || strs.length != 2)
								throw new IllegalArgumentException("line " + Integer.toString(l) + ": syntax error");
							String name = strs[0].trim();
							strs = strs[1].split(";");
							if(strs == null || strs.length != 2)
								throw new IllegalArgumentException("line " + Integer.toString(l) + ": syntax error");
							double x = Double.parseDouble(strs[0].trim());
							double y = Double.parseDouble(strs[1].trim());
							Node search = new Node(name, x, y);
							n1 = this.nodes.stream().filter((n)->{return n.equals(search);}).toArray();
						} else {
							n1 = this.nodes.stream().filter((x)->{
								if(name1.equals(""))
									return x.getName() == null;
								else
									if(x.getName() == null)
										return false;
									else 
										return x.getName().matches(name1);
							}).toArray();
						}
						if(name2.contains(":")) {
							String[] strs = name2.split(":");
							if(strs == null || strs.length != 2)
								throw new IllegalArgumentException("line " + Integer.toString(l) + ": syntax error");
							String name = strs[0].trim();
							strs = strs[1].split(";");
							if(strs == null || strs.length != 2)
								throw new IllegalArgumentException("line " + Integer.toString(l) + ": syntax error");
							double x = Double.parseDouble(strs[0].trim());
							double y = Double.parseDouble(strs[1].trim());
							Node search = new Node(name, x, y);
							n2 = this.nodes.stream().filter((n)->{return n.equals(search);}).toArray();
						} else {
							n2 = this.nodes.stream().filter((x)->{
								if(name2.equals(""))
									return x.getName() == null;
								else
									if(x.getName() == null)
										return false;
									else 
										return x.getName().matches(name2);
							}).toArray();
						}
						
						if(strict) {
							if(n1.length > 1)
								n1 = new Object[]{ n1[0] };
							if(n2.length > 1)
								n2 = new Object[]{ n2[0] };
						}
						
						// connect found objects
						for(Object so : n1) 
							for(Object eo : n2) {
								Node s = (Node)so, e = (Node)eo;
								String costS = st[1].trim();
								double cost;
								if(costS.equals("")) {
									double dx = e.getX() - s.getX();
									double dy = e.getY() - s.getY();
									cost = Math.sqrt(dx*dx + dy*dy);
								} else 
									cost = Double.parseDouble(costS);
								this.setConnection(s, e, cost);
								if(dir == 0)
									this.setConnection(e, s, cost);
							}
					} catch (NumberFormatException e) {
						throw new IllegalArgumentException("line " + Integer.toString(l) + ": couldn't parse");
					}				
				} else if(line.charAt(0) != '#') {
					throw new IllegalArgumentException("line " + Integer.toString(l) + ": unknown item");
				}
			}
			l++;
		}
	}

	// FIXME:
	/**
	 * The function writes the graph into the given Writer object.
	 * This will not output a correct network if there are one ore more nodes at the exact same location and the same name.
	 * @param out
	 * @throws IOException
	 */
	public void dumpNodesAndConnections(Writer out) throws IOException {
		PrintWriter outp = new PrintWriter(out);
		for(Node n : this.nodes)
			outp.println("n "+n);
		for(Node s : this.connections.keySet())
			for(Entry<Node, Double> e : this.connections.get(s).entrySet())
				if(s.distFrom(e.getKey()) == e.getValue())
					outp.println("c! "+s+"==>"+e.getKey());
				else
					outp.println("c! "+s+"="+e.getValue()+"=>"+e.getKey());
		outp.close();
	}
	
	public synchronized void setWaitTime(int waitTime) throws IllegalArgumentException {
		if(waitTime < -1)
			throw new IllegalArgumentException("wait time must be positive ore -1");
		if(waitTime != this.waitTime) {
			this.waitTime = waitTime;
			this.notify();
		}
	}
	
	public synchronized void setDistBias(double distBias) {
		this.distBias = distBias;
	}

	public Collection<Node> getNodes() {
		return new HashSet<>(this.nodes);
	}

	public synchronized void addNode(Node n) throws IllegalArgumentException {
		this.nodes.add(n);
		this.connections.put(n, new HashMap<>());
	}
	
	public synchronized void delNode(Node n) throws IllegalArgumentException {
		if(!this.nodes.contains(n))
			throw new IllegalArgumentException("node isn't inside the graph");
		this.connections.remove(n);
		for(Map<Node, Double> c : this.connections.values())
			c.remove(n);
		this.nodes.remove(n);
	}

	/**
	 * This function only finds one posible node with the given name.
	 * @param name
	 * @return a Node inside the graph with the given name
	 * @throws IllegalArgumentException
	 */
	public Node getNode(String name) throws IllegalArgumentException {
		if(name == null || name.equals(""))
			throw new IllegalArgumentException("can't search for unnamed nodes");
		Object[] n = this.nodes.stream().filter((x)->{
			if(x.getName() == null)
				return false;
			else
				return x.getName().equals(name);
		}).toArray();
		if(n.length == 0)
			throw new IllegalArgumentException("the node '" + name + "' doesn't exist");
		else 
			return (Node)n[0]; 
	}
	
	/**
	 * Return all the nodes with the given name.
	 * @param name
	 * @return array of all nodes inside the graph with the given name
	 * @throws IllegalArgumentException
	 */
	public Node[] getNodes(String name) throws IllegalArgumentException {
		if(name == null || name.equals(""))
			throw new IllegalArgumentException("can't search for unnamed nodes");
		Object[] n = this.nodes.stream().filter((x)->{
			if(x.getName() == null)
				return false;
			else
				return x.getName().equals(name);
		}).toArray();
		if(n.length == 0)
			throw new IllegalArgumentException("the node '" + name + "' doesn't exist");
		else 
			return (Node[])n; 
	}

	/**
	 * Returns the node closest to the point (x;y). If multiple are at the same location one of
	 * them is returned.
	 * @param x
	 * @param y
	 * @return the closest point
	 * @throws IllegalArgumentException
	 */
	public Node getNodeClosestTo(double x, double y) throws IllegalArgumentException {
		if(this.nodes.size() == 0)
			throw new IllegalArgumentException("there are no nodes");
		Node ret = null;
		double ret_dist = Double.POSITIVE_INFINITY;
		for(Node n : this.nodes)
			if(n.distFrom(x, y) < ret_dist) {
				ret = n;
				ret_dist = n.distFrom(x, y);
			}
		return ret;
	}
	
	public synchronized void setConnection(Node s, Node e, double c) throws IllegalArgumentException {
		if(c < 0)
			throw new IllegalArgumentException("cost must be positiv");
		if(!this.nodes.contains(e) || !this.nodes.contains(s))
			throw new IllegalArgumentException("the connection connects outside the graph");
		this.connections.get(s).put(e, c);
	}

	public synchronized void delConnection(Node s, Node e) throws IllegalArgumentException {
		if(!this.connections.containsKey(s))
			throw new IllegalArgumentException("start node isn't inside the graph");
		if(!this.connections.get(s).containsKey(e))
			throw new IllegalArgumentException("the nodes are not connected");
		this.connections.get(s).remove(e);
	}
	
	public double getConnection(Node s, Node e) throws IllegalArgumentException {
		if(!this.connections.containsKey(s))
			throw new IllegalArgumentException("start node isn't inside the graph");
		if(!this.connections.get(s).containsKey(e))
			throw new IllegalArgumentException("the nodes are not connected");
		return this.connections.get(s).get(e);
	}
	
	public synchronized void clear() {
		if(!this.inCalc) {
			this.delSavedPath();
			this.nodes.clear();
			this.connections.clear();
		}
	}
	
	/**
	 * @author roland
	 *
	 * Comparator used to sort the open list
	 */
	private class NodeComp implements Comparator<Node> {
		private Map<Node, Double> dist;
		private Node end;

		public NodeComp(Map<Node, Double> dist, Node end) {
			this.dist = dist;
			this.end = end;
		}

		@Override
		public int compare(Node o1, Node o2) {
			if(!this.dist.containsKey(o1)) {
				if(!this.dist.containsKey(o2))
					return 0;
				else
					return 1;
			} else if(!this.dist.containsKey(o2))
				return -1;
			else
				return Double.compare(this.dist.get(o1) + AStar.this.distBias*o1.distFrom(end), this.dist.get(o2) + AStar.this.distBias*o2.distFrom(end));
		}
	}

	/**
	 * Draws the graph to the given Graphics object
	 * @param status
	 */
	public synchronized void drawGraph(Graphics status) {
		if(status != null) {
			Graphics2D g2 = (Graphics2D)status;
			
			// Draw all nodes normal
			g2.setColor(new Color(200, 12, 12));
			for(Node n : this.nodes)
				g2.fillOval((int)(n.getX()*AStar.GRAPHICS_SCALE) - 5, (int)(n.getY()*AStar.GRAPHICS_SCALE) - 5, 10, 10);
				
			// Draw all connections
			for(Entry<Node, Map<Node, Double>> ent : this.connections.entrySet())
				for(Entry<Node, Double> con : ent.getValue().entrySet()) {
					g2.drawLine((int)(ent.getKey().getX()*AStar.GRAPHICS_SCALE), (int)(ent.getKey().getY()*AStar.GRAPHICS_SCALE), 
							(int)(con.getKey().getX()*AStar.GRAPHICS_SCALE), (int)(con.getKey().getY()*AStar.GRAPHICS_SCALE));
					
					double dirX = con.getKey().getX() - ent.getKey().getX();
					double dirY = con.getKey().getY() - ent.getKey().getY();
					double mag = Math.sqrt(dirX*dirX + dirY*dirY);
					dirX *= 5/mag;
					dirY *= 5/mag;		
					g2.fillOval((int)(con.getKey().getX()*AStar.GRAPHICS_SCALE-dirX)-3, (int)(con.getKey().getY()*AStar.GRAPHICS_SCALE-dirY)-3, 6, 6);
				}
			
			// Draw shortest path tree and the nodes of the closed set
			if(this.prev != null) {
				g2.setColor(new Color(12, 12, 200));
				g2.setStroke(new BasicStroke(3));
				for(Entry<Node, Node> con : this.prev.entrySet()) {
					g2.fillOval((int)(con.getKey().getX()*AStar.GRAPHICS_SCALE) - 5, (int)(con.getKey().getY()*AStar.GRAPHICS_SCALE) - 5, 10, 10);
					g2.drawLine((int)(con.getKey().getX()*AStar.GRAPHICS_SCALE), (int)(con.getKey().getY()*AStar.GRAPHICS_SCALE), 
							(int)(con.getValue().getX()*AStar.GRAPHICS_SCALE), (int)(con.getValue().getY()*AStar.GRAPHICS_SCALE));
				}
			}
			
			// Draw nodes of the open set
			if(this.openl != null) {
				g2.setColor(new Color(200, 200, 200));
				for(Node n : this.openl)			
					g2.fillOval((int)(n.getX()*AStar.GRAPHICS_SCALE) - 5, (int)(n.getY()*AStar.GRAPHICS_SCALE) - 5, 10, 10);
				g2.setColor(new Color(200, 200, 0));
				if(this.openl.size() > 0) {
					g2.fillOval((int)(this.openl.peek().getX()*AStar.GRAPHICS_SCALE) - 5, (int)(this.openl.peek().getY()*AStar.GRAPHICS_SCALE) - 5, 10, 10);
				}
			}
			
			// Draws the fastest path
			if(this.ret != null) {
				g2.setColor(new Color(12, 200, 12));
				g2.setStroke(new BasicStroke(4));
				for(int n = 0; n < this.ret.size()-1; n++)
					g2.drawLine((int)(this.ret.get(n).getX()*AStar.GRAPHICS_SCALE), (int)(this.ret.get(n).getY()*AStar.GRAPHICS_SCALE), 
							(int)(this.ret.get(n+1).getX()*AStar.GRAPHICS_SCALE), (int)(this.ret.get(n+1).getY()*AStar.GRAPHICS_SCALE));
			}
			// Draw all Nodes highlighted
			g2.setColor(new Color(0, 255, 0));
			for(Node n : this.nodes)
				if(n.getHighlight())
					g2.fillOval((int)(n.getX()*AStar.GRAPHICS_SCALE) - 5, (int)(n.getY()*AStar.GRAPHICS_SCALE) - 5, 10, 10);		
				
			// Draw all names
			g2.setColor(new Color(150,150,150));
			for(Node n : this.nodes)
				if(n.getName() != null)
					g2.drawString(n.getName(), (int)(n.getX()*AStar.GRAPHICS_SCALE), (int)(n.getY()*AStar.GRAPHICS_SCALE));
		}
	}
	
	public synchronized void delSavedPath() {
		if(!inCalc) {
			this.ret = null;
			this.prev = null;
			this.dist = null;
			this.openl = null;
		}
	}
	
	/**
	 * Computes and returns the path from node s to node e.
	 * @param s
	 * @param e
	 * @return the computed path
	 * @throws IllegalArgumentException
	 */
	public synchronized List<Node> getPathFromTo(Node s, Node e) throws IllegalArgumentException {
		if(s == null || e == null || !this.nodes.contains(e) || !this.nodes.contains(s))
			throw new IllegalArgumentException("node is outside the graph");
		
		inCalc = true;
		this.ret = null;
		this.prev = new HashMap<>();
		this.dist = new HashMap<>();
		this.openl = new PriorityQueue<>(new NodeComp(dist, e));
		dist.put(s, 0.0);
		openl.add(s);
		
		while(openl.peek() != null && !openl.peek().equals(e)) {
			Node c = openl.poll();
			if(this.connections.containsKey(c)) {
				for(Entry<Node, Double> con : this.connections.get(c).entrySet()) {
					if(dist.containsKey(con.getKey())) {
						if(dist.get(con.getKey()) > dist.get(c) + con.getValue()) {
							openl.remove(con.getKey());
							dist.put(con.getKey(), dist.get(c) + con.getValue());
							openl.add(con.getKey());
							prev.put(con.getKey(), c);
						}
					} else {
						dist.put(con.getKey(), dist.get(c) + con.getValue());
						openl.add(con.getKey());
						prev.put(con.getKey(), c);
					}
				}
			}
			if(this.waitTime > 0) {
				try {
					this.wait(this.waitTime);
				} catch (InterruptedException e1) { ; }	
			} else if(this.waitTime == -1) {
				try {
					this.wait();
				} catch (InterruptedException e1) { ; }
			}
		}
		if(!prev.containsKey(e))
			throw new IllegalArgumentException("no path exists");		
		
		ret = new LinkedList<>();
		Node c = e;
	
		while(c != s) {
			ret.add(0, c);
			c = prev.get(c);
		}
		ret.add(0, s);
		
		inCalc = false;
	
		return ret;
	}
	
	/**
	 * Returns the cost of the given path.
	 * @param path
	 * @return the computed cost
	 * @throws IllegalArgumentException
	 */
	public double getPathCost(List<Node> path) throws IllegalArgumentException {
		double ret = -1;
		if(path != null && path.size() > 0) {
			ret = 0;
			Node n = path.get(0);
			if(!this.nodes.contains(n))
				throw new IllegalArgumentException("not all nodes are contained");
			for(int i = 1; i < path.size(); i++) {
				Node f = path.get(i);
				if(!this.nodes.contains(f))
					throw new IllegalArgumentException("not all nodes are contained");
				if(!this.connections.get(n).containsKey(f))
					throw new IllegalArgumentException("imposible path");
				ret += this.connections.get(n).get(f);
				n = f;
			}
		}
		return ret;
	}
}
