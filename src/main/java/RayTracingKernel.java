
import com.aparapi.Kernel;

import java.util.Arrays;

public class RayTracingKernel extends Kernel {
    private int width;
    private int height;
    private float[] cameraPosition;  // Положение камеры
    private float[] cameraDirection; // Направление камеры
    private float[] cameraUp;        // Вектор "вверх"
    private float[] cameraRight;     // Вектор "вправо"
    private float fov;               // Поле зрения камеры
    private float[] lightPosition;   // Положение источника света
    private float[] shapes;          // Данные о фигурах
    private int[] pixels;           // Результирующие пиксели
    private int maxReflections;               // Поле зрения камеры

    public RayTracingKernel(int width, int height, float[] cameraPosition, float[] cameraDirection,
                            float[] cameraUp, float[] cameraRight, float fov,
                            float[] lightPosition, float[] shapes, int[] pixels, int maxReflections) {
        this.width = width;
        this.height = height;
        this.cameraPosition = cameraPosition;
        this.cameraDirection = cameraDirection;
        this.cameraUp = cameraUp;
        this.cameraRight = cameraRight;
        this.fov = fov;
        this.lightPosition = lightPosition;
        this.shapes = shapes;
        this.pixels = pixels;
        this.maxReflections = maxReflections;
    }

    @Override
    public void run() {
        int id = getGlobalId();
        if (id >= pixels.length) return;

        int x = id % width;
        int y = id / width;

        float aspectRatio = (float) width / height;
        float px = (2f * (x + 0.5f) / width - 1f) * (float) Math.tan(Math.toRadians(fov) / 2) * aspectRatio;
        float py = (1f - 2f * (y + 0.5f) / height) * (float) Math.tan(Math.toRadians(fov) / 2);

        float[] rayDirection = normalize(add(add(multiply(cameraRight, px), multiply(cameraUp, py)), cameraDirection));

        pixels[id] = traceRay(cameraPosition, rayDirection, maxReflections); // 5 отражений
    }

    private int traceRay(float[] origin, float[] direction, int remainingBounces) {
        if (remainingBounces <= 0) {
            return 0x000000; // Черный цвет при достижении предела отражений
        }

        float closestDistance = Float.MAX_VALUE;
        int color = 0x000000;
        float[] hitNormal = null;
        float[] hitPoint = null;
        int baseColor = 0;

        for (int i = 0; i < shapes.length; ) {
            int type = (int) shapes[i];

            float t = -1;
            float[] normal = new float[3];

            if (type == 1) { // Параллелепипед
                float[] center = {shapes[i + 2], shapes[i + 3], shapes[i + 4]};
                float[] corners = new float[24];
                System.arraycopy(shapes, i + 5, corners, 0, 24);
                t = intersectParallelepiped(direction, origin, center, corners, normal);
                baseColor = calculateColor(shapes, i + 29);
                if (t > 0 && t < closestDistance) {
                    closestDistance = t;
                    hitNormal = normal;
                    hitPoint = add(origin, multiply(direction, t));
                    color = baseColor;
                }
                i += 32;
            } else if (type == 2) { // Сфера
                t = intersectSphere(direction, origin, shapes, i + 2);
                baseColor = calculateColor(shapes, i + 8);
                if (t > 0 && t < closestDistance) {
                    closestDistance = t;
                    hitNormal = calculateSphereNormal(origin, direction, t, shapes, i + 2);
                    hitPoint = add(origin, multiply(direction, t));
                    color = baseColor;
                }
                i += 11;
            } else {
                i += 11; // Пропуск других фигур
            }
        }

        if (hitPoint == null || hitNormal == null) {
            return 0x000000; // Черный цвет, если нет пересечения
        }

        // Освещение для текущей точки
        float bias = 1e-4f;
        hitPoint = add(hitPoint, multiply(hitNormal, bias));
        float intensity = calculateLightIntensity(hitPoint, hitNormal);
        int lightingColor = applyLighting(color, intensity);

        // Reflections
        float[] reflectedDirection = reflect(direction, hitNormal);
        int reflectedColor = traceRay(hitPoint, reflectedDirection, remainingBounces - 1);

        // Blend base color and reflections
        return blendColors(lightingColor, reflectedColor, 0.5f); // Половина от базового цвета и отражения
    }

    private float intersectParallelepiped(float[] rayDirection, float[] origin, float[] center, float[] corners, float[] normal) {
        float tMin = Float.NEGATIVE_INFINITY;
        float tMax = Float.POSITIVE_INFINITY;
        int hitAxis = -1;

        // Вычисляем min и max для каждой оси
        for (int i = 0; i < 3; i++) {
            float min = Float.MAX_VALUE;
            float max = Float.MIN_VALUE;

            for (int j = 0; j < 8; j++) {
                float value = corners[j * 3 + i];
                min = Math.min(min, value);
                max = Math.max(max, value);
            }

            // Пересечение с осью
            float invD = 1.0f / rayDirection[i];
            float t0 = (min - origin[i]) * invD;
            float t1 = (max - origin[i]) * invD;
            if (invD < 0) {
                float temp = t0;
                t0 = t1;
                t1 = temp;
            }
            if (t0 > tMin) {
                tMin = t0;
                hitAxis = i;
            }
            tMax = Math.min(tMax, t1);
            if (tMax <= tMin) return -1.0f;
        }

        if (tMin > 0) {
            normal[0] = 0;
            normal[1] = 0;
            normal[2] = 0;
            normal[hitAxis] = rayDirection[hitAxis] > 0 ? -1 : 1;
        }

        return tMin;
    }

    private float[] calculateParallelepipedNormal(float[] hitPoint, float[] center, float[] corners) {
        float[] normal = new float[3];
        float bias = 1e-4f;

        for (int i = 0; i < 3; i++) {
            if (Math.abs(hitPoint[i] - (center[i] + corners[i * 2])) < bias) {
                normal[i] = 1.0f;
                break;
            } else if (Math.abs(hitPoint[i] - (center[i] - corners[i * 2])) < bias) {
                normal[i] = -1.0f;
                break;
            }
        }
        return normal;
    }

    private float intersectSphere(float[] direction, float[] origin, float[] shapes, int index) {
        float[] center = {shapes[index], shapes[index + 1], shapes[index + 2]};
        float radius = shapes[index + 3];
        float[] oc = subtract(origin, center);

        float a = dot(direction, direction);
        float b = 2.0f * dot(oc, direction);
        float c = dot(oc, oc) - radius * radius;

        float discriminant = b * b - 4 * a * c;
        if (discriminant < 0) return -1;

        return (-b - (float) Math.sqrt(discriminant)) / (2.0f * a);
    }

    private float[] calculateSphereNormal(float[] origin, float[] direction, float t, float[] shapes, int index) {
        float[] center = {shapes[index], shapes[index + 1], shapes[index + 2]};
        float[] hitPoint = add(origin, multiply(direction, t));
        return normalize(subtract(hitPoint, center));
    }

    private int blendColors(int color1, int color2, float ratio) {
        int r1 = (color1 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF;
        int b1 = color1 & 0xFF;

        int r2 = (color2 >> 16) & 0xFF;
        int g2 = (color2 >> 8) & 0xFF;
        int b2 = color2 & 0xFF;

        int r = (int) (r1 * (1 - ratio) + r2 * ratio);
        int g = (int) (g1 * (1 - ratio) + g2 * ratio);
        int b = (int) (b1 * (1 - ratio) + b2 * ratio);

        return (r << 16) | (g << 8) | b;
    }

    private float calculateLightIntensity(float[] intersection, float[] normal) {
        float[] lightDirection = normalize(subtract(lightPosition, intersection));
        return Math.max(0, dot(normal, lightDirection));
    }

    private int calculateColor(float[] shapes, int index) {
        int r = (int) (shapes[index] * 255);
        int g = (int) (shapes[index + 1] * 255);
        int b = (int) (shapes[index + 2] * 255);
        return (r << 16) | (g << 8) | b;
    }

    private int applyLighting(int baseColor, float intensity) {
        int r = (int) ((baseColor >> 16 & 0xFF) * intensity);
        int g = (int) ((baseColor >> 8 & 0xFF) * intensity);
        int b = (int) ((baseColor & 0xFF) * intensity);
        return (r << 16) | (g << 8) | b;
    }

    private float dot(float[] a, float[] b) {
        return a[0] * b[0] + a[1] * b[1] + a[2] * b[2];
    }

    private float[] subtract(float[] a, float[] b) {
        return new float[]{a[0] - b[0], a[1] - b[1], a[2] - b[2]};
    }

    private float[] add(float[] a, float[] b) {
        return new float[]{a[0] + b[0], a[1] + b[1], a[2] + b[2]};
    }

    private float[] multiply(float[] a, float scalar) {
        return new float[]{a[0] * scalar, a[1] * scalar, a[2] * scalar};
    }

    private float length(float[] v) {
        return (float) Math.sqrt(dot(v, v));
    }

    private float[] normalize(float[] v) {
        float len = length(v);
        return new float[]{v[0] / len, v[1] / len, v[2] / len};
    }

    private float[] reflect(float[] direction, float[] normal) {
        float dotProduct = dot(direction, normal);
        return subtract(direction, multiply(normal, 2 * dotProduct));
    }
}