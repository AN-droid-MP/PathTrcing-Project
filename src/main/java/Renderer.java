import Drawable.Shape;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;


public class Renderer extends JPanel {
    private Scene scene;
    private int width;
    private int height;

    public Renderer(Scene scene) {
        this.scene = scene;
        this.width = 800;
        this.height = 600;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Camera camera = scene.getCamera();

        int[] pixels = new int[width * height];

        float[] shapes = prepareShapes();

        float[] lightPosition = {10, 20, -10}; // Положение источника света
        float[] cameraPosition = camera.getPosition().toArray();
        float[] cameraUp = camera.getUp().toArray();
        float[] cameraRight = camera.getRight().toArray();
        float[] cameraDirection = camera.getForward().toArray();

        RayTracingKernel kernel = new RayTracingKernel (
                width, height, cameraPosition, cameraDirection,
                cameraUp, cameraRight, (float) camera.getFov(), lightPosition, shapes, pixels, 50
        );
        kernel.execute(pixels.length);

        Graphics2D g2d = (Graphics2D) g;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int color = pixels[y * width + x];
                g2d.setColor(new Color(color));
                g2d.fillRect(x, y, 1, 1);
            }
        }

        kernel.dispose();
    }

    private float[] prepareShapes() {
        List<Shape> shapes = scene.getShapes();
        List<Float> shapesData = new ArrayList<>();

        for (Shape shape : shapes) {
            float[] shapeData = shape.toGPUData();
            if (shapeData == null || shapeData.length == 0) {
                System.out.println("Ошибка: фигура вернула пустой массив данных.");
                continue;
            }
            System.out.println("Фигура: " + shape.getClass().getSimpleName() + ", длина данных: " + shapeData.length);
            for (float value : shapeData) {
                shapesData.add(value);
            }
        }

        float[] shapesArray = new float[shapesData.size()];
        for (int i = 0; i < shapesData.size(); i++) {
            shapesArray[i] = shapesData.get(i);
        }

        System.out.println("Количество фигур: " + shapes.size());
        System.out.println("Общая длина массива shapes: " + shapesArray.length);

        return shapesArray;
    }
}