import lejos.hardware.Button;

public class EV3Controller {
    private final MotorTracker motorTracker;
    private final GyroTracker gyroTracker;
    private final PathRecorder recorder;

    private int lastDirectionAngle;
    private int lastCommand = -1;

    public EV3Controller(MotorTracker motorTracker, GyroTracker gyroTracker, PathRecorder recorder) {
        this.motorTracker = motorTracker;
        this.gyroTracker = gyroTracker;
        this.recorder = recorder;
        this.lastDirectionAngle = gyroTracker.getNormalizedAngle();
    }

    public void controlLoop() {
        motorTracker.reset();

        while (true) {
            if (Button.ESCAPE.isDown()) break;

            int currentCommand = getCurrentCommand();

            // Taste losgelassen? = Bewegung beenden + Vektor speichern
            if (currentCommand != lastCommand && lastCommand != -1) {
                float distance = motorTracker.getDistanceTraveled();

                if (distance > 1.0) {
                    recorder.recordStep(lastDirectionAngle, distance);
                    motorTracker.reset();
                }

                // Wenn Richtungstaste gedrückt war  = neuen Winkel merken
                if (lastCommand == Button.LEFT.getId() || lastCommand == Button.RIGHT.getId()) {
                    lastDirectionAngle = gyroTracker.getNormalizedAngle();
                }
            }

            // Steuerung: Nur aktiv fahren, solange Taste gedrückt
            switch (currentCommand) {
                case Button.ID_UP:
                    motorTracker.travelForward();
                    break;
                case Button.ID_DOWN:
                    motorTracker.travelBackward();
                    break;
                case Button.ID_LEFT:
                    motorTracker.rotateLeft();
                    break;
                case Button.ID_RIGHT:
                    motorTracker.rotateRight();
                    break;
                case -1:
                    motorTracker.stop();
                    break;
            }

            lastCommand = currentCommand;

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                break;
            }
        }

        // Nach ESC: Letzte Bewegung speichern, falls vorhanden
        float remaining = motorTracker.getDistanceTraveled();
        if (remaining > 1.0) {
            recorder.recordStep(lastDirectionAngle, remaining);
        }

        motorTracker.stop();
    }

    private int getCurrentCommand() {
        if (Button.UP.isDown()) return Button.UP.getId();
        if (Button.DOWN.isDown()) return Button.DOWN.getId();
        if (Button.LEFT.isDown()) return Button.LEFT.getId();
        if (Button.RIGHT.isDown()) return Button.RIGHT.getId();
        return -1;
    }
}
