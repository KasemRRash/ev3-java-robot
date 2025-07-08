package Kalibrierung;
import lejos.hardware.Button;

public class RotationCalibrator {

    public static void main(String[] args) {
        MotorTracker motorTracker = new MotorTracker();
        GyroTracker gyroTracker = new GyroTracker();

        gyroTracker.reset();
        motorTracker.reset();

        System.out.println("Drücke ENTER für 90°-Drehung...");
        Button.ENTER.waitForPress();

        int angleBefore = gyroTracker.getNormalizedAngle();
        System.out.println("Startwinkel: " + angleBefore);

        motorTracker.rotateBy(180);  // Befehl: 90 Grad

        try { Thread.sleep(500); } catch (InterruptedException ignored) {}

        int angleAfter = gyroTracker.getNormalizedAngle();
        System.out.println("Endwinkel:   " + angleAfter);

        int realTurn = gyroTracker.normalizeAngle(angleAfter - angleBefore);
        System.out.println("→ Tatsächliche Drehung: " + realTurn + "°");

        System.out.println("\nStimmt der Winkel? (→ Taste drücken zum Wiederholen)");
        Button.waitForAnyPress();

        motorTracker.close();
        gyroTracker.close();
    }
}
