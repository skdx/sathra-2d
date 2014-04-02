package eu.sathra.ai;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.sathra.io.annotations.Defaults;
import eu.sathra.io.annotations.Deserialize;

/**
 * Base class for AI task. A task is a leaf in behaviour tree. Each task can
 * have indefinite number of children.
 * 
 * @author Milosz Moczkowski
 * 
 */
public abstract class Task {

	private static Map<String, Task> mUserNodes = new HashMap<String, Task>();

	protected List<Task> mChildren = new ArrayList<Task>();

	public static void registerNode(String name, Task node) {
		mUserNodes.put(name, node);
	}

	public Task() {
		this(null);
	}

	@Deserialize("children")
	@Defaults(Deserialize.NULL)
	public Task(Task[] children) {

		if (children != null)
			addChildren(children);
	}

	public void addChild(Task child) {
		mChildren.add(child);
	}

	public void addChildren(Task[] children) {
		mChildren.addAll(Arrays.asList(children));
	}

	/**
	 * Called by owning SceneNode. Do not call directly!
	 * @param context
	 */
	public void onAttach(AIContext context) {
		for (Task child : mChildren) {
			child.initialize(context);
		}

		initialize(context);
	}

	/**
	 * This method is called upon attaching task to an owner (SceneNode). This
	 * should be used for task initialization.
	 * 
	 * @param context
	 */
	protected abstract void initialize(AIContext context);

	/**
	 * Subclasses wanted to provide own logic, should implement this method.
	 * 
	 * @param context
	 * @param time
	 * @param delta
	 * @return
	 */
	public abstract TaskResult execute(AIContext context, long time, long delta);
}
