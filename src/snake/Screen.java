package snake;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.Timer;

public class Screen extends JPanel implements Runnable  {
        
        private ArrayList<SnakeListener> listeners = new ArrayList<SnakeListener>();
    
	private static final long serialVersionUID = 1L;

	public static final int WIDTH = 600;
	public static final int HEIGHT = WIDTH;
	public static final int GAME_FIELD_ROLS = 40;
	public static final int GAME_FIELD_COLS = GAME_FIELD_ROLS;
	private Thread thread;
        
        private volatile boolean forever = true;
	private volatile boolean running = false;

	private BodyPart b;
	private ArrayList<BodyPart> snake;

	// private Apple apple;
	private ArrayList<Apple> apples;
	

	private int xCoor;
	private int yCoor;
	private int size;

	public int snakeSpeed;
	private int snakeFinalSpeed = 1;
	public int speedSstep = 1;

	boolean right;
	boolean left;
	boolean up;
	boolean down;

	private int ticks = 0;
        
        public int manzanas;
        
	private Key key;
        
        private Timer timer;
        
	public Screen() {
		// setFocusable(true);
                
                timer = new Timer(1, new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                        if(running) {
                            repaint();
                            tick();
                        } else {
                            ((Timer)e.getSource()).stop();
                        }
                    }
                });
                timer.setRepeats(true);
                timer.setDelay(10);
                
            
		
                // System.out.println("Inicio juego");
                apples = new ArrayList<Apple>();
                
                key = new Key();
		addKeyListener(key);
                
                // KeyListener keyListener = new MotionWithKeyListener(component, 3, 3);
                // component.addKeyListener(keyListener);
                                
                setPreferredSize(new Dimension(WIDTH, HEIGHT));
                
                thread = new Thread(this);
	}
        
        // OBSERVABLE PATTERN
        public void addListener(SnakeListener toAdd) {
            listeners.add(toAdd);
        }

	public void tick() {
                // System.out.println("tick");
             		
		for (int i = 0; i < apples.size(); i++) {
			if ((xCoor == apples.get(i).getX())
					&& (yCoor == apples.get(i).getY())) {
                                // se la comio porque estan en la misma posicion
                                
				size++;
				apples.remove(i);
				i--;
				if (snakeSpeed != snakeFinalSpeed) {
					snakeSpeed -= speedSstep;
				}
                                
                                manzanas++;
                                
                                if(listeners.size()>0){
                                    for(SnakeListener l : listeners){
                                        l.ComioManzana();
                                    }
                                }
			}
		}
                
                
		for (int i = 0; i < snake.size(); i++) {
			if (xCoor == snake.get(i).getX() && yCoor == snake.get(i).getY()) {
				if (i < snake.size() - 3) {
					stop();
				}
			}
		}
                
                // LIMITES DEL JUEGO
		if (xCoor < 0 || xCoor > GAME_FIELD_COLS - 1  || yCoor < 0
				|| yCoor > GAME_FIELD_ROLS - 1 ) {
			stop();
		}
                
		ticks++;
                
                // System.out.printf("\nticks %d %d", ticks, snakeSpeed);
		
                if (ticks > snakeSpeed) {
			if (right) {
				xCoor+=1;
			}
			if (left) {
				xCoor-=1;
			}
			if (up) {
				yCoor-=1;
			}
			if (down) {
				yCoor+=1;
			}

			ticks = 0;

			b = new BodyPart(xCoor, yCoor, HEIGHT / GAME_FIELD_COLS);
			snake.add(b);

			if (snake.size() > size) {
				snake.remove(0);
			}
		}
	}

	public void paint(Graphics g) {
            // System.out.println("paint");
		g.clearRect(0, 0, WIDTH, HEIGHT);
		g.fillRect(0, 0, WIDTH, HEIGHT);
                
		g.setColor(Color.DARK_GRAY);
                
                int size = WIDTH / GAME_FIELD_COLS;
		for (int i = 0; i < GAME_FIELD_COLS; i++) {
                    g.drawLine(i * size, 0, i * size, HEIGHT);
		}

		for (int i = 0; i < GAME_FIELD_ROLS; i++) {
                    
                    g.drawLine(0, i * size, WIDTH, i * size);
		}
		g.setColor(Color.BLACK);
		g.fillRect(WIDTH, 0, WIDTH, HEIGHT);
                
                if(snake != null)
                {
                    for (int i = 0; i < snake.size(); i++) {
                            snake.get(i).draw(g);
                    }
                }
                
		for (int i = 0; i < apples.size(); i++) {
			apples.get(i).draw(g);
		}
	}
        
        public void generarManzana(int CordX, int CordY){
            apples.clear();
            if (apples.size() == 0) {
                Apple apple = new Apple(CordX, CordY, HEIGHT / GAME_FIELD_COLS);
		apples.add(apple);
            }
        }

	public void start() {
                size = 3;
                snakeSpeed = 11;
                
                manzanas = 0;
                        
                xCoor = 1;
                yCoor = 20;
                
                right = true;
                left = false;
                up = false;
                down = false;
                
		snake = new ArrayList<BodyPart>();
                
		b = new BodyPart(xCoor, yCoor, HEIGHT / GAME_FIELD_COLS);
		snake.add(b);
                
		running = true;
		
                // if(!thread.isAlive())
                //     thread.start();
                
                timer.start();
                
                if(listeners.size()>0){
                    for(SnakeListener l : listeners){
                        l.inicio();
                    }
                }
	}

	public void stop() {
            running = false;
            timer.stop();
            if(listeners.size()>0){
                for(SnakeListener l : listeners){
                    l.seDetuvo();
                }
            }
	}
        
        public void terminate(){
            forever = false;
            running = false;
            
            try {
                thread.join();
            } catch (InterruptedException ex) {
                Logger.getLogger(Screen.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        @Override
	public void run() {
            
            if(!timer.isRunning())
            timer.start();

        }

        
    private class Key implements KeyListener {
		public void keyPressed(KeyEvent e) {
                     // System.out.println("Presiono tecla " + e.toString());
		}

		@Override
		public void keyTyped(KeyEvent e) {
			// TODO Auto-generated method stub
		}

		@Override
		public void keyReleased(KeyEvent e) {
			// TODO Auto-generated method stub
                        int key = e.getKeyCode();

			if (key == KeyEvent.VK_RIGHT && !left) {
				up = false;
				down = false;
				right = true;
			}
			if (key == KeyEvent.VK_LEFT && !right) {
				up = false;
				down = false;
				left = true;
			}
			if (key == KeyEvent.VK_UP && !down) {
				up = true;
				right = false;
				left = false;
			}
			if (key == KeyEvent.VK_DOWN && !up) {
				down = true;
				right = false;
				left = false;
			}
                        
			if (key == KeyEvent.VK_P) {
				boolean pressOnce = true;
				if (pressOnce) {
					while (true) {
						int keyPause = e.getKeyCode();

						if (keyPause != KeyEvent.VK_P) {
							pressOnce = false;
							System.out.println(keyPause);
							break;
						}
					}
				} else {

				}
			}
                        
		}
	}
}