package net.robotics.components;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class VictimInfoPane extends JPanel {

	JLabel Image;
	JLabel PositionalData;

	private static BufferedImage undiscovered, novictim, critical, criticalrescued, noncritical, noncriticalrescued;
	
	public enum Status{
		undiscovered, novictim, critical, criticalrescued, noncritical, noncriticalrescued;
	}
	
	private int x, y;
	
	private Status status;

	public VictimInfoPane(int x, int y, int w, Status s){
		super();
		
		setSize(w, 50);
		
		try {
			if(undiscovered == null)
				undiscovered = ImageIO.read(new File("res/undiscovered.png"));
			if(novictim == null)
				novictim = ImageIO.read(new File("res/novictim.png"));
			if(critical == null)
				critical = ImageIO.read(new File("res/critical.png"));
			if(criticalrescued == null)
				criticalrescued = ImageIO.read(new File("res/critical-rescued.png"));
			if(noncritical == null)
				noncritical = ImageIO.read(new File("res/noncritical.png"));
			if(noncriticalrescued == null)
				noncriticalrescued = ImageIO.read(new File("res/noncritical-rescued.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		this.status = s;
		this.x = x;
		this.y = y;
		
		this.Image = new JLabel(new ImageIcon(getStatusImage(status).getScaledInstance(50, 50, BufferedImage.SCALE_SMOOTH)));
		this.Image.setSize(50, 50);
		
		add(this.Image);
		
		this.PositionalData = new JLabel(x + ", " + y + ": " + status.toString());
		add(this.PositionalData);
		
		this.setBorder(BorderFactory.createLineBorder(Color.black));
	}
	
	private static BufferedImage getStatusImage(Status status){
		if(status == Status.undiscovered)
			return undiscovered;
		if(status == Status.novictim)
			return novictim;
		if(status == Status.critical)
			return critical;
		if(status == Status.criticalrescued)
			return criticalrescued;
		if(status == Status.noncritical)
			return noncritical;
		if(status == Status.noncriticalrescued)
			return noncriticalrescued;
		return undiscovered;
	}
	
	

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
		ImageIcon i = new ImageIcon(getStatusImage(status).getScaledInstance(50, 50, BufferedImage.SCALE_SMOOTH));
		this.Image.setIcon(i);
		this.Image.setSize(50, 50);
		this.PositionalData.setText(x + ", " + y + ":" + status.toString());
		
		this.repaint();
	}
}
