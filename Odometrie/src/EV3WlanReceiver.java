import java.io.*;
import java.net.*;

public class EV3WlanReceiver {
    private final MotorTracker motor;
    private final GyroTracker gyro;
    private final PathRecorder recorder;

    public EV3WlanReceiver(MotorTracker motor, GyroTracker gyro, PathRecorder recorder) {
        this.motor = motor;
        this.gyro = gyro;
        this.recorder = recorder;
    }

    public void waitForClientAndControl() {
        ServerSocket server = null;
        Socket client = null;
        BufferedReader in = null;

        try {
            server = new ServerSocket(6789);
            System.out.println("Warte auf WLAN-Verbindung...");
            client = server.accept();
            System.out.println("Client verbunden.");

            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            String lastCommand = "";
            int lastAngle = gyro.getNormalizedAngle();

            String command;
            while ((command = in.readLine()) != null) {
                command = command.trim();

                if (!command.equals(lastCommand) && !lastCommand.equals("")) {
                    float distance = motor.getDistanceTraveled();
                    if (distance > 1.0f) {
                        recorder.recordStep(lastAngle, distance);
                        motor.reset();
                    }

                    if (lastCommand.equals("LEFT") || lastCommand.equals("RIGHT")) {
                        lastAngle = gyro.getNormalizedAngle();
                    }
                }

                switch (command) {
                    case "UP": motor.travelForward(); break;
                    case "DOWN": motor.travelBackward(); break;
                    case "LEFT": motor.rotateLeft(); break;
                    case "RIGHT": motor.rotateRight(); break;
                    case "STOP": motor.stop(); break;
                }

                lastCommand = command;
            }

        } catch (IOException e) {
            System.err.println("❌ Verbindung verloren oder Fehler: " + e.getMessage());
        } finally {
            System.out.println("🔌 Verbindung getrennt – Roboter stoppt.");
            motor.stop();

            // Letzten Schritt speichern
            float remaining = motor.getDistanceTraveled();
            if (remaining > 1.0f) {
                recorder.recordStep(gyro.getNormalizedAngle(), remaining);
            }

            try {
                if (in != null) in.close();
                if (client != null) client.close();
                if (server != null) server.close();
            } catch (IOException e) {
                System.err.println("Fehler beim Schließen: " + e.getMessage());
            }
        }
    }
}
