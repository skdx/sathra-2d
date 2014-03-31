package eu.sathra.physics.dyn4j;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dyn4j.collision.narrowphase.Penetration;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.dynamics.CollisionAdapter;
import org.dyn4j.dynamics.Step;
import org.dyn4j.dynamics.StepListener;
import org.dyn4j.dynamics.World;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.Mass;
import org.dyn4j.geometry.Polygon;
import org.dyn4j.geometry.Rectangle;
import org.dyn4j.geometry.Vector2;

import eu.sathra.io.annotations.Defaults;
import eu.sathra.io.annotations.Deserialize;
import eu.sathra.physics.shapes.Shape;
import eu.sathra.util.Log;

public class Dyn4jBody extends CollisionAdapter implements
		eu.sathra.physics.Body, StepListener {

	private static final float PHYSICS_TO_PIXEL = 10;

	private Body mBody;
	private Set<Body> mCollidedBodies = new HashSet<Body>();

	public Dyn4jBody() {
		mBody = new Body();
		mBody.setUserData(this);
		Dyn4jPhysics.getInstance().getWorld().addBody(mBody);
		setMass(0);
	}

	@Deserialize({ "mass", "shape" })
	@Defaults({ "0", Deserialize.NULL })
	public Dyn4jBody(float mass, Shape shape) {
		this();

		if (shape != null)
			setShape(shape);

		setMass(mass);

		World myWorld = Dyn4jPhysics.getInstance().getWorld();

		myWorld.addListener(this);
	}

	@Override
	public float getX() {
		return (float) mBody.getTransform().getTranslationX()
				* PHYSICS_TO_PIXEL;
	}

	@Override
	public float getY() {
		return (float) mBody.getTransform().getTranslationY()
				* PHYSICS_TO_PIXEL;
	}

	@Override
	public void setPosition(float x, float y) {
		mBody.getTransform().setTranslation(x / PHYSICS_TO_PIXEL,
				y / PHYSICS_TO_PIXEL);
	}

	public void setCollider() {
		// mBody.getMass().
	}

	@Override
	public void setRectangleShape(float width, float height) {
		mBody.removeAllFixtures();
		Rectangle myRectangle = new Rectangle(width / PHYSICS_TO_PIXEL, height
				/ PHYSICS_TO_PIXEL);
		mBody.addFixture(myRectangle);
		mBody.setMass();
	}

	@Override
	public void setCircleShape(float radius) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setPolygonShape(float[][] vertices) {
		mBody.removeAllFixtures();

		for (float convex[] : vertices) {
			List<Vector2> myVertices = new ArrayList<Vector2>();

			for (int c = 0; c < convex.length; c += 2) {
				myVertices.add(new Vector2(convex[c] / PHYSICS_TO_PIXEL,
						convex[c + 1] / PHYSICS_TO_PIXEL));
			}

			Polygon myPolygon = Geometry.createPolygon(myVertices
					.toArray(new Vector2[myVertices.size()]));
			// . new Polygon( myVertices.toArray(new
			// Vector2[myVertices.size()]));

			mBody.addFixture(myPolygon);
		}

		// mBody.setMass();
	}

	@Override
	public void setMass(float mass) {
		Mass myMass = mBody.getMass();
		mBody.setMass(new Mass(myMass.getCenter(), mass, myMass.getInertia()));
	}

	@Override
	public void setShape(Shape shape) {
		setPolygonShape(shape.getVertices());
	}

	@Override
	public void applyForce(float x, float y) {
		mBody.applyForce(new Vector2(x, y));
	}

	@Override
	public void setVelocity(float x, float y) {
		mBody.setLinearVelocity(x, y);// (new Vector2(x, y));
	}

	@Override
	public void setYVelocity(float y) {
		mBody.setLinearVelocity(mBody.getLinearVelocity().x, y);
	}

	@Override
	public void setXVelocity(float x) {
		mBody.setLinearVelocity(x, mBody.getLinearVelocity().y);
	}

	@Override
	public float getXVelocty() {
		return (float) mBody.getLinearVelocity().x;
	}

	@Override
	public float getYVelocity() {
		return (float) mBody.getLinearVelocity().y;
	}

	@Override
	public float getXForce() {
		return (float) mBody.getForce().x;
	}

	@Override
	public float getYForce() {
		return (float) mBody.getForce().y;
	}

	@Override
	public void setImpulse(float x, float y) {
		mBody.applyImpulse(new Vector2(x, y));
	}

	@Override
	public boolean collision(Body body1, BodyFixture fixture1, Body body2,
			BodyFixture fixture2, Penetration penetration) {
		if (body1 == mBody || body2 == mBody) {
			Body other = (mBody == body1 ? body2 : body1);// Log.error("collision!"
															// + other);

			// if(!mCollidedBodies.contains(other))
			mCollidedBodies.add(other);
		}

		return true;
	}

	@Override
	public boolean isColliding() {
		return !mCollidedBodies.isEmpty();
	}

	@Override
	public List<eu.sathra.physics.Body> getCollidedBodies() {
		List<eu.sathra.physics.Body> collided = new ArrayList<eu.sathra.physics.Body>();

		for (Body other : mCollidedBodies)
			collided.add((eu.sathra.physics.Body) other.getUserData());

		return collided;
	}

	@Override
	public void begin(Step step, World world) {
		List<Body> bodiesToRemove = new ArrayList<Body>();

		// update list of collisions
		for (Body other : mCollidedBodies)
			if (!other.isInContact(mBody))
				bodiesToRemove.add(other);

		mCollidedBodies.removeAll(bodiesToRemove);
	}

	@Override
	public void end(Step step, World world) {
		// TODO Auto-generated method stub

	}

	@Override
	public void updatePerformed(Step step, World world) {
		
	}

}
