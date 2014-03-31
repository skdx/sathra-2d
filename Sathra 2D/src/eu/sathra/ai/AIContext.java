package eu.sathra.ai;

import java.util.HashMap;
import java.util.Map;

import eu.sathra.scene.SceneNode;

public class AIContext {

	private Map<String, Object> mVariables = new HashMap<String, Object>();
	private SceneNode mOwner;
	
	public AIContext(SceneNode owner) {
		mOwner = owner;
	}
	
	public SceneNode getOwner() {
		return mOwner;
	}
	
	public void setOwner(SceneNode owner) {
		mOwner = owner;
	}
	
	public void setVariable(String name, Object var) {
		mVariables.put(name, var);
	}
	
	public Object getVariable(String name) {
		return mVariables.get(name);
	}
}
