package robotik;

import lejos.hardware.Brick;
import lejos.hardware.BrickFinder;
import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class Robot {
    public EV3LargeRegulatedMotor leftMotor;
    public EV3LargeRegulatedMotor rightMotor;
    public EV3ColorSensorWrapper colorSensor;
    static Brick brick;

    public void initRobot() {
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
}
