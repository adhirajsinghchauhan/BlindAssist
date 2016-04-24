package app.demons.blindassist.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import app.demons.blindassist.R;

/**
 * @author Adhiraj Singh Chauhan
 */

public class SplashActivity extends AppCompatActivity implements View.OnTouchListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		int splashScreenTimeout = 2000;
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

		final String username = sharedPreferences.getString("username", null);
		final boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);

		super.onCreate(savedInstanceState);

//		For fullscreen
		int visibility = 0;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			visibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_FULLSCREEN;
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			visibility |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
		}
		visibility |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
		getWindow().getDecorView().setSystemUiVisibility(visibility);

		setContentView(R.layout.activity_splash);
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				Intent intent;
				if (username != null && isLoggedIn) {
					intent = new Intent(SplashActivity.this, MainActivity.class);
				} else {
					intent = new Intent(SplashActivity.this, LogInActivity.class);
				}
				startActivity(intent);
				finish();
			}
		}, splashScreenTimeout);
		ImageView logo = (ImageView) findViewById(R.id.logo);
		final AnimationDrawable animationDrawable;
		if (logo != null) {
			animationDrawable = (AnimationDrawable) logo.getDrawable();
			animationDrawable.setCallback(logo);
			animationDrawable.setVisible(true, true);
			logo.post(new Runnable() {
				@Override
				public void run() {
					animationDrawable.start();
				}
			});
		}

	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		vibrator.vibrate(250);
		return true;
	}
}