import lejos.hardware.motor.Motor;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.Button;

public class Test {
    public static void main(String[] args) {

        // Motoren initialisieren
        EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(MotorPort.B); //links Rad
        EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(MotorPort.C); //rechter Rad
        //2 PIN ist entfernungssensor (Kolonee)
        //4
        //3

        // Geschwindigkeit setzen
        leftMotor.setSpeed(150);   // langsamer
        rightMotor.setSpeed(300);  // schneller

        // Beide Motoren vorwärts fahren
        leftMotor.forward();
        rightMotor.forward();

        // Fährt so lange, bis eine Taste gedrückt wird
        Button.waitForAnyPress();

        // Motoren stoppen
        leftMotor.stop(true);
        rightMotor.stop();

        // Motoren freigeben (optional)
        leftMotor.close();
        rightMotor.close();
    }
}
