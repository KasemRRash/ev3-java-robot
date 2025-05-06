import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.robotics.SampleProvider;
import lejos.utility.Delay;
import lejos.hardware.Brick;
import lejos.hardware.BrickFinder;
import lejos.hardware.Button;
import lejos.hardware.Sound;

public class PID_Regler {

    static Brick brick;
    static EV3LargeRegulatedMotor leftMotor;
    static EV3LargeRegulatedMotor rightMotor;
    static EV3ColorSensorWrapper colorSensor;

    public static void main(String[] args) {
        initRobot();

        System.out.println("Kalibrierung starten...");
        System.out.println("Sensor auf LINIE – Taste drücken");
        Button.waitForAnyPress();
        double blackValue = colorSensor.getReflectedRed();
        System.out.println("Linienwert: " + blackValue);

        Delay.msDelay(500);

        System.out.println("Sensor auf HINTERGRUND – Taste drücken");
        Button.waitForAnyPress();
        double whiteValue = colorSensor.getReflectedRed();
        System.out.println("Hintergrundwert: " + whiteValue);

        double targetValue = (blackValue + whiteValue) / 2;
        System.out.println("Zielwert (Mitte): " + targetValue);

        // PID-Parameter
        double Kp = 100;
        double Ki = 0;
        double Kd = 250;

        double lastError = 0;
        double integral = 0;

        float maxSpeed = 200;
        int baseSpeed = (int) (0.6 * maxSpeed);  // Basisgeschwindigkeit

        System.out.println("Starte Linienverfolgung...");
        Delay.msDelay(500);
        Button.LEDPattern(1); // grün

        while (true) {
            double reflected = colorSensor.getReflectedRed();

            // PID-Berechnung
            double error = targetValue - reflected;
            integral += error;
            double derivative = error - lastError;
            double turn = Kp * error + Ki * integral + Kd * derivative;
            lastError = error;

            // Begrenzung des Regelwerts
            turn = Math.max(-maxSpeed, Math.min(maxSpeed, turn));

            double absError = Math.abs(error);

            // Verstärkung bei starker Kurve
            if (absError > 0.2) {
                turn *= 2;
            }

            // Extremfall: Linie wohl komplett verloren
            if (absError > 0.4) {
                System.out.println("Starke Abweichung – probiere Korrektur-Drehung");
                leftMotor.setSpeed(100);
                rightMotor.setSpeed(100);
                leftMotor.backward();
                rightMotor.forward();
                Delay.msDelay(300);
                continue;
            }

            // Dynamische Anpassung des Basis-Tempos in Kurven
            int dynamicBaseSpeed = (int)(baseSpeed * (1.0 - Math.min(absError * 4.5, 0.8)));

            // Erzwinge minimale Lenkung bei starker Abweichung
            if (Math.abs(turn) < 30 && absError > 0.1) {
                turn = 30 * Math.signum(error);
            }

            // Geschwindigkeit berechnen
            int leftSpeed = (int) (dynamicBaseSpeed + turn);
            int rightSpeed = (int) (dynamicBaseSpeed - turn);

            // Begrenzen auf sinnvolle Werte
            leftSpeed = Math.max(50, Math.min((int) maxSpeed, leftSpeed));
            rightSpeed = Math.max(50, Math.min((int) maxSpeed, rightSpeed));

            leftMotor.setSpeed(leftSpeed);
            rightMotor.setSpeed(rightSpeed);
            leftMotor.forward();
            rightMotor.forward();

            // Debug-Ausgabe 
            System.out.println("Reflected: " + reflected + " | L: " + leftSpeed + " R: " + rightSpeed);

            if (Button.getButtons() != 0) {
                leftMotor.stop(true);
                rightMotor.stop();
                System.out.println("Stopp durch Button.");
                break;
            }

            Delay.msDelay(20);
        }

 }

    public static void initRobot() {
        if (brick != null) return;

        Button.LEDPattern(5); // rot blinkend
        System.out.println("Initialisiere...");
        brick = BrickFinder.getDefault();

        leftMotor = new EV3LargeRegulatedMotor(brick.getPort("B"));
        rightMotor = new EV3LargeRegulatedMotor(brick.getPort("C"));

        Thread colorThread = new Thread(() -> {
            while (colorSensor == null) {
                try {
                    colorSensor = new EV3ColorSensorWrapper(brick.getPort("S1"));
                } catch (IllegalArgumentException e) {
                    System.err.println("Farbsensorfehler: " + e.getMessage());
                }
            }
        });
        colorThread.start();

        try {
            colorThread.join();
        } catch (InterruptedException e) {
            System.err.println(e.getMessage());
        }

        Sound.beepSequenceUp();
        System.out.println("Bereit!");
    }

    public static class EV3ColorSensorWrapper extends EV3ColorSensor {
        private final SampleProvider sampleProviderRed;
        private final float[] samples;

        public EV3ColorSensorWrapper(Port port) {
            super(port);
            sampleProviderRed = super.getRedMode();
            samples = new float[3];
        }

        public double getReflectedRed() {
            sampleProviderRed.fetchSample(samples, 0);
            return samples[0];
        }
    }
}

