package main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.Random;

public class SistemaSolarPanel extends JPanel implements Runnable,
        MouseWheelListener, MouseMotionListener {

    private static final int NUM_PLANETAS = 9;

    // Ángulos
    private float[] angulo = new float[NUM_PLANETAS];

    // Periodos reales
    private double[] periodoDias = {
            88, 225, 365, 687, 4333, 10759, 30687, 60190, 90560
    };

    // Velocidades angulares
    private float[] velocidades = new float[NUM_PLANETAS];

    // Distancias base
    private int[] distanciasBase = {70, 90, 110, 140, 180, 220, 260, 300, 340};

    // Tamaños de planetas
    private int[] tamanos = {12, 16, 18, 14, 40, 32, 26, 26, 14};

    private String[] nombres = {
            "Mercurio", "Venus", "Tierra", "Marte",
            "Júpiter", "Saturno", "Urano", "Neptuno", "Plutón"
    };

    private Color[] planetColors = {
            new Color(168, 168, 168),
            new Color(230, 180, 30),
            new Color(70, 130, 255),
            new Color(255, 90, 70),
            new Color(240, 150, 30),
            new Color(210, 200, 150),
            new Color(140, 255, 255),
            new Color(70, 100, 255),
            new Color(200, 200, 200)
    };

    // Atmósferas
    private boolean[] tieneAtmosfera = {
            false, true, true, true,
            false, false, true, true, false
    };

    // Estrellas
    private int starCount = 250;
    private int[] starX = new int[starCount];
    private int[] starY = new int[starCount];
    private float[] starPhase = new float[starCount];
    private float[] starSpeed = new float[starCount];

    // Asteroides
    private int numAsteroides = 300;
    private double[] astAngle = new double[numAsteroides];
    private int[] astRadio = new int[numAsteroides];
    private float[] astVelocidad = new float[numAsteroides];

    // Días y vueltas completas
    private float[] dias = new float[NUM_PLANETAS];
    private int[] vueltasCompletas = new int[NUM_PLANETAS];

    // Control
    private float velocidadGlobal = 0.5f;
    private boolean pausado = false;

    private float zoomTarget = 0;
    private float zoomActual = 0;

    private float inclinacion = 0.7f;

    private int mouseX = -1000, mouseY = -1000;
    private int hoveredPlanet = -1;

    public SistemaSolarPanel() {

        setBackground(Color.BLACK);
        setDoubleBuffered(true);

        Random r = new Random();

        for (int i = 0; i < starCount; i++) {
            starX[i] = r.nextInt(2000);
            starY[i] = r.nextInt(2000);
            starPhase[i] = r.nextFloat() * (float)Math.PI * 2;
            starSpeed[i] = 0.01f + r.nextFloat() * 0.04f;
        }

        for (int i = 0; i < numAsteroides; i++) {
            astAngle[i] = r.nextDouble() * 360;
            astRadio[i] = 160 + r.nextInt(60);
            astVelocidad[i] = 0.1f + r.nextFloat() * 0.2f;
        }

        for (int i = 0; i < NUM_PLANETAS; i++)
            velocidades[i] = (float)(360.0 / periodoDias[i]);

        Thread t = new Thread(this);
        t.start();

        addMouseWheelListener(this);
        addMouseMotionListener(this);
        setFocusable(true);
    }

    // ---------------- CONTROLES ----------------

    public void togglePause() { pausado = !pausado; }

    public void cambiarVelocidad(float delta) {
        velocidadGlobal = Math.max(0.1f, Math.min(velocidadGlobal + delta, 20f));
    }

    public void cambiarZoom(int delta) {
        zoomTarget = Math.max(-100, Math.min(zoomTarget + delta, 300));
    }

    public void resetVista() {
        zoomTarget = 0;
        velocidadGlobal = 0.5f;
    }

    public void reiniciarSimulacion() {
        for (int i = 0; i < NUM_PLANETAS; i++) {
            angulo[i] = 0;
            vueltasCompletas[i] = 0;
            dias[i] = 0;
        }
        zoomTarget = zoomActual = 0;
        velocidadGlobal = 0.5f;
        repaint();
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        cambiarZoom(e.getWheelRotation() * 10);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
        actualizarHoveredPlanet();
        repaint();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        mouseMoved(e);
    }

    private void actualizarHoveredPlanet() {
        int cx = getWidth() / 2;
        int cy = getHeight() / 2;

        hoveredPlanet = -1;

        for (int i = 0; i < NUM_PLANETAS; i++) {

            int dist = distanciasBase[i] + (int)zoomActual;
            double rad = Math.toRadians(angulo[i]);

            int x = cx + (int)(Math.cos(rad) * dist);
            int y = cy + (int)(Math.sin(rad) * dist * inclinacion);

            int s = tamanos[i];

            double dx = mouseX - x;
            double dy = mouseY - y;

            if (dx * dx + dy * dy <= (s + 6) * (s + 6))
                hoveredPlanet = i;
        }
    }

    // ---------------- DIBUJO ----------------

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        paintStars(g2);
        paintSun(g2);

        int cx = getWidth() / 2;
        int cy = getHeight() / 2;

        paintOrbits(g2, cx, cy);
        paintAsteroids(g2, cx, cy);
        paintPlanets(g2, cx, cy);
        paintHUD(g2);

        if (hoveredPlanet != -1)
            paintTooltip(g2, hoveredPlanet, mouseX, mouseY);
    }

    private void paintStars(Graphics2D g2) {
        for (int i = 0; i < starCount; i++) {
            int x = starX[i] % getWidth();
            int y = starY[i] % getHeight();

            float brillo = (float)(0.5 + 0.5*Math.sin(starPhase[i]));
            int alpha = (int)(80 + brillo * 175);

            g2.setColor(new Color(255,255,255,alpha));
            g2.fillRect(x,y,2,2);
        }
    }

    private void paintSun(Graphics2D g2) {
        int cx = getWidth() / 2;
        int cy = getHeight() / 2;

        g2.setColor(Color.YELLOW);
        g2.fillOval(cx - 25, cy - 25, 50, 50);
    }

    private void paintOrbits(Graphics2D g2, int cx, int cy) {
        g2.setColor(new Color(150,150,150,120));
        for (int d0 : distanciasBase) {
            int d = d0 + (int)zoomActual;
            g2.drawOval(cx - d, cy - (int)(d * inclinacion), d * 2, (int)(d * 2 * inclinacion));
        }
    }

    private void paintAsteroids(Graphics2D g2, int cx, int cy) {
        g2.setColor(new Color(200,200,200,160));

        for (int i = 0; i < numAsteroides; i++) {
            int dist = astRadio[i] + (int)zoomActual;
            double a = Math.toRadians(astAngle[i]);

            int x = cx + (int)(Math.cos(a) * dist);
            int y = cy + (int)(Math.sin(a) * dist * inclinacion);

            g2.fillRect(x,y,2,2);
        }
    }

    private void paintPlanets(Graphics2D g2, int cx, int cy) {

        for (int i = 0; i < NUM_PLANETAS; i++) {

            int dist = distanciasBase[i] + (int)zoomActual;
            double ang = Math.toRadians(angulo[i]);

            int x = cx + (int)(Math.cos(ang) * dist);
            int y = cy + (int)(Math.sin(ang) * dist * inclinacion);

            int s = tamanos[i];

            // Cuerpo del planeta
            g2.setColor(planetColors[i]);
            g2.fillOval(x - s/2, y - s/2, s, s);

            // Atmósfera
            if (tieneAtmosfera[i]) {
                Color c = planetColors[i];
                g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 60));
                g2.drawOval(x - s/2 - 3, y - s/2 - 3, s + 6, s + 6);
            }

            // Anillos de Saturno
            if (i == 5) {
                int rw = s * 3;
                int rh = (int)(s * 1.4);
                g2.setColor(new Color(230,230,180,150));
                g2.fillOval(x - rw/2, y - rh/2, rw, rh);
            }

            g2.setColor(Color.WHITE);
            g2.drawString(nombres[i], x + s/2 + 4, y);
        }
    }

    private void paintHUD(Graphics2D g2) {
        g2.setColor(Color.WHITE);
        g2.drawString("Velocidad: " + velocidadGlobal + "x", 10, 20);

        int px = getWidth() - 240;
        g2.drawString("Tiempo por planeta:", px, 20);

        for (int i = 0; i < NUM_PLANETAS; i++) {

            int d = (int)dias[i];
            int anios = vueltasCompletas[i];

            g2.drawString(
                    nombres[i] + ": " + d + " días (" + anios + " años)",
                    px,
                    40 + i * 18
            );
        }
    }

    private void paintTooltip(Graphics2D g2, int i, int mx, int my) {

        String l1 = "Planeta: " + nombres[i];
        String l2 = "Periodo orbital: " + (int)periodoDias[i] + " días";
        String l3 = "Tiempo: " + (int)dias[i] + " días (" + vueltasCompletas[i] + " años)";

        FontMetrics fm = g2.getFontMetrics();

        int w = Math.max(Math.max(fm.stringWidth(l1), fm.stringWidth(l2)), fm.stringWidth(l3)) + 10;
        int h = fm.getHeight() * 3 + 10;

        int x = mx + 15;
        int y = my - h - 5;

        if (x + w > getWidth()) x = getWidth() - w - 5;
        if (y < 5) y = 5;

        g2.setColor(new Color(0,0,0,200));
        g2.fillRoundRect(x,y,w,h,10,10);

        g2.setColor(Color.WHITE);
        g2.drawRoundRect(x,y,w,h,10,10);

        int tx = x + 5;
        int ty = y + fm.getAscent() + 5;

        g2.drawString(l1, tx, ty);
        g2.drawString(l2, tx, ty + fm.getHeight());
        g2.drawString(l3, tx, ty + fm.getHeight() * 2);
    }

    // ---------------- LÓGICA ----------------

    @Override
    public void run() {

        while (true) {

            if (!pausado)
                actualizar();

            repaint();

            try { Thread.sleep(16); } catch (Exception ignored) {}
        }
    }

    private void actualizar() {

        zoomActual += (zoomTarget - zoomActual) * 0.15f;

        // Estrellas
        for (int i = 0; i < starCount; i++)
            starPhase[i] += starSpeed[i] * velocidadGlobal;

        // Asteroides
        for (int i = 0; i < numAsteroides; i++) {
            astAngle[i] += astVelocidad[i] * velocidadGlobal * 0.2f;
            if (astAngle[i] >= 360) astAngle[i] = 0;
        }

        // Planetas
        for (int i = 0; i < NUM_PLANETAS; i++) {

            float previo = angulo[i];

            angulo[i] += velocidades[i] * velocidadGlobal;
            if (angulo[i] >= 360) angulo[i] -= 360;

            // Detectar vuelta completa
            if (previo > angulo[i]) {
                vueltasCompletas[i]++;
                dias[i] = vueltasCompletas[i] * (float)periodoDias[i];
            }
        }
    }
}
