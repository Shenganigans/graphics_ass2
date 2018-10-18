package unsw.graphics.world;

import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;
import unsw.graphics.geometry.Point3D;

/**
 * COMMENT: Comment Tree 
 *
 * @author malcolmr
 */
public class Avatar implements KeyListener{

    private Point3D position;
    private float rot;
    private Terrain myTerrain;
    
    public Avatar(float x, float y, float z, Terrain t) {
        position = new Point3D(x, y, z);
        rot = 0;
        myTerrain = t;
        
    }
    
    public Point3D getPosition() {
        return position;
    }
    public float getRotation() {
        return rot;
    }
    @Override
    public void keyPressed(KeyEvent e) {
    	float x;
    	float z;
    	float alt;
        switch(e.getKeyCode()) {
        case KeyEvent.VK_LEFT:
         
                rot += 5;                
            break;
            
        case KeyEvent.VK_RIGHT:
 
                rot -= 5;                
            break;

        case KeyEvent.VK_DOWN:
        	x = (float)Math.sin(Math.toRadians(rot));
        	z = (float)Math.cos(Math.toRadians(rot));
        	alt = myTerrain.altitude(x+position.getX(), -z+position.getZ())+1.5f;
        	
        	position = position.translate(x/5,alt-position.getY(),z/5);
        	System.out.println(position.getX());
        	System.out.println(position.getY());
        	System.out.println(position.getZ());
            break;

        case KeyEvent.VK_UP:
        	x = (float)Math.sin(Math.toRadians(rot));
        	z = (float)Math.cos(Math.toRadians(rot));
        	alt = myTerrain.altitude(x+position.getX(), z+position.getZ())+1.5f;

        	position = position.translate(-x/5,alt-position.getY(),-z/5);
        	System.out.println(position.getX());
        	System.out.println(position.getY());
        	System.out.println(position.getZ());
            break;
     
        }

    }
    


	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}
}