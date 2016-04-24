package app.demons.blindassist.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import app.demons.blindassist.R;
import app.demons.blindassist.fragments.HomeFragment;

/**
 * @author Adhiraj Singh Chauhan
 */
public class MainActivity extends AppCompatActivity {

	private DrawerLayout mDrawerLayout;
	private FragmentManager mFragmentManager;
	private static Toolbar toolbar;
	private static AppBarLayout appBarLayout;
	private MenuItem old;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
		NavigationView mNavigationView = (NavigationView) findViewById(R.id.navigationView);

		View view = null;
		if (mNavigationView != null) {
			view = mNavigationView.inflateHeaderView(R.layout.navigation_drawer_header);
		}
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
							appBarLayout.setElevation(0);
							old = menuItem;
							break;
						case R.id.nav_item_profile:
							appBarLayout.setElevation(0);
							old = menuItem;
							break;
						case R.id.nav_item_notifications:
							appBarLayout.setElevation(elevation);
							old = menuItem;
							break;
						case R.id.nav_item_iecse:
							appBarLayout.setElevation(0);
							old = menuItem;
							break;
						case R.id.nav_item_prometheus:
							appBarLayout.setElevation(elevation);
							old = menuItem;
							break;
						case R.id.nav_item_team:
							appBarLayout.setElevation(0);
							old = menuItem;
							break;
						case R.id.nav_item_feedback:
							appBarLayout.setElevation(elevation);
							old = menuItem;
							break;
						case R.id.nav_item_settings:
							intent = new Intent(MainActivity.this, SettingsActivity.class);
							startActivity(intent);
							break;
						case R.id.nav_item_about:
							break;
					}
					return true;
				}
			});
		}
	}
}
