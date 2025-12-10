package main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class Main {
    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            JFrame ventana = new JFrame("Sistema Solar");
            ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            ventana.setSize(1150, 900);
            ventana.setLocationRelativeTo(null);

            SistemaSolarPanel panel = new SistemaSolarPanel();
            ventana.setLayout(new BorderLayout());
            ventana.add(panel, BorderLayout.CENTER);

            JPanel controles = new JPanel();
            controles.setPreferredSize(new Dimension(ventana.getWidth(), 80));
            controles.setBackground(new Color(40, 40, 40));
            controles.setLayout(new FlowLayout(FlowLayout.CENTER, 25, 20));

            JButton btnPausa = new JButton("Pausar / Reanudar");
            btnPausa.addActionListener((ActionEvent e) -> panel.togglePause());

            JButton btnMasVel = new JButton("Velocidad +");
            btnMasVel.addActionListener(e -> panel.cambiarVelocidad(0.5f));

            JButton btnMenosVel = new JButton("Velocidad -");
            btnMenosVel.addActionListener(e -> panel.cambiarVelocidad(-0.5f));

            JButton btnZoomMas = new JButton("Zoom +");
            btnZoomMas.addActionListener(e -> panel.cambiarZoom(-20));

            JButton btnZoomMenos = new JButton("Zoom -");
            btnZoomMenos.addActionListener(e -> panel.cambiarZoom(20));

            JButton btnReset = new JButton("Reset vista");
            btnReset.addActionListener(e -> panel.resetVista());

            JButton btnReiniciar = new JButton("Reiniciar todo");
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
