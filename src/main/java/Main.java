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
                new Vector3(10, 20, -30),
                Matrix4x4.identity(),
                90
        );

        Scene scene = new Scene(camera);


        scene.addShape(new Sphere(new Vector3(13, 4, -5), Color.CYAN, 4, 10, 10, new Vector3(0, 0, 0), Material.MATTE));
        scene.addShape(new Parallelepiped(new Vector3(7, 2.5, 0), Color.MAGENTA, 5, 13d, 5, new Vector3(0, 0, 0), Material.MATTE));

        scene.addShape(new Parallelepiped(new Vector3(10, -1, 0), Color.WHITE, 20, 2, 20, new Vector3(0, 0, 0), Material.MATTE));
        scene.addShape(new Parallelepiped(new Vector3(0, 20, 0), Color.WHITE, 2, 40, 20, new Vector3(0, 0, 0), Material.MATTE));
        scene.addShape(new Parallelepiped(new Vector3(20, 20, 0), Color.WHITE, 2, 40, 20, new Vector3(0, 0, 0), Material.MATTE));
        scene.addShape(new Parallelepiped(new Vector3(10, 20, 10), Color.WHITE, 20, 40, 2, new Vector3(0, 0, 0), Material.MATTE));
        scene.addShape(new Parallelepiped(new Vector3(10, 40, 0), Color.WHITE, 20, 2, 20, new Vector3(0, 0, 0), Material.EMITTER));

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