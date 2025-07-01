package robotik;

import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.utility.Delay;

public class Linienfahrer {

    static final double speed = 100;
    static final int lostlimit = 50;

    static double e = 0;
    static double turn = 0;
    static double integral = 0;
    static double lastError = 0;
    static int lostcounter = 0;

    static final double KP = 400;
    static final double KI = 0.02;
    static final double KD = 450;

    static double threshold = 0;

    static final double OBSTACLE_DISTANCE = 0.08;
    static final double SAFE_DISTANCE = 0.10;

    static boolean justFoundLine = false;
    static int slowDriveCounter = 0;

    enum RobotState {
        FOLLOW_LINE,
        OBSTACLE_DETECTED,
        AVOID_RIGHT,
        PARALLEL_DRIVE,
        TURN_BACK_LEFT,
        RETURN_TO_LINE,
        LOST_LINE
    }

    static RobotState currentState = RobotState.FOLLOW_LINE;
    static int stateCounter = 0;

   public static void main(String[] args) {
        Robot robot = new Robot();
        robot.initRobot();

        threshold = calibration(robot);
        System.out.println("Hinderniserkennung aktiviert!");
        Sound.beepSequence();

        while (true) {
            double light = robot.colorSensor.getReflectedRed();
            double distance = robot.getDistanceValue();

            updateState(robot, light, distance);
            executeState(robot, light, distance);

            if (Button.getButtons() != 0) break;
            Delay.msDelay(30);
            
            if (Button.ESCAPE.isDown()) break;
        }

        robot.leftMotor.stop(true);
        robot.rightMotor.stop();
    }

    private static void updateState(Robot robot, double light, double distance) {
        boolean onLine = Math.abs(light - threshold) <= 0.25;
        boolean obstacleDetected = distance < OBSTACLE_DISTANCE;

        switch (currentState) {
            case FOLLOW_LINE:
                if (obstacleDetected) {
                    currentState = RobotState.OBSTACLE_DETECTED;
                    System.out.println("Hindernis erkannt!");
                    Sound.beep();
                } else if (!onLine) {
                    lostcounter++;
                    if (lostcounter > lostlimit) {
                        currentState = RobotState.LOST_LINE;
                    }
                } else {
                    lostcounter = 0;
                }
                break;

            case OBSTACLE_DETECTED:
                currentState = RobotState.AVOID_RIGHT;
                stateCounter = 0;
                break;

            case RETURN_TO_LINE:
                if (Math.abs(light - threshold) < 0.2) {
                    justFoundLine = true;
                    slowDriveCounter = 0;
                    currentState = RobotState.FOLLOW_LINE;
                    System.out.println("Linie wiedergefunden!");
                }
                break;

            case LOST_LINE:
                if (onLine) {
                    currentState = RobotState.FOLLOW_LINE;
                    lostcounter = 0;
                }
                break;

            default:
                break;
        }
    }

    private static void executeState(Robot robot, double light, double distance) {
        switch (currentState) {
            case FOLLOW_LINE:
                e = getError(light, threshold);
                turn = pidCalculator(e);
                if (justFoundLine) {
                    slowDriveCounter++;
                    drive(robot, speed * 0.4, turn);
                    if (slowDriveCounter > 25) justFoundLine = false;
                } else {
                    drive(robot, speed * 0.8, turn); 
                }

                break;

            case OBSTACLE_DETECTED:
                drive(robot, speed * 0.3, 0);
                break;

            case AVOID_RIGHT:
                // 90° Rechtsdrehung
                robot.leftMotor.setSpeed((float) speed);
                robot.rightMotor.setSpeed((float) speed);
                robot.leftMotor.forward();
                robot.rightMotor.backward();
                Delay.msDelay(800);
                robot.leftMotor.stop(true);
                robot.rightMotor.stop();

                currentState = RobotState.PARALLEL_DRIVE;
                stateCounter = 0;
                break;

            case PARALLEL_DRIVE:
                // Geradeaus neben Hindernis fahren
                robot.leftMotor.setSpeed((float)(speed * 0.6));
                robot.rightMotor.setSpeed((float)(speed * 0.6));
                robot.leftMotor.forward();
                robot.rightMotor.forward();
                Delay.msDelay(8000);
                robot.leftMotor.stop(true);
                robot.rightMotor.stop();

                currentState = RobotState.TURN_BACK_LEFT;
                break;

            case TURN_BACK_LEFT:
                // 90° Linksdrehung
                robot.leftMotor.setSpeed((float) speed);
                robot.rightMotor.setSpeed((float) speed);
                robot.leftMotor.backward();
                robot.rightMotor.forward();
                Delay.msDelay(1300);
                robot.leftMotor.stop(true);
                robot.rightMotor.stop();

                currentState = RobotState.RETURN_TO_LINE;
                break;

            case RETURN_TO_LINE:
                robot.leftMotor.setSpeed((float)(speed * 0.5));
                robot.rightMotor.setSpeed((float)(speed * 0.5));
                robot.leftMotor.forward();
                robot.rightMotor.forward();
                Delay.msDelay(30);
                break;

            case LOST_LINE:
                integral = 0;
                lastError = 0;
                robot.leftMotor.setSpeed((float) speed);
                robot.rightMotor.setSpeed((float) speed);
                if (light < threshold) {
                    robot.leftMotor.forward();
                    robot.rightMotor.backward();
                } else {
                    robot.leftMotor.backward();
                    robot.rightMotor.forward();
                }
                break;
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
        integral = clamp(integral + e, -8, 8);
        double derivative = e - lastError;
        lastError = e;
        return clamp(KP * e + KI * integral + KD * derivative, -speed, speed);
    }

    private static double getError(double is, double should) {
        return clamp(is - should, -1, 1);
    }

    private static void drive(Robot robot, double speed, double turn) {
        double correction = clamp(turn, -300, 300);
        double leftSpeed = clamp(speed - correction, -speed, speed);
        double rightSpeed = clamp(speed + correction, -speed, speed);
        robot.leftMotor.setSpeed((float) Math.abs(leftSpeed));
        robot.rightMotor.setSpeed((float) Math.abs(rightSpeed));
        if (leftSpeed >= 0) robot.leftMotor.forward();
        else robot.leftMotor.backward();
        if (rightSpeed >= 0) robot.rightMotor.forward();
        else robot.rightMotor.backward();
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
    
}
