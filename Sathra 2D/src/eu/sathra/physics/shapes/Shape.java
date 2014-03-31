package eu.sathra.physics.shapes;

import eu.sathra.io.annotations.Deserialize;

public class Shape {

	private float[][] mVertices;
	
	@Deserialize("vertices")
	public Shape(float[][] vertices) {
		mVertices = vertices;
	}
	
	public void setVertices(float[][] vertices) {
		mVertices = vertices;
	}
	
	public float[][] getVertices() {
		return mVertices;
	} 
}
