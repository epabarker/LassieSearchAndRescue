package net.robotics.screen;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.Format;

import javax.imageio.ImageIO;

import lejos.hardware.lcd.Font;
import lejos.hardware.lcd.GraphicsLCD;
import lejos.hardware.lcd.Image;
import net.robotics.main.Robot;

//Screen Width 178, Height 128; 

public class LCDRenderer{
	private GraphicsLCD lcd;

	private Font previousFont;

	private byte[] storedScreen;

	private BufferedImage up, down, left, right;

	private Mode mode;

	enum Mode {
		VisitInfo, OccupancyBelief, Nothing
	}

	public LCDRenderer(GraphicsLCD lcd){
		this.lcd = lcd;
		lcd.setContrast(0x60);

		mode = Mode.Nothing;

		try {
			up = ImageIO.read(new File("up.png"));
			down = ImageIO.read(new File("down.png"));
			right = ImageIO.read(new File("right.png"));
			left = ImageIO.read(new File("left.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}


	}

	public void cycleMode(){
		switch (mode) {
		case VisitInfo:
			mode = Mode.OccupancyBelief;
			break;
		case OccupancyBelief:
			mode = Mode.Nothing;
			break;
		case Nothing:
		default:
			mode = Mode.VisitInfo;
			break;
		}
	}

	public void switchScreenTest(){
		writeTo(new String[]{getWidth()+"",getHeight()+""}, 20, 20);
	}

	public void switchScreen(){
		byte[] temp = lcd.getDisplay().clone();
		lcd.clear();
		lcd.bitBlt(storedScreen, getWidth(), getHeight(), 0, 0, 20, 20, getWidth()+20, getHeight()+20, GraphicsLCD.ROP_COPY);
		storedScreen = temp;
	}

	public void drawEscapeButton(String content, int x, int y, int width, int height, int arc_diam){
		setFont(Font.getSmallFont());
		lcd.drawString(content, x+9, y+7, 0);
		lcd.drawLine(x, y,  width, y); // top line
		lcd.drawLine(x, y,  x, y+height-arc_diam/2); // left line
		lcd.drawLine(x+width, y,  width, y+height/2); // right line
		lcd.drawLine(x+arc_diam/2, y+height,  width-10, y+height); // bottom line
		lcd.drawLine(x+width-10, y+height, width, y+height/2); // diagonal
		lcd.drawArc(x, y+height-arc_diam, arc_diam, arc_diam, 180, 90);
		unsetFont();
	}

	public void drawUpButton(String content, int x, int y, int width, int height){
		setFont(Font.getSmallFont());
		lcd.drawString(content, x+width/2-lcd.getFont().stringWidth(content)/2, y-lcd.getFont().getHeight()/2+height/2, 0);

		lcd.drawLine(x+width/4, y,  x+(3*width)/4, y); // top line

		lcd.drawLine(x+(3*width)/4, y,  x+width, y+height/2); // top line
		lcd.drawLine(x+width, y+height/2,  x+(7*width)/8, y+(3*height)/4); // top line
		lcd.drawLine(x+(7*width)/8, y+(3*height)/4,  x+(7*width)/8, y+height); // top line

		lcd.drawLine(x+(7*width)/8, y+height,  x+(1*width)/8, y+height); // top line

		lcd.drawLine(x+(1*width)/8, y+height,  x+(1*width)/8, y+(3*height)/4); // top line
		lcd.drawLine(x+(1*width)/8, y+(3*height)/4,  x, y+height/2); // top line
		lcd.drawLine(x, y+height/2,  x+(1*width)/4, y); // top line

		unsetFont();
	}

	public void drawDownButton(String content, int x, int y, int width, int height){
		setFont(Font.getSmallFont());
		lcd.drawString(content, x+width/2-lcd.getFont().stringWidth(content)/2, y-lcd.getFont().getHeight()/2+height/2, 0);

		lcd.drawLine(x+width/4, y+height,  x+(3*width)/4, y+height); // top line

		lcd.drawLine(x+(3*width)/4, y+height,  x+width, y+height/2); // top line
		lcd.drawLine(x+width, y+height/2,  x+(7*width)/8, y+(1*height)/4); // top line
		lcd.drawLine(x+(7*width)/8, y+(1*height)/4,  x+(7*width)/8, y); // top line

		lcd.drawLine(x+(7*width)/8, y,  x+(1*width)/8, y); // top line

		lcd.drawLine(x+(1*width)/8, y,  x+(1*width)/8, y+(1*height)/4); // top line
		lcd.drawLine(x+(1*width)/8, y+(1*height)/4,  x, y+height/2); // top line
		lcd.drawLine(x, y+height/2,  x+(1*width)/4, y+height); // top line

		unsetFont();
	}

	public void drawRightButton(String content, int x, int y, int width, int height){
		setFont(Font.getSmallFont());
		lcd.drawString(content, x+width/2-lcd.getFont().stringWidth(content)/2, y-lcd.getFont().getHeight()/2+height/2, 0);

		lcd.drawLine(x, y,  x, y+height); // top line

		lcd.drawLine(x, y+height,  x+width/2, y+height); // top line
		lcd.drawLine(x+width/2, y+height,  x+width, y+height/2); // top line

		lcd.drawLine(x+width, y+height/2,  x+width/2, y); // top line
		lcd.drawLine(x+width/2, y,  x, y); // top line

		unsetFont();
	}

	public void drawLeftButton(String content, int x, int y, int width, int height){
		setFont(Font.getSmallFont());
		lcd.drawString(content, x+width/2-lcd.getFont().stringWidth(content)/2, y-lcd.getFont().getHeight()/2+height/2, 0);

		lcd.drawLine(x+width, y,  x+width, y+height); // top line

		lcd.drawLine(x+width, y+height,  x+width/2, y+height); // top line
		lcd.drawLine(x+width/2, y+height,  x, y+height/2); // top line

		lcd.drawLine(x, y+height/2,  x+width/2, y); // top line
		lcd.drawLine(x+width/2, y,  x+width, y); // top line

		unsetFont();
	}

	public void drawEnterButton(String content, int x, int y, int width, int height){
		setFont(Font.getSmallFont());
		lcd.drawString(content, x+width/2-lcd.getFont().stringWidth(content)/2, y-lcd.getFont().getHeight()/2+height/2, 0);

		lcd.drawLine(x+width, y,  x+width, y+height); // top line
		lcd.drawLine(x+width, y+height,  x, y+height); // top line
		lcd.drawLine(x, y+height,  x, y); // top line
		lcd.drawLine(x, y,  x+width, y); // top line


		unsetFont();
	}

	public void drawMenu(String left, String right, String up, String down, String enter){
		drawUpButton(up, getWidth()/2-45/2, 20, 45, 30);
		drawDownButton(down, getWidth()/2-45/2, 80, 45, 30);
		drawRightButton(right, getWidth()/2+(2*45)/4, 42, 35, 45);
		drawLeftButton(left, getWidth()/2-(5*45)/4, 42, 35, 45);
		drawEnterButton(enter, getWidth()/2-15, 52, 30, 25);
	}

	public void clearScreen(){
		lcd.clear();
	}

	public void setFont(Font font){
		previousFont = lcd.getFont();

		lcd.setFont(font);
	}

	public void unsetFont(){
		setFont(previousFont);
	}

	public void writeTo(String[] content, int x, int y, int anchor, int yshift, Font font, boolean fresh){
		if(fresh)
			clearScreen();

		if(font == null)
			font = Font.getDefaultFont();


		setFont(font);


		if(yshift == 0)
			yshift = font.getHeight()+1;

		int i = 0;
		for (String string : content) {
			lcd.drawString(string, x, y + (yshift*i), anchor);
			i++;
		}

		if(font != null)
			unsetFont();
	}

	public void writeTo(String[] content, int x, int y, int yshift, int anchor, boolean fresh){
		writeTo(content, x, y, anchor, yshift, null, fresh);
	}

	public void writeTo(String[] content, int x, int y, int anchor, boolean fresh){
		writeTo(content, x, y, anchor, lcd.getFont().getHeight()+1, null, fresh);
	}

	public void writeTo(String[] content, int x, int y, int yshift, int anchor, Font font){
		writeTo(content, x, y, anchor, yshift, font, false);
	}

	public void writeTo(String[] content, int x, int y, int anchor, Font font){
		writeTo(content, x, y, anchor, font.getHeight()+1, font, false);
	}

	public void writeTo(String[] content, int x, int y, int yshift, int anchor){
		writeTo(content, x, y, yshift, anchor, false);
	}

	public void writeTo(String[] content, int x, int y, int anchor){
		writeTo(content, x, y, anchor, lcd.getFont().getHeight()+1);
	}

	public void writeTo(String[] content, int x, int y){
		writeTo(content, x, y, 0);
	}

	public void writeTo(String[] content){
		writeTo(content, getWidth()/2, 0, GraphicsLCD.HCENTER);
	}


	private void drawImage(int dx, int dy, BufferedImage im){
		for (int x = 0; x < im.getWidth(); x++) {
			for (int y = 0; y < im.getHeight(); y++) {
				lcd.setPixel(x+dx, y+dy, Math.round((255f-(float)new Color(im.getRGB(x, y)).getRed())/255f));
			}
		}
	}

	public int getWidth(){
		return lcd.getWidth();
	}

	public int getHeight(){
		return lcd.getHeight();
	}

}
