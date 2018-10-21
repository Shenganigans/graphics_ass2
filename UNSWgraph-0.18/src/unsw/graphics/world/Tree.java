package unsw.graphics.world;

import com.jogamp.opengl.GL3;
import unsw.graphics.CoordFrame3D;
import unsw.graphics.geometry.Point3D;
import unsw.graphics.geometry.TriangleMesh;


/**
 * COMMENT: Comment Tree 
 *
 * @author malcolmr
 */
public class Tree {

    private Point3D position;
    private final static float TREEHEIGHT = 2.5f;

    public Tree(float x, float y, float z) {
        position = new Point3D(x, y, z);
    }
    
    public Point3D getPosition() {
        return position;
    }

    public void drawTree(GL3 gl, TriangleMesh treeMesh) {
        Point3D pos = getPosition();
        CoordFrame3D treeFrame = CoordFrame3D.identity().translate(pos.getX(), pos.getY() + TREEHEIGHT, pos.getZ()).scale(0.5f, 0.5f, 0.5f);
        treeMesh.draw(gl, treeFrame);
    }


}
