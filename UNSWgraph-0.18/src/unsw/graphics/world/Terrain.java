package unsw.graphics.world;



import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.util.GLBuffers;
import unsw.graphics.*;
import unsw.graphics.geometry.Point2D;
import unsw.graphics.geometry.Point3D;
import unsw.graphics.geometry.TriangleMesh;


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
        float altitude = 0;


        double xLerp = x % 1;
        double zLerp = z % 1;

        int x0 = (int) Math.floor(x), x1 = (int)Math.ceil(x);
        int z0 = (int) Math.floor(z), z1 = (int)Math.ceil(z);

        // constrain the dimensions.
        if (x0 < 0) x0 = 0;
        else if (x0 >= altitudes.length) x0 = altitudes.length - 1;

        if (x1 < 0) x1 = 0;
        else if (x1 >= altitudes.length) x1 = altitudes.length - 1;

        if (z0 < 0) z0 = 0;
        else if (z0 >= altitudes[x0].length) z0 = altitudes[x0].length - 1;

        if (z1 < 0) z1 = 0;
        else if (z1 >= altitudes[x1].length) z1 = altitudes[x1].length - 1;

        double alt00 = getGridAltitude(x0, z0);
        double alt01 = getGridAltitude(x0, z1);
        double alt10 = getGridAltitude(x1, z0);
        double alt11 = getGridAltitude(x1, z1);

        double lerp0 = linearInterpolate(alt00, alt10, xLerp);
        double lerp1 = linearInterpolate(alt01, alt11, xLerp);

        altitude = (float) linearInterpolate(lerp0, lerp1, zLerp);

        return altitude;
    }

    private static double linearInterpolate(double x0, double x1, double f) {
        return x0 + f * (x1 - x0);
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
     * @param width
     * @param spine
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
