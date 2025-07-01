import java.util.ArrayList;

public class PathRecorder {
    private final ArrayList<VectorStep> path = new ArrayList<>();
    private float currentX = 0;
    private float currentY = 0;

    public void recordStep(int angle, float distance) {
        double radians = Math.toRadians(angle); // Winkel in Bogenmaß

        float dx = (float) (Math.cos(radians) * distance); // x-Anteil
        float dy = (float) (Math.sin(radians) * distance); // y-Anteil

        currentX += dx;
        currentY += dy;

        path.add(new VectorStep(currentX, currentY, angle));
        System.out.printf("Recorded: %s%n", path.get(path.size() - 1));
    }

    public ArrayList<VectorStep> getPath() {
        return path;
    }

    public ArrayList<VectorStep> getInvertedPath() {
        ArrayList<VectorStep> reversed = new ArrayList<>();
        for (int i = path.size() - 1; i >= 0; i--) {
            VectorStep step = path.get(i);
            int reversedAngle = normalizeAngle(step.angle + 180);
            reversed.add(new VectorStep(step.x, step.y, reversedAngle));
        }
        return reversed;
    }

    public void clear() {
        path.clear();
        currentX = 0;
        currentY = 0;
    }

    private int normalizeAngle(int angle) {
        angle = angle % 360;
        if (angle > 180) angle -= 360;
        if (angle < -180) angle += 360;
        return angle;
    }

    public void printSteps() {
        System.out.println("Gespeicherter Pfad:");
        for (VectorStep s : path) {
            System.out.println(s);
        }
    }
}
