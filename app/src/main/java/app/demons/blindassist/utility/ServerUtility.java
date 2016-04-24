package app.demons.blindassist.utility;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Locale;

import app.demons.blindassist.activities.MainActivity;

/**
 * @author Adhiraj Singh Chauhan
 */
public class ServerUtility extends AsyncTask<String, Void, String> implements TextToSpeech.OnInitListener {
	private final Context context;
	private ProgressDialog progressDialog;
	private final String type;
	private TextToSpeech textToSpeech;
	private String text;
	private String username;
	private String name;
	private String email;

	public ServerUtility(Context context, String type) {
		this.context = context;
		this.type = type;
	}

	@Override
	protected String doInBackground(String... arg0) {
		try {
			String data = "";
			String link = "http://thefrontier.in/demons/";
			if (type.equals("login")) {
				username = arg0[0];
				String password = arg0[1];
				link += "login.php";
				data = URLEncoder.encode("username", "UTF-8") + "=" + URLEncoder.encode(username, "UTF-8");
				data += "&" + URLEncoder.encode("password", "UTF-8") + "=" + URLEncoder.encode(password, "UTF-8");
				Log.i(getClass().getSimpleName(), link);
			} else if (type.equals("signup")) {
				name = arg0[0];
				email = arg0[1];
				username = arg0[2];
				String password = arg0[3];
				link += "register.php";
				data = URLEncoder.encode("name", "UTF-8") + "=" + URLEncoder.encode(name, "UTF-8");
				data += "&" + URLEncoder.encode("email", "UTF-8") + "=" + URLEncoder.encode(email, "UTF-8");
				data += "&" + URLEncoder.encode("username", "UTF-8") + "=" + URLEncoder.encode(username, "UTF-8");
				data += "&" + URLEncoder.encode("password", "UTF-8") + "=" + URLEncoder.encode(password, "UTF-8");
				Log.i(getClass().getSimpleName(), link);
			} else if (type.equals("ocr")) {
				String encodedImage = arg0[0];
				link += "image.php";
				data = URLEncoder.encode("image", "UTF-8") + "=" + URLEncoder.encode(encodedImage, "UTF-8");
				Log.i(getClass().getSimpleName(), link);
			}

			URL url = new URL(link);
			URLConnection urlConnection = url.openConnection();

			urlConnection.setDoOutput(true);
			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(urlConnection.getOutputStream());

			outputStreamWriter.write(data);
			outputStreamWriter.flush();

			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

			StringBuilder stringBuilder = new StringBuilder();
			String line;

			while ((line = bufferedReader.readLine()) != null) {
				stringBuilder.append(line);
				break;
			}
			return stringBuilder.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	protected void onPreExecute() {
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivityManager.getActiveNetworkInfo() == null || connectivityManager.getActiveNetworkInfo().getState() ==
				NetworkInfo.State.DISCONNECTED) {
			new AlertDialog.Builder(context)
					.setTitle("No network")
					.setMessage("Connect to a network and try again")
					.setPositiveButton(android.R.string.yes, null)
					.create().show();
			return;
		}
		if (type.equals("ocr") || type.equals("signup") || type.equals("login")) {
			String title = type.equals("ocr") ? "Doing some magic" : type.equals("signup") ? "Signing up" : type.equals("login") ? "Logging in" : "";
			progressDialog = ProgressDialog.show(context, title, "Please wait", true);
		}
		super.onPreExecute();
	}

	/**
	 * @param result Response received from the server.
	 *               {status:?}, where ? is one of the following:
	 *               true: no error
	 *               false: error
	 */
	@Override
	protected void onPostExecute(String result) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivityManager.getActiveNetworkInfo() == null || connectivityManager.getActiveNetworkInfo().getState() == NetworkInfo.State.DISCONNECTED) {
			return;
		}

		if (progressDialog != null)
			progressDialog.dismiss();

		boolean status = false;
		JSONObject jsonObject = null;
		Log.e(getClass().getSimpleName(), result);
		try {
			jsonObject = new JSONObject(result);
			status = jsonObject.optBoolean("status");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		if (jsonObject == null)
			return;

		if (type.equals("login")) {
			if (!status) {
				new AlertDialog.Builder(context)
						.setTitle("Invalid credentials")
						.setMessage("Please try again")
						.setPositiveButton(android.R.string.yes, null)
						.create().show();
			} else {
				SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
				SharedPreferences.Editor editor = sharedPreferences.edit();
				try {
//					JSONObject jsonObject1 = jsonObject.getJSONObject("data");
//					String name = jsonObject1.optString("full_name");
//					String email = jsonObject1.optString("email");
//					String mobile = jsonObject1.optString("mobile");
//
//					editor.putString("name", name)
//							.putString("email", email)
//							.putString("mobile", mobile).apply();
//				} catch (JSONException e) {
//					e.printStackTrace();
				} finally {
					TextView navName = MainActivity.navName;
					TextView navEmail = MainActivity.navEmail;
					if (navName != null)
						navName.setText("Adhiraj Singh Chauhan");
					if (navEmail != null)
						navEmail.setText("adhirajsinghchauhan@gmail.com");
				}
				editor.putString("username", username);
				editor.putBoolean("isLoggedIn", true).apply();
				new AlertDialog.Builder(context)
						.setTitle("Login successful")
						.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								Intent intent = new Intent(context, MainActivity.class);
								intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								context.getApplicationContext().startActivity(intent);
							}
						})
						.create().show();
			}
		} else if (type.equals("signup")) {
			if (!status) {
				new AlertDialog.Builder(context)
						.setTitle("Invalid email")
						.setMessage("Email must be of the format: yourID@yourProvider.com")
						.setPositiveButton(android.R.string.yes, null)
						.create().show();
			} else {
				SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
				SharedPreferences.Editor editor = sharedPreferences.edit();
				editor.putString("username", username);
				editor.putString("name", name);
				editor.putString("email", email);
				editor.putBoolean("isLoggedIn", true).apply();
				new AlertDialog.Builder(context)
						.setTitle("Registration successful")
						.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								Intent intent = new Intent(context, MainActivity.class);
								intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								context.getApplicationContext().startActivity(intent);
							}
						})
						.create().show();
			}
		} else if (type.equals("ocr")) {
			if (!status) {
				new AlertDialog.Builder(context)
						.setTitle("Text to speech failed")
						.setMessage("Please try again, select a clear image")
						.setPositiveButton(android.R.string.yes, null)
						.create().show();
			} else {
				try {
					text = jsonObject.getString("ocr");
					textToSpeech = new TextToSpeech(context, this);
					textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		} else {
			Log.e(getClass().getSimpleName(), "No type specified");
		}
	}

	@Override
	public void onInit(int status) {
		if (status == TextToSpeech.SUCCESS) {
			int result = textToSpeech.setLanguage(Locale.getDefault());
			if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
				Log.e("TTS", "This Language is not supported");
			} else {
				if (text != null) {
					textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
				}
			}
		} else {
			Log.e("TTS", "Initialization Failed!");
		}
	}
}
