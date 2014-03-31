package eu.sathra.scene;

import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLES20;
import android.view.animation.Animation;
import eu.sathra.SathraActivity;
import eu.sathra.ai.Task;
import eu.sathra.io.annotations.Defaults;
import eu.sathra.io.annotations.Deserialize;
import eu.sathra.physics.Body;
import eu.sathra.video.opengl.Sprite;

public class LightNode extends SceneNode {
	private Sprite mSprite;

	public LightNode(Sprite sprite) {
		this(null, sprite, 0, 0, true, null, null, null, null);
	}

	@Deserialize({ "id", "sprite", "x", "y", "is_visible", "animation",
			"children", "body", "ai" })
	@Defaults({ Deserialize.NULL, Deserialize.NULL, "0", "0", "true",
			Deserialize.NULL, Deserialize.NULL, Deserialize.NULL,
			Deserialize.NULL })
	public LightNode(String id, Sprite sprite, int x, int y, boolean isVisible,
			Animation animation, SceneNode[] children, Body body, Task ai) {
		super(id, x, y, isVisible, animation, children, body, ai);
		mSprite = sprite;
	}

	@Override
	protected void draw(GL10 gl, long time, long delta) {

		// Switch to shadow buffer
		GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, SathraActivity.buf[0]);
		GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER,
				GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D,
				SathraActivity.tex[0], 0);

		// get and save current blend func
		int blend[] = new int[2];
		GLES20.glGetIntegerv(GLES20.GL_BLEND_SRC_ALPHA, blend, 0);
		GLES20.glGetIntegerv(GLES20.GL_BLEND_DST_ALPHA, blend, 1);

		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE);
		gl.glTranslatef(getX(), getY(), 0);
		mSprite.draw(gl);

		// Restore previous blend
		gl.glBlendFunc(blend[0], blend[1]);

		// Switch back to screen buffer
		GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

	}
}
