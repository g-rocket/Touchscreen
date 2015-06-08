package net.clonecomputers.lab.touchscreen2;

import java.awt.*;
import java.util.*;

import javax.swing.*;

public class Keyboard extends JPanel {
	private int[][][] layouts = {{
		{0xf0,0xf1,0xf2,0xf3,0xf4,0xf5,0xf6,0xf7,0xf8,0xf9,0xfa,0xfb,0xfc,0xfd},
		{'`','1','2','3','4','5','6','7','8','9','0','-','=',0x7f},
		{0x80,'q','w','e','r','t','y','u','i','o','p','[',']','\\'},
		{0xa1,'a','s','d','f','g','h','j','k','l',';','\'',0x81},
		{0xa2,'z','x','c','v','b','n','m',',','.','/',0xb2},
		{0xa3,0xa4,0xa5,0xa6,' ',0xb6,0xb5,0xc0,0xc1,0xc2,0xc3}
	}, {
		{0xf0,0xf1,0xf2,0xf3,0xf4,0xf5,0xf6,0xf7,0xf8,0xf9,0xfa,0xfb,0xfc,0xfd},
		{'~','!','@','#','$','%','^','&','*','(',')','_','+',0x7f},
		{0x80,'Q','W','E','R','T','Y','U','I','O','P','{','}','|'},
		{0xa1,'A','S','D','F','G','H','J','K','L',':','"',0x81},
		{0xa2,'Z','X','C','V','B','N','M','<','>','?',0xb2},
		{0xa3,0xa4,0xa5,0xa6,' ',0xb6,0xb5,0xc0,0xc1,0xc2,0xc3}
	}, {
		{0xf0,0xf1,0xf2,0xf3,0xf4,0xf5,0xf6,0xf7,0xf8,0xf9,0xfa,0xfb,0xfc,0xfd},
		{'`','1','2','3','4','5','6','7','8','9','0','-','=',0x7f},
		{0x80,'Q','W','E','R','T','Y','U','I','O','P','[',']','\\'},
		{0xa1,'A','S','D','F','G','H','J','K','L',';','\'',0x81},
		{0xa2,'Z','X','C','V','B','N','M',',','.','/',0xb2},
		{0xa3,0xa4,0xa5,0xa6,' ',0xb6,0xb5,0xc0,0xc1,0xc2,0xc3}
	}};
	
	private Map<Integer, String> keyTexts = new HashMap<Integer, String>(){{
		put(0xf0,"esc");
		put(0xf1,"F1");
		put(0xf2,"F2");
		put(0xf3,"F3");
		put(0xf4,"F4");
		put(0xf5,"F5");
		put(0xf6,"F6");
		put(0xf7,"F7");
		put(0xf8,"F8");
		put(0xf9,"F9");
		put(0xfa,"F10");
		put(0xfb,"F11");
		put(0xfc,"F12");
		put(0xfd,"eject");

		put(0x7f,"delete");
		put(0x80,"tab");
		put(0x81,"return");
		
		put(0xa1,"caps lock");
		put(0xa2,"shift");
		put(0xb2,"shift");
		put(0xa3,"ctrl");
		put(0xb3,"ctrl");
		put(0xa4,"fn");
		put(0xb4,"fn");
		put(0xa5,"alt");
		put(0xb5,"alt");
		put(0xa6,"cmd");
		put(0xb6,"cmd");
		
		put(0xc0,"<-");
		put(0xc1,"/\\");
		put(0xc2,"\\/");
		put(0xc3,"->");
		put(0xcc,"??");
	}};
	
	private double[] lineHeights = {
			0.5,1,1,1,1,1
	};
	
	private double[][] keyWidths = {
			{      1,1,1,1,1,1,1,1,1,1,1,1,1,1       },
			{      1,1,1,1,1,1,1,1,1,1,1,1,1,    1.5 },
			{1.5 , 1,1,1,1,1,1,1,1,1,1,1,1,1,        },
			{1.75, 1,1,1,1,1,1,1,1,1,1,1,        1.75},
			{2.25, 1,1,1,1,1,1,1,1,1,1,          2.25},
			{1,1,1,1.25,   6,   1.25,0.75,  1,-.5,-.5,1},
	};
	
	private Map<Integer, JButton> keys = new HashMap<Integer, JButton>();

	public static void main(String[] args) {
		JFrame window = new JFrame();
		window.setContentPane(new Keyboard());
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.pack();
		window.setVisible(true);
	}
	
	public Keyboard() {
		for(int[][] layout: layouts) {
			for(int[] line: layout) {
				for(int keyId: line) {
					if(!keyTexts.containsKey(keyId)) {
						if(keyId >= 0x20 && keyId < 0x7f) {
							keyTexts.put(keyId, Character.toString((char)keyId));
						} else {
							System.out.printf("No valid text for keyId %2x\n",keyId);
						}
					}
				}
			}
		}
		
		Dimension windowSize = new Dimension(1024,400);
		setMaximumSize(windowSize);
		setMinimumSize(windowSize);
		setPreferredSize(windowSize);
		setSize(windowSize);
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		
		double totalHeight = 0;
		for(double height: lineHeights) totalHeight += Math.abs(height);
		
		for(int i = 0; i < lineHeights.length; i++) {
			Box line = new Box(BoxLayout.LINE_AXIS);
			Dimension lineSize = new Dimension(windowSize.width, (int)(windowSize.height / totalHeight * lineHeights[i]));
			line.setMaximumSize(lineSize);
			line.setMinimumSize(lineSize);
			line.setPreferredSize(lineSize);
			line.setSize(lineSize);
			
			double totalWidth = 0;
			for(double width: keyWidths[i]) totalWidth += Math.abs(width);
			
			for(int j = 0; j < keyWidths[i].length; j++) {
				Dimension keySize = new Dimension((int)(lineSize.width / totalWidth * Math.abs(keyWidths[i][j])), lineSize.height);
				
				if(keyWidths[i][j] < 0) {
					// up&down arrow keys
					Box arrowPane = new Box(BoxLayout.PAGE_AXIS);
					keySize.width *= 2;
					
					arrowPane.setMaximumSize(keySize);
					arrowPane.setMinimumSize(keySize);
					arrowPane.setPreferredSize(keySize);
					arrowPane.setSize(keySize);
					
					for(int k = 0; k < 1; k++,j++) {
						Dimension actualKeySize = new Dimension(keySize.width, keySize.height/2);
						JButton key = new JButton(keyTexts.get(layouts[0][i][j]));
						key.setMaximumSize(actualKeySize);
						key.setMinimumSize(actualKeySize);
						key.setPreferredSize(actualKeySize);
						key.setSize(actualKeySize);
						
						keys.put(i<<8 | j, key);
						arrowPane.add(key);
					}
					
					line.add(arrowPane);
					
					j--;
					continue;
				}
				
				JButton key = new JButton(keyTexts.get(layouts[0][i][j]));
				key.setMaximumSize(keySize);
				key.setMinimumSize(keySize);
				key.setPreferredSize(keySize);
				key.setSize(keySize);
				
				keys.put(i<<8 | j, key);
				
				line.add(key);
				System.out.print(keyTexts.get(layouts[0][i][j]));
			}
			System.out.println();
			
			add(line);
		}
	}

	public void keyDown(int key) {
		// TODO Auto-generated method stub
		
	}

	public void keyUp(int key) {
		// TODO Auto-generated method stub
		
	}

	public void keyPress(int key) {
		// TODO Auto-generated method stub
		
	}

	public int getKeyCode(int x, int y) {
		// TODO Auto-generated method stub
		return 0;
	}

}
