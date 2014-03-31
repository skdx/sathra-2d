package eu.sathra.ai;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.sathra.io.annotations.Defaults;
import eu.sathra.io.annotations.Deserialize;
import eu.sathra.scene.SceneNode;

public abstract class Task {

	private static Map<String, Task> mUserNodes = new HashMap<String, Task>();
	
	protected List<Task> mChildren = new ArrayList<Task>();

	public static void registerNode(String name, Task node) {
		mUserNodes.put(name, node);
	}
	
	public Task() {
		this(null);
	}
	
	@Deserialize( "children" )
	@Defaults( Deserialize.NULL )
	public Task(Task[] children) {
		
		if(children != null)
			addChildren(children);
	}
	
	public void addChild(Task child) {
		mChildren.add(child);
	}
	
	public void addChildren(Task[] children) {
		mChildren.addAll(Arrays.asList(children));
	}
	
	public void onAttach(AIContext context) {
		for(Task child : mChildren) {
			child.initialize(context);
		}
		
		initialize(context);
	}
	
	protected abstract void initialize(AIContext context);
	public abstract TaskResult execute(AIContext context, long time, long delta);
}
