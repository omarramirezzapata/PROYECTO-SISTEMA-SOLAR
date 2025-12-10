package main;

import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            JFrame ventana = new JFrame("Sistema Solar OpenGL Mejorado");
            ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            ventana.setSize(1200, 900);
            ventana.setLocationRelativeTo(null);

            SistemaSolarPanel panel = new SistemaSolarPanel();
            ventana.setLayout(new BorderLayout());
            ventana.add(panel, BorderLayout.CENTER);

            // ----- PANEL DE CONTROLES -----
            JPanel controles = new JPanel();
            controles.setPreferredSize(new Dimension(ventana.getWidth(), 80));
            controles.setBackground(new Color(40, 40, 40));
            controles.setLayout(new FlowLayout(FlowLayout.CENTER, 25, 20));

            JButton btnPausa = new JButton("PAUSAR / REANUDAR");
            btnPausa.addActionListener(e -> panel.togglePause());

            JButton btnMasVel = new JButton("VELOCIDAD +");
            btnMasVel.addActionListener(e -> panel.cambiarVelocidad(0.5f));

            JButton btnMenosVel = new JButton("VELOCIDAD -");
            btnMenosVel.addActionListener(e -> panel.cambiarVelocidad(-0.5f));

            JButton btnZoomMas = new JButton("ZOOM -");
            btnZoomMas.addActionListener(e -> panel.cambiarZoom(-40)); // FUNCIONAL

            JButton btnZoomMenos = new JButton("ZOOM +");
            btnZoomMenos.addActionListener(e -> panel.cambiarZoom(40)); // FUNCIONAL

            JButton btnReset = new JButton("REINICIAR VISTA");
            btnReset.addActionListener(e -> panel.resetVista());

            JButton btnReiniciar = new JButton("REINICIAR");
            btnReiniciar.addActionListener(e -> panel.reiniciarSimulacion());

            controles.add(btnPausa);
            controles.add(btnMasVel);
            controles.add(btnMenosVel);
            controles.add(btnZoomMas);
            controles.add(btnZoomMenos);
            controles.add(btnReset);
            controles.add(btnReiniciar);

            ventana.add(controles, BorderLayout.SOUTH);

            ventana.setVisible(true);
            panel.requestFocusInWindow();
        });
    }
}
