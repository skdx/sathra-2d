package eu.sathra.scene;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.microedition.khronos.opengles.GL10;

import android.view.animation.Animation;
import eu.sathra.ai.Task;
import eu.sathra.io.annotations.Defaults;
import eu.sathra.io.annotations.Deserialize;
import eu.sathra.physics.Body;
import eu.sathra.video.opengl.Sprite;

public class AnimatedSpriteNode extends SceneNode {

	private static final float DEFAULT_DURATION = 300;

	private float mDuration = DEFAULT_DURATION;
	private float mFrameDuration;
	private boolean mIsPlaying = true;
	private long mStopTime;
	private List<Sprite> mFrames = new ArrayList<Sprite>();

	@Deserialize({ "id", "x", "y", "is_visible", "frames", "duration",
			"is_playing", "animation", "children", "body", "ai" })
	@Defaults({ Deserialize.NULL, "0", "0", "true", Deserialize.NULL, "0",
			"true", Deserialize.NULL, Deserialize.NULL, Deserialize.NULL,
			Deserialize.NULL })
	public AnimatedSpriteNode(String id, int x, int y, boolean isVisible,
			Sprite[] frames, float duration, boolean isPlaying,
			Animation animation, SceneNode[] children, Body body, Task ai) {
		super(id, x, y, isVisible, animation, children, body, ai);
		setFrames(frames);
		setDuration(duration);
		setIsPlaying(isPlaying);
	}

	@Override
	protected void draw(GL10 gl, long time, long delta) {
		int frame = (int) (time / mFrameDuration) % mFrames.size();
		mFrames.get(frame).draw(gl);
	}

	public void addFrame(Sprite frame) {
		mFrames.add(frame);
		mFrameDuration = mDuration / mFrames.size();
	}

	public void setFrames(Sprite[] frames) {
		mFrames.addAll(Arrays.asList(frames));
		mFrameDuration = mDuration / mFrames.size();
	}

	public void setDuration(float duration) {
		mDuration = duration;
		mFrameDuration = mDuration / mFrames.size();
	}

	public float getDuration() {
		return mDuration;
	}

	public void start() {
		setIsPlaying(true);
	}

	public void stop() {
		setIsPlaying(false);
	}

	public void pause() {
		setIsPlaying(false);
	}

	public void setIsPlaying(boolean isPlaying) {
		mIsPlaying = isPlaying;
		// mStopTime = Timer.getInstance().getVirtualTime();
	}

}
