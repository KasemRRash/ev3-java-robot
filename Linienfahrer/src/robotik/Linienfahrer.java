package robotik;

import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.utility.Delay;

public class Linienfahrer {

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
    
    public static void main(String[] args) {
        Robot robot = new Robot();
        robot.initRobot();

        // Kalibrierung
        threshold = calibration(robot); 
        
        while (true) {
            double light = robot.colorSensor.getReflectedRed();
            //e = getError( light, threshold);
            
            // getTurn = einfache Bestimmung durch p 
            //turn = KP * e; 
            
            // PID Berechnung 
            //turn = pidCalculator(e); 
            
            //drive(robot, speed, turn);
            
            
            boolean lostLine = (light < threshold - 0.3) || (light > threshold + 0.3);
            if (lostLine) {
            	integral = 0; 
            	lastError = 0;
            }
            
            boolean onLine = Math.abs(light - threshold) <= 0.25;
            
            if (onLine) {
            	lostcounter = 0; 
                // PID fahren
                e = getError(light, threshold);
                turn = pidCalculator(e);
                drive(robot, speed, turn);
            } else {
            	++lostcounter; 
            	
            	if (lostcounter > lostlimit) {
                    System.out.println("Linie dauerhaft verloren – STOP");
                    Sound.beepSequence();
                    break; 
                }
            	
                // Linie verloren: auf der Stelle drehen
                System.out.println("Linie verloren – Drehe auf der Stelle...");
                integral = 0;
                lastError = 0;
                
                if (light < threshold) {
                	//Delay.msDelay(500);
                    // Linie wurde überfahren -> Drehen nach rechts (zurück nach außen)
                    robot.leftMotor.setSpeed((float) speed);
                    robot.rightMotor.setSpeed((float) speed);
                    
                    robot.leftMotor.forward();
                    robot.rightMotor.backward();
                    
                  //Abbruch
                    if (Button.getButtons() != 0) {
                        break;
                    }
                    
                } else {
                	//Delay.msDelay(500);
                    // Linie zu weit weg -> Drehen nach links (zur Linie hin)
                    robot.leftMotor.setSpeed((float) speed);
                    robot.rightMotor.setSpeed((float) speed);
                    robot.leftMotor.backward();
                    robot.rightMotor.forward();
                    
                  //Abbruch
                    if (Button.getButtons() != 0) {
                        break;
                    }
                }
            }
        /*DriveState state = DriveState.FOLLOW_LINE;
        
        while (true) {
            double light = robot.colorSensor.getReflectedRed();
            boolean onLine = Math.abs(light - threshold) <= 0.25;

            if (onLine) {
                state = DriveState.FOLLOW_LINE;
                lostcounter = 0;
            } else {
                lostcounter++;
                if (lostcounter > lostlimit) {
                    state = DriveState.LOST_TOO_LONG;
                } else {
                    // Linie überfahren (schwarz) vs. zu weit weg (weiß)
                    if (light < threshold) {
                        state = DriveState.LOST_RIGHT;
                    } else {
                        state = DriveState.LOST_LEFT;
                    }
                }
            }

            // Zustand umsetzen
            switch (state) {
                case FOLLOW_LINE:
                    e = getError(light, threshold);
                    turn = pidCalculator(e);
                    drive(robot, speed, turn);
                    break;

                case LOST_LEFT:
                    integral = 0;
                    lastError = 0;
                    drive(robot, -speed, turn);  // Drehen nach rechts
                    break;

                case LOST_RIGHT:
                    integral = 0;
                    lastError = 0;
                    drive(robot, speed, -turn);  // Drehen nach links
                    break;

                case LOST_TOO_LONG:
                    //Sound.beepSequence();
                    state = DriveState.STOP;
                    break;

                case STOP:
                    robot.leftMotor.stop(true);
                    robot.rightMotor.stop();
                    return;  // beende main
            }*/

            // Benutzerabbruch
            if (Button.getButtons() != 0) {
                break;
            }
        }
     // Nach der Schleife: Anhalten
        robot.leftMotor.stop(true);
        robot.rightMotor.stop();
    }
    
    private static double calibration(Robot robot) {
    	
    	// getReflectedRed() liefert immer den reflektierten Rotanteil
        // Min: 0.0 = schwarzes Licht - es wurde sehr wenig rotes Licht reflektiert
        // Max: 1.0 = weißes Licht
    	
    	System.out.println("Setze Sensor auf WEISS (Hintergrund) und drücke Taste");
        Button.waitForAnyPress();
        
        double white = robot.colorSensor.getReflectedRed();
        Delay.msDelay(1000);
        
        System.out.println("Setze Sensor auf SCHWARZ (Linie) und drücke Taste");
        Button.waitForAnyPress();
        double black = robot.colorSensor.getReflectedRed();
        Delay.msDelay(1000);

        double threshold = (white + black) / 2;
        Delay.msDelay(1000);
        
    	return threshold; 
    }
    
    private static double pidCalculator(double e) {
       
    	// PID-Berechnung
        integral = clamp(integral + e, -8, 8);
        double derivative = e - lastError;
        lastError = e;

        // Kompletter Regelausgang (turn)
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
        
    	System.out.println("Werte in der Methode drive - Speed: " + speed + ", Turn: " + turn); 
    	    	
    	robot.leftMotor.setSpeed((float) (leftSpeed));
    	robot.rightMotor.setSpeed((float) (rightSpeed));

    	Direction leftDir = (leftSpeed >= 0) ? Direction.FORWARD : Direction.BACKWARD;
    	Direction rightDir = (rightSpeed >= 0) ? Direction.FORWARD : Direction.BACKWARD;
    	switch (leftDir) {
        
    	case FORWARD:
            robot.leftMotor.forward();
            break;
        case BACKWARD:
            robot.leftMotor.backward();
            break;
    	}

    	switch (rightDir) {
        	case FORWARD:
        		robot.rightMotor.forward();
        		break;
        	case BACKWARD:
        		robot.rightMotor.backward();
        		break;
    	}
    
    }
    
    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
    
    enum Direction {
        FORWARD,
        BACKWARD
    }
    enum DriveState {
        FOLLOW_LINE,
        LOST_LEFT,
        LOST_RIGHT,
        LOST_TOO_LONG,
        STOP
    }
}
