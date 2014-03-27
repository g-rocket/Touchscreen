package net.clonecomputers.lab.touchscreen;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

import javax.swing.*;

public class KeyboardWindow extends JPanel{
	public static final int dim = (int)(1024/14.5);
	public static Robot r;
	
	Container[][] keys = new Container[][]{
		{
			new JButton("esc"),
			new JButton("F1"),
			new JButton("F2"),
			new JButton("F3"),
			new JButton("F4"),
			new JButton("F5"),
			new JButton("F6"),
			new JButton("F7"),
			new JButton("F8"),
			new JButton("F9"),
			new JButton("F10"),
			new JButton("F11"),
			new JButton("F12"),
			new JButton("eject"),
		},
		{
			new JButton("<html>~<p>`</html>"),
			new JButton("<html>!<p>1</html>"),
			new JButton("<html>@<p>2</html>"),
			new JButton("<html>#<p>3</html>"),
			new JButton("<html>$<p>4</html>"),
			new JButton("<html>%<p>5</html>"),
			new JButton("<html>^<p>6</html>"),
			new JButton("<html>&<p>7</html>"),
			new JButton("<html>*<p>8</html>"),
			new JButton("<html>(<p>9</html>"),
			new JButton("<html>)<p>0</html>"),
			new JButton("<html>_<p>-</html>"),
			new JButton("<html>+<p>=</html>"),
			new JButton("delete"),
		},
		{
			new JButton("tab"),
			new JButton("Q"),
			new JButton("W"),
			new JButton("E"),
			new JButton("R"),
			new JButton("T"),
			new JButton("Y"),
			new JButton("U"),
			new JButton("I"),
			new JButton("O"),
			new JButton("P"),
			new JButton("<html>{<p>[</html>"),
			new JButton("<html>}<p>]</html>"),
			new JButton("<html>|<p>\\</html>"),
		},
		{
			new JToggleButton("caps lock"),
			new JButton("A"),
			new JButton("S"),
			new JButton("D"),
			new JButton("F"),
			new JButton("G"),
			new JButton("H"),
			new JButton("J"),
			new JButton("K"),
			new JButton("L"),
			new JButton("<html>:<p>;</html>"),
			new JButton("<html>\"<p>'</html>"),
			new JButton("return"),
		},
		{
			new JToggleButton("shift"),
			new JButton("Z"),
			new JButton("X"),
			new JButton("C"),
			new JButton("V"),
			new JButton("B"),
			new JButton("N"),
			new JButton("M"),
			new JButton("<html>&lt;<p>,</html>"),
			new JButton("<html>&gt;<p>.</html>"),
			new JButton("<html>?<p>/</html>"),
			new JToggleButton("shift"),
		},
		{
			new JToggleButton("fn"),
			new JToggleButton("control"),
			new JToggleButton("option"),
			new JToggleButton("command"),
			new JButton(" "),
			new JToggleButton("command"),
			new JToggleButton("option"),
			new JPanel(),
			new JPanel(),
			new JPanel(),
		},
	};
	JPanel[] rows = new JPanel[6]; //top to bottom
	// {fn, num, qwer, asdf, zxcv, ctrl}
	public KeyboardWindow(){
		try {
			r = new Robot();
		} catch (AWTException e) {
			throw new RuntimeException(e);
		}
		try {
			UIManager.setLookAndFeel(
			        UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (UnsupportedLookAndFeelException e) {
			throw new RuntimeException(e);
		}
		Container[] t = new Container[6];
		
		keys[5][7].setLayout(new BoxLayout(keys[5][7],BoxLayout.Y_AXIS));
		keys[5][8].setLayout(new BoxLayout(keys[5][8],BoxLayout.Y_AXIS));
		keys[5][9].setLayout(new BoxLayout(keys[5][9],BoxLayout.Y_AXIS));
		keys[5][7].add(t[0] = new JPanel());
		keys[5][7].add(t[1] = new JButton("???"));
		keys[5][8].add(t[2] = new JButton("???"));
		keys[5][8].add(t[3] = new JButton("???"));
		keys[5][9].add(t[4] = new JPanel());
		keys[5][9].add(t[5] = new JButton("???"));
		for(Container c: t){
			c.setPreferredSize(new Dimension(dim,dim/2));
			c.setMinimumSize(new Dimension(dim,dim/2));
			c.setMaximumSize(new Dimension(dim,dim/2));
		}
		
		keys[5][7].setBackground(Color.CYAN);
		t[0].setBackground(Color.RED);
		t[4].setBackground(Color.BLUE);
		
		rows[0] = new JPanel();
		rows[1] = new JPanel();
		rows[2] = new JPanel();
		rows[3] = new JPanel();
		rows[4] = new JPanel();
		rows[5] = new JPanel();
		
		List<String> resizableButtons = Arrays.asList(new String[]{
			"tab","caps lock","delete","return","shift"," ",
		});
		
		keys[1][13].setPreferredSize(new Dimension((int)(1024/14.5*1.5),dim));
		keys[1][13].setMinimumSize(new Dimension((int)(1024/14.5*1.5),dim));
		keys[1][13].setMaximumSize(new Dimension((int)(1024/14.5*1.5),dim));
		
		keys[2][0].setPreferredSize(new Dimension((int)(1024/14.5*1.5),dim));
		keys[2][0].setMinimumSize(new Dimension((int)(1024/14.5*1.5),dim));
		keys[2][0].setMaximumSize(new Dimension((int)(1024/14.5*1.5),dim));
		
		keys[3][0].setPreferredSize(new Dimension((int)(1024/14.5*1.75),dim));
		keys[3][0].setMinimumSize(new Dimension((int)(1024/14.5*1.75),dim));
		keys[3][0].setMaximumSize(new Dimension((int)(1024/14.5*1.75),dim));
		
		keys[3][12].setPreferredSize(new Dimension((int)(1024/14.5*1.75),dim));
		keys[3][12].setMinimumSize(new Dimension((int)(1024/14.5*1.75),dim));
		keys[3][12].setMaximumSize(new Dimension((int)(1024/14.5*1.75),dim));
		
		keys[4][0].setPreferredSize(new Dimension((int)(1024/14.5*2.25),dim));
		keys[4][0].setMinimumSize(new Dimension((int)(1024/14.5*2.25),dim));
		keys[4][0].setMaximumSize(new Dimension((int)(1024/14.5*2.25),dim));
		
		keys[4][11].setPreferredSize(new Dimension((int)(1024/14.5*2.25),dim));
		keys[4][11].setMinimumSize(new Dimension((int)(1024/14.5*2.25),dim));
		keys[4][11].setMaximumSize(new Dimension((int)(1024/14.5*2.25),dim));
		
		keys[5][4].setPreferredSize(new Dimension((int)(1024/14.5*5.5),dim));
		keys[5][4].setMinimumSize(new Dimension((int)(1024/14.5*5.5),dim));
		keys[5][4].setMaximumSize(new Dimension((int)(1024/14.5*5.5),dim));
		
		for(int i = 0; i < keys.length; i++){
			rows[i].setLayout(new BoxLayout(rows[i],BoxLayout.X_AXIS));
			for(Container x: keys[i]){
				if((x instanceof AbstractButton &&
						!resizableButtons.contains(((AbstractButton)x).getText())) ||
						!(x instanceof AbstractButton)){
					if(i==0){
						x.setPreferredSize(new Dimension(1024/14,dim/2));
						x.setMinimumSize(new Dimension(1024/14,dim/2));
						x.setMaximumSize(new Dimension(1024/14,dim/2));
					}else{
						x.setPreferredSize(new Dimension(dim,dim));
						x.setMinimumSize(new Dimension(dim,dim));
						x.setMaximumSize(new Dimension(dim,dim));
					}
				}
				rows[i].add(x);
			}
		}
		
		this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
		for(JPanel r: rows) this.add(r);
		
		addActionListeners();
	}
	private void addActionListeners() {
		((AbstractButton)keys[0][0]).addMouseListener(new KBListener(KeyEvent.VK_ESCAPE));
		((AbstractButton)keys[0][1]).addMouseListener(new KBListener(KeyEvent.VK_F1));
		((AbstractButton)keys[0][2]).addMouseListener(new KBListener(KeyEvent.VK_F2));
		((AbstractButton)keys[0][3]).addMouseListener(new KBListener(KeyEvent.VK_F3));
		((AbstractButton)keys[0][4]).addMouseListener(new KBListener(KeyEvent.VK_F4));
		((AbstractButton)keys[0][5]).addMouseListener(new KBListener(KeyEvent.VK_F5));
		((AbstractButton)keys[0][6]).addMouseListener(new KBListener(KeyEvent.VK_F6));
		((AbstractButton)keys[0][7]).addMouseListener(new KBListener(KeyEvent.VK_F7));
		((AbstractButton)keys[0][8]).addMouseListener(new KBListener(KeyEvent.VK_F8));
		((AbstractButton)keys[0][9]).addMouseListener(new KBListener(KeyEvent.VK_F9));
		((AbstractButton)keys[0][10]).addMouseListener(new KBListener(KeyEvent.VK_F10));
		((AbstractButton)keys[0][11]).addMouseListener(new KBListener(KeyEvent.VK_F11));
		((AbstractButton)keys[0][12]).addMouseListener(new KBListener(KeyEvent.VK_F12));
		//((AbstractButton)keys[0][13]).addMouseListener(new KBListener(KeyEvent.VK_F13));

		((AbstractButton)keys[1][0]).addMouseListener(new KBListener(KeyEvent.VK_DEAD_GRAVE));
		((AbstractButton)keys[1][1]).addMouseListener(new KBListener(KeyEvent.VK_1));
		((AbstractButton)keys[1][2]).addMouseListener(new KBListener(KeyEvent.VK_2));
		((AbstractButton)keys[1][3]).addMouseListener(new KBListener(KeyEvent.VK_3));
		((AbstractButton)keys[1][4]).addMouseListener(new KBListener(KeyEvent.VK_4));
		((AbstractButton)keys[1][5]).addMouseListener(new KBListener(KeyEvent.VK_5));
		((AbstractButton)keys[1][6]).addMouseListener(new KBListener(KeyEvent.VK_6));
		((AbstractButton)keys[1][7]).addMouseListener(new KBListener(KeyEvent.VK_7));
		((AbstractButton)keys[1][8]).addMouseListener(new KBListener(KeyEvent.VK_8));
		((AbstractButton)keys[1][9]).addMouseListener(new KBListener(KeyEvent.VK_9));
		((AbstractButton)keys[1][10]).addMouseListener(new KBListener(KeyEvent.VK_0));
		((AbstractButton)keys[1][11]).addMouseListener(new KBListener(KeyEvent.VK_MINUS));
		((AbstractButton)keys[1][12]).addMouseListener(new KBListener(KeyEvent.VK_EQUALS));
		((AbstractButton)keys[1][13]).addMouseListener(new KBListener(KeyEvent.VK_BACK_SPACE));

		((AbstractButton)keys[2][0]).addMouseListener(new KBListener(KeyEvent.VK_TAB));
		((AbstractButton)keys[2][1]).addMouseListener(new KBListener(KeyEvent.VK_Q));
		((AbstractButton)keys[2][2]).addMouseListener(new KBListener(KeyEvent.VK_W));
		((AbstractButton)keys[2][3]).addMouseListener(new KBListener(KeyEvent.VK_E));
		((AbstractButton)keys[2][4]).addMouseListener(new KBListener(KeyEvent.VK_R));
		((AbstractButton)keys[2][5]).addMouseListener(new KBListener(KeyEvent.VK_T));
		((AbstractButton)keys[2][6]).addMouseListener(new KBListener(KeyEvent.VK_Y));
		((AbstractButton)keys[2][7]).addMouseListener(new KBListener(KeyEvent.VK_U));
		((AbstractButton)keys[2][8]).addMouseListener(new KBListener(KeyEvent.VK_I));
		((AbstractButton)keys[2][9]).addMouseListener(new KBListener(KeyEvent.VK_O));
		((AbstractButton)keys[2][10]).addMouseListener(new KBListener(KeyEvent.VK_P));
		((AbstractButton)keys[2][11]).addMouseListener(new KBListener(KeyEvent.VK_OPEN_BRACKET));
		((AbstractButton)keys[2][12]).addMouseListener(new KBListener(KeyEvent.VK_CLOSE_BRACKET));
		((AbstractButton)keys[2][13]).addMouseListener(new KBListener(KeyEvent.VK_SLASH));


		((AbstractButton)keys[3][0]).addMouseListener(new KBListener(KeyEvent.VK_CAPS_LOCK));
		((AbstractButton)keys[3][1]).addMouseListener(new KBListener(KeyEvent.VK_A));
		((AbstractButton)keys[3][2]).addMouseListener(new KBListener(KeyEvent.VK_S));
		((AbstractButton)keys[3][3]).addMouseListener(new KBListener(KeyEvent.VK_D));
		((AbstractButton)keys[3][4]).addMouseListener(new KBListener(KeyEvent.VK_F));
		((AbstractButton)keys[3][5]).addMouseListener(new KBListener(KeyEvent.VK_G));
		((AbstractButton)keys[3][6]).addMouseListener(new KBListener(KeyEvent.VK_H));
		((AbstractButton)keys[3][7]).addMouseListener(new KBListener(KeyEvent.VK_J));
		((AbstractButton)keys[3][8]).addMouseListener(new KBListener(KeyEvent.VK_K));
		((AbstractButton)keys[3][9]).addMouseListener(new KBListener(KeyEvent.VK_L));
		((AbstractButton)keys[3][10]).addMouseListener(new KBListener(KeyEvent.VK_SEMICOLON));
		((AbstractButton)keys[3][11]).addMouseListener(new KBListener(KeyEvent.VK_DEAD_ACUTE));
		((AbstractButton)keys[3][12]).addMouseListener(new KBListener(KeyEvent.VK_ENTER));

		((AbstractButton)keys[4][0]).addMouseListener(new KBListener(KeyEvent.VK_SHIFT));
		((AbstractButton)keys[4][1]).addMouseListener(new KBListener(KeyEvent.VK_Z));
		((AbstractButton)keys[4][2]).addMouseListener(new KBListener(KeyEvent.VK_X));
		((AbstractButton)keys[4][3]).addMouseListener(new KBListener(KeyEvent.VK_C));
		((AbstractButton)keys[4][4]).addMouseListener(new KBListener(KeyEvent.VK_V));
		((AbstractButton)keys[4][5]).addMouseListener(new KBListener(KeyEvent.VK_B));
		((AbstractButton)keys[4][6]).addMouseListener(new KBListener(KeyEvent.VK_N));
		((AbstractButton)keys[4][7]).addMouseListener(new KBListener(KeyEvent.VK_M));
		((AbstractButton)keys[4][8]).addMouseListener(new KBListener(KeyEvent.VK_COMMA));
		((AbstractButton)keys[4][9]).addMouseListener(new KBListener(KeyEvent.VK_PERIOD));
		((AbstractButton)keys[4][10]).addMouseListener(new KBListener(KeyEvent.VK_BACK_SLASH));
		((AbstractButton)keys[4][11]).addMouseListener(new KBListener(KeyEvent.VK_SHIFT));

		//((AbstractButton)keys[5][0]).addMouseListener(new KBListener(KeyEvent.VK_FN));
		((AbstractButton)keys[5][1]).addMouseListener(new KBListener(KeyEvent.VK_CONTROL));
		((AbstractButton)keys[5][2]).addMouseListener(new KBListener(KeyEvent.VK_ALT));
		((AbstractButton)keys[5][3]).addMouseListener(new KBListener(KeyEvent.VK_WINDOWS));
		((AbstractButton)keys[5][4]).addMouseListener(new KBListener(KeyEvent.VK_SPACE));
		((AbstractButton)keys[5][5]).addMouseListener(new KBListener(KeyEvent.VK_WINDOWS));
		((AbstractButton)keys[5][6]).addMouseListener(new KBListener(KeyEvent.VK_ALT));
		((AbstractButton)(keys[5][7].getComponents()[1])).addMouseListener(new KBListener(KeyEvent.VK_LEFT));
		((AbstractButton)(keys[5][8].getComponents()[0])).addMouseListener(new KBListener(KeyEvent.VK_UP));
		((AbstractButton)(keys[5][8].getComponents()[1])).addMouseListener(new KBListener(KeyEvent.VK_DOWN));
		((AbstractButton)(keys[5][9].getComponents()[1])).addMouseListener(new KBListener(KeyEvent.VK_RIGHT));
		
	}
	private class KBListener implements MouseListener{
		private int key;
		
		public KBListener(int key){
			this.key = key;
		}

		//@Override
		public void mouseClicked(MouseEvent e) {
			// do nothing
		}

		//@Override
		public void mouseEntered(MouseEvent e) {
			// do nothing
		}

		//@Override
		public void mouseExited(MouseEvent e) {
			// do nothing
		}

		//@Override
		public void mousePressed(MouseEvent e) {
			/*Process p;
			try {
				p = Runtime.getRuntime().exec("/Users/gavinsyancey/Desktop/swap.sh");
			} catch (IOException ioe) {
				throw new RuntimeException(ioe);
			}
			try {
				p.waitFor();Thread.sleep(100);
			} catch (InterruptedException ie) {
				throw new RuntimeException(ie);
			}*/
			/*try {
				Thread.sleep(1000);
			} catch (InterruptedException ie) {
				// TODO Auto-generated catch block
				throw new RuntimeException(ie);
			}*/
			r.keyPress(key);
			System.out.println("Pressing " + KeyEvent.getKeyText(key));
		}

		//@Override
		public void mouseReleased(MouseEvent e) {
			/*try {
				Runtime.getRuntime().exec("/Users/gavinsyancey/Desktop/swap.sh");
			} catch (IOException ioe) {
				throw new RuntimeException(ioe);
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException ie) {
				// TODO Auto-generated catch block
				throw new RuntimeException(ie);
			}*/
			r.keyRelease(key);
			System.out.println("Releasing " + KeyEvent.getKeyText(key));
		}
		
	}
}
