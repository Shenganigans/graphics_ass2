package unsw.graphics.world;

import java.awt.Color;
import java.awt.List;
import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;

import com.jogamp.newt.event.MouseEvent;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.util.GLBuffers;

import unsw.graphics.Application3D;
import unsw.graphics.CoordFrame2D;
import unsw.graphics.CoordFrame3D;
import unsw.graphics.Matrix4;
import unsw.graphics.Point3DBuffer;
import unsw.graphics.Shader;
import unsw.graphics.Texture;
import unsw.graphics.examples.person.Camera;
import unsw.graphics.geometry.Point3D;
import unsw.graphics.geometry.TriangleMesh;



/**
 * COMMENT: Comment Game
 *
 * @author malcolmr
 */
public class World extends Application3D implements KeyListener {

    private Terrain terrain;
    private Point3DBuffer vertexBuffer;
    private IntBuffer indicesBuffer;
    private int verticesName;
    private int indicesName;
    private TriangleMesh terrainMesh;
    float rotationY = 0;
    float rotationX = 0;
    static ArrayList<Tree> allTrees;
    private final static float TREEHEIGHT = 2.5f;
    private TriangleMesh treeMesh;
    private Camera3D camera;
    private boolean useCamera;
    Texture texture;
    public World(Terrain terrain) {
        super("Assignment 2", 800, 600);
        this.terrain = terrain;
        camera = new Camera3D(terrain);

    }

    /**
     * Load a level file and display it.
     *
     * @param args - The first argument is a level file in JSON format
     * @throws FileNotFoundException
     */
    public static void main(String[] args) throws FileNotFoundException {
        Terrain terrain = LevelIO.load(new File(args[0]));
        World world = new World(terrain);
        world.start();
    }

    @Override
    public void display(GL3 gl) {
        super.display(gl);
        CoordFrame3D frame;
        Shader.setInt(gl, "tex", 0);
        
        gl.glActiveTexture(GL.GL_TEXTURE0);
        gl.glBindTexture(GL.GL_TEXTURE_2D, texture.getId());

        Shader.setPenColor(gl, Color.WHITE);
        Shader.setPoint3D(gl, "lightPos", terrain.getSunlight().asPoint3D());

//         set other light stuff
        Shader.setColor(gl, "lightIntensity", Color.WHITE);
        Shader.setColor(gl, "ambientIntensity", new Color(0.7f, 0.7f, 0.7f));
        Shader.setColor(gl, "ambientCoeff", Color.WHITE);
        Shader.setColor(gl, "diffuseCoeff", new Color(0.5f, 0.5f, 0.5f));
        Shader.setFloat(gl, "phongExp", 1f);

        if (!useCamera) {
            // Bring everything into view by scaling down the world
            Shader.setPenColor(gl, Color.WHITE);
            CoordFrame3D frame1 = CoordFrame3D.identity()
                    .translate(-2, -2f, -9)
                    .scale(0.5f, 0.5f, 0.5f);
            drawTerrain(gl, frame1);
        } else {
            // Use a camera instead
            camera.setView(gl);
            frame = CoordFrame3D.identity();
            drawTerrain(gl, frame);
        }

        //rotationY += 1;
    }

    private void drawTerrain(GL3 gl, CoordFrame3D frame) {
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, verticesName);
        gl.glVertexAttribPointer(Shader.POSITION, 3, GL.GL_FLOAT, false, 0, 0);

        gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, indicesName);

        Shader.setModelMatrix(gl, frame.getMatrix());
        gl.glDrawElements(GL.GL_TRIANGLES, indicesBuffer.capacity(),
                GL.GL_UNSIGNED_INT, 0);

        for (Tree t: terrain.trees()) {
            Shader.setPenColor(gl, Color.BLACK);
            Point3D pos = t.getPosition();
            CoordFrame3D treeFrame = CoordFrame3D.identity().translate(pos.getX(), pos.getY() + TREEHEIGHT, pos.getZ()).scale(0.5f, 0.5f, 0.5f);
            treeMesh.draw(gl, treeFrame);
        }
    }

    @Override
    public void destroy(GL3 gl) {
        super.destroy(gl);
        gl.glDeleteBuffers(2, new int[] { indicesName, verticesName }, 0);
        terrainMesh.destroy(gl);
        treeMesh.destroy(gl);
    }

    @Override
    public void init(GL3 gl) {
        super.init(gl);
        getWindow().addKeyListener(this);
        getWindow().addKeyListener(camera);

        // shader
        Shader shader = new Shader(gl, "shaders/vertex_sunlight.glsl",
                "shaders/fragment_sunlight.glsl");
//        Shader shader = new Shader(gl, "shaders/vertex_tex_3d.glsl",
//                "shaders/fragment_tex_3d.glsl");
        shader.use(gl);

        // terrain
        int i = 0;
        int j = 0;
        ArrayList<Point3D> points = new ArrayList<Point3D>();
        for (j = 0;j<this.terrain.getDepth();j++) {
            for(i = 0;i<this.terrain.getWidth();i++) {
                points.add(new Point3D(i,terrain.altitude(i, j), j));
            }
        }
        vertexBuffer = new Point3DBuffer(points);

        ArrayList<Integer> indexes = new ArrayList<Integer>();
        for (j=0;j<this.terrain.getDepth()-1;j++) {
            for(i=1;i<this.terrain.getWidth();i++) {
                indexes.add(i+j*terrain.getDepth());
                indexes.add(i+j*terrain.getDepth()-1);
                indexes.add(i+(j+1)*terrain.getDepth()-1);

                indexes.add(i+j*terrain.getDepth());
                indexes.add(i+(j+1)*terrain.getDepth()-1);
                indexes.add(i+(j+1)*terrain.getDepth());
                
            }
        }
        int[] arr = new int[indexes.size()];
        int count = 0;
        for (Integer n : indexes){
            arr[count++] = n.intValue();
        }

        indicesBuffer = GLBuffers.newDirectIntBuffer(arr);

        int[] names = new int[2];
        gl.glGenBuffers(2, names, 0);

        verticesName = names[0];
        indicesName = names[1];
        //gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL3.GL_LINE);

        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, verticesName);
        gl.glBufferData(GL.GL_ARRAY_BUFFER, vertexBuffer.capacity() * 3 * Float.BYTES,
                vertexBuffer.getBuffer(), GL.GL_STATIC_DRAW);

        gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, indicesName);
        gl.glBufferData(GL.GL_ELEMENT_ARRAY_BUFFER, indicesBuffer.capacity() * Integer.BYTES,
                indicesBuffer, GL.GL_STATIC_DRAW);

        terrainMesh = new TriangleMesh(points, indexes, true);
        terrainMesh.init(gl);

        // tree
        try {
            treeMesh = new TriangleMesh("res/models/tree.ply", true, true);
        } catch (IOException e) {
            System.out.println("Tree Mesh failed to load :(");
            System.out.println(e.toString());
        }
        treeMesh.init(gl);
        
        texture = new Texture(gl, "res/textures/grass.bmp", "bmp", false);
    }

    @Override
    public void reshape(GL3 gl, int width, int height) {
        super.reshape(gl, width, height);
        Shader.setProjMatrix(gl, Matrix4.perspective(60, width/(float)height, 0.01f, 100));
    }

    @Override

    public void keyPressed(KeyEvent e) {
        switch(e.getKeyCode()) {

            case KeyEvent.VK_SPACE:
                useCamera ^= true;
                break;
        }


    }

    @Override
    public void keyReleased(KeyEvent e) {
        // TODO Auto-generated method stub

    }


}
