package unsw.graphics.world;



import java.util.ArrayList;
import java.util.List;

import unsw.graphics.Vector3;
import unsw.graphics.geometry.Point2D;

/**
 * COMMENT: Comment HeightMap 
 *
 * @author malcolmr
 */
public class Terrain {

    private int width;
    private int depth;
    private float[][] altitudes;
    private List<Tree> trees;
    private List<Road> roads;
    private Vector3 sunlight;

    /**
     * Create a new terrain
     *
     * @param width The number of vertices in the x-direction
     * @param depth The number of vertices in the z-direction
     */
    public Terrain(int width, int depth, Vector3 sunlight) {
        this.width = width;
        this.depth = depth;
        altitudes = new float[width][depth];
        trees = new ArrayList<Tree>();
        roads = new ArrayList<Road>();
        this.sunlight = sunlight;
    }

    public List<Tree> trees() {
        return trees;
    }

    public List<Road> roads() {
        return roads;
    }

    public Vector3 getSunlight() {
        return sunlight;
    }

    /**
     * Set the sunlight direction. 
     * 
     * Note: the sun should be treated as a directional light, without a position
     * 
     * @param dx
     * @param dy
     * @param dz
     */
    public void setSunlightDir(float dx, float dy, float dz) {
        sunlight = new Vector3(dx, dy, dz);      
    }

    /**
     * Get the altitude at a grid point
     * 
     * @param x
     * @param z
     * @return
     */
    public double getGridAltitude(int x, int z) {
        return altitudes[x][z];
    }

    /**
     * Set the altitude at a grid point
     * 
     * @param x
     * @param z
     * @return
     */
    public void setGridAltitude(int x, int z, float h) {
        altitudes[x][z] = h;
    }

    /**
     * Get the altitude at an arbitrary point. 
     * Non-integer points should be interpolated from neighbouring grid points
     * 
     * @param x
     * @param z
     * @return
     */
    public float altitude(float x, float z) {
        
        if (x % 1 == 0 && z % 1 == 0) {
        	return (float) getGridAltitude((int)x, (int)z);
        }else { //if(x % 1 != 0 && z % 1 != 0) 
	        double z1 = Math.floor(z);
	        double z2 = Math.ceil(z);
	        double x1 = Math.floor(x);
	        double x2 = Math.ceil(x);
	        double q2 = getGridAltitude((int)x1, (int)z2);
	        double q1 = getGridAltitude((int)x1, (int)z1);
	        double q3 = getGridAltitude((int)x2, (int)z1);
	        double q4 = getGridAltitude((int)x2, (int)z2);
	        
	        double r1 = (((z-z1)/(z2-z1))*q2)+(((z2-z)/(z2-z1))*q1);
	        double r2 = (((z-z1)/(z2-z1))*q4)+(((z2-z)/(z2-z1))*q3);
	        
	        double p = (((x-x1)/(x2-x1))*r2)+(((x2-x)/(x2-x1))*r1);
	        return (float) p;
        }
    }

    /**
     * Add a tree at the specified (x,z) point. 
     * The tree's y coordinate is calculated from the altitude of the terrain at that point.
     * 
     * @param x
     * @param z
     */
    public void addTree(float x, float z) {
        float y = altitude(x, z);
        Tree tree = new Tree(x, y, z);
        trees.add(tree);
    }


    /**
     * Add a road. 
     * 
     * @param x
     * @param z
     */
    public void addRoad(float width, List<Point2D> spine) {
        Road road = new Road(width, spine);
        roads.add(road);        
    }
    
    public int getWidth() {
    	return width;
    }
    public int getDepth() {
    	return depth;
    }

}
