package eu.sathra.ai;

/**
 * Iterates through children until one of them returns
 * <code>TaskResult.FALSE</code> or it reachs last subtask. If a child
 * returns <code>TaskResult.RUNNING</code> it will be picked up next frame.
 * Returns <code>TaskResult.TRUE</code> if all children succeeded. 
 * 
 * @author Milosz Moczkowski
 * 
 */
public class SequenceTask extends Task {

	private int mCurrentTask;

	@Override
	protected void initialize(AIContext context) {

	}

	@Override
	public TaskResult execute(AIContext context, long time, long delta) {

		for(int c = (mCurrentTask != -1? mCurrentTask : 0); c< mChildren.size(); ++c) {
			TaskResult result = mChildren.get(c).execute(context, time, delta);
			
			switch(result) {
				case RUNNING:
				mCurrentTask = c;
				case FALSE:
					return result;
				case TRUE: break;
			}
		}
		
		// all children executed
		return TaskResult.TRUE;
	}

}
