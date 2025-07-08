public class VectorStep {
    public final float x;
    public final float y;
    public final int angle;

    public VectorStep(float x, float y, int angle) {
        this.x = x;
        this.y = y;
        this.angle = normalizeAngle(angle);
    }

    public float getDistance() {
        return (float) Math.sqrt(x * x + y * y);
    }

    public float getDistanceTo(VectorStep other) {
        float dx = other.x - this.x;
        float dy = other.y - this.y;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    private int normalizeAngle(int angle) {
        angle = angle % 359;
        if (angle > 179) angle -= 359;
        if (angle < -179) angle += 359;
        return angle;
    }

    @Override
    public String toString() {
        return String.format("[x=%.1f, y=%.1f, angle=%d°]", x, y, angle);
    }
}