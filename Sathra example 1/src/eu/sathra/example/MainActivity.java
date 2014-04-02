package eu.sathra.example;

import android.view.View;
import android.view.View.OnClickListener;
import eu.sathra.Parameters;
import eu.sathra.SathraActivity;
import eu.sathra.ai.AIContext;
import eu.sathra.ai.SelectorTask;
import eu.sathra.example.ai.DieTask;
import eu.sathra.example.ai.FallTask;
import eu.sathra.example.ai.IdleTask;
import eu.sathra.example.ai.JumpTask;
import eu.sathra.example.ai.PlayerTask;
import eu.sathra.example.ai.WalkTask;
import eu.sathra.scene.SceneNode;

public class MainActivity extends SathraActivity implements OnClickListener {

	private View mAButton;
	private View mLeftButton;
	private View mRightButton;
	private View mSelectButton;
	private View mPauseView;

	private SceneNode mAnimParent;
	private SceneNode mPlayerNode;

	private boolean mIsPaused = false;

	@Override
	protected Parameters getParameters() {
		Parameters params = new Parameters();
		params.fullscreen = true;
		params.layout = R.layout.activity_main;
		params.ambientColor = 0xAA000000;
		params.bgColor = 0x00000000;
		params.showFPS = true;

		return params;
	}
	
	@Override
	protected void onEngineInitiated() {
		try {
			// Load map
			loadScene("map_cave.json");

			// Load player
			mPlayerNode = loadScene("player_green.json");

			// Setup UI
			mAButton = findViewById(R.id.hud_a);
			mSelectButton = findViewById(R.id.hud_select);
			mLeftButton = findViewById(R.id.hud_left);
			mRightButton = findViewById(R.id.hud_right);
			mPauseView = findViewById(R.id.hud_pause);
			mSelectButton.setOnClickListener(this);

			mAnimParent = mPlayerNode.findChildById("anim_parent");
		
			// Setup AI
			AIContext playerContext = mPlayerNode.getAIContext();
			playerContext.setVariable(PlayerTask.VAR_JUMP_BUTTON, mAButton);
			playerContext.setVariable(PlayerTask.VAR_LEFT_BUTTON, mLeftButton);
			playerContext
					.setVariable(PlayerTask.VAR_RIGHT_BUTTON, mRightButton);

			SelectorTask rootTask = new SelectorTask();
			rootTask.addChild(new DieTask());
			rootTask.addChild(new JumpTask());
			rootTask.addChild(new FallTask());
			rootTask.addChild(new WalkTask());
			rootTask.addChild(new IdleTask());
			mPlayerNode.setAI(rootTask);

			
		} catch (Exception e) {
			// For some reason scene couldn't be loaded
			e.printStackTrace();
		}
	}

	@Override
	protected void onUpdate(long time, long timeDelta) {
		
		if (!mIsPaused) {
			if (mLeftButton.isPressed()) {
				// Move player left
				mAnimParent.setScale(-1, 1);
				mPlayerNode.getBody().setXVelocity(-30);
			} else if (mRightButton.isPressed()) {
				// Move player right
				mAnimParent.setScale(1, 1);
				mPlayerNode.getBody().setXVelocity(30);
			} else {
				// Stop player
				mPlayerNode.getBody().setXVelocity(0);
			}
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.hud_select:
			mIsPaused = !mIsPaused;

			if (mIsPaused) {
				mPauseView.setVisibility(View.VISIBLE);
				setTimeScale(0);
			} else {
				mPauseView.setVisibility(View.INVISIBLE);
				setTimeScale(1);
			}
			break;
		}

	}
}
