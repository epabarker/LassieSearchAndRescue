package net.robotics.communication;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.PrintStream;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JWindow;

import net.robotics.communication.Tile.TileType;
import net.robotics.components.RobotInfoPane;
import net.robotics.console.TextAreaOutputStream;

public class PCWindow extends JFrame {
	
	private static MapCanvas map;
	
	public PCWindow(String name){
		super(name);
		
		setSize(300, 400);
		addWindowListener(new WindowAdapter() {	
	         public void windowClosing(WindowEvent windowEvent){
	            System.exit(0);
	         }       
	    });
		

		//Make the center component big, since that's the
		//typical usage of BorderLayout.
		
		JPanel pane = new JPanel(new BorderLayout());
		pane.setPreferredSize(new Dimension(300, 600));
		add(pane, BorderLayout.LINE_END);
		

		JButton button = new JButton("Victim Info");
		button.setPreferredSize(new Dimension(300, 500));
		pane.add(button, BorderLayout.PAGE_START);
		
		RobotInfoPane robotInfo = new RobotInfoPane();
		robotInfo.setPreferredSize(new Dimension(300, 100));
		pane.add(robotInfo, BorderLayout.CENTER);
		
		pane = new JPanel(new BorderLayout());
		add(pane, BorderLayout.CENTER);
		
		
		JTextArea txtArea = new JTextArea();
		PrintStream con=new PrintStream(new TextAreaOutputStream(txtArea));
		System.setOut(con);
		System.setErr(con);
		
		JScrollPane scroll = new JScrollPane (txtArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		txtArea.setEditable(false);
		
		scroll.setPreferredSize(new Dimension(600, 300));
		pane.add(scroll, BorderLayout.PAGE_START);

		//Make the center component big, since that's the
		//typical usage of BorderLayout.
		map = new MapCanvas();
		map.setPreferredSize(new Dimension(600, 600));
		pane.add(map, BorderLayout.CENTER);
		
		pack();
		setVisible(true);
		
		
	}
	
	public static void main(String[] args){
		new PCWindow("Robotics Assignment 2");
		map.UpdateMap(new KnownMap(6,6, new Tile[]{
				new Tile(1, 1, TileType.Hospital),
				new Tile(1, 2, TileType.Victim),
				new Tile(2, 2),
		}));
		
		System.out.println("TEST");
		
		//map.UpdateMap(new KnownMap(6,6));
	}
}
