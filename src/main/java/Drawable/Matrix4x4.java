package Drawable;

public class Matrix4x4 {
    private double[][] values;

    public Matrix4x4(double[][] values) {
        this.values = values;
    }

    public static Matrix4x4 identity() {
        return new Matrix4x4(new double[][]{
                {1, 0, 0, 0},
                {0, 1, 0, 0},
                {0, 0, 1, 0},
                {0, 0, 0, 1}
        });
    }

    public static Matrix4x4 rotation(Vector3 axis, double angle) {
        double rad = Math.toRadians(angle);
        double cos = Math.cos(rad);
        double sin = Math.sin(rad);
        double x = axis.x, y = axis.y, z = axis.z;

        return new Matrix4x4(new double[][]{
                {cos + x * x * (1 - cos), x * y * (1 - cos) - z * sin, x * z * (1 - cos) + y * sin, 0},
                {y * x * (1 - cos) + z * sin, cos + y * y * (1 - cos), y * z * (1 - cos) - x * sin, 0},
                {z * x * (1 - cos) - y * sin, z * y * (1 - cos) + x * sin, cos + z * z * (1 - cos), 0},
                {0, 0, 0, 1}
        });
    }

    public static Matrix4x4 translation(Vector3 translation) {
        return new Matrix4x4(new double[][]{
                {1, 0, 0, translation.x},
                {0, 1, 0, translation.y},
                {0, 0, 1, translation.z},
                {0, 0, 0, 1}
        });
    }

    public static Matrix4x4 transformation(Vector3 position, Matrix4x4 rotation) {
        double[][] values = rotation.values;
        return new Matrix4x4(new double[][]{
                {values[0][0], values[0][1], values[0][2], position.x},
                {values[1][0], values[1][1], values[1][2], position.y},
                {values[2][0], values[2][1], values[2][2], position.z},
                {0, 0, 0, 1}
        });
    }

    public Matrix4x4 applyTranslation(Vector3 translation) {
        Matrix4x4 translationMatrix = Matrix4x4.translation(translation);
        return this.multiply(translationMatrix);
    }

    public Matrix4x4 applyRotation(Vector3 axis, double angle) {
        Matrix4x4 rotationMatrix = Matrix4x4.rotation(axis, angle);
        return this.multiply(rotationMatrix);
    }

    public Vector3 extractPosition() {
        return new Vector3(values[0][3], values[1][3], values[2][3]);
    }

    public Matrix4x4 extractOrientation() {
        double[][] orientationValues = {
                {values[0][0], values[0][1], values[0][2], 0},
                {values[1][0], values[1][1], values[1][2], 0},
                {values[2][0], values[2][1], values[2][2], 0},
                {0, 0, 0, 1}
        };
        return new Matrix4x4(orientationValues);
    }

    public Vector3 extractForward() {
        return new Vector3(values[0][2], values[1][2], values[2][2]).normalize();
    }

    public Vector3 extractUp() {
        return new Vector3(values[0][1], values[1][1], values[2][1]).normalize();
    }

    public Vector3 extractRight() {
        return new Vector3(values[0][0], values[1][0], values[2][0]).normalize();
    }

    public Matrix4x4 multiply(Matrix4x4 other) {
        double[][] result = new double[4][4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                for (int k = 0; k < 4; k++) {
                    result[i][j] += this.values[i][k] * other.values[k][j];
                }
            }
        }
        return new Matrix4x4(result);
    }

    public Vector3 transform(Vector3 v) {
        double x = v.x * values[0][0] + v.y * values[0][1] + v.z * values[0][2] + values[0][3];
        double y = v.x * values[1][0] + v.y * values[1][1] + v.z * values[1][2] + values[1][3];
        double z = v.x * values[2][0] + v.y * values[2][1] + v.z * values[2][2] + values[2][3];
        return new Vector3(x, y, z);
    }

    public float[] toArray() {
        float[] array = new float[16];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                array[i * 4 + j] = (float) values[i][j];
            }
        }
        return array;
    }
}
