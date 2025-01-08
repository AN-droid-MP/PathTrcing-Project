package Drawable.ThreeDimObj;

import Drawable.Material;
import Drawable.Shape;
import Drawable.Vector3;

import java.awt.*;

public class Sphere extends Shape {
    private int latitudeBands;
    private int longitudeBands;
    private double radius;
    private Vector3[] vertices;

    public Sphere(Vector3 position, Color color, double radius, int latitudeBands,
                  int longitudeBands, Vector3 rotation, Material material) {
        super(position, color, rotation, material);
        this.radius = radius;
        this.latitudeBands = latitudeBands;
        this.longitudeBands = longitudeBands;
        initializeVertices();
    }

    private void initializeVertices() {
        int totalVertices = (latitudeBands + 1) * (longitudeBands + 1);
        vertices = new Vector3[totalVertices];
        int index = 0;

        for (int lat = 0; lat <= latitudeBands; lat++) {
            double theta = Math.PI * lat / latitudeBands;
            double sinTheta = Math.sin(theta);
            double cosTheta = Math.cos(theta);

            for (int lon = 0; lon <= longitudeBands; lon++) {
                double phi = 2 * Math.PI * lon / longitudeBands;
                double sinPhi = Math.sin(phi);
                double cosPhi = Math.cos(phi);

                double x = radius * sinTheta * cosPhi;
                double y = radius * sinTheta * sinPhi;
                double z = radius * cosTheta;

                vertices[index++] = new Vector3(x, y, z);
            }
        }
        rotate(rotation.x, rotation.y, rotation.z);
    }

    @Override
    public double intersect(Vector3 origin, Vector3 direction) {
        Vector3 oc = origin.subtract(position);
        double a = direction.dot(direction);
        double b = 2.0 * oc.dot(direction);
        double c = oc.dot(oc) - radius * radius;
        double discriminant = b * b - 4 * a * c;

        if (discriminant < 0) {
            return -1; // Пересечений нет
        } else {
            return (-b - Math.sqrt(discriminant)) / (2.0 * a); // Ближайшее пересечение
        }
    }

    @Override
    public void rotate(double angleX, double angleY, double angleZ) {
        for (int i = 0; i < vertices.length; i++) {
            vertices[i] = vertices[i].rotateX(angleX).rotateY(angleY).rotateZ(angleZ);
        }
    }

    @Override
    public float[] toGPUData() {
        float[] positionArray = position.toArray();
        if (positionArray.length != 3) {
            System.out.println("Ошибка: данные позиции сферы некорректны.");
        }

        System.out.println(color.toString());

        return new float[]{
                2f, // Тип объекта (1 - сфера)
                material.getId(),
                positionArray[0], positionArray[1], positionArray[2], // Центр
                (float) radius, // Радиус
                (float) latitudeBands,
                (float) longitudeBands,
                color.getRed() / 255f, // R
                color.getGreen() / 255f, // G
                color.getBlue() / 255f
        };
    }
}
