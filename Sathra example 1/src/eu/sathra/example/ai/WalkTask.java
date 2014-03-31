package eu.sathra.example.ai;

import android.view.View;
import eu.sathra.ai.AIContext;
import eu.sathra.ai.TaskResult;
import eu.sathra.scene.SceneNode;

public class WalkTask extends PlayerTask {

	private View mLeftButton;
	private View mRightButton;
	
	@Override
	protected void initialize(AIContext context) {
		super.initialize(context);
		mLeftButton = (View) context.getVariable(PlayerTask.VAR_LEFT_BUTTON);
		mRightButton = (View) context.getVariable(PlayerTask.VAR_RIGHT_BUTTON);
	}
	
	@Override
	public TaskResult execute(AIContext context, long time, long delta) {
		
		if(mLeftButton.isPressed() || mRightButton.isPressed()) {
			switchActiveAnimation(mRunNode);
			return TaskResult.TRUE;
		}
		
		return TaskResult.FALSE;
	}

}
