import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.robotics.RegulatedMotor;

public class MotorTracker {
    private final RegulatedMotor leftMotor;
    private final RegulatedMotor rightMotor;

    private static final float WHEEL_DIAMETER_MM = 17.0f;
    private static final int MOTOR_SPEED = 200;
    private static final float ROTATION_FACTOR = 2.14f; // Kalibrierung notwendig!

    public MotorTracker() {
        leftMotor = new EV3LargeRegulatedMotor(MotorPort.B);
        rightMotor = new EV3LargeRegulatedMotor(MotorPort.C);

        leftMotor.setSpeed(MOTOR_SPEED);
        rightMotor.setSpeed(MOTOR_SPEED);
    }

    // 🔄 Dreht den Roboter um den gegebenen Winkel
    public void rotateBy(int angle) {
        int degrees = Math.round(Math.abs(angle) * ROTATION_FACTOR);

        if (angle > 0) {
            leftMotor.rotate(degrees, true);
            rightMotor.rotate(-degrees);
        } else {
            leftMotor.rotate(-degrees, true);
            rightMotor.rotate(degrees);
        }
    }

    // ▶️ Vorwärtsfahrt (manuell)
    public void travelForward() {
        leftMotor.forward();
        rightMotor.forward();
    }

    // ◀️ Rückwärtsfahrt (manuell)
    public void travelBackward() {
        leftMotor.backward();
        rightMotor.backward();
    }

    // 🔙 Rückwärts um bestimmte Distanz in mm
    public void travelBackward(float distanceMM) {
        int degrees = convertDistanceToDegrees(distanceMM);

        leftMotor.resetTachoCount();
        rightMotor.resetTachoCount();

        leftMotor.backward();
        rightMotor.backward();

        while (Math.abs(leftMotor.getTachoCount()) < degrees &&
               Math.abs(rightMotor.getTachoCount()) < degrees) {
            try { Thread.sleep(10); } catch (InterruptedException e) { break; }
        }

        stop();
    }

    // ↩️ Drehung nach links (manuell)
    public void rotateLeft() {
        leftMotor.backward();
        rightMotor.forward();
    }

    // ↪️ Drehung nach rechts (manuell)
    public void rotateRight() {
        leftMotor.forward();
        rightMotor.backward();
    }

    // ⏹ Stoppt beide Motoren
    public void stop() {
        leftMotor.stop(true);
        rightMotor.stop(true);
    }

    // 🔁 Reset für Distanzmessung
    public void reset() {
        leftMotor.resetTachoCount();
        rightMotor.resetTachoCount();
    }

    // 📏 Gemessene Strecke in mm (Durchschnitt beider Räder)
    public float getDistanceTraveled() {
        int leftTacho = Math.abs(leftMotor.getTachoCount());
        int rightTacho = Math.abs(rightMotor.getTachoCount());
        int avgTacho = (leftTacho + rightTacho) / 2;

        double wheelCircumference = Math.PI * WHEEL_DIAMETER_MM;
        return (float) (avgTacho * wheelCircumference / 360.0);
    }

    // Umrechnung: mm → Grad
    private int convertDistanceToDegrees(float distanceMM) {
        double wheelCircumference = Math.PI * WHEEL_DIAMETER_MM;
        return (int) ((360.0 * distanceMM) / wheelCircumference);
    }

    // 🔚 Motoren sauber schließen
    public void close() {
        leftMotor.close();
        rightMotor.close();
    }
    
    public void travelBackwardWithGyro(float distanceMM, int correctionAngle) {
        int degrees = convertDistanceToDegrees(distanceMM);

        leftMotor.resetTachoCount();
        rightMotor.resetTachoCount();

        // Basisgeschwindigkeit setzen
        leftMotor.setSpeed(MOTOR_SPEED);
        rightMotor.setSpeed(MOTOR_SPEED);

        // Korrektur: reduzierte Geschwindigkeit je nach Abweichung
        float correctionFactor = 1.0f - (Math.min(Math.abs(correctionAngle), 45) / 90.0f);
        correctionFactor = Math.max(0.6f, correctionFactor); // nicht zu langsam

        if (correctionAngle > 3) {
            leftMotor.setSpeed((int)(MOTOR_SPEED * correctionFactor));
        } else if (correctionAngle < -3) {
            rightMotor.setSpeed((int)(MOTOR_SPEED * correctionFactor));
        }

        leftMotor.backward();
        rightMotor.backward();

        while (Math.abs(leftMotor.getTachoCount()) < degrees &&
               Math.abs(rightMotor.getTachoCount()) < degrees) {
            try { Thread.sleep(10); } catch (InterruptedException e) { break; }
        }

        stop();

        // Nach Schritt wieder Basisgeschwindigkeit setzen
        leftMotor.setSpeed(MOTOR_SPEED);
        rightMotor.setSpeed(MOTOR_SPEED);
    }
    
    

}
