import java.util.ArrayList;

public class PathRecorder {
    private final ArrayList<VectorStep> path = new ArrayList<>();
    private float currentX = 0;
    private float currentY = 0;

    public void recordStep(int angle, float distance) {
        angle = ((angle % 360) + 360) % 360;

        /*if (distance < 1.0f) {
            path.add(new VectorStep(currentX, currentY, angle));
            System.out.printf("Nur Drehung: Winkel %d Grad, Pos bleibt %.1f/%.1f\n", angle, currentX, currentY);
        } else {*/
            float dx = cosFromDegree(angle) * distance;
            float dy = sinFromDegree(angle) * distance;

            currentX += dx;
            currentY += dy;

            path.add(new VectorStep(currentX, currentY, angle));
            System.out.printf("Bewegung: Winkel %d Grad, y=%.1f, x=%.1f => Neu: %.1f/%.1f\n", angle, dy, dx, currentY, currentX);
        //}
    }

    private float sinFromDegree(int angle) {
        angle = ((angle % 360) + 360) % 360;

        switch (angle) {
            case 0: return 0f;
            case 30: return 0.5f;
            case 45: return 0.7071f;
            case 60: return 0.8660f;
            case 90: return 1f;
            case 120: return 0.8660f;
            case 135: return 0.7071f;
            case 150: return 0.5f;
            case 180: return 0f;
            case 210: return -0.5f;
            case 225: return -0.7071f;
            case 240: return -0.8660f;
            case 270: return -1f;
            case 300: return -0.8660f;
            case 315: return -0.7071f;
            case 330: return -0.5f;
            case 360: return 0f;
            default:
                double radians = Math.toRadians(angle);
                return (float) Math.sin(radians); // Fallback
        }
    }

    private float cosFromDegree(int angle) {
        return sinFromDegree(angle + 90); // cos(x) = sin(x + 90°)
    }

    public ArrayList<VectorStep> getPath() {
        return path;
    }

    public void clear() {
        path.clear();
        currentX = 0;
        currentY = 0;
    }

    public void printSteps() {
        System.out.println("Aufgezeichneter Pfad:");
        for (VectorStep step : path) {
            System.out.println(step);
        }
    }
}