package unsw.graphics.world;

import java.util.ArrayList;
import java.util.List;

import unsw.graphics.Matrix4;
import unsw.graphics.Vector3;
import unsw.graphics.Vector4;
import unsw.graphics.geometry.Point2D;
import unsw.graphics.geometry.Point3D;
import unsw.graphics.geometry.TriangleMesh;

/**
 * COMMENT: Comment Road 
 *
 * @author malcolmr
 */
public class Road {

    private List<Point2D> points;
    private float width;
    private Point3D point;
    
    /**
     * Create a new road with the specified spine 
     *
     * @param width
     * @param spine
     */
    public Road(float width, List<Point2D> spine) {
        this.width = width;
        this.points = spine;
    }

    /**
     * The width of the road.
     * 
     * @return
     */
    public double width() {
        return width;
    }
    
    /**
     * Get the number of segments in the curve
     * 
     * @return
     */
    public int size() {
        return points.size() / 3;
    }

    /**
     * Get the specified control point.
     * 
     * @param i
     * @return
     */
    public Point2D controlPoint(int i) {
        return points.get(i);
    }
   public TriangleMesh createRoad(Road r, float altitude, int num) {
	   ArrayList<Point3D> mainRoad = new ArrayList<Point3D>();
	   ArrayList<Point3D> abovePoints = new ArrayList<Point3D>();
	   ArrayList<Point3D> belowPoints = new ArrayList<Point3D>();
	   ArrayList<Point3D> allPoints = new ArrayList<Point3D>();
	   ArrayList<Integer> index = new ArrayList<Integer>();
	   ArrayList<Point2D> texCoords = new ArrayList<Point2D>();
	   
	   float dt = 1.0f/32;
	   if(point != null) {
		   mainRoad.add(point);
	   }
	   for(int i = 0; i<32; i++) {
		   float t = i*dt;
		   mainRoad.add(new Point3D(point(t+num).getX(), altitude , point(t+num).getY()));
	   }
	   point = mainRoad.get(mainRoad.size()-1);
	   for(int i = 0; i<=mainRoad.size()-1; i++) {
		   Point3D a;
		   Point3D b;
		   Vector3 c;
		   Matrix4 rot90 = Matrix4.rotationY(90);
		   Matrix4 rot270 = Matrix4.rotationY(270);
		   if(i == mainRoad.size()-1) {
			   a = mainRoad.get(i);
			   b = mainRoad.get(i-1);
			   c = a.minus(b).normalize().scale(width/2);
		   }else {
			   a = mainRoad.get(i+1);
			   b = mainRoad.get(i);
			   c = a.minus(b).normalize().scale(width/2);
		   }
		   Vector4 d = new Vector4(c.getX(),c.getY(),c.getZ(),0);
		   
		   abovePoints.add(mainRoad.get(i).translate(rot90.multiply(d).trim()));
		   texCoords.add(new Point2D(mainRoad.get(i).translate(rot90.multiply(d).trim()).getX(),mainRoad.get(i).translate(rot90.multiply(d).trim()).getZ()));	        
		   belowPoints.add(mainRoad.get(i).translate(rot270.multiply(d).trim()));
		   texCoords.add(new Point2D(mainRoad.get(i).translate(rot270.multiply(d).trim()).getX(),mainRoad.get(i).translate(rot270.multiply(d).trim()).getZ()));
	   }
	   allPoints.addAll(abovePoints);
	   allPoints.addAll(belowPoints);
	   for(int j = 0; j<mainRoad.size()-1;j++) {
		   int tl = j;
		   int tr = j+1;
		   int bl = j+mainRoad.size();
		   int br = j+mainRoad.size()+1;
		   
		   index.add(tr);
		   index.add(tl);
		   index.add(bl);
		   index.add(tr);
		   index.add(bl);
		   index.add(br);
	   }
	   
	   
	   TriangleMesh tm = new TriangleMesh(allPoints, index, true, texCoords);
	   return tm;
    }
    /**
     * Get a point on the spine. The parameter t may vary from 0 to size().
     * Points on the kth segment take have parameters in the range (k, k+1).
     * 
     * @param t
     * @return
     */
 
    public Point2D point(float t) {
        int i = (int)Math.floor(t);
        t = t - i;
        
        i *= 3;
        
        Point2D p0 = points.get(i++);
        Point2D p1 = points.get(i++);
        Point2D p2 = points.get(i++);
        Point2D p3 = points.get(i++);
        

        float x = b(0, t) * p0.getX() + b(1, t) * p1.getX() + b(2, t) * p2.getX() + b(3, t) * p3.getX();
        float y = b(0, t) * p0.getY() + b(1, t) * p1.getY() + b(2, t) * p2.getY() + b(3, t) * p3.getY();        
        
        return new Point2D(x, y);
    }
    
    /**
     * Calculate the Bezier coefficients
     * 
     * @param i
     * @param t
     * @return
     */
    private float b(int i, float t) {
        
        switch(i) {
        
        case 0:
            return (1-t) * (1-t) * (1-t);

        case 1:
            return 3 * (1-t) * (1-t) * t;
            
        case 2:
            return 3 * (1-t) * t * t;

        case 3:
            return t * t * t;
        }
        
        // this should never happen
        throw new IllegalArgumentException("" + i);
    }


}
