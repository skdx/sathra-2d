package eu.sathra.example.ai;

import android.widget.ImageView;
import eu.sathra.ai.AIContext;
import eu.sathra.ai.TaskResult;
import eu.sathra.util.Log;

public class JumpTask extends PlayerTask {

	private ImageView mJumpButton;
	
	@Override
	protected void initialize(AIContext context) {
		super.initialize(context);

		mJumpButton = (ImageView) context.getVariable(VAR_JUMP_BUTTON);
	}
	
	@Override
	public TaskResult execute(AIContext context, long time, long delta) {

		if(mJumpButton.isPressed()) {
			switchActiveAnimation(mJumpNode);
			context.getOwner().getBody().setImpulse(0, 2f*delta);
			
			return TaskResult.TRUE;
		}
		
		return TaskResult.FALSE;
	}

}
