package app.demons.blindassist.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import app.demons.blindassist.R;
import app.demons.blindassist.fragments.HomeFragment;
import app.demons.blindassist.fragments.ProfileFragment;

/**
 * @author Adhiraj Singh Chauhan
 */
public class MainActivity extends AppCompatActivity implements View.OnTouchListener {

	private DrawerLayout mDrawerLayout;
	private FragmentManager mFragmentManager;
	private static Toolbar toolbar;
	private static AppBarLayout appBarLayout;
	private MenuItem old;

	public static TextView navName;
	public static TextView navEmail;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		String theme = sharedPreferences.getString("theme", "Dark");
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
		setContentView(R.layout.activity_main);

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
		NavigationView mNavigationView = (NavigationView) findViewById(R.id.navigationView);

		View view = null;
		if (mNavigationView != null) {
			view = mNavigationView.inflateHeaderView(R.layout.navigation_drawer_header);
			navName = (TextView) view.findViewById(R.id.navName);
			navEmail = (TextView) view.findViewById(R.id.navEmail);
		}
		String name = sharedPreferences.getString("name", null);
		String email = sharedPreferences.getString("email", null);
		if (name != null)
			navName.setText(name);
		if (email != null)
			navEmail.setText(email);

		mFragmentManager = getSupportFragmentManager();
		FragmentTransaction mFragmentTransaction = mFragmentManager.beginTransaction();

		toolbar = (Toolbar) findViewById(R.id.toolbar);
		appBarLayout = (AppBarLayout) findViewById(R.id.app_bar);
		if (appBarLayout != null)
			appBarLayout.setElevation(0);
		final float scale = getResources().getDisplayMetrics().density;
		final int elevation = (int) (4 * scale + 0.5f);
		mFragmentTransaction.replace(R.id.containerView, new HomeFragment(), "Home").commit();
		toolbar.setTitle("Home");
		ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.app_name, R.string.app_name);
		mDrawerLayout.setDrawerListener(mDrawerToggle);
		mDrawerToggle.syncState();

		if (mNavigationView != null) {
			mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
				@Override
				public boolean onNavigationItemSelected(MenuItem menuItem) {
					mDrawerLayout.closeDrawers();
					Intent intent;
					FragmentTransaction fragmentTransaction;
					menuItem.setChecked(true);
					if (old != null) {
						old.setChecked(false);
					}

					switch (menuItem.getItemId()) {
						case R.id.nav_item_home:
							fragmentTransaction = mFragmentManager.beginTransaction();
							fragmentTransaction.addToBackStack("fragment");
							fragmentTransaction.replace(R.id.containerView, new HomeFragment(), "Home");
							fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
							toolbar.setTitle("Home");
							appBarLayout.setElevation(0);
							old = menuItem;
							break;
						case R.id.nav_item_profile:
							fragmentTransaction = mFragmentManager.beginTransaction();
							fragmentTransaction.addToBackStack("fragment");
							fragmentTransaction.replace(R.id.containerView, new ProfileFragment(), "Profile");
							fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
							toolbar.setTitle("Profile");
							appBarLayout.setElevation(elevation);
							old = menuItem;
							break;
						case R.id.nav_item_settings:
							intent = new Intent(MainActivity.this, SettingsActivity.class);
							startActivity(intent);
							break;
						case R.id.nav_item_about:
							intent = new Intent(MainActivity.this, DeveloperActivity.class);
							ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(MainActivity.this);
							ActivityCompat.startActivity(MainActivity.this, intent, activityOptionsCompat.toBundle());
							break;
					}
					return true;
				}
			});
		}
	}

	@Override
	public void onBackPressed() {
		if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
			mDrawerLayout.closeDrawer(GravityCompat.START);
			return;
		}
		if (getSupportFragmentManager().findFragmentByTag("fragment") == null) {
			super.onBackPressed();
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		vibrator.vibrate(250);
		return true;
	}
}
