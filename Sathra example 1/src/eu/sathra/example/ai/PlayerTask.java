package eu.sathra.example.ai;

import eu.sathra.ai.AIContext;
import eu.sathra.ai.Task;
import eu.sathra.scene.SceneNode;

public abstract class PlayerTask extends Task {

	public static final String VAR_JUMP_BUTTON = "var_button_jump";
	public static final String VAR_LEFT_BUTTON = "var_left_jump";
	public static final String VAR_RIGHT_BUTTON = "var_right_jump";
	
	public static final String VAR_FALL_NODE = "fall";
	
	protected SceneNode mStandNode;
	protected SceneNode mRunNode;
	protected SceneNode mJumpNode;
	protected SceneNode mFallNode;
	
	@Override
	protected void initialize(AIContext context) {
		SceneNode animParent = context.getOwner().findChildById("anim_parent");
		mStandNode = animParent.findChildById("stand");
		mRunNode = animParent.findChildById("run");
		mJumpNode = animParent.findChildById("jump");
		mFallNode = animParent.findChildById(VAR_FALL_NODE);
	}
	
	protected void switchActiveAnimation(SceneNode node) {
		mStandNode.setVisible(false);
		mRunNode.setVisible(false);
		mJumpNode.setVisible(false);
		mFallNode.setVisible(false);
		node.setVisible(true);
	}
}
