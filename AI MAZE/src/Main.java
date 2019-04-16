import java.awt.Graphics;
import java.awt.Image;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.JComboBox;

public class Main {
	
	//FRAME
	JFrame frame;
	//GENERAL VARIABLES
	private int cells = 20;
	private int delay = 35;
	private double dense = .50;
	private double density = (cells*cells)*.50;
	private int startx = -1;
	private int starty = -1;
	private int finishx = -1;
	private int finishy = -1;
	private int tool = 0;
	private int checks = 0;
	private int length = 0;
	private int WIDTH = 655;
	private final int HEIGHT = 890;
	private final int MSIZE = 600;
	private int CSIZE = MSIZE/cells;
	private String[] tools = {"Start Point","Finish Point","Add Wall", "Remove Wall"};
	private boolean solving = false;
	Node[][] map;
	Algorithm Alg = new Algorithm();
	Random r = new Random();
	JSlider size = new JSlider(1,5,2);
	JLabel toolL = new JLabel("Options:");
	JLabel sizeL = new JLabel("Size:");
	JLabel cellsL = new JLabel(cells+"x"+cells);
	JLabel checkL = new JLabel("Explorations: "+checks);
	JLabel lengthL = new JLabel("Path Length: "+length);
	JButton searchB = new JButton("Start Search");
	JButton genMapB = new JButton("Generate Maze");
	JButton resetB = new JButton("Reset");
	JButton clearMapB = new JButton("Clear");
	JComboBox toolBx = new JComboBox(tools);
	JPanel toolP = new JPanel();
	Map canvas;
	Border loweredetched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);

	public static void main(String[] args) {
		new Main();
	}

	public Main() {	//CONSTRUCTOR
		clearMap();
		initialize();
	}
	
	public void generateMap() {	//GENERATE MAP
		clearMap();	//CREATE CLEAR MAP TO START
		for(int i = 0; i < density; i++) {
			Node current;
			do {
				int x = r.nextInt(cells);
				int y = r.nextInt(cells);
				current = map[x][y];	//FIND A RANDOM NODE IN THE GRID
			} while(current.getType()==2);	//IF IT IS ALREADY A WALL, FIND A NEW ONE
			current.setType(2);	//SET NODE TO BE A WALL
		}
	}
	
	public void clearMap() {	//CLEAR MAP
		finishx = -1;	//RESET THE START AND FINISH
		finishy = -1;
		startx = -1;
		starty = -1;
		map = new Node[cells][cells];	//CREATE NEW MAP OF NODES
		for(int x = 0; x < cells; x++) {
			for(int y = 0; y < cells; y++) {
				map[x][y] = new Node(3,x,y);	//SET ALL NODES TO EMPTY
			}
		}
		reset();	//RESET SOME VARIABLES
	}
	
	public void resetMap() {	//RESET MAP
		for(int x = 0; x < cells; x++) {
			for(int y = 0; y < cells; y++) {
				Node current = map[x][y];
				if(current.getType() == 4 || current.getType() == 5)	//CHECK TO SEE IF CURRENT NODE IS EITHER CHECKED OR FINAL PATH
					map[x][y] = new Node(3,x,y);	//RESET IT TO AN EMPTY NODE
			}
		}
		if(startx > -1 && starty > -1) {	//RESET THE START AND FINISH
			map[startx][starty] = new Node(0,startx,starty);
			map[startx][starty].setHops(0);
		}
		if(finishx > -1 && finishy > -1)
			map[finishx][finishy] = new Node(1,finishx,finishy);
		reset();	//RESET SOME VARIABLES
	}

	private void initialize() {	//INITIALIZE THE GUI ELEMENTS
		frame = new JFrame();
		frame.setVisible(true);
		frame.setResizable(false);
		frame.setSize(WIDTH,HEIGHT);
		frame.setTitle("Maze");
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		toolP.setBorder(BorderFactory.createTitledBorder(loweredetched));
		
		toolP.setLayout(null);
		toolP.setBounds(10,650,625,190);
		
		searchB.setBounds(25,40, 120, 25);
		toolP.add(searchB);
		
		
		genMapB.setBounds(175,40,120,25);
		toolP.add(resetB);
		
		
		resetB.setBounds(325,40, 120, 25);
		toolP.add(genMapB);
		
		
		clearMapB.setBounds(475,40, 120, 25);
		toolP.add(clearMapB);
		
		
		toolL.setBounds(25,95,120,25);
		toolP.add(toolL);
		
		
		toolBx.setBounds(25,120,120,25);
		toolP.add(toolBx);
		
		
		sizeL.setBounds(175,95,120,25);
		toolP.add(sizeL);
		
		size.setMajorTickSpacing(10);
		size.setBounds(175,120,120,25);
		toolP.add(size);
		
		cellsL.setBounds(300,120,40,25);
		toolP.add(cellsL);
		
		
		checkL.setBounds(350,120,100,25);
		toolP.add(checkL);
		
		
		lengthL.setBounds(475,120,100,25);
		toolP.add(lengthL);
		
		
		frame.getContentPane().add(toolP);
		
		canvas = new Map();
		canvas.setBounds(25, 20, MSIZE+1, MSIZE+1);
		frame.getContentPane().add(canvas);
		
		searchB.addActionListener(new ActionListener() {		//ACTION LISTENERS
			@Override
			public void actionPerformed(ActionEvent e) {
				reset();
				if((startx > -1 && starty > -1) && (finishx > -1 && finishy > -1))
					solving = true;
			}
		});
		resetB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				resetMap();
				Update();
			}
		});
		genMapB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				generateMap();
				Update();
			}
		});
		clearMapB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				clearMap();
				Update();
			}
		});
		toolBx.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				tool = toolBx.getSelectedIndex();
			}
		});
		size.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				cells = size.getValue()*10;
				clearMap();
				reset();
				Update();
			}
		});
		
		startSearch();	//START STATE
	}
	
	public void startSearch() {	//START STATE
		if(solving) {
			Alg.AStar();
		}
		pause();	//PAUSE STATE
	}
	
	public void pause() {	//PAUSE STATE
		int i = 0;
		while(!solving) {
			i++;
			if(i > 500)
				i = 0;
			try {
				Thread.sleep(1);
			} catch(Exception e) {}
		}
		startSearch();	//START STATE
	}
	
	public void Update() {	//UPDATE ELEMENTS OF THE GUI
		density = (cells*cells)*dense;
		CSIZE = MSIZE/cells;
		canvas.repaint();
		cellsL.setText(cells+"x"+cells);
		lengthL.setText("Path Length: "+length);
		checkL.setText("Explorations: "+checks);
	}
	
	public void reset() {	//RESET METHOD
		solving = false;
		length = 0;
		checks = 0;
	}
	
	public void delay() {	//DELAY METHOD
		try {
			Thread.sleep(delay);
		} catch(Exception e) {}
	}
	
	class Map extends JPanel implements MouseListener, MouseMotionListener{	//MAP CLASS
		
		public Map() {
			addMouseListener(this);
			addMouseMotionListener(this);
		}
		
		public void paintComponent(Graphics g) {	//REPAINT
			super.paintComponent(g);
			Image img = new ImageIcon("res/mario.png").getImage();
			Image img1 = new ImageIcon("res/flag2.png").getImage();
			Image img2 = new ImageIcon("res/brick.png").getImage();
			Image img3 = new ImageIcon("res/grass (1).png").getImage();
			Image img4 = new ImageIcon("res/white.png").getImage();
			Image img5 = new ImageIcon("res/blue.png").getImage();
		    
		    
			for(int x = 0; x < cells; x++) {	//PAINT EACH NODE IN THE GRID
				for(int y = 0; y < cells; y++) {
					switch(map[x][y].getType()) {
						case 0:
							g.drawImage(img, x*CSIZE,y*CSIZE,CSIZE,CSIZE,this);
							break;
						case 1:
							g.drawImage(img1, x*CSIZE,y*CSIZE,CSIZE,CSIZE,this);
							break;
						case 2:
							g.drawImage(img2, x*CSIZE,y*CSIZE,CSIZE,CSIZE,this);
							break;
						case 3:
							g.drawImage(img3, x*CSIZE,y*CSIZE,CSIZE,CSIZE,this);
							break;
						case 4:
							g.drawImage(img4, x*CSIZE,y*CSIZE,CSIZE,CSIZE,this);
							break;
						case 5:
							g.drawImage(img5, x*CSIZE,y*CSIZE,CSIZE,CSIZE,this);
							break;
					}
					
					g.setColor(Color.BLACK);
					g.drawRect(x*CSIZE,y*CSIZE,CSIZE,CSIZE);
					
				}
			}
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			try {
				int x = e.getX()/CSIZE;	
				int y = e.getY()/CSIZE;
				Node current = map[x][y];
				if((tool == 2 || tool == 3) && (current.getType() != 0 && current.getType() != 1))
					current.setType(tool);
				Update();
			} catch(Exception z) {}
		}

		@Override
		public void mouseMoved(MouseEvent e) {}

		@Override
		public void mouseClicked(MouseEvent e) {}

		@Override
		public void mouseEntered(MouseEvent e) {}

		@Override
		public void mouseExited(MouseEvent e) {}

		@Override
		public void mousePressed(MouseEvent e) {
			resetMap();	//RESET THE MAP WHENEVER CLICKED
			try {
				int x = e.getX()/CSIZE;	//GET THE X AND Y OF THE MOUSE CLICK IN RELATION TO THE SIZE OF THE GRID
				int y = e.getY()/CSIZE;
				Node current = map[x][y];
				switch(tool ) {
					case 0: {	//START NODE
						if(current.getType()!=2) {	//IF NOT WALL
							if(startx > -1 && starty > -1) {	//IF START EXISTS SET IT TO EMPTY
								map[startx][starty].setType(3);
								map[startx][starty].setHops(-1);
							}
							current.setHops(0);
							startx = x;	//SET THE START X AND Y
							starty = y;
							current.setType(0);	//SET THE NODE CLICKED TO BE START
						}
						break;
					}
					case 1: {//FINISH NODE
						if(current.getType()!=2) {	//IF NOT WALL
							if(finishx > -1 && finishy > -1)	//IF FINISH EXISTS SET IT TO EMPTY
								map[finishx][finishy].setType(3);
							finishx = x;	//SET THE FINISH X AND Y
							finishy = y;
							current.setType(1);	//SET THE NODE CLICKED TO BE FINISH
						}
						break;
					}
					default:
						if(current.getType() != 0 && current.getType() != 1)
							current.setType(tool);
						break;
				}
				Update();
			} catch(Exception z) {}	//EXCEPTION HANDLER
		}

		@Override
		public void mouseReleased(MouseEvent e) {}
	}
	
	class Algorithm {
		public void AStar() {
			ArrayList<Node> priority = new ArrayList<Node>();
			priority.add(map[startx][starty]);
			while(solving) {
				if(priority.size() <= 0) {
					solving = false;
					break;
				}
				int hops = priority.get(0).getHops()+1;
				ArrayList<Node> explored = exploreNeighbors(priority.get(0),hops);
				if(explored.size() > 0) {
					priority.remove(0);
					priority.addAll(explored);
					Update();
					delay();
				} else {
					priority.remove(0);
				}
				sortQue(priority);
			}
		}
		
		public ArrayList<Node> sortQue(ArrayList<Node> sort) {
			int c = 0;
			while(c < sort.size()) {
				int sm = c;
				for(int i = c+1; i < sort.size(); i++) {
					if(sort.get(i).getEuclidDist()+sort.get(i).getHops() < sort.get(sm).getEuclidDist()+sort.get(sm).getHops())
						sm = i;
				}
				if(c != sm) {
					Node temp = sort.get(c);
					sort.set(c, sort.get(sm));
					sort.set(sm, temp);
				}	
				c++;
			}
			return sort;
		}
		
		public ArrayList<Node> exploreNeighbors(Node current, int hops) {	//EXPLORE NEIGHBORS
			ArrayList<Node> explored = new ArrayList<Node>();	//LIST OF NODES THAT HAVE BEEN EXPLORED
			for(int a = -1; a <= 1; a++) {
				for(int b = -1; b <= 1; b++) {
					int xbound = current.getX()+a;
					int ybound = current.getY()+b;
					if((xbound > -1 && xbound < cells) && (ybound > -1 && ybound < cells)) {	//MAKES SURE THE NODE IS NOT OUTSIDE THE GRID
						Node neighbor = map[xbound][ybound];
						if((neighbor.getHops()==-1 || neighbor.getHops() > hops) && neighbor.getType()!=2) {	//CHECKS IF THE NODE IS NOT A WALL AND THAT IT HAS NOT BEEN EXPLORED
							explore(neighbor, current.getX(), current.getY(), hops);	//EXPLORE THE NODE
							explored.add(neighbor);	//ADD THE NODE TO THE LIST
						}
					}
				}
				
			}
			return explored;
			
		}
		
		public void explore(Node current, int lastx, int lasty, int hops) {	//EXPLORE A NODE
			if(current.getType()!=0 && current.getType() != 1)	//CHECK THAT THE NODE IS NOT THE START OR FINISH
				current.setType(4);	//SET IT TO EXPLORED
			current.setLastNode(lastx, lasty);	//KEEP TRACK OF THE NODE THAT THIS NODE IS EXPLORED FROM
			current.setHops(hops);	//SET THE HOPS FROM THE START
			checks++;
			if(current.getType() == 1) {	//IF THE NODE IS THE FINISH THEN BACKTRACK TO GET THE PATH
				backtrack(current.getLastX(), current.getLastY(),hops);	
			}
			
		}
		
		public void backtrack(int lx, int ly, int hops) {	//BACKTRACK
			length = hops;
			while(hops > 1) {	//BACKTRACK FROM THE END OF THE PATH TO THE START
				Node current = map[lx][ly];
				current.setType(5);
				lx = current.getLastX();
				ly = current.getLastY();
				hops--;
				
			}
			solving = false;
		}
	}
	
	class Node {
		
		// 0 = start, 1 = finish, 2 = wall, 3 = empty, 4 = checked, 5 = finalpath
		private int cellType = 0;
		private int hops;
		private int x;
		private int y;
		private int lastX;
		private int lastY;
		private double dToEnd = 0;
	
		public Node(int type, int x, int y) {	//CONSTRUCTOR
			cellType = type;
			this.x = x;
			this.y = y;
			hops = -1;
		}
		
		public double getEuclidDist() {		//CALCULATES THE EUCLIDIAN DISTANCE TO THE FINISH NODE
			int xdif = Math.abs(x-finishx);
			int ydif = Math.abs(y-finishy);
			dToEnd = Math.sqrt((xdif*xdif)+(ydif*ydif));
			return dToEnd;
		}
		
		public int getX() {return x;}		//GET METHODS
		public int getY() {return y;}
		public int getLastX() {return lastX;}
		public int getLastY() {return lastY;}
		public int getType() {return cellType;}
		public int getHops() {return hops;}
		
		public void setType(int type) {cellType = type;}		//SET METHODS
		public void setLastNode(int x, int y) {lastX = x; lastY = y;}
		public void setHops(int hops) {this.hops = hops;}
	}
}
