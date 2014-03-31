package eu.sathra.scene;

import javax.microedition.khronos.opengles.GL10;

import org.dyn4j.dynamics.World;

import eu.sathra.io.annotations.Deserialize;

public class RootNode extends SceneNode {

	@Deserialize( {"children" } )
	public RootNode(SceneNode[] children) {
		super(null, 0, 0, true, null, children, null, null);

	}
	
	@Override
	protected void draw(GL10 gl, long time, long delta) {

	}

}
