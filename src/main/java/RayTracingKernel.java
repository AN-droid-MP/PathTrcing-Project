
import com.aparapi.Kernel;

import java.util.Arrays;

public class RayTracingKernel extends Kernel {
    private float[] lightColor = {1.0f, 1.0f, 1.0f};
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

        pixels[id] = traceRayWithSampling(cameraPosition, rayDirection, maxReflections, 20, 1);
//        pixels[id] = traceRay(cameraPosition, rayDirection, maxReflections, 1);
    }

    private int traceRayWithSampling(float[] origin, float[] direction, int remainingBounces, int samplesPerPixel, float currentIntensity) {
        int accumulatedColor = 0;
        for (int i = 0; i < samplesPerPixel; i++) {
            float[] jitteredDirection = applyJitter(direction); // Случайное смещение
            int sampleColor = traceRay(origin, jitteredDirection, remainingBounces, currentIntensity);
            accumulatedColor = blendColors(accumulatedColor, sampleColor, 1.0f / (i + 1));
        }
        return accumulatedColor;
    }

    private float[] applyJitter(float[] direction) {
        float[] jitter = {randomInRange(-0.001f, 0.001f), randomInRange(-0.001f, 0.001f), randomInRange(-0.001f, 0.001f)};
        return normalize(add(direction, jitter));
    }

    private float randomInRange(float min, float max) {
        return min + (float) Math.random() * (max - min);
    }

    private int traceRay(float[] origin, float[] direction, int remainingBounces, float currentIntensity) {
        if (remainingBounces <= 0 || currentIntensity < 0.01f) {
            return 0x000000; // Черный цвет при достижении предела отражений или низкой интенсивности
        }

        float closestDistance = Float.MAX_VALUE;
        int color = 0x000000;
        float[] hitNormal = null;
        float[] hitPoint = null;
        int baseColor = 0;
        int materialType = 1;

        for (int i = 0; i < shapes.length; ) {
            int type = (int) shapes[i];
            int surfaceType = (int) shapes[i + 1];

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
                    materialType = surfaceType;
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
                    materialType = surfaceType;
                }
                i += 11;
            } else {
                i += 11; // Пропуск других фигур
            }
        }

        if (hitPoint == null || hitNormal == null) {
            return 0x000000; // Черный цвет, если нет пересечения
        }

        if(materialType == 4) {
            return color;
        }

        float bias = 1e-4f;
        hitPoint = add(hitPoint, multiply(hitNormal, bias));

        // Освещение
        float intensity = calculateLightIntensity(hitPoint, hitNormal);
        int diffuseColor = applyLighting(color, intensity); // Диффузный свет

        // Вектор направления света
        float[] lightDirection = normalize(subtract(lightPosition, hitPoint));

        // Вектор взгляда (от точки к камере)
        float[] viewDirection = normalize(subtract(cameraPosition, hitPoint));

        // Рассчёт бликов
        float[] specularColor = calculateSpecular(lightDirection, hitNormal, viewDirection, 50.0f, lightColor); // Shininess = 50.0f

        // Смешиваем диффузное освещение с бликами
        int lightingColor = blendColors(diffuseColor, convertColorToInt(specularColor), 0.5f);

        for (int i = 0; i < shapes.length; ) {
            int emitterType = (int) shapes[i+1];
            if (emitterType == 4) { // Если это эмиттер
                float[] emitterPosition = {shapes[i + 2], shapes[i + 3], shapes[i + 4]};

                if (!isInShadow(hitPoint, emitterPosition)) {
                    float emitterContribution = calculateLightFromEmitter(hitPoint, emitterPosition, currentIntensity, hitNormal);
                    lightingColor = blendColors(lightingColor, scaleColor(baseColor, emitterContribution), emitterContribution);
                }
                else {
                    // Если эмиттер перекрыт объектом, уменьшаем вклад освещения
                    lightingColor = darkenColor(lightingColor, 0.5f);
                }
            }
            i += 32; // Пропуск параметров эмиттера
        }

        // Проверка на наличие теней
        if (isInShadow(hitPoint, hitNormal)) {
            lightingColor = darkenColor(lightingColor, 0.5f); // Уменьшаем яркость в 2 раза
        }

        // Отражение
        float reflectionIntensity = currentIntensity;
        if (materialType == 1) { // Матовый
            direction = diffuseReflection(hitNormal); // Новый случайный луч
            reflectionIntensity *= 0.3f; // Матовые поверхности уменьшают интенсивность сильнее
        } else if (materialType == 2) { // Полу-матовый
            direction = blendDirections(reflect(direction, hitNormal), diffuseReflection(hitNormal), 0.8f); // Смешиваем отражение и рассеяние
            reflectionIntensity *= 0.5f; // Полу-матовые поверхности уменьшают интенсивность меньше
        } else if (materialType == 3) { // Глянец
            direction = reflect(direction, hitNormal); // Чистое зеркальное отражение
            reflectionIntensity *= 0.8f; // Глянцевые поверхности почти не уменьшают интенсивность
        }

        int reflectedColor = traceRay(hitPoint, direction, remainingBounces - 1, reflectionIntensity);

        // Смешиваем базовый цвет и отражение
        return blendColors(lightingColor, reflectedColor, reflectionIntensity);
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

    private int scaleColor(int color, float intensity) {
        int r = (int) Math.min(255, ((color >> 16) & 0xFF) * intensity);
        int g = (int) Math.min(255, ((color >> 8) & 0xFF) * intensity);
        int b = (int) Math.min(255, (color & 0xFF) * intensity);
        return (r << 16) | (g << 8) | b;
    }

    private float calculateLightFromEmitter(float[] hitPoint, float[] emitterPosition, float intensity, float[] hitNormal) {
        float[] lightDir = normalize(subtract(emitterPosition, hitPoint));
        float distanceSquared = pow(length(subtract(emitterPosition, hitPoint)), 2);
        float attenuation = intensity / (4 * (float) Math.PI * distanceSquared);
        return attenuation * Math.max(0, dot(hitNormal, lightDir));
    }

    private float calculateLightIntensity(float[] point, float[] normal) {
        float[] lightDir = normalize(subtract(lightPosition, point));
        float totalIntensity = Math.max(0, dot(normal, lightDir));
        for (int i = 0; i < shapes.length; ) {
            int materialType = (int) shapes[i + 3];
            if (materialType == 4) { // Эмиттер
                float[] emitterPosition = {shapes[i + 1], shapes[i + 2], shapes[i + 3]};
                float intensity = shapes[i + shapes.length - 1]; // Интенсивность эмиттера
                totalIntensity += calculateLightFromEmitter(point, emitterPosition, intensity, normal);
            }
            i += 31; // Пропускаем параметры эмиттера
        }
        return totalIntensity; // Ограничиваем интенсивность в пределах [0, 1]
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

    private boolean isInShadow(float[] point, float[] normal) {
        float[] lightDir = normalize(subtract(lightPosition, point));
        float bias = 1e-4f;
        float[] shadowOrigin = add(point, multiply(normal, bias));

        for (int i = 0; i < shapes.length; ) {
            int type = (int) shapes[i];
            float materialType = shapes[i + 1];
            float t = -1;
            if (type == 2) { // Сфера
                t = intersectSphere(lightDir, shadowOrigin, shapes, i + 2);
                if (t > 0 && materialType != 4) return true;
                i += 11;
            } else if (type == 1) { // Параллелепипед
                float[] center = {shapes[i + 2], shapes[i + 3], shapes[i + 4]};
                float[] corners = new float[24];
                System.arraycopy(shapes, i + 5, corners, 0, 24);
                float[] tempNormal = new float[3];
                t = intersectParallelepiped(lightDir, shadowOrigin, center, corners, tempNormal);
                if (t > 0 && materialType != 4) return true;
                i += 32;
            } else {
                i += 11;
            }
        }
        return false;
    }

    private int darkenColor(int color, float factor) {
        int r = (int) ((color >> 16 & 0xFF) * factor);
        int g = (int) ((color >> 8 & 0xFF) * factor);
        int b = (int) ((color & 0xFF) * factor);
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

    private float[] negate(float[] vector) {
        return new float[]{-vector[0], -vector[1], -vector[2]};
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


    private float[] diffuseReflection(float[] normal) {
        float[] randomDir = randomUnitVector();
        if (dot(randomDir, normal) < 0) {
            randomDir = multiply(randomDir, -1); // Убедимся, что луч в ту же сторону, что и нормаль
        }
        return normalize(add(normal, randomDir));
    }

    private float[] blendDirections(float[] reflectedDirection, float[] diffuseDirection, float ratio) {
        // Нормализуем направления
        reflectedDirection = normalize(reflectedDirection);
        diffuseDirection = normalize(diffuseDirection);

        // Смешиваем направления в заданной пропорции
        return normalize(new float[]{
                reflectedDirection[0] * ratio + diffuseDirection[0] * (1 - ratio),
                reflectedDirection[1] * ratio + diffuseDirection[1] * (1 - ratio),
                reflectedDirection[2] * ratio + diffuseDirection[2] * (1 - ratio)
        });
    }

    private float[] randomUnitVector() {
        float z = (float) (Math.random() * 2 - 1);
        float theta = (float) (Math.random() * 2 * Math.PI);
        float r = (float) Math.sqrt(1 - z * z);
        return new float[]{r * (float) Math.cos(theta), r * (float) Math.sin(theta), z};
    }

    float[] calculateSpecular(float[] lightDirection, float[] normal, float[] viewDirection, float shininess, float[] lightColor) {
        float[] reflectionDirection = reflect(negate(lightDirection), normal);
        float cosTheta = Math.max(0.0f, dot(viewDirection, reflectionDirection));
        float specularIntensity = (float) Math.pow(cosTheta, shininess);
        return multiply(lightColor, specularIntensity); // Блики теперь учитывают цвет света
    }

    private int convertColorToInt(float[] color) {
        int r = (int) (color[0] * 255);
        int g = (int) (color[1] * 255);
        int b = (int) (color[2] * 255);
        return (r << 16) | (g << 8) | b;
    }
}