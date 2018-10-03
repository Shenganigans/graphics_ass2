package unsw.graphics.world;

import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;
import com.jogamp.opengl.GL3;

import unsw.graphics.CoordFrame2D;
import unsw.graphics.CoordFrame3D;
import unsw.graphics.Shader;
import unsw.graphics.geometry.LineStrip2D;
import unsw.graphics.geometry.Point2D;
import unsw.graphics.geometry.Point3D;



/**
 * The camera for the person demo
 *
 * @author malcolmr
 * @author Robert Clifton-Everest
 */
public class Camera3D implements KeyListener {

    private Point3D myPos;
    private float myScale;
    private float rotX = 0;
    private Terrain myTerrain;

    public Camera3D(Terrain terrain) {
        myPos = new Point3D(3.5f,0,15);
        myScale = 0;
        myTerrain = terrain;
    }
    
    public void draw(GL3 gl, CoordFrame3D frame) {
        CoordFrame3D cameraFrame = frame.translate(myPos)
                .scale(myScale, myScale, myScale);
       
    }

    /**
     * Set the view transform
     * 
     * Note: this is the inverse of the model transform above
     * 
     * @param gl
     */
    public void setView(GL3 gl) {
        CoordFrame3D viewFrame = CoordFrame3D.identity()
        		.scale(0.5f, 0.5f,0.5f)
        		.rotateX(0).rotateY(rotX).rotateZ(0)
                .translate(-myPos.getX(), -myPos.getY(), -myPos.getZ());
        Shader.setViewMatrix(gl, viewFrame.getMatrix());
    }

    @Override
    public void keyPressed(KeyEvent e) {
    	float x;
    	float z;
    	float alt;
        switch(e.getKeyCode()) {
        case KeyEvent.VK_LEFT:
         
                rotX -= 10;                
            break;
            
        case KeyEvent.VK_RIGHT:
 
                rotX += 10;                
            break;

        case KeyEvent.VK_DOWN:
        	x = (float)Math.sin(Math.toRadians(rotX));
        	z = (float)Math.cos(Math.toRadians(rotX));
        	alt = myTerrain.altitude(-x+myPos.getX(), z+myPos.getZ());
        	
        	myPos = myPos.translate(-x,alt-myPos.getY(),z);
        	System.out.println(myPos.getX());
        	System.out.println(myPos.getY());
        	System.out.println(myPos.getZ());
            break;

        case KeyEvent.VK_UP:
        	x = (float)Math.sin(Math.toRadians(rotX));
        	z = (float)Math.cos(Math.toRadians(rotX));
        	alt = myTerrain.altitude(x+myPos.getX(), z+myPos.getZ());

        	myPos = myPos.translate(x,alt-myPos.getY(),-z);
        	System.out.println(myPos.getX());
        	System.out.println(myPos.getY());
        	System.out.println(myPos.getZ());
            break;
        }

    }

    @Override
    public void keyReleased(KeyEvent e) {}

}
