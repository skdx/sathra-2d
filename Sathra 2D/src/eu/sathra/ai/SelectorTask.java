package eu.sathra.ai;

import eu.sathra.util.Log;



public class SelectorTask extends Task {

	private int mCurrentNode = 0;

	@Override
	public TaskResult execute(AIContext context, long time, long delta) {
		TaskResult myResult = TaskResult.FALSE;

		for (; mCurrentNode < mChildren.size(); ++mCurrentNode) {
			Task myTask = mChildren.get(mCurrentNode);
			myResult = myTask.execute(context, time, delta);

			switch(myResult) {
				case TRUE: 
					mCurrentNode = 0;
				case RUNNING:	
					return myResult;
				case FALSE: 
					// noop
					break;
			}
		}
		
		mCurrentNode = 0;
		return myResult;
	}
	
	public void addChild(Task child) {
		super.addChild(child);
		mCurrentNode = 0;
	}
	
	@Override
	public void addChildren(Task[] children) {
		super.addChildren(children);
		mCurrentNode = 0;
	}

	@Override
	protected void initialize(AIContext context) {
		// TODO Auto-generated method stub
		
	}

}