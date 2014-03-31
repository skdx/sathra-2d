package eu.sathra.view;

import eu.sathra.util.Log;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

public class DPadView extends ImageView {

	private float mX;
	private float mY;
	
	public DPadView(Context context) {
		super(context);


	}

	public DPadView(Context context, AttributeSet attrs) {
		super(context, attrs);

	}

	public DPadView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		Log.info(event.getX() + " " + event.getY());
		//Math.toDegrees(Math.atan2(arg0, arg1))
		;
		//event.getY();
		return super.onTouchEvent(event);
	}
}
