package io.github.some_example_name;

import com.badlogic.gdx.math.Vector2;

/** Obstáculo circular que o NPC deve evitar. */
public class CircleObstacle {
    public final Vector2 center = new Vector2();
    public final float radius;

    public CircleObstacle(float x, float y, float radius) {
        center.set(x, y);
        this.radius = radius;
    }
}
