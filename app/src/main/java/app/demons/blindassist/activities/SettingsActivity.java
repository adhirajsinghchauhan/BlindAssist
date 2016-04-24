package app.demons.blindassist.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.view.MotionEvent;
import android.view.View;

import app.demons.blindassist.R;

/**
 * @author Adhiraj Singh Chauhan
 */

public class SettingsActivity extends PreferenceActivity implements Preference.OnPreferenceChangeListener, View.OnTouchListener {
	public void onCreate(Bundle savedInstanceState) {
		final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
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
		addPreferencesFromResource(R.xml.settings_general);
		bindPreferenceSummaryToValue(findPreference(getString(R.string.theme_key)));

		Preference forgotPassword = findPreference("forgotPassword");
		Preference logout = findPreference("logout");
		final String username = sharedPreferences.getString("username", null);
		if (username == null) {
			forgotPassword.setEnabled(false);
			logout.setEnabled(false);
		} else {
			forgotPassword.setEnabled(true);
			logout.setEnabled(true);
			forgotPassword.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					new AlertDialog.Builder(SettingsActivity.this)
							.setTitle("Confirm")
							.setMessage("An email with the password reset link will be sent to your Email ID")
							.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
//									new ServerUtility(SettingsActivity.this, "forgotPassword").execute(username);
								}
							})
							.setNegativeButton(android.R.string.cancel, null)
							.create().show();
					return true;
				}
			});
			logout.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					SharedPreferences.Editor editor = sharedPreferences.edit();
					editor.remove("name")
							.remove("email")
							.remove("username")
							.remove("isLoggedIn").apply();
					new AlertDialog.Builder(SettingsActivity.this)
							.setTitle("You have been logged out")
							.setMessage("Do you want to log in again?")
							.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									MainActivity.navName.setText("");
									MainActivity.navEmail.setText("");
									Intent intent = new Intent(SettingsActivity.this, LogInActivity.class);
									startActivity(intent);
								}
							})
							.setNegativeButton(android.R.string.no, null)
							.create().show();
					return true;
				}
			});
		}
	}

	private void bindPreferenceSummaryToValue(Preference preference) {
		preference.setOnPreferenceChangeListener(this);
		onPreferenceChange(preference, PreferenceManager.getDefaultSharedPreferences(preference.getContext()).getString(preference.getKey(), ""));
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object value) {
		String stringValue = value.toString();

		if (preference instanceof ListPreference) {
			ListPreference listPreference = (ListPreference) preference;
			int prefIndex = listPreference.findIndexOfValue(stringValue);
			if (prefIndex >= 0) {
				preference.setSummary(listPreference.getEntries()[prefIndex]);
			}
		} else
			preference.setSummary(stringValue);

		return true;
	}

	@Override
	public void onBackPressed() {
		final Intent intent = new Intent(this, MainActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		vibrator.vibrate(250);
		return true;
	}
}
