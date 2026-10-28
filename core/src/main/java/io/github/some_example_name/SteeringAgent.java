package io.github.some_example_name;

import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.steer.SteeringAcceleration;
import com.badlogic.gdx.ai.utils.Location;
import com.badlogic.gdx.math.Vector2;

/**
 * NPC que implementa Steerable<Vector2> (a entidade exigida pelo gdx-ai, slide 25).
 * Guarda posição, velocidade e os limites (máxima velocidade/aceleração) usados
 * pelos comportamentos de steering. O método update() aplica a aceleração calculada.
 */
public class SteeringAgent implements Steerable<Vector2> {
    public final Vector2 position = new Vector2();
    public final Vector2 linearVelocity = new Vector2();
    public float angularVelocity;
    public float orientation;

    private float boundingRadius;
    private boolean tagged;

    // Limites do comportamento (Limiter).
    private float zeroLinearSpeedThreshold = 0.001f;
    private float maxLinearSpeed = 130f;
    private float maxLinearAcceleration = 600f;
    private float maxAngularSpeed = 20f;
    private float maxAngularAcceleration = 40f;

    public SteeringAgent(float x, float y, float boundingRadius) {
        position.set(x, y);
        this.boundingRadius = boundingRadius;
    }

    /** Integra a aceleração de steering: atualiza posição, velocidade e orientação. */
    public void update(float delta, SteeringAcceleration<Vector2> steering) {
        position.mulAdd(linearVelocity, delta);
        linearVelocity.mulAdd(steering.linear, delta).limit(maxLinearSpeed);
        // Faz o NPC "olhar" para a direção do movimento.
        if (linearVelocity.len2() > zeroLinearSpeedThreshold) {
            orientation = vectorToAngle(linearVelocity);
        }
    }

    // --- Steerable ---
    @Override public Vector2 getLinearVelocity() { return linearVelocity; }
    @Override public float getAngularVelocity() { return angularVelocity; }
    @Override public float getBoundingRadius() { return boundingRadius; }
    @Override public boolean isTagged() { return tagged; }
    @Override public void setTagged(boolean tagged) { this.tagged = tagged; }

    // --- Limiter ---
    @Override public float getZeroLinearSpeedThreshold() { return zeroLinearSpeedThreshold; }
    @Override public void setZeroLinearSpeedThreshold(float value) { zeroLinearSpeedThreshold = value; }
    @Override public float getMaxLinearSpeed() { return maxLinearSpeed; }
    @Override public void setMaxLinearSpeed(float v) { maxLinearSpeed = v; }
    @Override public float getMaxLinearAcceleration() { return maxLinearAcceleration; }
    @Override public void setMaxLinearAcceleration(float v) { maxLinearAcceleration = v; }
    @Override public float getMaxAngularSpeed() { return maxAngularSpeed; }
    @Override public void setMaxAngularSpeed(float v) { maxAngularSpeed = v; }
    @Override public float getMaxAngularAcceleration() { return maxAngularAcceleration; }
    @Override public void setMaxAngularAcceleration(float v) { maxAngularAcceleration = v; }

    // --- Location ---
    @Override public Vector2 getPosition() { return position; }
    @Override public float getOrientation() { return orientation; }
    @Override public void setOrientation(float orientation) { this.orientation = orientation; }

    @Override
    public float vectorToAngle(Vector2 vector) {
        return (float) Math.atan2(-vector.x, vector.y);
    }

    @Override
    public Vector2 angleToVector(Vector2 outVector, float angle) {
        outVector.x = -(float) Math.sin(angle);
        outVector.y = (float) Math.cos(angle);
        return outVector;
    }

    @Override
    public Location<Vector2> newLocation() {
        return new Location2D();
    }
}
