package robotik;

import lejos.hardware.Button;
import lejos.utility.Delay;

public class Linienfahrer {

    static final double speed = 100;
    static double e = 0; 
    static double turn = 0; 
    
    // PID-Konstanten 
    static final double KP = 150;
    static final double KI = 0.01;
    static final double KD = 30;
    
    // PID-Variablen
    static double integral = 0;
    static double lastError = 0;
    
    
    
    public static void main(String[] args) {
        Robot robot = new Robot();
        robot.initRobot();

        // === Kalibrierung ===
        //getReflectedRed() liefert immer den reflektierten Rotanteil
        // Min: 0.0 = schwarzes Licht - es wurde sehr wenig rotes Licht reflektiert
        // Max: 1.0 = weißes Licht
        System.out.println("Setze Sensor auf WEISS (Hintergrund) und drücke Taste");
        Button.waitForAnyPress();
        
        double white = robot.colorSensor.getReflectedRed();
        System.out.println("White Messung: " + white);
        Button.waitForAnyPress();
        
        System.out.println("Setze Sensor auf SCHWARZ (Linie) und drücke Taste");
        Button.waitForAnyPress();
        
        double black = robot.colorSensor.getReflectedRed();
        System.out.println("Black Messung: " + black);
        Button.waitForAnyPress();

        double threshold = (black + white) / 2.0;
        System.out.println("Start mit Schwelle: " + threshold);
        Button.waitForAnyPress();

        
        while (true) {
            double light = robot.colorSensor.getReflectedRed();
            System.out.println("Light Messung: " + light); 
            getError( light, threshold);
            
            // getTurn = einfache Bestimmung durch p 
            turn = getTurn();
            
            // PID Berechnung 
            pidCalculator(); 
            
            drive(robot, speed, turn);
            
            //Abbruch
            if (Button.getButtons() != 0) {
                break;
            }
        }  
     // Nach der Schleife: Anhalten
        robot.leftMotor.stop(true);
        robot.rightMotor.stop();
    }
    
    private static double pidCalculator() {
       
    	// PID-Berechnung
        integral += e;
        double derivative = e - lastError;
        lastError = e;

        // Kompletter Regelausgang (turn)
        turn = KP * e + KI * integral + KD * derivative;
        return turn;
        
        //Begrenzung einbauen
    }
    
    private static double getTurn() {
    	
    	double p = 0.1;
    	turn = e * p; 
    	
    	return turn; 
    }
    
    private static double getError(double is, double should) {
    	
    	if (e > 1 ) {
    		e = 1; 
    	}else if (e < -1){
    		e = -1; 
    	}
    	
    	e = should - is; 
    	
    	System.out.println("getError Methode - e: " + e); 
    	
    	return e; 
         
    }
   
    
    private static void drive(Robot robot, double speed, double turn) {
    	double sright = 0; 
    	double sleft = 0; 
    	
    	System.out.println("Werte in der Methode drive - Speed: " + speed + ", Turn: " + turn); 
    	
    	
    	if ( turn >= 0) {
    		sleft = speed; 
    		sright = turn * (-speed / 50) + speed;
    		if (sright > 100) {
    			sright = 100; 
    		}else if (sright < -100) {
    			sright = -100; 
    		}
    	}else if ( turn < 0 ) {
    		sleft = turn * (speed /50) - speed;
    		sright = speed; 
    		if (sleft > 100) {
    			sleft = 100; 
    		}else if (sleft < -100) {
    			sleft = -100; 
    		}
    	}
    	
    	robot.leftMotor.setSpeed((int) sleft);
  	    robot.rightMotor.setSpeed((int) sright);
  	    
  	  if (sleft >= 0) {
          robot.leftMotor.forward();
      } else {
          robot.leftMotor.backward();
      }

      if (sright >= 0) {
          robot.rightMotor.forward();
      } else {
          robot.rightMotor.backward();
      }
    }
}
