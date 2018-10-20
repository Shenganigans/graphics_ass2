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
public class Camera3rD{

    private Point3D myPos;
    private float myScale;
    private float rotX = 0;
    private Camera3D avatar;

    public Camera3rD(Camera3D avatar) {
        this.avatar=avatar;
    }
    
    public void draw(GL3 gl, CoordFrame3D frame) {
        CoordFrame3D cameraFrame = frame.translate(myPos)
                .scale(myScale, myScale, myScale);
       
    }
    public float rotation() {
    	return rotX;
    }

    /**
     * Set the view transform
     * 
     * Note: this is the inverse of the model transform above
     * 
     * @param gl
     */
    public void setView(GL3 gl) {
    	float x = avatar.getLocation().getX();
    	float y = avatar.getLocation().getY();
    	float z = avatar.getLocation().getZ();
    	float rot = avatar.rotation();
        CoordFrame3D viewFrame = CoordFrame3D.identity()
        		//.scale(0.5f, 0.5f,0.5f)
        		.rotateX(0).rotateY(rot).rotateZ(0)

                //.translate(-x, -y, -z);
        		.translate((float)-x+(float)Math.sin(Math.toRadians(rot)), -y, (float)-z-(float)Math.cos(Math.toRadians(rot)));
        Shader.setViewMatrix(gl, viewFrame.getMatrix());
    }
    
}
