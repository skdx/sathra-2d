package eu.sathra.scene;

import javax.microedition.khronos.opengles.GL10;

import android.view.animation.Animation;
import eu.sathra.SathraActivity;
import eu.sathra.ai.Task;
import eu.sathra.io.annotations.Defaults;
import eu.sathra.io.annotations.Deserialize;
import eu.sathra.physics.Body;
import eu.sathra.util.Log;

// TODO: aspect ratio
public class CameraNode extends SceneNode {

	private static CameraNode sActive = null;

	private SathraActivity mSathra;

	public static CameraNode getActiveCamera() {
		return sActive;
	}

	@Deserialize({ Deserialize.NULL, "id", "transform", "is_visible",
			"animation", "children", "body", "ai" })
	@Defaults({ Deserialize.NULL, Deserialize.NULL, Deserialize.NULL, "true",
			Deserialize.NULL, Deserialize.NULL, Deserialize.NULL,
			Deserialize.NULL })
	public CameraNode(SathraActivity sathra, String id, Transform t,
			boolean isVisible, Animation animation, SceneNode[] children,
			Body body, Task ai) {
		super(id, t, isVisible, animation, children, body, ai);

		mSathra = sathra;
		
	}

	@Override
	protected void draw(GL10 gl, long time, long delta) {

		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glLoadIdentity();
//gl.glTranslatef(x, y, z);
//		float x1 = getAbsoluteX() - mSathra.getScreenWidth() / 2;
//		float y1 = getAbsoluteY() - mSathra.getScreenHeight() / 2;
//
//		float x2 = (getAbsoluteX() + mSathra.getScreenWidth())
//				- mSathra.getScreenWidth() / 2;
//		float y2 = (getAbsoluteY() + mSathra.getScreenHeight())
//				- mSathra.getScreenHeight() / 2;
//
//		gl.glScalef(getScaleX(), getScaleY(), 1);
//		gl.glOrthof(x1, x2, y1, y2, -1, 1);

		gl.glScalef(getAbsoluteScaleX(), getAbsoluteScaleY(), 1);
		gl.glTranslatef(-getAbsoluteX()/(mSathra.getScreenWidth()*0.5f)+1f, -getAbsoluteY()/(mSathra.getScreenHeight()*0.5f)+1, 0);
		//gl.glOrthof(0, mSathra.getScreenWidth(), 0, mSathra.getScreenHeight(), -1, 1);
		gl.glOrthof(0, mSathra.getScreenWidth(), 0, mSathra.getScreenHeight(), -1, 1);
		gl.glMatrixMode(GL10.GL_MODELVIEW);
	}

	@Override
	public void setVisible(boolean isVisible) {
		super.setVisible(isVisible);

		if (!isVisible && sActive == this)
			sActive = null;

		if (isVisible) {
			// There can be only one!!!
			if (sActive != null)
				sActive.setVisible(false);

			sActive = this;
		}

	}

}
