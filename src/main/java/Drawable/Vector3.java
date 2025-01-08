package Drawable;

public class Vector3 {
    public double x, y, z;

    public Vector3(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public float[] toArray() {
        return new float[]{(float) x, (float) y, (float) z};
    }

    public Vector3 add(Vector3 other) {
        return new Vector3(x + other.x, y + other.y, z + other.z);
    }

    public Vector3 subtract(Vector3 other) {
        return new Vector3(x - other.x, y - other.y, z - other.z);
    }

    public Vector3 multiply(double scalar) {
        return new Vector3(x * scalar, y * scalar, z * scalar);
    }

    public Vector3 normalize() {
        double length = magnitude();
        if (length == 0) return new Vector3(0, 0, 0);
        return new Vector3(x / length, y / length, z / length);
    }

    public double magnitude() {
        return Math.sqrt(x * x + y * y + z * z);
    }

    public double dot(Vector3 other) {
        return x * other.x + y * other.y + z * other.z;
    }

    public Vector3 cross(Vector3 other) {
        return new Vector3(
                y * other.z - z * other.y,
                z * other.x - x * other.z,
                x * other.y - y * other.x
        );
    }

    public Vector3 rotateX(double angle) {
        double rad = Math.toRadians(angle);
        double newY = y * Math.cos(rad) - z * Math.sin(rad);
        double newZ = y * Math.sin(rad) + z * Math.cos(rad);
        return new Vector3(x, newY, newZ);
    }

    public Vector3 rotateY(double angle) {
        double rad = Math.toRadians(angle);
        double newX = x * Math.cos(rad) + z * Math.sin(rad);
        double newZ = -x * Math.sin(rad) + z * Math.cos(rad);
        return new Vector3(newX, y, newZ);
    }

    public Vector3 rotateZ(double angle) {
        double rad = Math.toRadians(angle);
        double newX = x * Math.cos(rad) - y * Math.sin(rad);
        double newY = x * Math.sin(rad) + y * Math.cos(rad);
        return new Vector3(newX, newY, z);
    }

    public Vector3 transform(Vector3 cameraPosition, Vector3 cameraForward, Vector3 cameraUp, Vector3 cameraRight) {
        Vector3 relative = this.subtract(cameraPosition);
        return new Vector3(
                relative.x * cameraRight.x + relative.y * cameraRight.y + relative.z * cameraRight.z,
                relative.x * cameraUp.x + relative.y * cameraUp.y + relative.z * cameraUp.z,
                relative.x * cameraForward.x + relative.y * cameraForward.y + relative.z * cameraForward.z
        );
    }

    public Vector3 rotateAroundAxis(Vector3 axis, double angle) {
        double rad = Math.toRadians(angle);
        double cosTheta = Math.cos(rad);
        double sinTheta = Math.sin(rad);

        return this.multiply(cosTheta)
                .add(axis.cross(this).multiply(sinTheta))
                .add(axis.multiply(axis.dot(this) * (1 - cosTheta)));
    }

    public double distanceTo(Vector3 other) {
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        double dz = this.z - other.z;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    public double getComponent(int index) {
        return switch (index) {
            case 0 -> x;
            case 1 -> y;
            case 2 -> z;
            default -> throw new IllegalArgumentException("Index out of bounds: " + index);
        };
    }

    @Override
    public String toString() {
        return String.format("Vector3(%.2f, %.2f, %.2f)", x, y, z);
    }
}

