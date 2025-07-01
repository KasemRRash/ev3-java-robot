package robotik;

import lejos.hardware.Brick;
import lejos.hardware.BrickFinder;
import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.robotics.SampleProvider;

public class Robot {
    public EV3LargeRegulatedMotor leftMotor;
    public EV3LargeRegulatedMotor rightMotor;
    public EV3ColorSensorWrapper colorSensor;
    static Brick brick;
    public EV3UltrasonicSensor ultrasonicSensor;
    public SampleProvider distanceProvider;
    public float[] distanceSample;

    public void initRobot() {
        if (brick != null) return;

        Button.LEDPattern(5);
        System.out.println("Initializing...");

        brick = BrickFinder.getDefault();

        leftMotor = new EV3LargeRegulatedMotor(brick.getPort("B"));
        rightMotor = new EV3LargeRegulatedMotor(brick.getPort("C"));
        
     // Sensoren initialisieren
        ultrasonicSensor = new EV3UltrasonicSensor(SensorPort.S4);
        distanceProvider = ultrasonicSensor.getDistanceMode();
        distanceSample = new float[distanceProvider.sampleSize()];

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
        
        //s4 ultrasensor
    }
    
    // Gibt die Entfernung in Metern zurück
    
   public double getDistanceValue() {
       distanceProvider.fetchSample(distanceSample, 0);
       return distanceSample[0]; // In Metern
   }
   //Schließt alle Sensoren
   public void close() {
	   if (ultrasonicSensor != null) {
          ultrasonicSensor.close();
	   }
   }
}
