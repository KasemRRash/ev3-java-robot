package TEST;

import robotik.Robot;

public class tester {
	
    static final double speed = 100;
    static double e = 0; 
    static double turn = 0; 
    static double pidTurn = 0; 
    
    // PID-Konstanten 
    static final double KP = 150;
    static final double KI = 0.01;
    static final double KD = 30;
    
    // PID-Variablen
    static double integral = 0;
    static double lastError = 0;
	
	public static void main(String[]args) {
		int i = 0; 
		
		double white = 0.7; 
		double black = 0.3; 
		double threshold = (black + white) / 2.0;
		System.out.println("Schwelle: " + threshold); 	
		
		while(i!=2) {
			double light = 0.7; 
			getError( light, threshold);
			System.out.println("While Schleife - e: " + e); 
			 
            // getTurn = einfache Bestimmung durch p 
            //turn = getTurn();
            //System.out.println("While Schleife - turn: " + turn);
            
            // PID Berechnung 
            pidTurn = pidCalculator();
            System.out.println("While Schleife - pidTurn: " + pidTurn);
            
            drive(speed, pidTurn);
            
			++i;
		}
		
	}
    private static double pidCalculator() {
        
    	// PID-Berechnung
        integral += e;
        System.out.println("pidCalculator Methode - integral: " + integral); 
        double derivative = e - lastError;
        System.out.println("pidCalculator Methode - derivative: " + derivative); 
        lastError = e;
        System.out.println("pidCalculator Methode - lastError: " + lastError); 
        
        // Kompletter Regelausgang (turn)
        pidTurn= KP * e + KI * integral + KD * derivative;
        System.out.println("pidCalculator Methode - pidTurn: " + pidTurn); 
        return pidTurn;
        
        //Begrenzung einbauen
    }
    
    private static double getTurn() {
    	
    	double p = 0.1;
    	turn = e * p; 
    	System.out.println("getTurn Methode - turn: " + turn); 
    	
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
    
    private static void drive(double speed, double turn) {
    	double sright = 0; 
    	double sleft = 0; 
    	
    	System.out.println("Drive Methode - Speed: " + speed + ", Turn: " + turn); 
    	
    	
    	if ( turn >= 0) {
    		sleft = speed; 
    		sright = turn * (-speed / 50) + speed;
    		if (sright > 100) {
    			sright = 100; 
    		}else if (sright < -100) {
    			sright = -100; 
    		}
    		System.out.println("sright wenn turn größer Null: " + sright); 
    		
    	}else if ( turn <= 0 ) {
    		sleft = turn * (speed /50) - speed;
    		sright = speed; 
    		if (sleft > 100) {
    			sleft = 100; 
    		}else if (sleft < -100) {
    			sleft = -100; 
    		}
    		System.out.println("sleft wenn turn kleiner Null: " + sleft);
    	}
    }
}
