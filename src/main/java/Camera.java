import Drawable.Matrix4x4;
import Drawable.Vector3;

public class Camera {
    private double fov;
    private Matrix4x4 transformation;

    public Camera(Vector3 position, Matrix4x4 orientation, double fov) {
        this.transformation = Matrix4x4.transformation(position, orientation);
        this.fov = fov;
    }

    public double getFov() {
        return fov;
    }

    public float[] getTransformation() {
        return transformation.toArray();
    }

    public Vector3 getPosition() {
        return transformation.extractPosition();
    }

    public Matrix4x4 getOrientation() {
        return transformation.extractOrientation();
    }

    public void move(Vector3 delta) {
        this.transformation = transformation.applyTranslation(delta);
    }

    public void rotate(Vector3 axis, double angle) {
        this.transformation = transformation.applyRotation(axis, angle);
    }

    public Vector3 getForward() {
        return transformation.extractForward();
    }

    public Vector3 getUp() {
        return transformation.extractUp();
    }

    public Vector3 getRight() {
        return transformation.extractRight();
    }
}
