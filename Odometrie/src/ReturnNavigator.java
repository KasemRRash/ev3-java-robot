import java.util.List;

public class ReturnNavigator {
    private final MotorTracker motor;

    public ReturnNavigator(MotorTracker motor) {
        this.motor = motor;
    }

    public void followPath(List<VectorStep> path) {
        System.out.println("Starte Rückfahrt rückwärts, ohne Rotation…");

        for (int i = path.size() - 1; i >= 0; i--) {
            VectorStep step = path.get(i);
            float distance = getStepDistance(i, path);

            System.out.printf("Fahre rückwärts %.2f mm entlang Winkel %.2f°%n",
                    distance, (float) normalizeAngle(step.angle));

            motor.travelBackward(distance);
        }

        motor.stop();
    }

    private float getStepDistance(int i, List<VectorStep> path) {
        if (i == 0) return 0;

        VectorStep from = path.get(i);
        VectorStep to = path.get(i - 1);

        float dx = to.x - from.x;
        float dy = to.y - from.y;

        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    private int normalizeAngle(int angle) {
        angle = angle % 360;
        if (angle > 180) angle -= 360;
        if (angle < -180) angle += 360;
        return angle;
    }
}
