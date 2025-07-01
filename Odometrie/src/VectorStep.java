public class VectorStep {
    public final float x;
    public final float y;
    public final int angle;

    public VectorStep(float x, float y, int angle) {
        this.x = x;
        this.y = y;
        this.angle = angle;
    }

    @Override
    public String toString() {
        return String.format("x=%.2f, y=%.2f, angle=%d°", x, y, angle);
    }
}
