package io.github.some_example_name;

import com.badlogic.gdx.ai.utils.Collision;
import com.badlogic.gdx.ai.utils.Ray;
import com.badlogic.gdx.ai.utils.RaycastCollisionDetector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

/**
 * Detector de colisão por raycast contra obstáculos circulares.
 * É o que o RaycastObstacleAvoidance usa para "enxergar" os obstáculos à frente
 * (sem depender do Box2D). Resolve a interseção segmento-círculo e devolve o
 * ponto de colisão mais próximo e a normal da superfície.
 */
public class CircleRaycastCollisionDetector implements RaycastCollisionDetector<Vector2> {
    private final Array<CircleObstacle> obstacles;

    public CircleRaycastCollisionDetector(Array<CircleObstacle> obstacles) {
        this.obstacles = obstacles;
    }

    @Override
    public boolean collides(Ray<Vector2> ray) {
        return findCollision(null, ray);
    }

    @Override
    public boolean findCollision(Collision<Vector2> outputCollision, Ray<Vector2> inputRay) {
        boolean collided = false;
        float nearestT = Float.MAX_VALUE;

        Vector2 start = inputRay.start;
        Vector2 end = inputRay.end;
        float dx = end.x - start.x;
        float dy = end.y - start.y;
        float a = dx * dx + dy * dy;
        if (a == 0f) return false; // raio de comprimento zero

        for (int i = 0; i < obstacles.size; i++) {
            CircleObstacle o = obstacles.get(i);
            float fx = start.x - o.center.x;
            float fy = start.y - o.center.y;
            float b = 2f * (fx * dx + fy * dy);
            float c = fx * fx + fy * fy - o.radius * o.radius;
            float disc = b * b - 4f * a * c;
            if (disc < 0f) continue; // sem interseção
            disc = (float) Math.sqrt(disc);

            // Menor t dentro do segmento [0,1].
            float t = -1f;
            float t1 = (-b - disc) / (2f * a);
            if (t1 >= 0f && t1 <= 1f) {
                t = t1;
            } else {
                float t2 = (-b + disc) / (2f * a);
                if (t2 >= 0f && t2 <= 1f) t = t2;
            }

            if (t >= 0f && t < nearestT) {
                nearestT = t;
                collided = true;
                if (outputCollision != null) {
                    outputCollision.point.set(start.x + t * dx, start.y + t * dy);
                    outputCollision.normal.set(outputCollision.point).sub(o.center).nor();
                }
            }
        }
        return collided;
    }
}
