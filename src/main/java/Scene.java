import Drawable.Shape;
import Drawable.Vector3;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;


public class Scene {
    private Camera camera;
    private List<Shape> shapes;

    public Scene(Camera camera) {
        this.camera = camera;
        this.shapes = new ArrayList<>();
    }

    public void addShape(Shape shape) {
        shapes.add(shape);
    }

    public void removeShape(Shape shape) {
        shapes.remove(shape);
    }

    public List<Shape> getShapes() {
        return shapes;
    }

    public Camera getCamera() {
        return camera;
    }

    public void setCamera(Camera camera) {
        this.camera = camera;
    }

    public void handleCameraInput(int keyCode) {
        Vector3 delta = new Vector3(0, 0, 0);
        double moveSpeed = 10;
        double rotateSpeed = 5;

        switch (keyCode) {
            case KeyEvent.VK_W -> delta = new Vector3(0, 0, moveSpeed); // Вперёд
            case KeyEvent.VK_S -> delta = new Vector3(0, 0, -moveSpeed); // Назад
            case KeyEvent.VK_A -> delta = new Vector3(-moveSpeed, 0, 0); // Влево
            case KeyEvent.VK_D -> delta = new Vector3(moveSpeed, 0, 0); // Вправо
            case KeyEvent.VK_SPACE -> delta = new Vector3(0, moveSpeed, 0); // Вверх
            case KeyEvent.VK_SHIFT -> delta = new Vector3(0, -moveSpeed, 0); // Вниз

            // Вращение камеры
            case KeyEvent.VK_UP -> camera.rotate(new Vector3(1, 0, 0), -rotateSpeed); // Наклон вверх
            case KeyEvent.VK_DOWN -> camera.rotate(new Vector3(1, 0, 0), rotateSpeed); // Наклон вниз
            case KeyEvent.VK_LEFT -> camera.rotate(new Vector3(0, 1, 0), -rotateSpeed); // Поворот влево
            case KeyEvent.VK_RIGHT -> camera.rotate(new Vector3(0, 1, 0), rotateSpeed); // Поворот вправо
            case KeyEvent.VK_Q -> camera.rotate(new Vector3(0, 0, 1), rotateSpeed); // Крен влево
            case KeyEvent.VK_E -> camera.rotate(new Vector3(0, 0, 1), -rotateSpeed); // Крен вправо
        }

        camera.move(delta);
    }
}
