import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.robotics.RegulatedMotor;

public class MotorTracker {
    private final RegulatedMotor leftMotor;
    private final RegulatedMotor rightMotor;

    private static final float WHEEL_DIAMETER_MM = 17.0f;  // 17 mm = Größe von einem Rad
    private static final int MOTOR_SPEED = 300;            // Geschwindigkeit in °/s

    public MotorTracker() {
        leftMotor = new EV3LargeRegulatedMotor(MotorPort.B);
        rightMotor = new EV3LargeRegulatedMotor(MotorPort.C);

        leftMotor.setSpeed(MOTOR_SPEED);
        rightMotor.setSpeed(MOTOR_SPEED);
    }

    // Vorwärts fahren (manuell)
    public void travelForward() {
        leftMotor.forward();
        rightMotor.forward();
    }

    // Rückwärts fahren (manuell)
    public void travelBackward() {
        leftMotor.backward();
        rightMotor.backward();
    }

    // Rückwärts fahren (gezielte Strecke)
    public void travelBackward(float distanceMM) {
        int degrees = convertDistanceToDegrees(distanceMM);

        leftMotor.resetTachoCount();
        rightMotor.resetTachoCount();

        leftMotor.backward();
        rightMotor.backward();

        while (Math.abs(leftMotor.getTachoCount()) < degrees &&
               Math.abs(rightMotor.getTachoCount()) < degrees) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                break;
            }
        }

        stop();
    }

    // Nach links drehen (manuell)
    public void rotateLeft() {
        leftMotor.backward();
        rightMotor.forward();
    }

    // Nach rechts drehen (manuell)
    public void rotateRight() {
        leftMotor.forward();
        rightMotor.backward();
    }

    // Stoppt beide Motoren
    public void stop() {
        leftMotor.stop(true);
        rightMotor.stop(true);
    }

    // Reset für Distanzmessung
    public void reset() {
        leftMotor.resetTachoCount();
        rightMotor.resetTachoCount();
    }

    // Gibt zurückgelegte Strecke zurück (Durchschnitt beider Motoren)
    public float getDistanceTraveled() {
        int leftTacho = Math.abs(leftMotor.getTachoCount());
        int rightTacho = Math.abs(rightMotor.getTachoCount());
        int avgTacho = (leftTacho + rightTacho) / 2;

        double wheelCircumference = Math.PI * WHEEL_DIAMETER_MM;
        return (float) (avgTacho * wheelCircumference / 360.0);
    }

    // Umrechnung Millimeter = Grad für gezielte Fahrt
    private int convertDistanceToDegrees(float distanceMM) {
        double wheelCircumference = Math.PI * WHEEL_DIAMETER_MM;
        return (int) ((360.0 * distanceMM) / wheelCircumference);
    }

    // Motoren freigeben
    public void close() {
        leftMotor.close();
        rightMotor.close();
    }
}
