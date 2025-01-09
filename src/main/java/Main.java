import Drawable.Material;
import Drawable.Matrix4x4;
import Drawable.ThreeDimObj.Parallelepiped;
import Drawable.ThreeDimObj.Sphere;
import Drawable.Vector3;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class Main {
    public static void main(String[] args) {
        Camera camera = new Camera(
                new Vector3(10, 30, -50),
                Matrix4x4.identity(),
                90
        );

        Scene scene = new Scene(camera);


        //scene.addShape(new Sphere(new Vector3(25, 27, -10), Color.CYAN, 7, 10, 10, new Vector3(0, 0, 0), Material.GLASS));
        scene.addShape(new Sphere(new Vector3(25, 7, -10), Color.CYAN, 7, 10, 10, new Vector3(0, 0, 0), Material.GLASS));
        scene.addShape(new Parallelepiped(new Vector3(15, 10, 0), Color.MAGENTA, 10, 20, 10, new Vector3(0, 0, 45), Material.SEMI_MATTE));

        scene.addShape(new Parallelepiped(new Vector3(20, -1, 0), Color.WHITE, 40, 2, 40, new Vector3(0, 0, 0), Material.GLOSS));
        scene.addShape(new Parallelepiped(new Vector3(0, 30, 0), Color.ORANGE, 2, 60, 40, new Vector3(0, 0, 0), Material.MATTE));
        scene.addShape(new Parallelepiped(new Vector3(40, 30, 0), Color.BLUE, 2, 60, 40, new Vector3(0, 0, 0), Material.MATTE));
        scene.addShape(new Parallelepiped(new Vector3(20, 30, 10), Color.RED, 40, 60, 2, new Vector3(0, 0, 0), Material.SEMI_MATTE));
        scene.addShape(new Parallelepiped(new Vector3(20, 60, 0), Color.WHITE, 40, 2, 40, new Vector3(0, 0, 0), Material.MATTE));

        Renderer renderer = new Renderer(scene);

        JFrame frame = new JFrame("3D Renderer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.add(renderer);
        frame.setVisible(true);

        frame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                scene.handleCameraInput(e.getKeyCode());
                renderer.repaint();
            }
        });
    }
}