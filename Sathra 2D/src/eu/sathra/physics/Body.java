package eu.sathra.physics;

import java.util.List;

import eu.sathra.physics.shapes.Shape;
import eu.sathra.scene.Transform;

public interface Body {

	void setTransform(Transform t);
	Transform getTransform();
	void setPosition(float x, float y);
	float getX();
	float getY();
	void setRectangleShape(float width, float height);
	void setCircleShape(float radius);
	void setPolygonShape(float[][] vertices);
	void setShape(Shape shape);
	void setMass(float mass);
	void applyForce(float x, float y);
	float getXForce();
	float getYForce();
	void setImpulse(float x, float y);
	void setVelocity(float x, float y);
	void setYVelocity(float y);
	void setXVelocity(float x);
	float getXVelocty();
	float getYVelocity();
	boolean isColliding();
	List<Body> getCollidedBodies();
}
