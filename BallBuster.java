import java.applet.Applet;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

public class BallBuster extends Applet implements MouseListener, MouseMotionListener, KeyListener{

	public static final int MAX_BALLS = 100;
	public static final double MAX_VELOCITY = 50;
	public static final double MIN_MASS = 5;
	public static final double MAX_MASS = 30-MIN_MASS;
	public static Ball[] balls = new Ball[MAX_BALLS];
	public static int total_balls = 0;
	public static final int WIDTH = 600;
	public static final int HEIGHT = 400;
	public static int PAUSE = 55;
	public static boolean freeze = false;
	public static boolean still = false;
	public static boolean vector_mode = false;
	public static int draggo = -1;
	public static int[] mousedrag = new int [2];
	
	
	// controls
	public Panel cp = new Panel();
	public static Button set = new Button("Set");
	public static Label vmode = new Label("MOVE MODE");
	public static Label fmode = new Label("UNFREEZE");	
	public static Label smode = new Label("UNSTILL");	
	
	//public void 
	
	Random rand = new Random(System.currentTimeMillis());

	// utility methods
	public Color random_colour(){
		return new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));
	}

	public double distsq(double x1, double y1, double x2, double y2){
		double dx = x1 - x2;
		double dy = y1 - y2;
		return (dx * dx) + (dy * dy);	
	}
	
	// mouse stuff
	// MouseListener
	public void mouseClicked(MouseEvent me){
		if(total_balls >= MAX_BALLS) return;
		
		// add a new ball
		if(still){
			balls[total_balls] = (new Ball(rand.nextDouble()*MAX_MASS+MIN_MASS, me.getX(), me.getY(), 0, 0, random_colour()));
			repaint();
			return;
		}
		//balls[total_balls] = new Ball(rand.nextDouble()*MAX_MASS+MIN_MASS, me.getX(), me.getY(),0, rand.nextDouble()*MAX_VELOCITY-MAX_VELOCITY/2, random_colour());
		balls[total_balls] = new Ball(rand.nextDouble()*MAX_MASS+MIN_MASS, me.getX(), me.getY(),rand.nextDouble()*MAX_VELOCITY-MAX_VELOCITY/2, rand.nextDouble()*MAX_VELOCITY-MAX_VELOCITY/2, random_colour());	
		//balls[total_balls] = new Ball(rand.nextDouble()*MAX_MASS+MIN_MASS, me.getX(), me.getY(), rand.nextDouble()*MAX_VELOCITY-MAX_VELOCITY/2, 0, random_colour());
		++total_balls;
		repaint();
	}
	
	public void mouseEntered (MouseEvent me) {} 
	public void mousePressed (MouseEvent me) {

	} 
	public void mouseReleased (MouseEvent me) {
		draggo = -1;
		mousedrag[0] = 0;
		mousedrag[1] = 0;
		if(vector_mode) freeze = false;
	}  
	public void mouseExited (MouseEvent me) {}  
	
	// MouseMotionListener
	public void mouseDragged(MouseEvent me){
		if(!freeze) return;
		if(!vector_mode)
		{
			int i = 0;
			for(; i < total_balls; ++i){
				if(distsq(me.getX(), me.getY(), balls[i].pos[0], balls[i].pos[1]) < balls[i].mass*balls[i].mass){
					balls[i].pos[0] = me.getX(); 
					balls[i].pos[1] = me.getY();
					repaint();
					return;
				}
			}
			return;
		}
		int i = draggo;
		if(i == -1)
		{
			for(i = 0; i < total_balls; ++i)
			{
				if(distsq(me.getX(), me.getY(), balls[i].pos[0], balls[i].pos[1]) < balls[i].mass*balls[i].mass){
					balls[i].pos[0] = me.getX(); 
					balls[i].pos[1] = me.getY();
					repaint();
					draggo = i;
					return;
				}
			}
		}
		else{
			balls[i].vel[0] =  me.getX() - balls[i].pos[0];
			balls[i].vel[1] =  me.getY() - balls[i].pos[1] ;
			mousedrag[0] = me.getX();
			mousedrag[1] = me.getY();
		}
	}
	
	public void mouseMoved(MouseEvent me){}
	
	
	// keyboard stuff
	public void keyPressed(KeyEvent ke) {
		int kc = ke.getKeyCode();
		switch (kc) {
			case KeyEvent.VK_F:
				toggle_freeze();
			break;
			
			case KeyEvent.VK_V:
				toggle_vector();
			break;				
			
			case KeyEvent.VK_S:
				toggle_still();
			break;		
			
			case KeyEvent.VK_UP:
				increase_speed();
			break;
			
			case KeyEvent.VK_DOWN:
				decrease_speed();
			break;	
		}
	}
	public void keyReleased( KeyEvent ke ) { }
	public void keyTyped( KeyEvent ke ) {}


	// user control functions
	public void toggle_freeze(){
		fmode.setText((freeze) ? "UNFREEZE" : "FREEZE");
		System.out.println((freeze) ? "UNFREEZE" : "FREEZE");
		freeze = !freeze;
	}
	
	public void toggle_vector(){
		vmode.setText((vector_mode) ? "MOVE MODE" : "VECTOR MODE");
		System.out.println((vector_mode) ? "MOVE MODE" : "VECTOR MODE");
		vector_mode = !vector_mode;
	}
	
	public void increase_speed(){
		if(PAUSE > 10) --PAUSE;
	}
	
	public void decrease_speed(){
		++PAUSE;
	}
	
	public void toggle_still(){
		smode.setText((still) ? "UNSTILL" : "STILL");
		System.out.println((still) ? "UNSTILL" : "STILL");
		still = !still;
	}
	// rendering methods
	public void init(){
		this.setSize(WIDTH, HEIGHT);

		
		/*this.add(cp,BorderLayout.SOUTH);
		Label mlab = new Label("Mass: ");
		Label vlab = new Label("Vel: ");
		cp.add(mlab);
		cp.add(vlab);
		cp.add(vmode);
		cp.add(fmode);*/
		
		
		setBackground(Color.black);
		
		addMouseListener(this);
		addMouseMotionListener(this);
		addKeyListener(this);
		
	}
	
	
	public void paint(Graphics g){
		for(int i = 0; i < total_balls; ++i){
			balls[i].draw(g);
			if(!freeze){
				balls[i].move();
				for(int j = 0; j < total_balls; ++j){
					if(i == j) continue;
					balls[i].collide(balls[j]);
				}
				Dimension d = getSize();
				balls[i].hit_wall(d.width, d.height);
			}
		}
		if(vector_mode && freeze && draggo > -1){
			g.setColor(balls[draggo].color);
			g.drawLine(mousedrag[0], mousedrag[1], (int)balls[draggo].pos[0], (int)balls[draggo].pos[1]);
		}
	}
	
	public void update(Graphics g){
		paint(g);
		try{Thread.sleep(PAUSE);}catch(Exception e){} 
		Dimension d = getSize();
		g.setColor(Color.black);
		g.fillRect(0, 0, d.width, d.height);	
		repaint();
	}
	
	// lol
	public static void main(String[] args){
		
	}
}