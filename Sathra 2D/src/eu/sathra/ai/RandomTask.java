package eu.sathra.ai;

import java.util.Random;

/**
 * Runs randomly selected child task and returns it's status. If a child returns
 * <code>TaskResult.RUNNING</code>, it will be selected next frame.
 * 
 * @author Milosz Moczkowski
 * 
 */
public class RandomTask extends Task {

	private Task mCurrentTask;
	private Random mRandom = new Random();

	@Override
	protected void initialize(AIContext context) {
		// TODO Auto-generated method stub

	}

	@Override
	public TaskResult execute(AIContext context, long time, long delta) {
		Task myTask;

		if (mCurrentTask != null)
			myTask = mCurrentTask;
		else {
			int max = mChildren.size();
			mRandom.nextInt(max);
			myTask = mChildren.get(max);
		}

		TaskResult result = myTask.execute(context, time, delta);

		if (result == TaskResult.RUNNING)
			mCurrentTask = myTask;
		else 
			mCurrentTask = null;

		return result;
	}

}
