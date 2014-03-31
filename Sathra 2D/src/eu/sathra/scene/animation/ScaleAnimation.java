package eu.sathra.scene.animation;

import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;
import eu.sathra.io.annotations.Defaults;
import eu.sathra.io.annotations.Deserialize;

public class ScaleAnimation extends Animation {

	private float mFromX;
	private float mFromY;
	private float mToX;
	private float mToY;

	public ScaleAnimation(float fromX, float fromY, float toX, float toY, int duration)
	{
		this(fromX, fromY, toX, toY, duration, 0, new LinearInterpolator(), true);
	}
	
	@Deserialize({ "from_x", "from_y", "to_x", "to_y", "duration", "count", "interpolator", "reverse" })
	@Defaults({ "1", "1", "1", "1", "300", "1", "linear", "true" })
	public ScaleAnimation(float fromX, float fromY, float toX, float toY, int duration, int count, Interpolator interpolator,  boolean reverse) {
		mFromX = fromX;
		mFromY = fromY;
		mToX = toX;
		mToY = toY;
		setRepeatCount(count == -1? INFINITE : count);
		setRepeatMode(reverse ? Animation.REVERSE : Animation.RESTART);
		setDuration(duration);
		setInterpolator(interpolator);
	}

	@Override
	protected void applyTransformation(float interpolatedTime, Transformation t) {
		float dx = mFromX;
		float dy = mFromY;
		if (mFromX != mToX) {
			dx = mFromX + ((mToX - mFromX) * interpolatedTime);
		}
		if (mFromY != mToY) {
			dy = mFromY + ((mToY - mFromY) * interpolatedTime);
		}
		t.getMatrix().setScale(dx, dy);//.setTranslate(dx, dy);
	}
}