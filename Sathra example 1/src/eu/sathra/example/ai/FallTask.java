package eu.sathra.example.ai;

import eu.sathra.ai.AIContext;
import eu.sathra.ai.TaskResult;

public class FallTask extends PlayerTask {

	@Override
	public TaskResult execute(AIContext context, long time, long delta) {

		if(!context.getOwner().getBody().isColliding()) {
			switchActiveAnimation(mFallNode);
			
			return TaskResult.TRUE;
		}
		
		return TaskResult.FALSE;
	}

}
