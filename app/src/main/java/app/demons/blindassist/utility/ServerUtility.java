package app.demons.blindassist.utility;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Locale;

/**
 * @author Adhiraj Singh Chauhan
 */
public class ServerUtility extends AsyncTask<String, Void, String> implements TextToSpeech.OnInitListener {
	private final Context context;
	private ProgressDialog progressDialog;
	private final String type;
	TextToSpeech textToSpeech;
	String text;

	public ServerUtility(Context context, String type) {
		this.context = context;
		this.type = type;
	}

	@Override
	protected String doInBackground(String... arg0) {
		try {
			String data = "";
			String link = "http://thefrontier.in/demons/";

			if (type.equals("ocr")) {
				String encodedImage = arg0[0];
				link += "ocr.php";
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
		if (type.equals("ocr") || type.equals("signup")) {
			String title = type.equals("ocr") ? "Doing some magic" : type.equals("signup") ? "Signing up" : "";
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

		if (type.equals("ocr")) {
			if (!status) {
				new AlertDialog.Builder(context)
						.setTitle("Text to speech failed")
						.setMessage("Please try again, select a clear image")
						.setPositiveButton(android.R.string.yes, null)
						.create().show();
			} else {
				try {
					text = jsonObject.getString("text");
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
				if (text != null)
					Log.e("TTS", text);
				textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
			}
		} else {
			Log.e("TTS", "Initialization Failed!");
		}
	}
}
