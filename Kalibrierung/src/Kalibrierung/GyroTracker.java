package Kalibrierung;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3GyroSensor;
import lejos.robotics.SampleProvider;

public class GyroTracker {
    private final EV3GyroSensor gyroSensor;
    private final SampleProvider angleProvider;
    private final float[] sample;

    private int lastRecordedAngle = 0;
    private final int turnThreshold = 5;  // Empfindlichkeit für Richtungswechsel

    public GyroTracker() {
        gyroSensor = new EV3GyroSensor(SensorPort.S3); 
        angleProvider = gyroSensor.getAngleMode();
        sample = new float[angleProvider.sampleSize()];
        reset();
    }

    /** Liest den aktuellen absoluten Gyro-Winkel */
    public int getCurrentAngle() {
        angleProvider.fetchSample(sample, 0);
        return Math.round(sample[0]);
    }

    /** Gibt den aktuellen Winkel normalisiert auf [-179, 179] zurück */
    public int getNormalizedAngle() {
        return normalizeAngle(getCurrentAngle());
    }

    /** Führt ein Reset des Gyrosensors durch */
    public void reset() {
        gyroSensor.reset();
        waitForStabilization();
        lastRecordedAngle = getNormalizedAngle();
    }

    /** Gibt true zurück, wenn sich der Roboter signifikant gedreht hat */
    public boolean hasTurned() {
        int current = getNormalizedAngle();
        int delta = normalizeAngle(current - lastRecordedAngle);

        if (Math.abs(delta) >= turnThreshold) {
            lastRecordedAngle = current;
            return true;
        }

        return false;
    }

    /** Liefert den zuletzt erkannten Winkel bei einer Richtungsänderung */
    public int getLastAngle() {
        return lastRecordedAngle;
    }

    /** Gibt die Differenz zum Zielwinkel zurück (z.B. für Rotation) */
    public int getAngleDifference(int targetAngle) {
        int current = getNormalizedAngle();
        int delta = normalizeAngle(targetAngle - current);
        return delta;
    }

    /** Sensor schließen */
    public void close() {
        gyroSensor.close();
    }

    /** Interne Winkel-Normalisierung auf [-180°, 180°] */
    int normalizeAngle(int angle) {
        angle = angle % 359;
        if (angle > 179) angle -= 359;
        if (angle < -179) angle += 359;
        return angle;
    }

    /** Wartet kurz nach Reset */
    private void waitForStabilization() {
        try {
            Thread.sleep(500); // Sensor stabilisieren lassen
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
