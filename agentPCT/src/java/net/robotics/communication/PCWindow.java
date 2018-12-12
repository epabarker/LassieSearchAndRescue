package net.robotics.communication;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JWindow;

import jason.environment.grid.Location;
import net.robotics.communication.Tile.TileType;
import net.robotics.components.RobotInfoPane;
import net.robotics.components.VictimInfoPane;
import net.robotics.components.VictimInfoPane.Status;
import net.robotics.console.TextAreaOutputStream;

public class PCWindow extends JFrame {

	private MapCanvas map;
	private RobotInfoPane robotInfoPanel;
	private JPanel victimPanel;

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


		victimPanel = new JPanel();
		victimPanel.setLayout(new BoxLayout(victimPanel, BoxLayout.Y_AXIS));
		victimPanel.setPreferredSize(new Dimension(300, 500));
		pane.add(victimPanel, BorderLayout.PAGE_START);

		robotInfoPanel = new RobotInfoPane();
		robotInfoPanel.setPreferredSize(new Dimension(300, 100));
		pane.add(robotInfoPanel, BorderLayout.CENTER);

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
		map = new MapCanvas(new KnownMap(6,6));
		map.setPreferredSize(new Dimension(600, 600));
		pane.add(map, BorderLayout.CENTER);

		victimList = new HashMap<>();

		pack();
		setVisible(true);


	}

	public MapCanvas getMap() {
		return map;
	}

	private HashMap<String, VictimInfoPane> victimList;

	public void addVictims(Location victim){
		System.out.print("addingVictim");
		
		VictimInfoPane vpane = new VictimInfoPane(victim.x, victim.y, victimPanel.getWidth(), Status.undiscovered);
		victimPanel.add(vpane);
		victimList.put(victim.x + "," + victim.y, vpane);
		
		victimPanel.repaint();
		victimPanel.revalidate();
		
	}
	
	public VictimInfoPane getVictimPane(int x, int y){
		return victimList.get(x + "," + y);
	}

	public static void main(String[] args){
		PCWindow win = new PCWindow("Robotics Assignment 2");
		win.getMap().updateMap(TileType.OBSTACLE, 2, 2);
		win.getMap().updateMap(TileType.Hospital, 1, 1);
		win.getMap().updateMap(TileType.Victim, 1, 2);

		System.out.println("TEST");

		//map.UpdateMap(new KnownMap(6,6));
	}

	public RobotInfoPane getRobotInfoPanel() {
		return robotInfoPanel;
	}
}
