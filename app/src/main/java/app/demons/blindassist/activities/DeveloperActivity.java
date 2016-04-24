package app.demons.blindassist.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;

import app.demons.blindassist.R;

/**
 * @author Adhiraj Singh Chauhan
 */

public class DeveloperActivity extends AppCompatActivity implements View.OnTouchListener {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		String theme = sharedPreferences.getString("theme", "Light");
		boolean colouredNavigationBar = sharedPreferences.getBoolean("colouredNavigationBar", false);

		if (colouredNavigationBar) {
			if (theme.equals("Light"))
				setTheme(R.style.AppThemeLightColouredNavigationBar);
			else if (theme.equals("Dark"))
				setTheme(R.style.AppThemeDarkColouredNavigationBar);
		} else {
			if (theme.equals("Light"))
				setTheme(R.style.AppThemeLight);
			else if (theme.equals("Dark"))
				setTheme(R.style.AppThemeDark);
		}

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_developer);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		if (toolbar != null)
			toolbar.setTitle("Developed by");

		setSupportActionBar(toolbar);
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public void onBackPressed() {
		Intent intent = new Intent(this, MainActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		super.onBackPressed();
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		vibrator.vibrate(250);
		return true;
	}
}
