package kz.edu.nu.sst.quickshot;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.ViewFlipper;

public class StartActivity extends Activity {
	ViewFlipper viewFlipper;
	private float initialX;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start);
		((ImageView) findViewById(R.id.start1))
				.setImageResource(R.drawable.pic1);
		((ImageView) findViewById(R.id.start2))
				.setImageResource(R.drawable.pic2);
		((ImageView) findViewById(R.id.start3))
				.setImageResource(R.drawable.pic3);

		viewFlipper = (ViewFlipper) findViewById(R.id.flipper);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.start, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onTouchEvent(MotionEvent touchevent) {
		switch (touchevent.getAction()) {
		case MotionEvent.ACTION_DOWN:
			initialX = touchevent.getX();
			break;
		case MotionEvent.ACTION_UP:
			float finalX = touchevent.getX();
			if (initialX > finalX) {
				if (viewFlipper.getDisplayedChild() == 2)
					startActivity(new Intent(this, MainActivity.class));

				viewFlipper.setInAnimation(this, R.anim.flipin_right);
				viewFlipper.setOutAnimation(this, R.anim.flipout_left);

				viewFlipper.showNext();
			} else {
				if (viewFlipper.getDisplayedChild() == 0)
					break;

				viewFlipper.setInAnimation(this, R.anim.flipin_left);
				viewFlipper.setOutAnimation(this, R.anim.flipout_right);

				viewFlipper.showPrevious();
			}
			break;
		}
		return false;
	}
}
