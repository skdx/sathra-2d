package eu.sathra.example.ai;

import eu.sathra.ai.AIContext;
import eu.sathra.ai.TaskResult;
import eu.sathra.scene.SceneNode;
import eu.sathra.scene.animation.Animation;

public class DieTask extends PlayerTask {

	private static final float SPAWN_POINT_X = 200;
	private static final float SPAWN_POINT_Y = 200;
	private Animation mRespawnAnimation;
	
	public DieTask() {
//		Transform from = new Transform(SPAWN_POINT_X, SPAWN_POINT_Y, 0, 0, 0);
//		Transform to = new Transform(SPAWN_POINT_X, SPAWN_POINT_Y, 0, 1, 1);
//		mRespawnAnimation = new Animation(from, to, new LinearInterpolator(), 2000, 1, true);
	}
	
	@Override
	public TaskResult execute(AIContext context, long time, long delta) {
		SceneNode playerNode = context.getOwner();
		if(context.getOwner().getAbsoluteY() <= 0) {
			playerNode.setPosition(SPAWN_POINT_X, SPAWN_POINT_Y);
			//playerNode.startAnimation(mRespawnAnimation);
			return TaskResult.TRUE;
		}
		
		return TaskResult.FALSE;
	}

}
