package eu.sathra.scene;

import javax.microedition.khronos.opengles.GL10;

import android.view.animation.Animation;
import eu.sathra.ai.Task;
import eu.sathra.io.annotations.Defaults;
import eu.sathra.io.annotations.Deserialize;
import eu.sathra.physics.Body;
import eu.sathra.video.opengl.Sprite;

public class SpriteNode extends SceneNode {

	private Sprite mSprite;

	@Deserialize({ "id", "sprite", "x", "y", "is_visible", "animation",
			"children", "body", "ai" })
	@Defaults({ Deserialize.NULL, Deserialize.NULL, "0", "0", "true",
			Deserialize.NULL, Deserialize.NULL, Deserialize.NULL,
			Deserialize.NULL })
	public SpriteNode(String id, Sprite sprite, float x, float y,
			boolean isVisible, Animation animation, SceneNode[] children,
			Body body, Task ai) {
		super(id, x, y, isVisible, animation, children, body, ai);

		mSprite = sprite;
	}

	@Override
	protected void draw(GL10 gl, long time, long delta) {
		mSprite.draw(gl);
	}

}
