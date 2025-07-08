

import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.utility.Delay;

public class KolonneFahren {
	
    static final double speed = 100;
    static double e = 0; 
    static double turn = 0; 
    static int lostcounter = 0; 
    static final int lostlimit = 50;
    
    // PID-Konstanten 
    static final double KP = 300;
    static final double KI = 0.01;
    static final double KD = 380;
    
    // PID-Variablen
    static double integral = 0;
    static double lastError = 0;
    
    static double threshold = 0; 
    
    // Kolonnenfahren Parameter
    static final double TARGET_DISTANCE = 0.20; // 20cm Abstand zum Vordermann
    static final double MIN_DISTANCE = 0.15;    // 15cm Mindestabstand
    static final double DISTANCE_TOLERANCE = 0.05; // 5cm Toleranz
    static final double CONVOY_SPEED_FACTOR = 0.8; // Reduktion der Grundgeschwindigkeit
    
    public static void main(String[] args) {
        Robot robot = new Robot();
        robot.initRobot();

        // Kalibrierung
        threshold = calibration(robot); 
        
        System.out.println("Kolonnenfahren aktiviert!");
        Sound.beepSequence();
        
        while (true) {
            double light = robot.colorSensor.getReflectedRed();
            double distance = robot.getDistanceValue();
            
            // Abstand zum Vordermann prüfen
            double adjustedSpeed = calculateConvoySpeed(distance);
            
            boolean lostLine = (light < threshold - 0.3) || (light > threshold + 0.3);
            if (lostLine) {
                integral = 0; 
                lastError = 0;
            }
            
            boolean onLine = Math.abs(light - threshold) <= 0.25;
            
            if (onLine) {
                lostcounter = 0; 
                // pid fahren mit angepasster Geschwindigkeit
                e = getError(light, threshold);
                turn = pidCalculator(e);
                
                // nur fahren wenn der Abstand stimmt
                if (distance > MIN_DISTANCE) {
                    drive(robot, adjustedSpeed, turn);
                } else {
                    // Ist zu nah, dann  stoppen
                    robot.leftMotor.stop(true);
                    robot.rightMotor.stop(true);
                    System.out.println("Zu nah am Vordermann  warte...");
                }
            } else {
                ++lostcounter; 
                
                
                
                // Linie verloren: auf der Stelle drehen (nur wenn genug Abstand)
                if (distance > MIN_DISTANCE) {
                    System.out.println("Linie verloren. Drehe auf der Stelle...");
                    integral = 0;
                    lastError = 0;
                    
                    if (light < threshold) {
                        // Drehen nach rechts
                        robot.leftMotor.setSpeed((float) (adjustedSpeed * 0.8));
                        robot.rightMotor.setSpeed((float) (adjustedSpeed * 0.8));
                        robot.leftMotor.forward();
                        robot.rightMotor.backward();
                    } else {
                        // Drehen nach links
                        robot.leftMotor.setSpeed((float) (adjustedSpeed * 0.8));
                        robot.rightMotor.setSpeed((float) (adjustedSpeed * 0.8));
                        robot.leftMotor.backward();
                        robot.rightMotor.forward();
                    }
                }
            }

            // ausmachen durch den button
            if (Button.getButtons() != 0) {
                break;
            }
            
            Delay.msDelay(10); // Kurze Pause für die sensoren
        }
        
        // Nach der Schleife: Anhalten
        robot.leftMotor.stop(true);
        robot.rightMotor.stop();
    }
    
    /**
     * Berechnet die angepasste Geschwindigkeit basierend auf dem Abstand zum Vordermann
     */
    private static double calculateConvoySpeed(double distance) {
        if (distance < MIN_DISTANCE) {
            return 0; // Stopp
        } else if (distance < TARGET_DISTANCE - DISTANCE_TOLERANCE) {
            // Zu nah ==> langsamer fahren
            double factor = (distance - MIN_DISTANCE) / (TARGET_DISTANCE - MIN_DISTANCE);
            return speed * CONVOY_SPEED_FACTOR * factor * 0.5; // Sehr langsam
        } else if (distance < TARGET_DISTANCE + DISTANCE_TOLERANCE) {
            // idealer Abstand ==> normale Kolonnengeschwindigkeit
            return speed * CONVOY_SPEED_FACTOR;
        } else if (distance < TARGET_DISTANCE + 0.20) {
            // Etwas zu weit ==> beschleunigen
            return speed * CONVOY_SPEED_FACTOR * 1.2;
        } else {
            // Sehr weit weg ==> volle Geschwindigkeit
            return speed;
        }
    }
    
    private static double calibration(Robot robot) {
        System.out.println("Setze Sensor auf WEISS (Hintergrund) und drücke Taste");
        Button.waitForAnyPress();
        
        double white = robot.colorSensor.getReflectedRed();
        Delay.msDelay(1000);
        
        System.out.println("Setze Sensor auf SCHWARZ (Linie) und drücke Taste");
        Button.waitForAnyPress();
        double black = robot.colorSensor.getReflectedRed();
        Delay.msDelay(1000);

        double threshold = (white + black) / 2;
        System.out.println("Kalibrierung abgeschlossen. Threshold: " + threshold);
        Delay.msDelay(1000);
        
        return threshold; 
    }
    
    private static double pidCalculator(double e) {
        // PID-Berechnung
        integral = clamp(integral + e, -8, 8);
        double derivative = e - lastError;
        lastError = e;

        return clamp(KP * e + KI * integral + KD * derivative, -speed, speed);
    }
    
    private static double getError(double is, double should) {
        e = clamp(is - should, -1, 1);  
        return e; 
    }
   
    private static void drive(Robot robot, double speed, double turn) {
        double correction = clamp(turn, -300, 300);
        double leftSpeed = clamp(speed - correction, -speed, speed);
        double rightSpeed = clamp(speed + correction, -speed, speed);
        
        robot.leftMotor.setSpeed((float) Math.abs(leftSpeed));
        robot.rightMotor.setSpeed((float) Math.abs(rightSpeed));

        if (leftSpeed >= 0) {
            robot.leftMotor.forward();
        } else {
            robot.leftMotor.backward();
        }

        if (rightSpeed >= 0) {
            robot.rightMotor.forward();
        } else {
            robot.rightMotor.backward();
        }
    }
    
    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
