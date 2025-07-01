import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.hardware.lcd.LCD;

public class Main {
    public static void main(String[] args) {
        // === Komponenten initialisieren ===
        GyroTracker gyro = new GyroTracker();
        MotorTracker motor = new MotorTracker();
        PathRecorder recorder = new PathRecorder();

        gyro.reset();
        motor.reset();

        System.out.println("Start bei Gyro-Winkel: " + gyro.getCurrentAngle());
        
        /*
         // === Manuelle Steuerung am Brick ===
        System.out.println("Brick-Steuerung: ESC zum Beenden");
        EV3Controller controller = new EV3Controller(motor, gyro, recorder);
        controller.controlLoop();
        */

        // === WLAN-Steuerung vom PC ===
        System.out.println("Warte auf WLAN-Steuerung...");
        EV3WlanReceiver wlan = new EV3WlanReceiver(motor, gyro, recorder);
        Sound.beepSequence();
        wlan.waitForClientAndControl();
        
        Sound.beepSequence();

        // === Pfad anzeigen ===
        System.out.println("Gesamter Pfad:");
        recorder.printSteps();
        drawPathOnLCD(recorder);

        // === 3. Rückfahrt starten ===
        System.out.println("Taste drücken zum Start der Rückfahrt...");
        Button.waitForAnyPress();

        System.out.println("🔁 Starte Rückfahrt zum Ursprung...");
        ReturnNavigator returner = new ReturnNavigator(motor, gyro, recorder);
        returner.returnToStart();

        // === Abschluss ===
        System.out.println("Rückfahrt abgeschlossen.");
        System.out.println("Gyro-Endwinkel: " + gyro.getCurrentAngle());
        System.out.println("Taste drücken zum Beenden.");
        Button.waitForAnyPress();

        motor.close();
        gyro.close();
        System.out.println("Programm beendet.");
    }

    // === Pfad auf dem LCD anzeigen (vereinfacht als Punkte) ===
    private static void drawPathOnLCD(PathRecorder recorder) {
        LCD.clear();
        for (VectorStep step : recorder.getPath()) {
            int x = Math.round(step.x / 20); // Maßstab anpassen
            int y = Math.round(step.y / 20);
            int drawX = 10 + x;
            int drawY = 50 - y;

            if (drawX >= 0 && drawX < 178 && drawY >= 0 && drawY < 128) {
                LCD.getPixel(drawX, drawY);
            }
        }
        LCD.refresh();
    }
}
