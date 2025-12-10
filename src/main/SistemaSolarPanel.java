package main;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.gl2.GLUT;
import com.jogamp.opengl.glu.GLU;

// 🔹 NUEVOS IMPORTS PARA TEXTURAS
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

import java.awt.event.*;

public class SistemaSolarPanel extends GLJPanel implements GLEventListener,
        MouseWheelListener, MouseMotionListener {

    private static final int NUM_PLANETAS = 9;

    private float[] angOrbita = new float[NUM_PLANETAS];
    private float[] angRotacion = new float[NUM_PLANETAS];

    private double[] periodoDias = {88, 225, 365, 687, 4333, 10759, 30687, 60190, 90560};
    private float[] velOrbita = new float[NUM_PLANETAS];
    private float[] velRotacion = {3, 2, 2.5f, 2, 1.5f, 1.2f, 1, 0.9f, 0.5f};

    // Distancias más separadas
    private int[] distancias = {90, 130, 170, 210, 260, 320, 380, 450, 520};
    private int[] tamanos = {12, 16, 18, 16, 32, 28, 24, 24, 14};

    private String[] nombres = {
            "MERCURIO", "VENUS", "TIERRA", "MARTE",
            "JÚPITER", "SATURNO", "URANO", "NEPTUNO", "PLUTÓN"
    };

    private float[][] colors = {
            {0.6f, 0.6f, 0.6f},
            {1f, 0.7f, 0.1f},
            {0.2f, 0.5f, 1f},
            {1f, 0.2f, 0.2f},
            {0.9f, 0.6f, 0.1f},
            {0.9f, 0.9f, 0.5f},
            {0.4f, 1f, 1f},
            {0.3f, 0.4f, 1f},
            {0.8f, 0.8f, 0.8f}
    };

    private float velocidadGlobal = 0.5f;
    private boolean pausado = false;

    private float zoom = -850f;
    private float zoomTarget = -850f;

    private float diasTotales = 0;
    private int[] anos = new int[NUM_PLANETAS];

    private GLUT glut = new GLUT();

    private Texture fondoEspacio;

    public SistemaSolarPanel() {
        super(new GLCapabilities(GLProfile.get(GLProfile.GL2)));

        for (int i = 0; i < NUM_PLANETAS; i++)
            velOrbita[i] = (float)(360.0 / periodoDias[i]);

        addGLEventListener(this);
        addMouseWheelListener(this);
        addMouseMotionListener(this);
        setFocusable(true);

        FPSAnimator anim = new FPSAnimator(this, 60);
        anim.start();
    }

    //──────────────────────── CONTROLES ────────────────────────

    public void togglePause() { pausado = !pausado; }

    public void cambiarVelocidad(float delta) {
        velocidadGlobal = Math.max(0.1f, Math.min(velocidadGlobal + delta, 20f));
    }

    public void cambiarZoom(int delta) { zoomTarget += delta; }

    public void resetVista() {
        zoomTarget = -850f;
        velocidadGlobal = 0.5f;
    }

    public void reiniciarSimulacion() {
        diasTotales = 0;
        for (int i = 0; i < NUM_PLANETAS; i++) {
            angOrbita[i] = 0;
            angRotacion[i] = 0;
            anos[i] = 0;
        }
        zoom = zoomTarget = -850f;
    }

   
    @Override
    public void init(GLAutoDrawable d) {
        GL2 gl = d.getGL().getGL2();

        gl.glEnable(GL.GL_BLEND);
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

        gl.glEnable(GL.GL_DEPTH_TEST);
        gl.glClearColor(0f, 0f, 0f, 1f);

        try {
            fondoEspacio = TextureIO.newTexture(
                    getClass().getResourceAsStream("/texturas/2k_stars.jpg"),
                    true,
                    TextureIO.JPG
            );
        } catch (Exception e) {
            System.out.println("Error cargando textura de fondo: " + e.getMessage());
        }
    }

    @Override public void dispose(GLAutoDrawable d) {}

    @Override
    public void display(GLAutoDrawable d) {

        actualizarMovimiento();

        GL2 gl = d.getGL().getGL2();
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
        gl.glLoadIdentity();

        if (fondoEspacio != null) {
            fondoEspacio.enable(gl);
            fondoEspacio.bind(gl);

            gl.glPushMatrix();
            gl.glTranslatef(0f, 0f, -3000f); // Muy al fondo

            gl.glBegin(GL2.GL_QUADS);
            gl.glTexCoord2f(0f, 0f); gl.glVertex3f(-3000f, -2000f, 0f);
            gl.glTexCoord2f(1f, 0f); gl.glVertex3f( 3000f, -2000f, 0f);
            gl.glTexCoord2f(1f, 1f); gl.glVertex3f( 3000f,  2000f, 0f);
            gl.glTexCoord2f(0f, 1f); gl.glVertex3f(-3000f,  2000f, 0f);
            gl.glEnd();

            gl.glPopMatrix();

            fondoEspacio.disable(gl);
        }

        zoom += (zoomTarget - zoom) * 0.15f;
        gl.glTranslatef(0, 0, zoom);

        gl.glColor3f(1f, 1f, 0.2f);
        dibujarCirculo(gl, 0, 0, 45);

        //───────── ÓRBITAS
        gl.glColor4f(0.7f, 0.7f, 0.7f, 0.4f);
        for (int i = 0; i < NUM_PLANETAS; i++)
            dibujarElipse(gl, distancias[i] * 2, distancias[i] * 1.5f);

        //───────── PLANETAS
        for (int i = 0; i < NUM_PLANETAS; i++) {

            float ang = (float)Math.toRadians(angOrbita[i]);

            float x = (float)(Math.cos(ang) * distancias[i]);
            float y = (float)(Math.sin(ang) * distancias[i] * 0.75f);

            gl.glPushMatrix();
            gl.glTranslatef(x, y, 0);
            gl.glRotatef(angRotacion[i], 0, 0, 1);

            gl.glColor3f(colors[i][0], colors[i][1], colors[i][2]);
            dibujarCirculo(gl, 0, 0, tamanos[i]);

            gl.glPopMatrix();

            // NOMBRE ENCIMA DEL PLANETA
            gl.glColor3f(1, 1, 1);
            dibujarTextoGrande(gl, x - 15, y + tamanos[i] + 12, nombres[i]);
        }

       
        gl.glColor3f(1, 1, 1);

        dibujarTextoXL(gl, -400,300, "VELOCIDAD: " + velocidadGlobal + "x");

        dibujarTextoXL(gl, 300,320, "DIAS TOTALES: " + (int)diasTotales);

        int yList = 340;

        dibujarTextoXL(gl, 440, yList, "VUELTAS AL SOL");
        yList -= 30;

        for (int i = 0; i < NUM_PLANETAS; i++) {
            dibujarTextoXL(gl, 440, yList, nombres[i] + ": " + anos[i] + " AÑOS");
            yList -= 25;
        }
    }

    @Override
    public void reshape(GLAutoDrawable d, int x, int y, int w, int h) {

        GL2 gl = d.getGL().getGL2();
        GLU glu = new GLU();

        if (h == 0) h = 1;
        float aspect = (float) w / h;

        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();

        // ZOOM REAL EN 3D
        glu.gluPerspective(45.0, aspect, 1.0, 5000.0);

        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();
    }

    private void actualizarMovimiento() {
        if (pausado) return;

        diasTotales += velocidadGlobal;

        for (int i = 0; i < NUM_PLANETAS; i++) {

            float prev = angOrbita[i];
            angOrbita[i] += velOrbita[i] * velocidadGlobal;
            angRotacion[i] += velRotacion[i] * velocidadGlobal;

            if (angOrbita[i] >= 360) angOrbita[i] -= 360;
            if (angRotacion[i] >= 360) angRotacion[i] -= 360;

            if (prev > angOrbita[i])
                anos[i]++;
        }
    }

  
    private void dibujarCirculo(GL2 gl, float cx, float cy, float r) {
        gl.glBegin(GL2.GL_TRIANGLE_FAN);
        gl.glVertex2f(cx, cy);
        for (int i = 0; i <= 40; i++) {
            double ang = 2 * Math.PI * i / 40;
            gl.glVertex2f(cx + (float)Math.cos(ang) * r,
                    cy + (float)Math.sin(ang) * r);
        }
        gl.glEnd();
    }

    private void dibujarElipse(GL2 gl, float w, float h) {
        gl.glBegin(GL2.GL_LINE_LOOP);
        for (int i = 0; i <= 120; i++) {
            double ang = 2 * Math.PI * i / 120;
            gl.glVertex2f((float)Math.cos(ang) * w / 2f,
                    (float)Math.sin(ang) * h / 2f);
        }
        gl.glEnd();
    }

    private void dibujarTextoGrande(GL2 gl, float x, float y, String t) {
        gl.glRasterPos2f(x, y);
        for (char c : t.toCharArray())
            glut.glutBitmapCharacter(GLUT.BITMAP_HELVETICA_18, c);
    }

    private void dibujarTextoXL(GL2 gl, float x, float y, String t) {
        gl.glRasterPos2f(x, y);
        for (char c : t.toCharArray())
            glut.glutBitmapCharacter(GLUT.BITMAP_HELVETICA_18, c);
    }

    @Override public void mouseWheelMoved(MouseWheelEvent e) { cambiarZoom(e.getWheelRotation() * 40); }
    @Override public void mouseMoved(MouseEvent e) {}
    @Override public void mouseDragged(MouseEvent e) {}
}
