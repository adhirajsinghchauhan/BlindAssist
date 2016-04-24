package app.demons.blindassist.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaActionSound;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import app.demons.blindassist.R;
import app.demons.blindassist.utility.ServerUtility;

public class Camera2Fragment extends Fragment implements View.OnTouchListener {
	static final String CAPTURE_FILENAME_PREFIX = "BlindAssist";
	static final String TAG = Camera2Fragment.class.getSimpleName();
	HandlerThread mBackgroundThread;
	Handler mBackgroundHandler;
	Handler mForegroundHandler;
	SurfaceView mSurfaceView;
	ImageReader mCaptureBuffer;
	CameraManager mCameraManager;
	CameraDevice mCamera = null;
	CameraCaptureSession mCaptureSession;

	Button shutter, gallery;
	public static String text;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_camera, container, false);
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


		mBackgroundThread = new HandlerThread("background");
		mBackgroundThread.start();
		mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
		mForegroundHandler = new Handler(getActivity().getMainLooper());
		mCameraManager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);
		// Inflate the SurfaceView, set it as the main layout, and attach a listener
		mSurfaceView = (SurfaceView) view.findViewById(R.id.surfaceView);
		mSurfaceView.getHolder().addCallback(mSurfaceHolderCallback);
		mSurfaceView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onClickOnSurfaceView(v);
				MediaActionSound sound = new MediaActionSound();
				sound.play(MediaActionSound.SHUTTER_CLICK);
			}
		});

		gallery = (Button) view.findViewById(R.id.gallery);
		gallery.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				startActivityForResult(intent, 2);
			}
		});

		shutter = (Button) view.findViewById(R.id.shutter);
		shutter.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onClickOnSurfaceView(v);
				MediaActionSound sound = new MediaActionSound();
				sound.play(MediaActionSound.SHUTTER_CLICK);
				shutter.setRotationY(-180);
			}
		});
		return view;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == 2) {
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

	/**
	 * Given choices of sizes supported by a camera, chooses the smallest one whose
	 * width and height are at least as large as the respective requested values.
	 *
	 * @param choices The list of sizes that the camera supports for the intended output class
	 * @param width   The minimum desired width
	 * @param height  The minimum desired height
	 *
	 * @return The optimal size, or an arbitrary one if none were big enough
	 */
	static Size chooseBigEnoughSize(Size[] choices, int width, int height) {
		// Collect the supported resolutions that are at least as big as the preview Surface
		List<Size> bigEnough = new ArrayList<>();
		for (Size option : choices) {
			if (option.getWidth() >= width && option.getHeight() >= height) {
				bigEnough.add(option);
			}
		}
		// Pick the smallest of those, assuming we found any
		if (bigEnough.size() > 0) {
			return Collections.min(bigEnough, new CompareSizesByArea());
		} else {
			Log.e(TAG, "Couldn't find any suitable preview size");
			return choices[0];
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		Vibrator vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
		vibrator.vibrate(250);
		return false;
	}

	static class CompareSizesByArea implements Comparator<Size> {
		@Override
		public int compare(Size lhs, Size rhs) {
			// We cast here to ensure the multiplications won't overflow
			return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
					(long) rhs.getWidth() * rhs.getHeight());
		}
	}

	public void onClickOnSurfaceView(View v) {
		if (mCaptureSession != null) {
			try {
				CaptureRequest.Builder requester = mCamera.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
				requester.set(CaptureRequest.STATISTICS_FACE_DETECT_MODE, CameraMetadata.STATISTICS_FACE_DETECT_MODE_FULL);
				requester.addTarget(mCaptureBuffer.getSurface());
				try {
					// This handler can be null because we aren't actually attaching any callback
					mCaptureSession.capture(requester.build(), /*listener*/null, /*handler*/null);
				} catch (CameraAccessException ex) {
					Log.e(TAG, "Failed to file actual capture request", ex);
				}
			} catch (CameraAccessException ex) {
				Log.e(TAG, "Failed to build actual capture request", ex);
			}
		} else {
			Log.e(TAG, "User attempted to perform a capture outside our session");
		}
		// Control flow continues in mImageCaptureListener.onImageAvailable()
	}

	// Callback invoked when the state of SurfaceView changes
	final SurfaceHolder.Callback mSurfaceHolderCallback = new SurfaceHolder.Callback() {
		private String mCameraId;
		private boolean mGotSecondCallback;

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			// This is called every time the surface returns to the foreground
			Log.i(TAG, "Surface created");
			mCameraId = null;
			mGotSecondCallback = false;
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			Log.i(TAG, "Surface destroyed");
			holder.removeCallback(this);
			mCamera.close();
			// We don't stop receiving callbacks forever because onResume() will reattach us
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
			// On the first invocation, width and height were automatically set to the view's size
			if (mCameraId == null) {
				// Find the device's back-facing camera and set the destination buffer sizes
				try {
					for (String cameraId : mCameraManager.getCameraIdList()) {
						CameraCharacteristics cameraCharacteristics = mCameraManager.getCameraCharacteristics(cameraId);
						if (cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK) {
							Log.i(TAG, "Found a back-facing camera");
							StreamConfigurationMap info = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
							// Bigger is better when it comes to saving our image
							Size largestSize = Collections.max(Arrays.asList(info.getOutputSizes(ImageFormat.JPEG)), new CompareSizesByArea());
							// Prepare an ImageReader in case the user wants to capture images
							Log.i(TAG, "Capture size: " + largestSize);
							mCaptureBuffer = ImageReader.newInstance(largestSize.getWidth(), largestSize.getHeight(), ImageFormat.JPEG, /*maxImages*/2);
							mCaptureBuffer.setOnImageAvailableListener(mImageCaptureListener, mBackgroundHandler);
							// Danger, W.R.! Attempting to use too large a preview size could
							// exceed the camera bus' bandwidth limitation, resulting in
							// gorgeous previews but the storage of garbage capture data.
							Log.i(TAG, "SurfaceView size: " + mSurfaceView.getWidth() + 'x' + mSurfaceView.getHeight());
							Size optimalSize = chooseBigEnoughSize(info.getOutputSizes(SurfaceHolder.class), width, height);
							// Set the SurfaceHolder to use the camera's largest supported size
							Log.i(TAG, "Preview size: " + optimalSize);
							SurfaceHolder surfaceHolder = mSurfaceView.getHolder();
							surfaceHolder.setFixedSize(optimalSize.getWidth(), optimalSize.getHeight());
							mCameraId = cameraId;
							return;
							// Control flow continues with this method one more time
							// (since we just changed our own size)
						}
					}
				} catch (CameraAccessException ex) {
					Log.e(TAG, "Unable to list cameras", ex);
				}
				Log.e(TAG, "Didn't find any back-facing cameras");
				// This is the second time the method is being invoked: our size change is complete
			} else if (!mGotSecondCallback) {
				if (mCamera != null) {
					Log.e(TAG, "Aborting camera open because it hadn't been closed");
					return;
				}
				// Open the camera device
				try {
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
						return;
					}
					mCameraManager.openCamera(mCameraId, mCameraStateCallback,
							mBackgroundHandler);
				} catch (CameraAccessException ex) {
					Log.e(TAG, "Failed to configure output surface", ex);
				}
				mGotSecondCallback = true;
				// Control flow continues in mCameraStateCallback.onOpened()
			}
		}
	};

	/**
	 * Callbacks invoked upon state changes in CameraDevice.
	 * These are run on a background thread, mBackgroundThread
	 */
	final CameraDevice.StateCallback mCameraStateCallback =
			new CameraDevice.StateCallback() {
				@Override
				public void onOpened(CameraDevice camera) {
					Log.i(TAG, "Successfully opened camera");
					mCamera = camera;
					try {
						List<Surface> outputs = Arrays.asList(mSurfaceView.getHolder().getSurface(), mCaptureBuffer.getSurface());
						camera.createCaptureSession(outputs, mCaptureSessionListener, mBackgroundHandler);
					} catch (CameraAccessException ex) {
						Log.e(TAG, "Failed to create a capture session", ex);
					}
					// Control flow continues in mCaptureSessionListener.onConfigured()
				}

				@Override
				public void onDisconnected(CameraDevice camera) {
					Log.e(TAG, "Camera was disconnected");
					camera.close();
					mCamera.close();
				}

				@Override
				public void onError(CameraDevice camera, int error) {
					Log.e(TAG, "State error on device '" + camera.getId() + "': code " + error);
				}
			};
	/**
	 * Callbacks invoked upon state changes in our CameraCaptureSession.
	 * These are run on mBackgroundThread
	 */
	final CameraCaptureSession.StateCallback mCaptureSessionListener = new CameraCaptureSession.StateCallback() {
		@Override
		public void onConfigured(CameraCaptureSession session) {
			Log.i(TAG, "Finished configuring camera outputs");
			mCaptureSession = session;
			SurfaceHolder holder = mSurfaceView.getHolder();
			if (holder != null) {
				try {
					// Build a request for preview footage
					CaptureRequest.Builder requestBuilder = mCamera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
					requestBuilder.addTarget(holder.getSurface());
					requestBuilder.set(CaptureRequest.STATISTICS_FACE_DETECT_MODE, CameraMetadata.STATISTICS_FACE_DETECT_MODE_FULL);
					CaptureRequest previewRequest = requestBuilder.build();
					// Start displaying preview images
					try {
						session.setRepeatingRequest(previewRequest, /*listener*/null, /*handler*/null);
					} catch (CameraAccessException ex) {
						Log.e(TAG, "Failed to make repeating preview request", ex);
					}
				} catch (CameraAccessException ex) {
					Log.e(TAG, "Failed to build preview request", ex);
				}
			} else {
				Log.e(TAG, "Holder didn't exist when trying to formulate preview request");
			}
		}

		@Override
		public void onClosed(CameraCaptureSession session) {
			mCaptureSession = null;
		}

		@Override
		public void onConfigureFailed(CameraCaptureSession session) {
			Log.e(TAG, "Configuration error on device '" + mCamera.getId());
		}
	};

	/**
	 * Callback invoked when I receive a JPEG image from the camera.
	 */
	final ImageReader.OnImageAvailableListener mImageCaptureListener =
			new ImageReader.OnImageAvailableListener() {
				@Override
				public void onImageAvailable(ImageReader reader) {
					// Save the image once we get a chance
					mBackgroundHandler.post(new CapturedImageSaver(reader.acquireNextImage()));
					// Control flow continues in CapturedImageSaver#run()
				}
			};

	/**
	 * Deferred processor responsible for saving snapshots to disk.
	 * This is run on, guess what? mBackgroundThread.
	 */
	class CapturedImageSaver implements Runnable {
		private Image mCapture;

		public CapturedImageSaver(Image capture) {
			mCapture = capture;
		}

		@Override
		public void run() {
			try {
				// Choose an unused filename under the Pictures/ directory
				File file = File.createTempFile(CAPTURE_FILENAME_PREFIX, ".jpg", Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES));
				try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
					Log.i(TAG, "Retrieved image is" + (mCapture.getFormat() == ImageFormat.JPEG ? "" : "n't") + " a JPEG");
					ByteBuffer buffer = mCapture.getPlanes()[0].getBuffer();
					Log.i(TAG, "Captured image size: " + mCapture.getWidth() + 'x' + mCapture.getHeight());
					// Write the image out to the chosen file
					byte[] jpeg = new byte[buffer.remaining()];
					buffer.get(jpeg);
					fileOutputStream.write(jpeg);
					encodeString(file);
				} catch (FileNotFoundException ex) {
					Log.e(TAG, "Unable to open output file for writing", ex);
				} catch (IOException ex) {
					Log.e(TAG, "Failed to write the image to the output file", ex);
				} finally {
//					file.delete();
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				mCapture.close();
			}
		}

	}

	public void encodeString(final File filePath) {
		final String[] encodedImage = new String[1];
		Log.e(TAG, filePath.toString());
		new AsyncTask<Void, Void, String>() {
			@Override
			protected String doInBackground(Void... params) {
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inSampleSize = 3;
				Bitmap bitmap = BitmapFactory.decodeFile(filePath.getAbsolutePath(), options);
				ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
				bitmap.compress(Bitmap.CompressFormat.JPEG, 85, byteArrayOutputStream);
				byte[] bytes = byteArrayOutputStream.toByteArray();
				encodedImage[0] = Base64.encodeToString(bytes, 0);
				return encodedImage[0];
			}

			@Override
			protected void onPostExecute(String result) {
				// Trigger image upload
				makeRequest(result, "ocr");
			}
		}.execute(null, null, null);
	}

	private void makeRequest(String result, String type) {
		if (type.equals("ocr")) {
			new ServerUtility(getContext(), type).execute(result);
		}
	}
}