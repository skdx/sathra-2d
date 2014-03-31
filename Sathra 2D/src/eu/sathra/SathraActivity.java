package eu.sathra;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import eu.sathra.Parameters.Orientation;
import eu.sathra.io.IO;
import eu.sathra.io.adapters.TypeAdapter;
import eu.sathra.physics.dyn4j.Dyn4jPhysics;
import eu.sathra.scene.CameraNode;
import eu.sathra.scene.SceneNode;
import eu.sathra.util.Log;
import eu.sathra.util.Util;
import eu.sathra.video.opengl.Sprite;
import eu.sathra.video.opengl.Texture;

public abstract class SathraActivity extends Activity implements
		android.opengl.GLSurfaceView.Renderer {

	private class SathraAdapter implements TypeAdapter<SathraActivity> {

		@Override
		public SathraActivity load(String param, JSONObject parent)
				throws JSONException {
			return SathraActivity.this;
		}
	}
	
	private class FPSCounterUpdater implements Runnable {

		@Override
		public void run() {
			mFPSCounterView.setText(String.format(FPS_TEXT_FORMAT, mFPS));
			if (mIsRunning)
				mFPSCounterView.postDelayed(this, 1000);
		}
	}
	
	private static final float MILISECONDS_TO_SECONDS = 0.001F;
	private static final float SECONDS_TO_MILISECONDS = 1000F;
	private static final float FPS_VIEW_TEXT_SIZE = 20;
	private static final String FPS_TEXT_FORMAT = "FPS: %.0f";

	private GLSurfaceView mSurfaceView;
	private Parameters mParams;
	private List<SceneNode> mChildren = new ArrayList<SceneNode>();
	private long mLastDrawTimestamp;
	private long mTime;
	private long mVirtualTime;
	private long mTimeDelta;
	private long mVirtualTimeDelta;
	private TextView mFPSCounterView;
	private float mFPS = 0;
	private float mTimeScale;
	private boolean mIsRunning = false;
	private boolean mWasInitiated = false;
	private FPSCounterUpdater mFPSCounterUpdater = new FPSCounterUpdater();

	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mParams = getParameters();

		if (mParams.fullscreen) {
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
					WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}

		if (mParams.layout == 0) {
			mSurfaceView = new GLSurfaceView(this);
			setContentView(mSurfaceView);
		} else {
			setContentView(mParams.layout);
			mSurfaceView = (GLSurfaceView) findViewById(R.id.surface);
		}

		mSurfaceView.setRenderer(this);
		mSurfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
		mSurfaceView.setPreserveEGLContextOnPause(true);
		
		// resize view to forced resolution
		if(mParams.width != 0 && mParams.height != 0)
			mSurfaceView.getHolder().setFixedSize(mParams.width, mParams.height);
		
		SathraAdapter adapter = new SathraAdapter();
		
		IO.getInstance().registerAdapter(SathraActivity.class, adapter);
		IO.getInstance().registerAdapter(Context.class, adapter);

		mIsRunning = true;

		// setup FPS view
		mFPSCounterView = new TextView(this);
		mFPSCounterView.setTextSize(FPS_VIEW_TEXT_SIZE);
		mFPSCounterView.setTextColor(Color.WHITE);
		((ViewGroup) findViewById(android.R.id.content))
				.addView(mFPSCounterView);
		mFPSCounterView.post(mFPSCounterUpdater);
		showFPS(mParams.showFPS);

		setTimeScale(1);

		setAmbientColor(mParams.ambientColor);

		setRequestedOrientation(mParams.orientation == Orientation.VERTICAL ? ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
				: ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
	}

	@Override
	public void onStart() {
		super.onStart();
		mIsRunning = true;
	}

	@Override
	public void onStop() {
		super.onStop();
		mIsRunning = false;
	}

	@Override
	public void onResume() {
		super.onResume();
		mSurfaceView.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
		mSurfaceView.onPause();
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		if (!mWasInitiated) {
			// Enable Smooth Shading, default not really needed.
			gl.glShadeModel(GL10.GL_SMOOTH);
			// Depth buffer setup.
			gl.glClearDepthf(1.0f);
			// Enables depth testing.
			gl.glEnable(GL10.GL_DEPTH_TEST);
			// The type of depth testing to do.
			gl.glDepthFunc(GL10.GL_LEQUAL);
			// Really nice perspective calculations.
			gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);

			gl.glEnable(GL10.GL_DITHER);
			gl.glEnable(GL10.GL_MULTISAMPLE);
			gl.glEnable(GL10.GL_BLEND);

			mLastDrawTimestamp = System.currentTimeMillis();
		}

		Log.debug("Surface created");
	}

	public static int buf[] = new int[1];
	public static int tex[] = new int[1];
	int shadowtex;
	Sprite shad;

	@SuppressLint("WrongCall")
	@Override
	public void onDrawFrame(GL10 gl) {
		mTimeDelta = System.currentTimeMillis() - mLastDrawTimestamp;
		mLastDrawTimestamp = System.currentTimeMillis();
		mTime += mTimeDelta;
		mVirtualTimeDelta = (long) (mTimeDelta * getTimeScale());
		mVirtualTime += mVirtualTimeDelta;

		// Setup defaults
		gl.glBindTexture(GL10.GL_TEXTURE_2D, 0);
		GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

		// Clear BG

		float r = (float) Color.red(mParams.bgColor) / 255;
		float g = (float) Color.green(mParams.bgColor) / 255;
		float b = (float) Color.blue(mParams.bgColor) / 255;
		float a = (float) Color.alpha(mParams.bgColor) / 255;
		gl.glClearColor(r, g, b, a);
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		GLES20.glBlendEquation(GLES20.GL_FUNC_ADD);

		gl.glMatrixMode(GL10.GL_TEXTURE);
		gl.glLoadIdentity();
		gl.glScalef(1, 1, 1);

		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();

		gl.glEnable(GL10.GL_BLEND);
		gl.glBlendFunc(GL10.GL_ONE, GL10.GL_ONE_MINUS_SRC_ALPHA);

		for (SceneNode child : mChildren)
			child.onDraw(gl, mVirtualTime, mVirtualTimeDelta);

		// Draw lights and shadows
		GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
		gl.glMatrixMode(GL10.GL_TEXTURE);
		gl.glLoadIdentity();
		gl.glScalef(1, -1, 1);
		gl.glMatrixMode(GL10.GL_MODELVIEW);

		CameraNode activeCam = CameraNode.getActiveCamera();

		if (activeCam != null) {
			shad.setPosition(activeCam.getAbsoluteX(), activeCam.getAbsoluteY());
		}

		gl.glBlendFunc(GL10.GL_DST_COLOR, GL10.GL_ONE_MINUS_SRC_ALPHA); // ALMOST!!!

		shad.draw(gl);

		GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, buf[0]);
		GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER,
				GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, tex[0], 0);

		// Clear shadow
		r = (float) Color.red(mParams.ambientColor) / 255;
		g = (float) Color.green(mParams.ambientColor) / 255;
		b = (float) Color.blue(mParams.ambientColor) / 255;
		a = (float) Color.alpha(mParams.ambientColor) / 255;

		gl.glClearColor(r, g, b, a);
		GLES20.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

		// TODO
		Dyn4jPhysics.getInstance().getWorld()
				.updatev(mVirtualTimeDelta * MILISECONDS_TO_SECONDS);

		if (mIsRunning)
			onUpdate(mVirtualTime, mVirtualTimeDelta);

		// Update fps counter
		mFPS = 1000f / mTimeDelta;
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		if (mParams.width == 0 || mParams.height == 0) {
			mParams.width = width;
			mParams.height = height;
		}

		gl.glViewport(0, 0, mParams.width, mParams.height);

		Log.debug("Surface resized [" + mParams.width + "x" + mParams.height + "]");

		if (!mWasInitiated) {
			GLES20.glGenFramebuffers(1, buf, 0);
			GLES20.glGenTextures(1, tex, 0);
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tex[0]);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
					GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
					GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
			IntBuffer tmp = ByteBuffer
					.allocateDirect(mParams.width * mParams.height * 4)
					.order(ByteOrder.nativeOrder()).asIntBuffer();
			GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA,
					mParams.width, mParams.height, 0, GLES20.GL_RGBA,
					GLES20.GL_UNSIGNED_SHORT_4_4_4_4, tmp);

			shad = new Sprite(
					new Texture(tex[0], mParams.width, mParams.height));
			shad.setPivot(0.5f, 0.5f);
			
//			this.runOnUiThread(new Runnable() {
//
//				@Override
//				public void run() {
//					onEngineInitiated();
//				}
//			});
			onEngineInitiated();
			mWasInitiated = true;
		}
	}

	/**
	 * Adds a SceneNode to root container
	 * 
	 * @param child
	 *            SceneNode to be added
	 */
	public void addNode(SceneNode child) {
		mChildren.add(child);
	}

	public void addNodes(SceneNode[] children) {
		mChildren.addAll(Arrays.asList(children));
	}

	public void removeNode(SceneNode child) {
		mChildren.remove(child);
	}

	public float getFPS() {
		return 1000f / mTimeDelta;
	}

	public void setAmbientColor(int light) {
		mParams.ambientColor = light;
	}

	/**
	 * Convenient method for for loading scene nodes from file.
	 * 
	 * @param filename
	 *            Path to a file located in assets folder
	 * @return Root scene node from file
	 * @throws Exception
	 */
	public SceneNode loadScene(String filename) throws Exception {
		JSONObject jObj = new JSONObject(Util.readFile(this, filename));
		SceneNode rootNode = IO.getInstance().load(jObj, SceneNode.class);
		addNode(rootNode);

		return rootNode;
	}

	/**
	 * Hides or shows FPS counter in the top left of the screen.
	 * 
	 * @param show
	 *            If true, FPS counter will be shown
	 */
	public void showFPS(boolean show) {
		mFPSCounterView.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
	}

	public boolean isShowingFPS() {
		if (mFPSCounterView.getVisibility() == View.VISIBLE)
			return true;

		return false;
	}

	protected abstract Parameters getParameters();

	protected abstract void onEngineInitiated();

	protected abstract void onUpdate(long time, long timeDelta);

	// public float getMaxFPS() {
	// return SECONDS_TO_MILISECONDS/mMinimumFrameDuration;
	// }
	//
	// public void setMaxFPS(float limit) {
	// mMinimumFrameDuration = (long) (SECONDS_TO_MILISECONDS/limit);
	// }

	public float getTimeScale() {
		return mTimeScale;
	}

	public void setTimeScale(float scale) {
		mTimeScale = scale;
	}

	/**
	 * Returns time the application has been active
	 * 
	 * @return Uptime in milliseconds
	 */
	public long getTime() {
		return mTime;
	}

	/**
	 * Returns time affected by time scale
	 * 
	 * @return Scaled time in milliseconds
	 */
	public long getVirtualTime() {
		return mVirtualTime;
	}

	/**
	 * Returns duration of last frame
	 * 
	 * @return Duration of last frame in milliseconds
	 */
	public long getTimeDelta() {
		return mTimeDelta;
	}

	/**
	 * Returns the time the world has been advanced in last frame.
	 * 
	 * @return Scaled time in milliseconds
	 */
	public long getVirtualTimeDelta() {
		return mVirtualTimeDelta;
	}

	public int getScreenWidth() {
		return mParams.width;
	}

	public int getScreenHeight() {
		return mParams.height;
	}
}
