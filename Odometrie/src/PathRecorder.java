import java.util.ArrayList;

public class PathRecorder {
    private final ArrayList<VectorStep> path = new ArrayList<>();
    private float currentX = 0;
    private float currentY = 0;

    /*
    public void recordStep(int angle, float distance) {
        double radians = Math.toRadians(angle);

        float dx = (float) (Math.cos(radians) * distance);
        float dy = (float) (Math.sin(radians) * distance);

        currentX += dx;
        currentY += dy;

        VectorStep step = new VectorStep(currentX, currentY, angle);
        path.add(step);

        System.out.printf("📍 Gespeichert: %s\n", step);
    }
    */
    
    public void recordStep(int angle, float distance) {
        if (distance < 1.0f) {
            // Nur Drehung – speichere neuen Winkel, aber Position bleibt gleich
            path.add(new VectorStep(currentX, currentY, angle));
            System.out.printf("📐 Nur Drehung: Winkel %d°, Pos bleibt %.1f/%.1f\n", angle, currentX, currentY);
        } else {
            // Bewegung – Position verändern anhand des Winkels
            double radians = Math.toRadians(angle);
            float dx = (float) (Math.cos(radians) * distance);
            float dy = (float) (Math.sin(radians) * distance);

            currentX += dx;
            currentY += dy;

            path.add(new VectorStep(currentX, currentY, angle));
            System.out.printf("➡️ Bewegung: Winkel %d°, Δx=%.1f, Δy=%.1f → Neu: %.1f/%.1f\n", angle, dx, dy, currentX, currentY);
        }
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
        System.out.println("📋 Aufgezeichneter Pfad:");
        for (VectorStep step : path) {
            System.out.println(step);
        }
    }
}
