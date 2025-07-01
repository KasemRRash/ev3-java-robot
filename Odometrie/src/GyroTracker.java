import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3GyroSensor;
import lejos.robotics.SampleProvider;

public class GyroTracker {
    private final EV3GyroSensor gyroSensor;
    private final SampleProvider angleProvider;
    private final float[] sample;

    private int lastRecordedAngle = 0;
    private final int turnThreshold = 5;  // mindestens 5° Differenz

    public GyroTracker() {
        gyroSensor = new EV3GyroSensor(SensorPort.S3); 
        angleProvider = gyroSensor.getAngleMode();
        sample = new float[angleProvider.sampleSize()];
    }

    public int getCurrentAngle() {
        angleProvider.fetchSample(sample, 0);
        return Math.round(sample[0]);
    }

    public int getNormalizedAngle() {
        return normalizeAngle(getCurrentAngle());
    }

    public void reset() {
        gyroSensor.reset();
        lastRecordedAngle = 0;
        try { Thread.sleep(500); } catch (InterruptedException e) {}
    }

    public void close() {
        gyroSensor.close();
    }

    public boolean hasTurned() {
        int current = getNormalizedAngle();
        int delta = normalizeAngle(current - lastRecordedAngle);

        if (Math.abs(delta) >= turnThreshold) {
            lastRecordedAngle = current;  // aktualisieren
            return true;
        }

        return false;
    }

    public int getLastAngle() {
        return lastRecordedAngle;
    }

    private int normalizeAngle(int angle) {
        angle = angle % 360;
        if (angle > 180) angle -= 360;
        if (angle < -180) angle += 360;
        return angle;
    }
}
