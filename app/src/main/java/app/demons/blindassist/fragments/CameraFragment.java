package app.demons.blindassist.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Locale;

import app.demons.blindassist.R;
import app.demons.blindassist.utility.ServerUtility;

public class CameraFragment extends Fragment implements TextToSpeech.OnInitListener {

	public static TextToSpeech textToSpeech;
	public static String text;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.camera_layout, container, false);
		Button clickToOpenCamera = (Button) view.findViewById(R.id.button);
		clickToOpenCamera.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};

				AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
				builder.setTitle("Add Photo");
				builder.setItems(options, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int item) {
						if (options[item].equals("Take Photo")) {
							Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
							File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "BlindAssist.jpg");
							intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
							startActivityForResult(intent, 1);
						} else if (options[item].equals("Choose from Gallery")) {
							Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
							startActivityForResult(intent, 2);
						} else if (options[item].equals("Cancel")) {
							dialog.dismiss();
						}
					}
				});
				builder.show();
			}
		});

		if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
			// TODO: Consider overriding
			// public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
			// to handle the case where the user grants the permission.

			// Permissions strings to access phone's camera and external storage
			String[] permissions = new String[]{
					Manifest.permission.READ_EXTERNAL_STORAGE,
					Manifest.permission.WRITE_EXTERNAL_STORAGE,
					Manifest.permission.CAMERA
			};
			// Request permission in Marshmallow style
			ActivityCompat.requestPermissions(getActivity(), permissions, 1);
		}

		return view;
	}

	public void encodeString(final File filePath) {
		final String[] encodedImage = new String[1];
		new AsyncTask<Void, Void, String>() {
			@Override
			protected String doInBackground(Void... params) {
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inSampleSize = 3;
				Bitmap bitmap = BitmapFactory.decodeFile(filePath.getPath(), options);
				ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
				bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
				byte[] bytes = byteArrayOutputStream.toByteArray();
				encodedImage[0] = Base64.encodeToString(bytes, 0);
				return encodedImage[0];
			}

			@Override
			protected void onPostExecute(String result) {
				// Trigger image upload
				Log.e("Blah", "Encoding done");
				makeRequest(result, "ocr");
			}
		}.execute(null, null, null);
	}

	private void makeRequest(String result, String type) {
		if (type.equals("ocr")) {
			new ServerUtility(getContext(), type).execute(result);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == 1) {
				File file1 = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString());
				for (File temp : file1.listFiles()) {
					if (temp.getName().equals("BlindAssist.jpg")) {
						file1 = temp;
						break;
					}
				}
				try {
					Bitmap bitmap;
					BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();

					bitmap = BitmapFactory.decodeFile(file1.getPath(), bitmapOptions);

					String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
					file1.delete();
					OutputStream outFile;
					File file = new File(path, "BlindAssist.jpg");
					Log.e("ads", file.toString());
					try {
						outFile = new FileOutputStream(file);
						bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outFile);
						outFile.flush();
						outFile.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				encodeString(file1);
			} else if (requestCode == 2) {
				Uri selectedImage = data.getData();
				String[] filePath = {MediaStore.Images.Media.DATA};
				Cursor cursor = getActivity().getContentResolver().query(selectedImage, filePath, null, null, null);
				if (cursor != null) {
					cursor.moveToFirst();
					int columnIndex = cursor.getColumnIndex(filePath[0]);
					String picturePath = cursor.getString(columnIndex);
					cursor.close();
					Bitmap thumbnail = (BitmapFactory.decodeFile(picturePath));
					Log.w("Path from gallery", picturePath + "");
					encodeString(new File(picturePath));
				}
			}
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