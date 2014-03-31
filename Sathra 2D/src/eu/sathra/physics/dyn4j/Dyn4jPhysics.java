package eu.sathra.physics.dyn4j;

import java.util.ArrayList;
import java.util.List;

import org.dyn4j.dynamics.RaycastResult;
import org.dyn4j.dynamics.World;
import org.dyn4j.geometry.Ray;
import org.dyn4j.geometry.Vector2;

import eu.sathra.physics.Body;
import eu.sathra.physics.Physics;

public class Dyn4jPhysics implements Physics {

	private static Dyn4jPhysics sInstance;

	private World mWorld;

	public static Dyn4jPhysics getInstance() {
		if (sInstance == null)
			sInstance = new Dyn4jPhysics();

		return sInstance;
	}

	private Dyn4jPhysics() {
		mWorld = new World();
		mWorld.setGravity(new Vector2(0, -100)); // TODO
	}

	@Override
	public void setGravity(int x, int y) {
		mWorld.setGravity(new Vector2(x, y));
	}

	public World getWorld() {
		return mWorld;
	}

	public List<eu.sathra.physics.Body> raycast(float x, float y, float toX,
			float toY, float length, boolean all) {
		float angle = (float) Math.toDegrees(Math.atan2(toX - x, toY - y)) % 360;

		return raycast(x, y, angle, length, all);
	}

	public List<eu.sathra.physics.Body> raycast(float x, float y, float angle,
			float length, boolean all) {
		List<eu.sathra.physics.Body> hits = new ArrayList<eu.sathra.physics.Body>();
		List<RaycastResult> results = new ArrayList<RaycastResult>();

		mWorld.raycast(
				new Ray(new Vector2(x, y), angle * Math.toRadians(angle)),
				length, true, all, results);

		for (RaycastResult result : results) {
			hits.add((Body) result.getBody().getUserData());
		}

		return hits;
	}

}
