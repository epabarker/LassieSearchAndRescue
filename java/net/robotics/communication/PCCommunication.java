package net.robotics.communication;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JWindow;

import net.robotics.communication.Tile.TileType;

public class PCCommunication extends JFrame {
	
	private static MapCanvas map;
	
	public PCCommunication(String name){
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
		
		button = new JButton("Robot Info");
		button.setPreferredSize(new Dimension(300, 100));
		pane.add(button, BorderLayout.CENTER);
		
		pane = new JPanel(new BorderLayout());
		add(pane, BorderLayout.CENTER);
		
		button = new JButton("Agentspeak display");
		button.setPreferredSize(new Dimension(600, 300));
		pane.add(button, BorderLayout.PAGE_START);

		//Make the center component big, since that's the
		//typical usage of BorderLayout.
		map = new MapCanvas();
		map.setPreferredSize(new Dimension(600, 600));
		pane.add(map, BorderLayout.CENTER);
		
		pack();
		setVisible(true);
		
		
	}
	
	public static void main(String[] args){
		new PCCommunication("Robotics Assignment 2");
		map.UpdateMap(new KnownMap(6,6, new Tile[]{
				new Tile(1, 1, TileType.Hospital),
				new Tile(1, 2, TileType.Victim),
				new Tile(2, 2),
		}));
		
		//map.UpdateMap(new KnownMap(6,6));
	}
}
