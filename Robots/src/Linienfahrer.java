import lejos.hardware.motor.Motor;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.robotics.SampleProvider;
import lejos.robotics.chassis.Chassis;
import lejos.robotics.chassis.Wheel;
import lejos.robotics.chassis.WheeledChassis;
import lejos.robotics.localization.OdometryPoseProvider;
import lejos.robotics.localization.PoseProvider;
import lejos.robotics.navigation.MovePilot;
import lejos.utility.Delay;
import lejos.hardware.Brick;
import lejos.hardware.BrickFinder;
import lejos.hardware.Button;
import lejos.hardware.Sound;


public class Linienfahrer {
	
	/**
	  * Diameter of a standard EV3 wheel in meters.
	  */
	 static final double WHEEL_DIAMETER = 0.056; //5,6cm
	 /**
	  * Distance between center of wheels in meters.
	  */
	 static final double WHEEL_SPACING = 0.12; //12cm

	 // Globale Variabeln initRobot()
	 static Brick brick; //Steuerzentrale
	 static EV3LargeRegulatedMotor leftMotor; //linkes Rad
	 static EV3LargeRegulatedMotor rightMotor; //rechtes Rad
	 static EV3ColorSensorWrapper colorSensor; //farbensensor

	 // These variables are initalized by initPilot()
	 static MovePilot pilot;
	 static PoseProvider poseProvider; //positionbestimmung

	
	 public static void main(String[] args) {
		    initRobot();
		    String lastScanDirection = "";

		    System.out.println("Press any button to start!");
		    Button.waitForAnyPress(); //start 

		    //Schwellenwerte
		  //  double lineThreshold = 0.1; //maximaler Helligkeitswert für "Linie erkannt"
		   // double backgroundThreshold = 0.5; //minimaler Wert für "kein Linie mehr"

		    float maxSpeed = 200; //maximale Geschwindigkeit
		    int baseSpeed = (int)(0.7 * maxSpeed); //normale Fahrgeschwindigkeit (70% von max)
		    int turnSpeedDelta = (int)(0.1 * maxSpeed); //Drehzahl links/rechts zum Kurvenfahren

		    leftMotor.setSpeed(baseSpeed);
		    rightMotor.setSpeed(baseSpeed);
		    leftMotor.forward();
		    rightMotor.forward();

		    int offLineCounter = 0;

		    
		    System.out.println("Kalibrierung: Stelle den Roboter auf die LINIE und drücke einen Button.");
		    Button.waitForAnyPress();
		    double lineValue = colorSensor.getReflectedRed();
		    System.out.println("Linienwert: " + lineValue);

		    Delay.msDelay(500);
		    System.out.println("Jetzt auf den HINTERGRUND stellen und Button drücken.");
		    Button.waitForAnyPress();
		    double backgroundValue = colorSensor.getReflectedRed();
		    System.out.println("Hintergrundwert: " + backgroundValue);

		    double lineThreshold = (lineValue + backgroundValue) / 2;
		    double backgroundThreshold = backgroundValue - 0.1; // etwas Reserve
		    System.out.println("Berechneter lineThreshold: " + lineThreshold);
		    System.out.println("Berechneter backgroundThreshold: " + backgroundThreshold);

		    while (true) {
		        double reflected = colorSensor.getReflectedRed(); //Reflexionswert wird geholt vom Sensor

		        // Linienverfolgung
		        if (reflected < lineThreshold) {
		            leftMotor.setSpeed(baseSpeed + turnSpeedDelta);
		            rightMotor.setSpeed(baseSpeed - turnSpeedDelta);
		            leftMotor.forward();
		            rightMotor.forward();
		        } else {
		            leftMotor.setSpeed(baseSpeed - turnSpeedDelta);
		            rightMotor.setSpeed(baseSpeed + turnSpeedDelta);
		            leftMotor.forward();
		            rightMotor.forward();
		        }

		        // Linie verloren?
		        if (reflected > backgroundThreshold) {
		            offLineCounter++;
		        } else {
		            offLineCounter = 0;
		        }

		        // === SCAN STARTEN ===
		        if (offLineCounter > 2) {
		            System.out.println("Linie verloren — Starte Suche");
		            leftMotor.stop(true); //Motor stoppt
		            rightMotor.stop();
		            Delay.msDelay(200);

		            //Drehgeschwindigkeit reduziert
		            int scanSpeed = (int)(0.5 * maxSpeed);
		            leftMotor.setSpeed(scanSpeed);
		            rightMotor.setSpeed(scanSpeed);

		            boolean found = false;

		            // === Kontinuierliche Drehung nach rechts ===
		            System.out.println("Scan nach rechts...");
		            leftMotor.forward(); //linke Rad vorwärts
		            rightMotor.backward(); //rechter Rad rückwarts

		            long startTime = System.currentTimeMillis();
		            long maxScanTime = 1000; // in ms

		            while (System.currentTimeMillis() - startTime < maxScanTime) {
		                if (colorSensor.getReflectedRed() <= lineThreshold) {
		                    lastScanDirection = "right";
		                    found = true;
		                    break;
		                }
		                if (Button.getButtons() != 0) return;
		                Delay.msDelay(10);
		            }

		            leftMotor.stop(true);
		            rightMotor.stop();
		            Delay.msDelay(100);

		         // === Nach links scannen, wenn rechts nichts ===
		            if (!found) {
		                System.out.println("Dauerhafte Linksdrehung bis Linie gefunden wird...");
		                leftMotor.backward();
		                rightMotor.forward();

		                while (true) {
		                    if (colorSensor.getReflectedRed() <= lineThreshold) {
		                        lastScanDirection = "left";
		                        found = true;
		                        break;
		                    }
		                    if (Button.getButtons() != 0) return;
		                    Delay.msDelay(10);
		                }

		                leftMotor.stop(true);
		                rightMotor.stop();
		                Delay.msDelay(100);
		            }

		            // === Letzter Versuch rechts ===
		            if (!found) {
		                System.out.println("Letzter Versuch: nochmal rechts");
		                leftMotor.forward();
		                rightMotor.backward();
		                startTime = System.currentTimeMillis();
		                while (System.currentTimeMillis() - startTime < maxScanTime * 2) {
		                    if (colorSensor.getReflectedRed() <= lineThreshold) {
		                        lastScanDirection = "right";
		                        found = true;
		                        break;
		                    }
		                    if (Button.getButtons() != 0) return;
		                    Delay.msDelay(10);
		                }

		                leftMotor.stop(true);
		                rightMotor.stop();
		                Delay.msDelay(100);
		            }

		            // === Wenn Linie gefunden: korrekt eindrehen ===
		            if (found) {
		                if (lastScanDirection.equals("left")) {
		                    System.out.println("Eindrehen nach links");
		                    leftMotor.setSpeed((int)(0.3 * baseSpeed));
		                    rightMotor.setSpeed((int)(0.3 * baseSpeed));
		                    leftMotor.backward();
		                    rightMotor.forward();
		                    Delay.msDelay(700); // passt für 90°
		                } else {
		                    System.out.println("Leichte Rechtsdrehung zur Linie");
		                    leftMotor.setSpeed(baseSpeed);
		                    rightMotor.setSpeed((int)(0.5 * baseSpeed));
		                    leftMotor.forward();
		                    rightMotor.forward();
		                    Delay.msDelay(400);
		                }

		                // danach normal weiterfahren
		                leftMotor.setSpeed(baseSpeed);
		                rightMotor.setSpeed(baseSpeed);
		                leftMotor.forward();
		                rightMotor.forward();

		                offLineCounter = 0;
		                continue;
		            }
		        }

		        // Not-Stopp
		        if (Button.getButtons() != 0) {
		            leftMotor.stop(true);
		            rightMotor.stop();
		            break;
		        }

		        Delay.msDelay(50);
		    }
		}


    
    
    public static void initRobot() {
        if (brick != null) {
            // Already initialized
            return;
        }
        Button.LEDPattern(5); // Rot leuchten
        System.out.println("Initializing...");

        brick = BrickFinder.getDefault();
        
        leftMotor = new EV3LargeRegulatedMotor(brick.getPort("B"));
        rightMotor = new EV3LargeRegulatedMotor(brick.getPort("C"));

        // Initialize sensors in separate threads because each take a lot of time.
    
        Thread colorThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (colorSensor == null) {
                    try {
                        colorSensor = new EV3ColorSensorWrapper(brick.getPort("S1"));
                    } catch (IllegalArgumentException e) {
                        System.err.println("Color sensor: " + e.getMessage() + ". Retrying...");
                    }
                }
            }
        });
    
        colorThread.start();

        // Wait for sensors to be initialized.
        try {
            colorThread.join();
        } catch (InterruptedException e) {
            System.err.println(e.getMessage());
        }

        Button.LEDPattern(1); // Steady green
        Sound.beepSequenceUp();
        System.out.println("Ready!");
    }


    /**
     * Wrapper class to allow easier use of EV3ColorSensor.
     */
    public static class EV3ColorSensorWrapper extends EV3ColorSensor {

        private final SampleProvider sampleProviderRed;
        private final SampleProvider sampleProviderRGB;
        private final SampleProvider sampleProviderColor;
        private final float[] samples;

        public EV3ColorSensorWrapper(Port port) {
            super(port);

            try {
                sampleProviderRed = super.getRedMode();
                sampleProviderRGB = super.getRGBMode();
                sampleProviderColor = super.getColorIDMode();
            } catch (IllegalArgumentException e) {
                super.close();
                throw e;
            }

            samples = new float[3];
        }

        /**
         * Measures the level of reflected light from the sensors RED LED.
         *
         * @return A value containing the intensity level (Normalized between 0
         * and 1) of reflected light.
         *
         */
        public double getReflectedRed() {
            sampleProviderRed.fetchSample(samples, 0);

            return samples[0];
        }

        /**
         * Measures the level of reflected red, green and blue light when
         * illuminated by a white light source.
         *
         * @return The sample contains 3 elements containing the intensity level
         * (Normalized between 0 and 1) of red, green and blue light
         * respectivily.
         */
        public double[] getReflectedRGB() {
            sampleProviderRGB.fetchSample(samples, 0);

            return new double[]{samples[0], samples[1], samples[2]};
        }

        /**
         * Measures the color ID of a surface. The sensor can identify 8 unique
         * colors (NONE, BLACK, BLUE, GREEN, YELLOW, RED, WHITE, BROWN).
         *
         * @return A value corresponding to identified color. See
         * {@link lejos.robotics.Color}
         */
        public int getColor() {
            sampleProviderColor.fetchSample(samples, 0);

            return (int) samples[0];
        }
    }
    
}
