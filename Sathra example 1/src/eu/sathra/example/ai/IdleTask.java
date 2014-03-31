package eu.sathra.example.ai;

import eu.sathra.ai.AIContext;
import eu.sathra.ai.TaskResult;
import eu.sathra.scene.SceneNode;

public class IdleTask extends PlayerTask {

	@Override
	public TaskResult execute(AIContext context, long time, long delta) {
		switchActiveAnimation(mStandNode);
		
		return TaskResult.TRUE;
	}
}
