package app.demons.blindassist.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ViewFlipper;

import app.demons.blindassist.R;
import app.demons.blindassist.utility.ServerUtility;

/**
 * @author Adhiraj Singh Chauhan
 */

public class LogInActivity extends AppCompatActivity implements View.OnTouchListener {
	private EditText nameField;
	private EditText emailField;
	private EditText usernameField_login;
	private EditText passwordField_login;
	private EditText usernameField_signup;
	private EditText passwordField_signup;
	private TextView fabLabel;

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

		setContentView(R.layout.activity_login);
		final ViewFlipper viewFlipper = (ViewFlipper) findViewById(R.id.viewFlipper);
		usernameField_login = (EditText) findViewById(R.id.username_login);
		passwordField_login = (EditText) findViewById(R.id.password_login);
		usernameField_signup = (EditText) findViewById(R.id.username_signup);
		passwordField_signup = (EditText) findViewById(R.id.password_signup);
		fabLabel = (TextView) findViewById(R.id.fabLabel);
		nameField = (EditText) findViewById(R.id.name);
		emailField = (EditText) findViewById(R.id.email);
		CheckBox showPassword = (CheckBox) findViewById(R.id.checkbox_show_password_signup);
		final Button skip = (Button) findViewById(R.id.skip);
		if (skip != null) {
			skip.setVisibility(View.VISIBLE);
		}

		if (skip != null)
			if (colouredNavigationBar) {
				if (theme.equals("Light"))
					skip.setBackgroundResource(R.color.iecse_blue);
				else if (theme.equals("Dark"))
					skip.setBackgroundResource(R.color.darker_iecse_blue);
			} else
				skip.setBackgroundResource(R.color.black);

		if (showPassword != null) {
			showPassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if (isChecked)
						passwordField_signup.setTransformationMethod(null);
					else
						passwordField_signup.setTransformationMethod(PasswordTransformationMethod.getInstance());
				}
			});
		}
		FloatingActionButton floatingActionButton = (FloatingActionButton) findViewById(R.id.fab);
		if (floatingActionButton != null && viewFlipper != null && skip != null) {
			floatingActionButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (viewFlipper.getDisplayedChild() == 0) {
						viewFlipper.setInAnimation(v.getContext(), R.anim.slide_in_from_left);
						viewFlipper.setOutAnimation(v.getContext(), R.anim.slide_out_to_right);
						viewFlipper.showPrevious();
						skip.setVisibility(View.VISIBLE);
						Animation animation = AnimationUtils.loadAnimation(v.getContext(), R.anim.slide_in_from_left);
						skip.startAnimation(animation);
						fabLabel.setText("Sign Up instead");
						Animation fadeIn = AnimationUtils.loadAnimation(v.getContext(), R.anim.fade_in);
						fabLabel.startAnimation(fadeIn);
						CheckBox showPassword = (CheckBox) findViewById(R.id.checkbox_show_password_login);
						if (showPassword != null) {
							showPassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

								@Override
								public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
									if (isChecked)
										passwordField_login.setTransformationMethod(null);
									else
										passwordField_login.setTransformationMethod(PasswordTransformationMethod.getInstance());
								}
							});
						}
					} else if (viewFlipper.getDisplayedChild() == 1) {
						viewFlipper.setInAnimation(v.getContext(), R.anim.slide_in_from_right);
						viewFlipper.setOutAnimation(v.getContext(), R.anim.slide_out_to_left);
						viewFlipper.showNext();
						Animation animation = AnimationUtils.loadAnimation(v.getContext(), R.anim.slide_out_to_left);
						skip.startAnimation(animation);
						skip.setVisibility(View.GONE);
						fabLabel.setText("Login instead");
						Animation fadeIn = AnimationUtils.loadAnimation(v.getContext(), R.anim.fade_in);
						fabLabel.startAnimation(fadeIn);
						CheckBox showPassword = (CheckBox) findViewById(R.id.checkbox_show_password_signup);
						if (showPassword != null) {
							showPassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

								@Override
								public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
									if (isChecked)
										passwordField_signup.setTransformationMethod(null);
									else
										passwordField_signup.setTransformationMethod(PasswordTransformationMethod.getInstance());
								}
							});
						}
					}
				}
			});
		}

		Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
		setSupportActionBar(toolbar);
		CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
		if (appBarLayout != null) {
			appBarLayout.setTitle("Hi, let's get started");
		}
		final float scale = getResources().getDisplayMetrics().density;
		int elevation = (int) (4 * scale + 0.5f);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && appBarLayout != null)
			appBarLayout.setElevation(elevation);
	}

	public void signUp(View view) {
		String name = nameField.getText().toString();
		String email = emailField.getText().toString();
		String username = usernameField_signup.getText().toString();
		String password = passwordField_signup.getText().toString();
		String message = "";
		if (name.equals(""))
			message += "Name\n";
		if (email.equals(""))
			message += "Email\n";
		if (username.equals(""))
			message += "Username\n";
		if (password.equals(""))
			message += "Password\n";
		if (!(message.equals(""))) {
			new AlertDialog.Builder(this)
					.setTitle("The following fields can't be empty")
					.setMessage(message)
					.setPositiveButton(android.R.string.yes, null)
					.create().show();
			return;
		}
		new ServerUtility(this, "signup").execute(name, email, username, password);
		ViewFlipper viewFlipper = (ViewFlipper) findViewById(R.id.viewFlipper);
		Button skip = (Button) findViewById(R.id.skip);

		if (viewFlipper != null && skip != null) {
			viewFlipper.setInAnimation(view.getContext(), R.anim.slide_in_from_left);
			viewFlipper.setOutAnimation(view.getContext(), R.anim.slide_out_to_right);
			viewFlipper.showPrevious();
			skip.setVisibility(View.VISIBLE);
			Animation animation = AnimationUtils.loadAnimation(view.getContext(), R.anim.slide_in_from_left);
			skip.startAnimation(animation);
			fabLabel.setText("Sign Up instead");
			Animation fadeIn = AnimationUtils.loadAnimation(view.getContext(), R.anim.fade_in);
			fabLabel.startAnimation(fadeIn);
			CheckBox showPassword = (CheckBox) findViewById(R.id.checkbox_show_password_login);
			if (showPassword != null) {
				showPassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						if (isChecked)
							passwordField_login.setTransformationMethod(null);
						else
							passwordField_login.setTransformationMethod(PasswordTransformationMethod.getInstance());
					}
				});
			}
		}

	}

	public void logIn(View view) {
		String username = usernameField_login.getText().toString();
		String password = passwordField_login.getText().toString();
		String message = "";
		if (username.equals(""))
			message += "Username\n";
		if (password.equals(""))
			message += "Password\n";
		if (!(message.equals(""))) {
			new AlertDialog.Builder(this)
					.setTitle("The following fields can't be empty")
					.setMessage(message)
					.setPositiveButton(android.R.string.yes, null)
					.create().show();
			return;
		}
		new ServerUtility(this, "login").execute(username, password);
	}

	public void skip(View view) {
		Intent intent = new Intent(LogInActivity.this, MainActivity.class);
		startActivity(intent);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		vibrator.vibrate(250);
		return true;
	}
}