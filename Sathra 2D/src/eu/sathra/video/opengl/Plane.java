package eu.sathra.video.opengl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL10;

import android.graphics.Color;
import android.graphics.Matrix;

public class Plane implements Renderable {

	private static final int DEFAULT_COLOR = 0xffffffff;
	private static final short[] INDICES = { 0, 1, 2, 0, 2, 3 };

	private FloatBuffer vertexBuffer;
	private ShortBuffer indexBuffer;
	private int mWidth;
	private int mHeight;
	private float mX;
	private float mY;
	private float mOffsetX;
	private float mOffsetY;
	private float mRotation;
	private int mColor = DEFAULT_COLOR;

	public Plane() {
		setSize(0, 0);
	}

	public Plane(int width, int height) {
		setSize(width, height);
	}

	public Plane(int width, int height, float anchorX, float anchorY) {
		this(width, height);

		setPivot(anchorX, anchorY);
	}

	public void draw(GL10 gl) {
		gl.glPushMatrix();

		/* TRANSLATION */
		gl.glTranslatef(mX + mOffsetX * getWidth(),
				mY + mOffsetY * getHeight(), 0);

		/* ROTATION */
		// gl.glRotatef(mRotation, 0, 0, 1);

		/* DRAWING */
		 float r = (float) Color.red(mColor) / 255;
		 float g = (float) Color.green(mColor) / 255;
		 float b = (float) Color.blue(mColor) / 255;
		 float a = (float) Color.alpha(mColor) / 255;
		
		 gl.glColor4f(r, g, b, a);

		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);

		gl.glDrawElements(GL10.GL_TRIANGLES, INDICES.length,
				GL10.GL_UNSIGNED_SHORT, indexBuffer);

		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glPopMatrix();
	}

	public int getWidth() {
		return mWidth;
	}

	public int getHeight() {
		return mHeight;
	}

	public void setSize(int width, int height) {
		mWidth = width;
		mHeight = height;

		float vertices[] = { 0f, mHeight, 0.0f, // 0, Top Left
				0f, 0f, 0.0f, // 1, Bottom Left
				mWidth, 0f, 0.0f, // 2, Bottom Right
				mWidth, mHeight, 0.0f, // 3, Top Right
		};

		ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
		vbb.order(ByteOrder.nativeOrder());
		vertexBuffer = vbb.asFloatBuffer();
		vertexBuffer.put(vertices);
		vertexBuffer.position(0);

		ByteBuffer ibb = ByteBuffer.allocateDirect(INDICES.length * 2);
		ibb.order(ByteOrder.nativeOrder());
		indexBuffer = ibb.asShortBuffer();
		indexBuffer.put(INDICES);
		indexBuffer.position(0);
	}

	/**
	 * 
	 * @param Angle
	 *            in degrees
	 */
	public void rotate(float angle) {
		mRotation = angle;
	}

	public float getRotation() {
		return mRotation;
	}

	public void setAnchor(Pivot origin) {
		switch (origin) {
		case CENTER:
			mOffsetX = -0.5f;
			mOffsetY = -0.5f;
			break;

		case BOTTOM_LEFT:
			mOffsetX = 0f;
			mOffsetY = 0f;
			break;
		}
	}

	public void setPivot(float x, float y) {
		mOffsetX = -x;
		mOffsetY = -y;
	}

	public void setPosition(float x, float y) {
		mX = x;
		mY = y;
	}

	public float getX() {
		return mX;
	}

	public float getY() {
		return mY;
	}

	// TODO: rotation
	public void setMatrix(Matrix matrix) {
		float array[] = new float[9];

		matrix.getValues(array);

		mX = array[2];
		mY = array[5];
	}

	public ShortBuffer getIndices() {
		return indexBuffer;
	}

	public FloatBuffer getVertices() {
		return vertexBuffer;
	}

	public void setColor(int color) {
		mColor = color;
	}

}