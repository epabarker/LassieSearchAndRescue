package net.robotics.components;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.robotics.communication.PCComms;

public class RobotInfoPane extends JPanel implements ActionListener {
	
	private JTextField ipAddress;
	private JButton confirmButton;
    private static final String ADDRESS = "192.168.70.64";
    
    private PCComms pcComms;
    
    private RobotInfo robotInfo;
	
	public RobotInfoPane(){
		super();
		
		setRobotInfo(new RobotInfo());
		
		ipAddress = new JTextField(ADDRESS); 
		confirmButton = new JButton("Connect");
		
		confirmButton.setActionCommand("Connect");
		confirmButton.addActionListener(this);
		
		add(ipAddress);
		add(confirmButton);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		if("Connect".equalsIgnoreCase(e.getActionCommand())){
			System.out.println(ipAddress.getText() + " connection attempt.");
			
			try {
				pcComms = new PCComms(ADDRESS, 0);
			} catch (IOException e1) {
				System.out.println("Connection Refused");
				return;
			}
			
			System.out.println("Connection Established");
			
			pcComms.start();
			
			pcComms.sendCommand("MOVE 0");
		}
	}
	
	public RobotInfo getRobotInfo() {
		return robotInfo;
	}

	private void setRobotInfo(RobotInfo robotInfo) {
		this.robotInfo = robotInfo;
	}

	public class RobotInfo {
		private int x, y;
		
		public RobotInfo(){
			this.setPos(x, y);
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
	}
}


