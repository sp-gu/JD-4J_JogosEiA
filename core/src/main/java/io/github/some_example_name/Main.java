package io.github.some_example_name;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.GdxAI;
import com.badlogic.gdx.ai.steer.SteeringAcceleration;
import com.badlogic.gdx.ai.steer.behaviors.Arrive;
import com.badlogic.gdx.ai.steer.behaviors.PrioritySteering;
import com.badlogic.gdx.ai.steer.behaviors.RaycastObstacleAvoidance;
import com.badlogic.gdx.ai.steer.utils.rays.CentralRayWithWhiskersConfiguration;
import com.badlogic.gdx.ai.utils.Ray;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;

/**
 * Demonstração do slide 25 - "gdx-ai: steering pronto no libGDX".
 *
 * O NPC (Steerable<Vector2>) persegue o cursor do mouse usando um PrioritySteering
 * que combina, em ordem de prioridade:
 *   1) RaycastObstacleAvoidance - evitar os obstáculos tem prioridade;
 *   2) Arrive - chegar suavemente ao alvo quando o caminho está livre.
 *
 * A cada quadro: steering.calculateSteering(saida); npc.update(saida, delta).
 * Todo o tratamento de erro fica centralizado em try/catch, imprimindo a exceção.
 */
public class Main extends ApplicationAdapter {
    private static final String TAG = "JogosEiA";
    private static final float W = 800f;
    private static final float H = 600f;

    private OrthographicCamera camera;
    private ShapeRenderer shapes;
    private SpriteBatch batch;
    private BitmapFont font;

    private SteeringAgent npc;
    private Location2D target;
    private Array<CircleObstacle> obstacles;

    private CentralRayWithWhiskersConfiguration<Vector2> rayConfig;
    private CircleRaycastCollisionDetector detector;
    private PrioritySteering<Vector2> steering;
    private SteeringAcceleration<Vector2> steeringOutput;

    private final Vector3 mouse = new Vector3();
    private final Vector2 heading = new Vector2();

    @Override
    public void create() {
        try {
            camera = new OrthographicCamera();
            camera.setToOrtho(false, W, H);
            shapes = new ShapeRenderer();
            batch = new SpriteBatch();
            font = new BitmapFont();

            // NPC e alvo.
            npc = new SteeringAgent(W * 0.15f, H * 0.5f, 12f);
            target = new Location2D();
            target.position.set(W * 0.85f, H * 0.5f);

            // Obstáculos circulares no meio do caminho.
            obstacles = new Array<CircleObstacle>();
            obstacles.add(new CircleObstacle(W * 0.50f, H * 0.50f, 60f));
            obstacles.add(new CircleObstacle(W * 0.42f, H * 0.78f, 40f));
            obstacles.add(new CircleObstacle(W * 0.62f, H * 0.26f, 50f));

            // Configuração de raios (central + dois "bigodes") e detector de colisão.
            detector = new CircleRaycastCollisionDetector(obstacles);
            rayConfig = new CentralRayWithWhiskersConfiguration<Vector2>(
                    npc, 110f, 70f, 35f * MathUtils.degreesToRadians);
            RaycastObstacleAvoidance<Vector2> avoid =
                    new RaycastObstacleAvoidance<Vector2>(npc, rayConfig, detector, 50f);

            // Chegar suave ao alvo.
            Arrive<Vector2> chegar = new Arrive<Vector2>(npc, target)
                    .setTimeToTarget(0.1f)
                    .setArrivalTolerance(2f)
                    .setDecelerationRadius(90f);

            // Prioridade: primeiro evitar obstáculos, depois chegar (igual ao slide 25).
            steering = new PrioritySteering<Vector2>(npc, 0.0001f)
                    .add(avoid)
                    .add(chegar);

            steeringOutput = new SteeringAcceleration<Vector2>(new Vector2());
        } catch (Exception e) {
            Gdx.app.error(TAG, "Falha ao inicializar a demonstração de steering", e);
            e.printStackTrace();
        }
    }

    @Override
    public void render() {
        try {
            float delta = Math.min(Gdx.graphics.getDeltaTime(), 1f / 30f);
            GdxAI.getTimepiece().update(delta);

            // O alvo acompanha o cursor do mouse.
            mouse.set(Gdx.input.getX(), Gdx.input.getY(), 0f);
            camera.unproject(mouse);
            target.position.set(mouse.x, mouse.y);

            // Calcula e aplica o steering (o coração do gdx-ai).
            steering.calculateSteering(steeringOutput);
            npc.update(delta, steeringOutput);

            draw();
        } catch (Exception e) {
            Gdx.app.error(TAG, "Erro durante a atualização/renderização", e);
            e.printStackTrace();
        }
    }

    private void draw() {
        ScreenUtils.clear(0.08f, 0.09f, 0.13f, 1f);
        camera.update();
        shapes.setProjectionMatrix(camera.combined);

        // Obstáculos.
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.35f, 0.37f, 0.45f, 1f);
        for (int i = 0; i < obstacles.size; i++) {
            CircleObstacle o = obstacles.get(i);
            shapes.circle(o.center.x, o.center.y, o.radius, 48);
        }
        // Alvo (cursor).
        shapes.setColor(1f, 0.84f, 0.04f, 1f);
        shapes.circle(target.position.x, target.position.y, 6f, 16);
        // NPC como um triângulo apontando na direção do movimento.
        npc.angleToVector(heading, npc.orientation);
        float a = npc.orientation;
        float r = npc.getBoundingRadius() + 4f;
        float tipX = npc.position.x + heading.x * r;
        float tipY = npc.position.y + heading.y * r;
        float leftX = npc.position.x + MathUtils.cos(a + 2.6f) * r;
        float leftY = npc.position.y + MathUtils.sin(a + 2.6f) * r;
        float rightX = npc.position.x + MathUtils.cos(a - 2.6f) * r;
        float rightY = npc.position.y + MathUtils.sin(a - 2.6f) * r;
        shapes.setColor(0.26f, 0.78f, 1f, 1f);
        shapes.triangle(tipX, tipY, leftX, leftY, rightX, rightY);
        shapes.end();

        // Raios do sensor: vermelho se batem em obstáculo, verde se livre.
        Ray<Vector2>[] rays = rayConfig.getRays();
        if (rays != null) {
            shapes.begin(ShapeRenderer.ShapeType.Line);
            for (Ray<Vector2> ray : rays) {
                if (ray == null || ray.start == null || ray.end == null) continue;
                boolean hit = detector.collides(ray);
                shapes.setColor(hit ? Color.RED : new Color(0.3f, 0.9f, 0.4f, 1f));
                shapes.line(ray.start.x, ray.start.y, ray.end.x, ray.end.y);
            }
            shapes.end();
        }

        // Texto de instrução.
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        font.setColor(Color.WHITE);
        font.draw(batch, "gdx-ai: PrioritySteering = RaycastObstacleAvoidance + Arrive", 16f, H - 16f);
        font.setColor(0.75f, 0.8f, 0.95f, 1f);
        font.draw(batch, "Mova o mouse: o NPC (triangulo) persegue o alvo (amarelo) desviando dos obstaculos.", 16f, H - 38f);
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, W, H);
    }

    @Override
    public void dispose() {
        try {
            if (shapes != null) shapes.dispose();
            if (batch != null) batch.dispose();
            if (font != null) font.dispose();
        } catch (Exception e) {
            Gdx.app.error(TAG, "Erro ao liberar recursos", e);
            e.printStackTrace();
        }
    }
}
