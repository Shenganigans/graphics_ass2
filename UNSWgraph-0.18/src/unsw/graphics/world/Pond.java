package unsw.graphics.world;

import java.util.ArrayList;
import java.util.List;

import unsw.graphics.geometry.Point2D;
import unsw.graphics.geometry.Point3D;
import unsw.graphics.geometry.TriangleMesh;

public class Pond {
	private Point3D middle;
	private float radius;
	private TriangleMesh pondMesh;
	
	public Pond(float radius, Point3D middle) {
        this.radius = radius;
        this.middle = middle;
        Point3D tl = middle.translate(-radius,0.01f,radius);
        Point3D tr = middle.translate(radius,0.01f,radius);
        Point3D bl = middle.translate(-radius,0.01f,-radius);
        Point3D br = middle.translate(radius,0.01f,-radius);
        
        List<Point3D> v = new ArrayList<>();
        v.add(tl);
        v.add(tr);
        v.add(bl);
        v.add(br);
        List<Integer> indicies = new ArrayList<>();
        indicies.add(1);
        indicies.add(2);
        indicies.add(0);
        indicies.add(3);
        indicies.add(2);
        indicies.add(1);
        List<Point2D> texCoords = new ArrayList<>();
        texCoords.add(new Point2D(0,1));
        texCoords.add(new Point2D(1,1));
        texCoords.add(new Point2D(0,0));
        texCoords.add(new Point2D(1,0));
        
        pondMesh = new TriangleMesh(v, indicies, true, texCoords);
        
        
    }
	public TriangleMesh getMesh(Pond p) {
		return p.pondMesh;
	}
}
