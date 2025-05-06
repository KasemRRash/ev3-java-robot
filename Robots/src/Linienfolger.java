import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.robotics.SampleProvider;

public class Linienfolger {

    // Ports für Motoren
    private static final EV3LargeRegulatedMotor leftMotor  = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("B"));
    private static final EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("C"));

    // Farbsensor und SampleProvider
    private static final EV3ColorSensor colorSensor = 
        new EV3ColorSensor(SensorPort.S1);
    private static final SampleProvider lightMode = 
        colorSensor.getRedMode();

    public static void main(String[] args) {
        TextLCD lcd = LocalEV3.get().getTextLCD();
        float[] sample = new float[lightMode.sampleSize()];

        // 1. Kalibriere Schwarz
        lcd.clear();
        lcd.drawString("Schwarz auf Linie", 0, 0);
        lcd.drawString("ENTER drücken",    0, 2);
        Button.ENTER.waitForPress();
        lightMode.fetchSample(sample, 0);
        float black = sample[0];

        // 2. Kalibriere Weiß
        lcd.clear();
        lcd.drawString("Weiß Untergrund",  0, 0);
        lcd.drawString("ENTER drücken",    0, 2);
        Button.ENTER.waitForPress();
        lightMode.fetchSample(sample, 0);
        float white = sample[0];

        // Schwellenwert berechnen
        float threshold = (black + white) / 2f;
        lcd.clear();
        lcd.drawString("Kalib. fertig",   0, 0);
        lcd.drawString("Threshold:",      0, 2);
        lcd.drawString(String.format("%.3f", threshold), 0, 3);
        Button.ENTER.waitForPress();

        // Starte Linienfolger
        folgen(threshold);

        // Aufräumen
        colorSensor.close();
        leftMotor.close();
        rightMotor.close();
    }

    private static void folgen(float threshold) {
        final float Kp = 300;       // Proportionalfaktor (justierbar)
        final int baseSpeed = 200;  // Grundgeschwindigkeit

        float[] sample = new float[lightMode.sampleSize()];

        // Endlosschleife bis ESC gedrückt
        while (Button.ESCAPE.isUp()) {
            lightMode.fetchSample(sample, 0);
            float error = sample[0] - threshold;
            float turn  = Kp * error;

            // Motorgeschwindigkeiten berechnen
            int leftSpeed  = (int)(baseSpeed + turn);
            int rightSpeed = (int)(baseSpeed - turn);

            leftMotor.setSpeed(Math.max(0, leftSpeed));
            rightMotor.setSpeed(Math.max(0, rightSpeed));

            leftMotor.forward();
            rightMotor.forward();
        }
        // Bei ESC: stoppen
        leftMotor.stop(true);
        rightMotor.stop();
    }
}
