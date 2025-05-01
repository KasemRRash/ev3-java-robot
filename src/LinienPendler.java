
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.robotics.SampleProvider;
import lejos.utility.Delay;
import lejos.hardware.Brick;
import lejos.hardware.BrickFinder;
import lejos.hardware.Button;
import lejos.hardware.Sound;

public class LinienPendler {

    static final double WHEEL_DIAMETER = 0.056;
    static final double WHEEL_SPACING = 0.12;

    static Brick brick;
    static EV3LargeRegulatedMotor leftMotor;
    static EV3LargeRegulatedMotor rightMotor;
    static EV3ColorSensorWrapper colorSensor;

    public static void main(String[] args) {
        initRobot();
        String lastScanDirection = "";
        boolean fahreVorwaerts = true;

        System.out.println("Press any button to start!");
        Button.waitForAnyPress();

        double lineThreshold       = 0.1;
        double backgroundThreshold = 0.5;
        float maxSpeed             = 200;
        int baseSpeed              = (int)(0.7 * maxSpeed);
        int turnSpeedDelta         = (int)(0.1 * maxSpeed);

        int offLineCounter = 0;

        // Starte auf der Linie
        leftMotor.setSpeed(baseSpeed);
        rightMotor.setSpeed(baseSpeed);
        leftMotor.forward();
        rightMotor.forward();

        while (true) {
            double reflected = colorSensor.getReflectedRed();

            // Debug: Helligkeit und Counter
            System.out.println("Reflected=" + reflected + "  offLine=" + offLineCounter);

            // 1) Linienverfolgung
            if (reflected < lineThreshold) {
                leftMotor.setSpeed(baseSpeed + turnSpeedDelta);
                rightMotor.setSpeed(baseSpeed - turnSpeedDelta);
            } else {
                leftMotor.setSpeed(baseSpeed - turnSpeedDelta);
                rightMotor.setSpeed(baseSpeed + turnSpeedDelta);
            }
            // Richtung wählen
            if (fahreVorwaerts) {
                leftMotor.forward();
                rightMotor.forward();
            } else {
                leftMotor.backward();
                rightMotor.backward();
            }

            // 2) Linie verloren?
            if (reflected > backgroundThreshold) {
                offLineCounter++;
            } else {
                offLineCounter = 0;
            }

            // 3) Scan-Block
            if (offLineCounter > 2) {
                System.out.println(">> SCAN-BLOCK betreten!");
                Sound.beep();  // akustischer Hinweis
                leftMotor.stop(true);
                rightMotor.stop();
                Delay.msDelay(200);

                // Scan-Parameter
                int scanSpeed   = (int)(0.5 * maxSpeed);
                long maxScanDur = 800;  // ms, Dauer für 90°-Drehung

                leftMotor.setSpeed(scanSpeed);
                rightMotor.setSpeed(scanSpeed);
                boolean found = false;

                // --- a) Rechts scannen ---
                System.out.println("Scan nach rechts...");
                leftMotor.forward();
                rightMotor.backward();
                long start = System.currentTimeMillis();
                while (System.currentTimeMillis() - start < maxScanDur) {
                    double r = colorSensor.getReflectedRed();
                    if (r <= lineThreshold) {
                        System.out.println("  -> rechts gefunden!  R=" + r);
                        Sound.twoBeeps();
                        leftMotor.stop(true);
                        rightMotor.stop();
                        lastScanDirection = "right";
                        found = true;
                        break;
                    }
                    Delay.msDelay(10);
                }
                leftMotor.stop(true);
                rightMotor.stop();
                Delay.msDelay(100);

                // --- b) Links scannen, wenn rechts nichts war ---
                if (!found) {
                    System.out.println("Scan nach links...");
                    leftMotor.backward();
                    rightMotor.forward();
                    start = System.currentTimeMillis();
                    while (System.currentTimeMillis() - start < maxScanDur * 2) {
                        double r = colorSensor.getReflectedRed();
                        if (r <= lineThreshold) {
                            System.out.println("  -> links gefunden!  R=" + r);
                            Sound.twoBeeps();
                            leftMotor.stop(true);
                            rightMotor.stop();
                            lastScanDirection = "left";
                            found = true;
                            break;
                        }
                        Delay.msDelay(10);
                    }
                    leftMotor.stop(true);
                    rightMotor.stop();
                    Delay.msDelay(100);
                }

                // --- c) Letzter Versuch rechts ---
                if (!found) {
                    System.out.println("Letzter Versuch: rechts");
                    leftMotor.forward();
                    rightMotor.backward();
                    start = System.currentTimeMillis();
                    while (System.currentTimeMillis() - start < maxScanDur * 2) {
                        double r = colorSensor.getReflectedRed();
                        if (r <= lineThreshold) {
                            System.out.println("  -> rechts (2. Versuch)  R=" + r);
                            Sound.twoBeeps();
                            leftMotor.stop(true);
                            rightMotor.stop();
                            lastScanDirection = "right";
                            found = true;
                            break;
                        }
                        Delay.msDelay(10);
                    }
                    leftMotor.stop(true);
                    rightMotor.stop();
                    Delay.msDelay(100);
                }

                // 4) Einlenken und Pendel-Logik
                if (found) {
                    if (lastScanDirection.equals("left")) {
                        System.out.println("Eindrehen nach links");
                        leftMotor.setSpeed((int)(0.3 * baseSpeed));
                        rightMotor.setSpeed((int)(0.3 * baseSpeed));
                        leftMotor.backward();
                        rightMotor.forward();
                        Delay.msDelay(700);
                    } else {
                        System.out.println("Eindrehen nach rechts");
                        leftMotor.setSpeed(baseSpeed);
                        rightMotor.setSpeed((int)(0.5 * baseSpeed));
                        leftMotor.forward();
                        rightMotor.forward();
                        Delay.msDelay(400);
                    }

                    // Pendel-Richtung umkehren
                    fahreVorwaerts = !fahreVorwaerts;
                    Sound.beep();

                    // Neustart
                    offLineCounter = 0;
                    continue;
                }
            }

            // 5) Not-Aus per Button
            if (Button.getButtons() != 0) {
                leftMotor.stop(true);
                rightMotor.stop();
                break;
            }

            Delay.msDelay(50);
        }
    }


    public static void initRobot() {
        if (brick != null) return;

        Button.LEDPattern(5);
        System.out.println("Initializing...");
        brick = BrickFinder.getDefault();
        leftMotor = new EV3LargeRegulatedMotor(brick.getPort("B"));
        rightMotor = new EV3LargeRegulatedMotor(brick.getPort("C"));

        Thread colorThread = new Thread(() -> {
            while (colorSensor == null) {
                try {
                    colorSensor = new EV3ColorSensorWrapper(brick.getPort("S1"));
                } catch (IllegalArgumentException e) {
                    System.err.println("Color sensor: " + e.getMessage() + ". Retrying...");
                }
            }
        });

        colorThread.start();
        try {
            colorThread.join();
        } catch (InterruptedException e) {
            System.err.println(e.getMessage());
        }

        Button.LEDPattern(1);
        Sound.beepSequenceUp();
        System.out.println("Ready!");
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
