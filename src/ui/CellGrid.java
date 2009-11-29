package ui;

//package cellgrid;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.LineBorder;

public class CellGrid extends JFrame {
	private static final long serialVersionUID = 1L;
	private final int WINDOW_WIDTH = 400; // size of the JFrame
	private final int WINDOW_HEIGHT = 400;
	ArrayList<ArrayList<myJCanvas>> myList;
	
	// Takes the number of x and y cells (width and height) and makes the grid
	public CellGrid(int rows, int cols) {
		
		// int xCellSize = WINDOW_WIDTH/(xCells+1); //gridlayout auto-resizes
		// int yCellSize = WINDOW_HEIGHT/(yCells+1);
		// int xCellSize = 65;
		// int yCellSize = 65;
		
		JPanel panel1 = new JPanel(); // just a holder
		GridLayout layout1 = new GridLayout(rows, cols, 5, 5); // sets the width
		// and height
		panel1.setLayout(layout1); // adds the layout we just made
		panel1.setBackground(Color.black); // sets the background black
		setTitle("CABS"); // sets the title
		setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
		LineBorder bord1 = new LineBorder(Color.black, 5); // border because it
		// looked funny
		// before
		panel1.setBorder(bord1); // adds the border
		JScrollPane scroller = new JScrollPane(panel1); // makes a scroll pane
		// from the panel
		add(scroller); // adds the scroll pane to CellGrid
		
		myList = // array list of array lists
		new ArrayList<ArrayList<myJCanvas>>();
		
		// for(int i=0;i<rows;i++) //add the second set of array lists to the
		// first
		// myList.add(new ArrayList<myJCanvas>());
		for (int x = 0; x < rows; x++) {
			myList.add(new ArrayList<myJCanvas>());
			for (int y = 0; y < cols; y++) { // steps through the lists and
				// makes canvases
				myList.get(x).add(new myJCanvas());
				panel1.add(myList.get(x).get(y)); // adds the canvases to the
				// panel
			}
		}
		
	}
	
	public void setColor(int xPos, int yPos, Color color) {
		myList.get(yPos).get(xPos).setBackground(color);
	}
	
	// could extend anything that's Swing (not awt)
	// All this does at the moment is make a "cell" with a changeable color.
	// This could potentially be something that held text like 4r,3g = 4
	// rabbits, 3 grass
	public class myJCanvas extends JPanel {
		private static final long serialVersionUID = 1L;
		
		public myJCanvas(int tempWidth, int tempHeight) {
			this.setSize(tempWidth, tempHeight); // sizes that do nothing
			// because of GridLayout
			this.setBackground(Color.blue); // default color
			this.setMaximumSize(new Dimension(25, 25)); // doesn't matter
			// because of GridLayout
			this.setMinimumSize(new Dimension(60, 60));
			
			this.setPreferredSize(new Dimension(60, 60));
		}
		
		public myJCanvas() {
			this.setSize(60, 60);
			this.setBackground(Color.blue);
		}
	}
	
	// Just for demonstration/debug
	public static void main(String[] args) throws InterruptedException {
		int rows = 20;
		int cols = 10;
		CellGrid gpw = new CellGrid(rows, cols); // make my CellGrid
		
		gpw.setSize(500, 500);
		gpw.setVisible(true); // make the JFrame visible
		gpw.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		gpw.setColor(3, 0, Color.RED);
		Thread.sleep(2000);
		gpw.setColor(1, 1, Color.BLACK);
		gpw.setColor(2, 2, Color.BLACK);
		
	}
}
