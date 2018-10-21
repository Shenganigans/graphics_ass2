package unsw.graphics.world;

import java.awt.Color;
import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.IntBuffer;
import java.util.ArrayList;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.util.GLBuffers;

import unsw.graphics.Application3D;
import unsw.graphics.CoordFrame3D;
import unsw.graphics.Matrix4;
import unsw.graphics.Point3DBuffer;
import unsw.graphics.Shader;
import unsw.graphics.Texture;
import unsw.graphics.geometry.Point2D;
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
    private final static Color darkLightIntensity = new Color(0.3f, 0.3f, 0.3f);
    private TriangleMesh treeMesh;
    private TriangleMesh modelMesh;
    private ArrayList<TriangleMesh> roadMeshes = new ArrayList<TriangleMesh>();
    private Camera3D camera;
    private Camera3rD thirdCamera;
    private boolean useCamera;
    private boolean thirdPerson;
    private boolean nightTime;
    private boolean moveSun;
    private boolean morning;
    private Point3D startSun = new Point3D(-1, 0, 0);
    private float sunMovementX;
    private float sunMovementY;
    private Texture texture;
    private Texture texture2;
    private Texture texture3;
    private Avatar avatar;
    
    public World(Terrain terrain) {
        super("Assignment 2", 800, 600);
        this.terrain = terrain;
        camera = new Camera3D(terrain);
        thirdCamera = new Camera3rD(camera);
        avatar = new Avatar(camera.getLocation().getX(),camera.getLocation().getY(), camera.getLocation().getZ(), terrain);
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
        Shader.setInt(gl, "tex", 0);

        Shader.setPenColor(gl, Color.WHITE);

        // set lighting coordinates for sunlight
        Shader.setColor(gl, "lightIntensity", Color.WHITE);
        Shader.setColor(gl, "ambientIntensity", new Color(0.3f, 0.3f, 0.3f));
        Shader.setColor(gl, "ambientCoeff", Color.WHITE);
        Shader.setColor(gl, "diffuseCoeff", new Color(0.5f, 0.5f, 0.5f));
        Shader.setColor(gl, "specularCoeff", new Color(0.01f, 0.01f, 0.01f));
        Shader.setFloat(gl, "phongExp", 1f);

        Shader.setInt(gl, "torchOn", 0);

        Shader.setPenColor(gl, Color.WHITE);
        if (!useCamera) {
            // Bring everything into view by scaling down the world
            CoordFrame3D frame = CoordFrame3D.identity()
                    .translate(-2, -2, -9)
                    .scale(0.5f, 0.5f, 0.5f);
            Shader.setViewMatrix(gl, frame.getMatrix());
        } else {
        	if(!thirdPerson) {
	            camera.setView(gl);
        	} else {
	            thirdCamera.setView(gl);
        	}
        }

        if (nightTime) {
            Shader.setColor(gl, "lightIntensity", darkLightIntensity);
            Shader.setPoint3D(gl, "torchPos", camera.getLocation());
            Shader.setColor(gl, "torchAmbientIntensity", new Color(0.2f, 0.2f, 0.2f));
            Shader.setColor(gl, "torchLightIntensity", new Color(0.2f, 0.2f, 0.2f));
            Shader.setInt(gl,"torchOn", 1);
            double cutoff = 12.5f;
            Shader.setFloat(gl, "cutoff", (float) Math.cos(cutoff));
            Shader.setFloat(gl, "attenuation", 500); // set to lower for a brighter light

        }
        drawTerrain(gl, CoordFrame3D.identity());

        if (moveSun) {
            Point3D sunlightSpot = startSun.translate(sunMovementX, sunMovementY, 0);
            Shader.setPoint3D(gl, "lightPos", sunlightSpot);
            sunMovementX += 0.02;

            if (morning) sunMovementY += 0.01;
            else sunMovementY -= 0.01;

            if (sunMovementY > 1.0f) morning = false;

            if (sunMovementX > 4.0f) {
                sunMovementX = -1;
                sunMovementY = 0;
                morning = true;
            }
        } else {
            Shader.setPoint3D(gl, "lightPos", terrain.getSunlight().asPoint3D());
        }
    }

    private void drawTerrain(GL3 gl, CoordFrame3D frame) {

        gl.glActiveTexture(GL.GL_TEXTURE0);
        gl.glBindTexture(GL.GL_TEXTURE_2D, texture.getId());

        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, verticesName);
        gl.glVertexAttribPointer(Shader.POSITION, 3, GL.GL_FLOAT, false, 0, 0);

        gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, indicesName);

        terrainMesh.draw(gl, frame);

        gl.glBindTexture(GL.GL_TEXTURE_2D, texture3.getId());
        modelMesh.draw(gl, frame);
        CoordFrame3D aFrame = CoordFrame3D.identity()
        		.translate(avatar.getPosition().getX(), avatar.getPosition().getY()-0.2f, avatar.getPosition().getZ())
        		.rotateY(-90+avatar.getRotation());
        modelMesh.draw(gl, aFrame);

        gl.glBindTexture(GL.GL_TEXTURE_2D, texture2.getId());
        for (Tree t: terrain.trees()) {
            t.drawTree(gl, treeMesh);
        }
        for (TriangleMesh r: roadMeshes) {
        	r.draw(gl, frame);
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
        getWindow().addKeyListener(avatar);

        // shader
        Shader shader = new Shader(gl, "shaders/vertex_sunlight.glsl",
                "shaders/fragment_sunlight.glsl");
        shader.use(gl);
        Shader.setPoint3D(gl, "lightPos", terrain.getSunlight().asPoint3D());
        sunMovementX = 0.0f;
        sunMovementY = 0.0f;
        morning = true;

        // terrain
        int i = 0;
        int j = 0;
        ArrayList<Point3D> points = new ArrayList<Point3D>();
        ArrayList<Point2D> textureList = new ArrayList<Point2D>();
        for (j = 0;j<this.terrain.getDepth();j++) {
            for(i = 0;i<this.terrain.getWidth();i++) {
                points.add(new Point3D(i,terrain.altitude(i, j), j));
                textureList.add(new Point2D(i, j));
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
//        gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL3.GL_LINE);

        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, verticesName);
        gl.glBufferData(GL.GL_ARRAY_BUFFER, vertexBuffer.capacity() * 3 * Float.BYTES,
                vertexBuffer.getBuffer(), GL.GL_STATIC_DRAW);

        gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, indicesName);
        gl.glBufferData(GL.GL_ELEMENT_ARRAY_BUFFER, indicesBuffer.capacity() * Integer.BYTES,
                indicesBuffer, GL.GL_STATIC_DRAW);

        terrainMesh = new TriangleMesh(points, indexes, true, textureList);
        terrainMesh.init(gl);

        // tree
        try {
            treeMesh = new TriangleMesh("res/models/tree.ply", true, true);
        } catch (IOException e) {
            System.out.println("Tree Mesh failed to load :(");
            System.out.println(e.toString());
        }
        treeMesh.init(gl);

        //model
        try {
            modelMesh = new TriangleMesh("res/models/bunny.ply", true, true);
        } catch (IOException e) {
            System.out.println("Bunny Mesh failed to load :(");
            System.out.println(e.toString());
        }
        modelMesh.init(gl);
        
        texture = new Texture(gl, "res/textures/grass.bmp", "bmp", false);
        texture2 = new Texture(gl, "res/textures/rock.bmp", "bmp", false);
        texture3 = new Texture(gl, "res/textures/sky.bmp", "bmp", false);
        
        for (Road r: terrain.roads()) {
        	for(int w =0; w<r.size(); w++) {
	        	float altitude = terrain.altitude(r.controlPoint(0).getX(), r.controlPoint(0).getY());
	        	TriangleMesh roadMesh = r.createRoad(r, altitude+0.01f, w);
	        	roadMesh.init(gl);
	        	roadMeshes.add(roadMesh);
        	}
        }
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
            case KeyEvent.VK_V:
            	thirdPerson ^= true;
            	break;
            case KeyEvent.VK_N:
                nightTime ^= true;
                break;
            case KeyEvent.VK_S:
                moveSun ^= true;
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // TODO Auto-generated method stub
    }

}
