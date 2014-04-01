package eu.sathra.scene;

import java.util.Random;

import javax.microedition.khronos.opengles.GL10;

import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import eu.sathra.ai.Task;
import eu.sathra.io.annotations.Defaults;
import eu.sathra.io.annotations.Deserialize;
import eu.sathra.physics.Body;
import eu.sathra.scene.animation.TranslateAnimation;
import eu.sathra.util.Log;
import eu.sathra.video.opengl.Sprite;

public class ParticleEmitterNode extends SceneNode implements AnimationListener {

	public static class EmitParameters {

		public Sprite particle;
		public float minEmission;
		public float maxEmission;
		public long minLifetime;
		public long maxLifetime;
		public float minSize;
		public float maxSize;
		public float velocityX; // per sec
		public float velocityY; // per sec
		public float rndVelocityX; // per sec
		public float rndVelocityY; // per sec
		public float width;
		public float height;

		@Deserialize({ "particle", "min_emision", "max_emision", "min_life",
				"max_life", "min_size", "max_size", "velocity_x", "velocity_y",
				"rnd_velocity_x", "rnd_velocity_y", "width", "height" })
		@Defaults({ "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0",
				"0" })
		public EmitParameters(Sprite particle, float minEmission,
				float maxEmission, long minLife, long maxLife, float minSize,
				float maxSize, float velocityX, float velocityY,
				float rndVelocityX, float rndVelocityY, float width,
				float height) {

			this.particle = particle;
			this.minEmission = minEmission;
			this.maxEmission = maxEmission;
			this.minLifetime = minLife;
			this.maxLifetime = maxLife;
			this.minSize = minSize;
			this.maxSize = maxSize;
			this.velocityX = velocityX;
			this.velocityY = velocityY;
			this.rndVelocityX = rndVelocityX;
			this.rndVelocityY = rndVelocityY;
			this.width = width;
			this.height = height;
		}

	}

	// Destroyer of the worlds!
	private class NodeDestroyer implements AnimationListener {

		private SceneNode mChild;

		public NodeDestroyer(SceneNode child) {
			mChild = child;
		}

		@Override
		public void onAnimationEnd(Animation anim) {
			removeChild(mChild);
		}

		@Override
		public void onAnimationRepeat(Animation anim) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onAnimationStart(Animation anim) {

		}

	}

	private EmitParameters mParams;
	private boolean mIsEmitting;

	// Minimum time between consecutive emits (in millis)
	private float mMinEmitDelay;
	// Maximum time between consecutive emits (in millis)
	private float mMaxEmitDelay;
	// App time when next emit will occour
	private float mNextEmit;
	private Random mRandom = new Random();

	@Deserialize({ "id", "x", "y", "is_visible", "animation", "children",
			"body", "ai", "params" })
	@Defaults({ Deserialize.NULL, "0", "0", "true", Deserialize.NULL,
			Deserialize.NULL, Deserialize.NULL, Deserialize.NULL,
			Deserialize.NULL })
	public ParticleEmitterNode(String id, float x, float y, boolean isVisible,
			Animation animation, SceneNode[] children, Body body, Task ai,
			EmitParameters params) {

		super(id, x, y, isVisible, animation, children, body, ai);

		setEmitParameters(params);
		emit(true);
	}

	public void emit(boolean emit) {
		mIsEmitting = emit;
		mNextEmit = 0;
	}

	public boolean isEmitting() {
		return mIsEmitting;
	}

	/***
	 * 
	 * @param min
	 *            The minimum amount of particles to be spawned every second.
	 * @param max
	 *            The maximum amount of particles to be spawned every second.
	 */
	public void setParticleEmmision(float min, float max) {
		if (max < min)
			throw new IllegalArgumentException("max<min");

		mParams.minEmission = min;
		mParams.maxEmission = max;

		mMinEmitDelay = 1000 / mParams.minEmission;
		mMaxEmitDelay = 1000 / mParams.maxEmission;
	}

	/**
	 * 
	 * @param min
	 *            The minimum lifetime of spawned particle in milliseconds.
	 * @param max
	 *            The maximum lifetime of spawned particle in milliseconds.
	 */
	public void setParticleLifetime(long min, long max) {
		if (max < min)
			throw new IllegalArgumentException("max<min");

		mParams.minLifetime = min;
		mParams.maxLifetime = max;
	}

	public void setParticleSize(float min, float max) {
		if (max < min)
			throw new IllegalArgumentException("max<min");

		mParams.minSize = min;
		mParams.maxSize = max;
	}

	public void setEmitParameters(EmitParameters params) {
		mParams = params;
		mMinEmitDelay = 1000 / mParams.minEmission;
		mMaxEmitDelay = 1000 / mParams.maxEmission;

	}

	public EmitParameters getEmitParameters() {
		return mParams;
	}

	public void setParticle(Sprite particle) {
		mParams.particle = particle;
	}

	protected synchronized void draw(GL10 gl, long time, long delta) {
		if (time > mNextEmit && isEmitting()) {
			// time to emit
			final long lifespan = (long) (mParams.minLifetime + (mParams.maxLifetime - mParams.minLifetime)
					* mRandom.nextFloat());

			float size = mParams.minSize + (mParams.maxSize - mParams.minSize)
					* mRandom.nextFloat();

			// Calculate spawn point
			float spawnX = mParams.width * mRandom.nextFloat();
			float spawnY = mParams.height * mRandom.nextFloat();

			// Calculate velocity
			float velX = mParams.velocityX + (1 - 2 * mRandom.nextFloat())
					* mParams.rndVelocityX;
			float velY = mParams.velocityY + (1 - 2 * mRandom.nextFloat())
					* mParams.rndVelocityY;

			// Calculate destination point
			float destX = spawnX + velX * lifespan / 1000;
			float destY = spawnY + velY * lifespan / 1000;

			TranslateAnimation animation = new TranslateAnimation(spawnX,
					destY, destX, spawnY, lifespan);

			animation.setRepeatCount(1);

			SpriteNode particleNode = new SpriteNode(null, mParams.particle,
					spawnX, spawnY, true, animation, null, null, null);

			animation.setAnimationListener(new NodeDestroyer(particleNode));

			particleNode.setScale(size, size);

			addChild(particleNode);

			// calc next emit time
			mNextEmit = time + mMinEmitDelay + (mMaxEmitDelay - mMinEmitDelay)
					* mRandom.nextFloat();
		}
	}

	@Override
	public void onAnimationEnd(Animation anim) {

	}

	@Override
	public void onAnimationRepeat(Animation anim) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onAnimationStart(Animation anim) {
		// TODO Auto-generated method stub

	}
}
