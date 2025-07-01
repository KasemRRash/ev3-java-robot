package Client;

import java.io.*;
import java.net.*;
import java.awt.event.*;
import javax.swing.*;

public class EV3WlanClient extends JFrame implements KeyListener {
    private final Socket socket;
    private final PrintWriter out;

    public EV3WlanClient(String ev3Address, int port) throws IOException {
        super("EV3 WLAN-Controller");

        // Verbindung herstellen
        socket = new Socket(ev3Address, port);
        out = new PrintWriter(socket.getOutputStream(), true);

        // Fenster vorbereiten
        setSize(300, 100);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JLabel info = new JLabel("Steuerung mit Pfeiltasten, ESC beendet.");
        add(info);
        addKeyListener(this);
        setFocusable(true);
        setVisible(true);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP: out.println("UP"); break;
            case KeyEvent.VK_DOWN: out.println("DOWN"); break;
            case KeyEvent.VK_LEFT: out.println("LEFT"); break;
            case KeyEvent.VK_RIGHT: out.println("RIGHT"); break;
            case KeyEvent.VK_ESCAPE:
                out.println("STOP");
                try {
                    out.close();
                    socket.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                System.exit(0);
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        out.println("STOP");
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // unused
    }

    public static void main(String[] args) {
        try {
            new EV3WlanClient("192.168.0.10", 6789); // <- IP anpassen
        } catch (IOException e) {
            System.err.println("Verbindung fehlgeschlagen: " + e.getMessage());
        }
    }
}
