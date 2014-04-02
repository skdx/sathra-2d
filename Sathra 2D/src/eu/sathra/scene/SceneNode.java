package eu.sathra.scene;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.microedition.khronos.opengles.GL10;

import android.annotation.SuppressLint;
import android.graphics.Matrix;
import eu.sathra.ai.AIContext;
import eu.sathra.ai.Task;
import eu.sathra.io.annotations.Defaults;
import eu.sathra.io.annotations.Deserialize;
import eu.sathra.physics.Body;
import eu.sathra.scene.animation.Animation;

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
public class SceneNode {

	private String mId;
	private boolean mIsVisible = true;
	private SceneNode mParent;
	private Body mBody;
	private Task mAITask;
	private AIContext mAIContext;
	private Set<SceneNode> mChildren = new LinkedHashSet<SceneNode>();
	private SceneNode[] mChildrenCopy = new SceneNode[0];
	private Animation mCurrentAnimation;
	private Transform mTransform = new Transform();
	private Transform mAbsoluteTransform = new Transform();
	private Object mUserData = null;

	public SceneNode() {
		this(null, new Transform(), true, null, null, null, null);
	}

	public SceneNode(Transform transform) {
		this(null, transform, true, null, null, null, null);
	}

	@Deserialize({ "id", "transform", "is_visible", "animation", "children",
			"body", "ai" })
	@Defaults({ Deserialize.NULL, Deserialize.NULL, "true", Deserialize.NULL,
			Deserialize.NULL, Deserialize.NULL, Deserialize.NULL })
	public SceneNode(String id, Transform t, boolean isVisible,
			Animation animation, SceneNode[] children, Body body, Task ai) {

		setId(id);
		setBody(body);
		setVisible(isVisible);
		setAIContext(new AIContext(this));
		setAI(ai);

		if (animation != null)
			startAnimation(animation);

		if (children != null)
			addChildren(children);
		
		if(t != null)
			setTransform(t);
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
	 * 
	 * @param child
	 */
	public void addChild(SceneNode child) {
		if (child.mParent != null) {
			child.mParent.removeChild(child);
		}

		mChildren.add(child);
		child.mParent = this;
		
		updateChildrenCopy();
	}

	public void addChildren(SceneNode[] children) {
		for (SceneNode child : children) {
			addChild(child);
		}
		
		updateChildrenCopy();
	}

	public void removeChild(SceneNode child) {
		mChildren.remove(child);
		child.mParent = null;
		
		updateChildrenCopy();
	}

	/**
	 * Return a copy of an array containing this node's children.
	 * 
	 * @return
	 */
	public SceneNode[] getChildren() {
		return mChildren.toArray(new SceneNode[mChildren.size()]);
	}

	/**
	 * Searches recursively for a node with a given id.
	 * 
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

			// Update absolute transform
			if(getParent() != null) 
				mAbsoluteTransform.set(getParent().getAbsoluteTransform());
			else
				mAbsoluteTransform.clear();
			
			mAbsoluteTransform.add(getTransform());

			// Apply animation
			if (mCurrentAnimation != null) {
				mCurrentAnimation.animate(delta, time, mTransform);
			}

			gl.glPushMatrix();
			//gl.glTranslatef(getX(), getY(), 0);
			gl.glScalef(getScaleX(), getScaleY(), 0);
			gl.glTranslatef(getX() / getAbsoluteScaleX(), getY()
					/ getAbsoluteScaleY(), 0);

			// TODO: rotation
			//gl.glRotatef(mTransform.getRotation(), 0, getX(), getY());
			
			// Draw yourself
			draw(gl, time, delta);

			// Draw children
			SceneNode[] childrenCopy = mChildrenCopy;

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
	
	public void setTransform(Transform t) {
		mTransform = t;
		
		if(mBody != null)
			mBody.setTransform(t);
	}
	
	public Transform getTransform() {
		return mBody == null? mTransform : mBody.getTransform();
	}
	
	public Transform getAbsoluteTransform() {
		return mAbsoluteTransform;
	}

	public void setPosition(float x, float y) {
		mTransform.setX(x);
		mTransform.setY(y);

		if (mBody != null)
			mBody.setPosition(x, y);
	}

	public float getX() {
		return getTransform().getX();
	}

	public float getY() {
		return getTransform().getY();
	}

	public void setScale(float x, float y) {
		mTransform.setScaleX(x);
		mTransform.setScaleY(y);
	}

	public float getScaleX() {
		return mTransform.getScaleX();
	}

	public float getScaleY() {
		return mTransform.getScaleY();
	}
	
	public float getAbsoluteScaleX() {
		return mAbsoluteTransform.getScaleX();
	}

	public float getAbsoluteScaleY() {
		return mAbsoluteTransform.getScaleY();
	}

	public void setVisible(boolean isVisible) {
		mIsVisible = isVisible;
	}

	public boolean isVisible() {
		return mIsVisible;
	}

	public void startAnimation(Animation animation) {
		mCurrentAnimation = animation;
		mCurrentAnimation.start();
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
		return mAbsoluteTransform.getX();
	}

	public float getAbsoluteY() {
		return mAbsoluteTransform.getY();
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

	private void updateChildrenCopy() {
		mChildrenCopy = mChildren.toArray(new SceneNode[mChildren.size()]);
	}
}
