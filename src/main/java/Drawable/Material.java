package Drawable;

public enum Material {
    MATTE(1),
    SEMI_MATTE(2),
    GLOSS(3),
    EMITTER(4);

    private final int id;

    Material(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static Material fromId(int id) {
        for (Material material : values()) {
            if (material.id == id) {
                return material;
            }
        }
        throw new IllegalArgumentException("Unknown Material ID: " + id);
    }
}
