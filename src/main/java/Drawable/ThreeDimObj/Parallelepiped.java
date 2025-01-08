package Drawable.ThreeDimObj;

import Drawable.Material;
import Drawable.Shape;
import Drawable.Vector3;

import java.awt.*;
import java.util.Arrays;

public class Parallelepiped extends Shape {
    public double width, height, depth;
    private Vector3[] corners;

    public Parallelepiped(Vector3 position, Color color,
                          double width, double height, double depth, Vector3 rotation, Material material) {
        super(position, color, rotation, material);
        this.width = width;
        this.height = height;
        this.depth = depth;
        initializeCorners();
    }

    private void initializeCorners() {
        // Задаем вершины относительно центра позиции
        corners = new Vector3[]{
                new Vector3(position.x - width / 2, position.y - height / 2, position.z - depth / 2),
                new Vector3(position.x + width / 2, position.y - height / 2, position.z - depth / 2),
                new Vector3(position.x + width / 2, position.y + height / 2, position.z - depth / 2),
                new Vector3(position.x - width / 2, position.y + height / 2, position.z - depth / 2),
                new Vector3(position.x - width / 2, position.y - height / 2, position.z + depth / 2),
                new Vector3(position.x + width / 2, position.y - height / 2, position.z + depth / 2),
                new Vector3(position.x + width / 2, position.y + height / 2, position.z + depth / 2),
                new Vector3(position.x - width / 2, position.y + height / 2, position.z + depth / 2)
        };
        rotate(rotation.x, rotation.y, rotation.z);
        System.out.println("Color: " + color.toString());
        System.out.println("Corners: " + Arrays.toString(corners));
    }


    @Override
    public double intersect(Vector3 origin, Vector3 direction) {
        double tMin = Double.NEGATIVE_INFINITY;
        double tMax = Double.POSITIVE_INFINITY;

        Vector3 min = position.subtract(new Vector3(width / 2, height / 2, depth / 2));
        Vector3 max = position.add(new Vector3(width / 2, height / 2, depth / 2));

        for (int i = 0; i < 3; i++) {
            double invD = 1.0 / direction.getComponent(i);
            double t0 = (min.getComponent(i) - origin.getComponent(i)) * invD;
            double t1 = (max.getComponent(i) - origin.getComponent(i)) * invD;

            if (invD < 0) {
                double temp = t0;
                t0 = t1;
                t1 = temp;
            }

            tMin = Math.max(tMin, t0);
            tMax = Math.min(tMax, t1);

            if (tMax <= tMin) return -1;
        }

        return tMin;
    }

    @Override
    public void rotate(double angleX, double angleY, double angleZ) {
        for (int i = 0; i < corners.length; i++) {
            corners[i] = corners[i].rotateX(angleX).rotateY(angleY).rotateZ(angleZ);
        }
    }

    public float[] toGPUData() {
        float[] cornersData = new float[24]; // 8 вершин по 3 координаты (x, y, z)
        for (int i = 0; i < corners.length; i++) {
            float[] vertex = corners[i].toArray();
            System.arraycopy(vertex, 0, cornersData, i * 3, 3);
        }

        float[] data = new float[32];
        data[0] = 1f; // Тип объекта (6 - параллелепипед)
        data[1] = material.getId();
        data[2] = (float) position.x;
        data[3] = (float) position.y;
        data[4] = (float) position.z;
        System.arraycopy(cornersData, 0, data, 5, cornersData.length);
        data[29] = color.getRed() / 255f; // R
        data[30] = color.getGreen() / 255f; // G
        data[31] = color.getBlue() / 255f; // B

        return data;
    }
}
