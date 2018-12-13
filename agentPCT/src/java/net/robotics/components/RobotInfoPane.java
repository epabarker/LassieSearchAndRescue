package net.robotics.components;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.robotics.communication.PCComms;

public class RobotInfoPane extends JPanel implements ActionListener {
	
	private JTextField ipAddress;
	private JLabel robotInfoDisplay;
	private JButton confirmButton;
    private static final String ADDRESS = "192.168.70.64";
    
    private PCComms pcComms;
    
    private RobotInfo robotInfo;
	
	private boolean Connected = false;
	
	public RobotInfoPane(){
		super();
		
		setRobotInfo(new RobotInfo());
		
		ipAddress = new JTextField(ADDRESS); 
		confirmButton = new JButton("Connect");
		
		confirmButton.setActionCommand("Connect");
		confirmButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				(new Thread() {
				    @Override
				    public void run()
				    {
				    	System.out.println(ipAddress.getText() + " connection attempt.");
						
						if(!connect)
							return;
						
						try {
							pcComms = new PCComms(ipAddress.getText(), 0);
							pcComms.sendCommand("YO!");
						} catch (IOException e1) {
							System.out.println("Connection Refused");
							return;
						} 
						
						setConnected(true);
						
						System.out.println("Connection Established");
						
						pcComms.start();
						
						removeAll();
						
						add(robotInfoDisplay);
						
						revalidate();
						repaint();
				    }
				}).start();
				
			}
		});
		
		add(ipAddress);
		add(confirmButton);
		
		robotInfoDisplay = new JLabel("NOTHING");
		
	}
	
	private boolean connect = true;

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
		if("Connect".equalsIgnoreCase(e.getActionCommand())){
			
			
			//pcComms.start();
			
			/*removeAll();
			
			add(robotInfoDisplay);
			
			revalidate();
			repaint();*/
		}
	}
	
	public PCComms getPcComms() {
		return pcComms;
	}

	public boolean isConnected() {
		return Connected;
	}

	public void setConnected(boolean connected) {
		Connected = connected;
	}
	
	public RobotInfo getRobotInfo() {
		return robotInfo;
	}

	private void setRobotInfo(RobotInfo robotInfo) {
		this.robotInfo = robotInfo;
	}
	
	public JLabel getRobotInfoDisplay(){
		return robotInfoDisplay;
	}

	public class RobotInfo {
		private int x, y, h;
		private boolean locationFound;
		
		public RobotInfo(){
			this.setLocationFound(false);
		}
		
		public void setPos(int x, int y){
			this.setX(x);
			this.setY(y);
		}

		public int getX() {
			return x;
		}

		public void setX(int x) {
			this.x = x;
		}

		public int getY() {
			return y;
		}

		public void setY(int y) {
			this.y = y;
		}

		public boolean isLocationFound() {
			return locationFound;
		}

		public void setLocationFound(boolean locationFound) {
			this.locationFound = locationFound;
		}

		public int getHeading() {
			return h;
		}

		public void setHeading(int h) {
			this.h = h;
		}
	}
}


