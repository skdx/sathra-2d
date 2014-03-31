package eu.sathra.physics.shapes;

import eu.sathra.io.annotations.Deserialize;

public class Rectangle extends Shape {

	@Deserialize( { "width", "height" } )
	public Rectangle(float width, float height) {
		super(null);
		
		float[] vertices = { 
				0, 0, 
				width, 0, 
				width, height,
				0, height};
		
		setVertices(new float[][] {vertices});
	}
}
