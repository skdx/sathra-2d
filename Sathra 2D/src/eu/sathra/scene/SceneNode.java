package eu.sathra.scene;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.microedition.khronos.opengles.GL10;

import android.annotation.SuppressLint;
import android.graphics.Matrix;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import eu.sathra.ai.AIContext;
import eu.sathra.ai.Task;
import eu.sathra.io.annotations.Defaults;
import eu.sathra.io.annotations.Deserialize;
import eu.sathra.physics.Body;

/***
 * Base class for all scene nodes. Scene node is a part of hierarchical scene
 * graph. Each node can have arbitrary number of children (also scene nodes).
 * The transformation applied to a node is relative to it's parent. If a parent
 * is not visible, the children also won't be drawn.
 * 
 * @author Milosz Moczkowski
 * 
 */
// TODO: findChildrenById
public class SceneNode implements Cloneable {

	private String mId;
	private boolean mIsVisible = true;
	private SceneNode mParent;
	private Body mBody;
	private Task mAITask;
	private AIContext mAIContext;
	private Set<SceneNode> mChildren = new LinkedHashSet<SceneNode>();
	private Animation mCurrentAnimation;
	private Transformation mTransformation = new Transformation();
	private float mX;
	private float mY;
	private float mAbsoluteX;
	private float mAbsoluteY;
	private float mScaleX;
	private float mScaleY;
	private float mAbsoluteScaleX;
	private float mAbsoluteScaleY;
	private Object mUserData = null;

	public SceneNode() {
		this(null, 0, 0, true, null, null, null, null);
	}

	public SceneNode(float x, float y) {
		this(null, x, y, true, null, null, null, null);
	}

	@Deserialize({ "id", "x", "y", "is_visible", "animation", "children",
			"body", "ai" })
	@Defaults({ Deserialize.NULL, "0", "0", "true", Deserialize.NULL,
			Deserialize.NULL, Deserialize.NULL, Deserialize.NULL,
			Deserialize.NULL })
	public SceneNode(String id, float x, float y, boolean isVisible,
			Animation animation, SceneNode[] children, Body body, Task ai) {

		setId(id);
		setBody(body);
		setPosition(x, y);
		setVisible(isVisible);
		setScale(1, 1);
		setAIContext(new AIContext(this));
		setAI(ai);

		if (animation != null)
			startAnimation(animation);

		if (children != null)
			addChildren(children);
	}

	/**
	 * Returns this node string id. This can be used to identify nodes and
	 * obtaining references to deserialized nodes. See: {@see
	 * eu.sathra.scene.SceneNode#findChildById(String)}
	 * 
	 * @return
	 */
	public String getId() {
		return mId;
	}

	public void setId(String id) {
		mId = id;
	}

	/**
	 * Get this node's parent
	 * 
	 * @return Node's parent or null if it has no parent.
	 */
	public SceneNode getParent() {
		return mParent;
	}

	/**
	 * Set a parent of this node. Equivalent of calling
	 * {@code parent.addChild(node); }
	 * 
	 * @param parent
	 */
	public void setParent(SceneNode parent) {
		parent.addChild(this);
	}

	/**
	 * Add node to this child.
	 * @param child
	 */
	public void addChild(SceneNode child) {
		if (child.mParent != null) {
			child.mParent.removeChild(child);
		}

		mChildren.add(child);
		child.mParent = this;
	}

	public void addChildren(SceneNode[] children) {
		for (SceneNode child : children) {
			addChild(child);
		}
	}

	public void removeChild(SceneNode child) {
		mChildren.remove(child);
		child.mParent = null;
	}

	/**
	 * Return a copy of an array containing this node's children.
	 * @return
	 */
	public SceneNode[] getChildren() {
		return mChildren.toArray(new SceneNode[mChildren.size()]);
	}

	/**
	 * Searches recursively for a node with a given id.
	 * @param id
	 * @return
	 */
	public SceneNode findChildById(String id) {
		for (SceneNode child : mChildren) {
			if (child.getId() == null && id == null)
				return child;
			else if (child != null && child.getId().equals(id)) {
				return child;
			}

			SceneNode subChild = child.findChildById(id);

			if (subChild != null)
				return child;
		}

		return null;
	}

	@SuppressLint("WrongCall")
	public void onDraw(GL10 gl, long time, long delta) {
		if (isVisible()) {

			// Update absolute position and scale
			mAbsoluteX = mParent == null ? 0 : mParent.getAbsoluteX();
			mAbsoluteY = mParent == null ? 0 : mParent.getAbsoluteY();
			mAbsoluteX += getX();
			mAbsoluteY += getY();

			mAbsoluteScaleX = mParent == null ? 1 : mParent.getAbsoluteScaleX();
			mAbsoluteScaleY = mParent == null ? 1 : mParent.getAbsoluteScaleY();
			mAbsoluteScaleX *= getScaleX();
			mAbsoluteScaleY *= getScaleY();

			// Apply animation
			if (mCurrentAnimation != null) {

				boolean animated = mCurrentAnimation.getTransformation(
						System.currentTimeMillis(), mTransformation);

				if (animated)
					setMatrix(mTransformation.getMatrix());
			}

			gl.glPushMatrix();
			gl.glTranslatef(getX(), getY(), 0);
			// gl.glPushMatrix();
			gl.glScalef(getScaleX(), getScaleY(), 0);

			// Draw yourself
			draw(gl, time, delta);
			// gl.glPopMatrix();

			// Draw children
			SceneNode[] childrenCopy = mChildren
					.toArray(new SceneNode[mChildren.size()]);
			gl.glScalef(1, 1, 1);
			for (SceneNode child : childrenCopy) {
				child.onDraw(gl, time, delta);
			}

			gl.glPopMatrix();

			// Update AI
			if (mAITask != null)
				mAITask.execute(mAIContext, time, delta);
		}
	}

	public void setMatrix(Matrix matrix) {
		float array[] = new float[9];

		matrix.getValues(array);

		setPosition(array[2], array[5]);
		setScale(array[0], array[4]);
	}

	public void setPosition(float x, float y) {
		mX = x;
		mY = y;

		if (mBody != null)
			mBody.setPosition(x, y);
	}

	public float getX() {
		return mBody == null ? mX : mBody.getX();
	}

	public float getY() {
		return mBody == null ? mY : mBody.getY();
	}

	public void setScale(float x, float y) {
		mScaleX = x;
		mScaleY = y;
	}

	public float getScaleX() {
		return mScaleX;
	}

	public float getScaleY() {
		return mScaleY;
	}

	public float getAbsoluteScaleX() {
		return mAbsoluteScaleX;
	}

	public float getAbsoluteScaleY() {
		return mAbsoluteScaleY;
	}

	public void setVisible(boolean isVisible) {
		mIsVisible = isVisible;
	}

	public boolean isVisible() {
		return mIsVisible;
	}

	public void startAnimation(Animation animation) {
		mCurrentAnimation = animation;
		mCurrentAnimation.startNow();
	}

	public Animation getAnimation() {
		return mCurrentAnimation;
	}

	public void setBody(Body body) {
		mBody = body;
	}

	public Body getBody() {
		return mBody;
	}

	public float getAbsoluteX() {
		return mAbsoluteX;
	}

	public float getAbsoluteY() {
		return mAbsoluteY;
	}

	public void setAI(Task ai) {
		mAITask = ai;
		// mAIContext = new AIContext(this); //TODO: reconsider

		if (mAITask != null)
			mAITask.onAttach(mAIContext);
	}

	public AIContext getAIContext() {
		return mAIContext;
	}

	public void setAIContext(AIContext context) {
		mAIContext = context;
		mAIContext.setOwner(this);

		if (mAITask != null)
			mAITask.onAttach(mAIContext);
	}

	public Task getAI() {
		return mAITask;
	}

	public void setUserData(Object data) {
		mUserData = data;
	}

	public Object getUserData() {
		return mUserData;
	}

	protected void draw(GL10 gl, long time, long delta) {
		// noop
	}
}
