import java.util.ArrayList;

import lejos.hardware.Button;
import lejos.hardware.Sound;

public class ReturnNavigator {
    private final MotorTracker motorTracker;
    private final GyroTracker gyroTracker;
    private final PathRecorder recorder;

    public ReturnNavigator(MotorTracker motorTracker, GyroTracker gyroTracker, PathRecorder recorder) {
        this.motorTracker = motorTracker;
        this.gyroTracker = gyroTracker;
        this.recorder = recorder;
    }

    public void returnToStart() {
        ArrayList<VectorStep> path = recorder.getPath();

        /*if (path.size() < 2) {
            System.out.println("Pfad zu kurz für Rückfahrt.");
            return;
        }*/

        System.out.println("Starte exakte Rückfahrt mit Drehung...");

        for (int i = path.size() - 1; i > 0; i--) {
            VectorStep from = path.get(i);
            VectorStep to = path.get(i - 1);

            float dx = from.x - to.x;
            float dy = from.y - to.y;

            double distance = Math.sqrt(dx * dx + dy * dy);
            double movementAngle = Math.toDegrees(Math.atan2(dy, dx));
            int normalizedTarget = normalizeAngle((int) Math.round(movementAngle));

            int currentGyro = gyroTracker.getNormalizedAngle();
            int angleDiff = gyroTracker.getAngleDifference(normalizedTarget);

            System.out.printf("Schritt %d → Zielrichtung: %d Grad, Gyro: %d Grad, Winkel=%d Grad | Distanz: %.1f mm\n",
                    i, normalizedTarget, currentGyro, angleDiff, distance);

         // Drehe nur, wenn eine echte Drehung nötig ist (also z. B. vom vorherigen Schritt)
            if (Math.abs(angleDiff) > 10) {
                rotateBy(angleDiff);
            }

            // Immer rückwärts fahren
            motorTracker.travelBackward((float) distance);

            
            if (i == 1) {
            		motorTracker.stop();
            		System.out.println("Gefahren & Endpunkt erreicht");
            		Sound.beepSequence();
            		break;
            }
            
            if (Button.ESCAPE.isDown()) break;
        }

        System.out.println("Rückfahrt abgeschlossen.");
        
    }

    private void rotateBy(int angleDiff) {
        //if (Math.abs(angleDiff) > 3) {
            motorTracker.rotateBy(angleDiff);
        //}
    }

    private int normalizeAngle(int angle) {
        angle = angle % 359;
        if (angle > 179) angle -= 359;
        if (angle < -179) angle += 359;
        return angle;
    }
}
