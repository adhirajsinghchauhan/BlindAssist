package app.demons.blindassist.fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.LinkedList;
import java.util.List;

import app.demons.blindassist.R;

/**
 * @author Adhiraj Singh Chauhan
 */
public class MapFragment extends Fragment implements GoogleMap.OnMyLocationChangeListener, View.OnTouchListener {

	private MapView mapView;
	private GoogleMap googleMap;
	private LocationManager locationManager;
	private String provider;
	private final double PI = 3.141592653589793;
	// Mean radius of earth in km
	private final double RADIUS = 6371;
	private List<LatLng> points = new LinkedList<>();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_map, container, false);
		mapView = (MapView) view.findViewById(R.id.mapView);
		mapView.onCreate(savedInstanceState);

		// Needed to update the map immediately, since this is part of the default tab
		mapView.onResume();

		try {
			MapsInitializer.initialize(getActivity().getApplicationContext());
		} catch (Exception e) {
			e.printStackTrace();
		}

		googleMap = mapView.getMap();


		/**
		 * Manipulates the map once permissions are granted
		 * This callback is triggered when the map is ready to be used.
		 * This is where I'm adding markers or lines, adding listeners or moving the camera. In this case,
		 * I just add a marker at the current and the initial location.
		 * <p/>
		 * Note: If Google Play services is not installed on the device, the user will be prompted to install
		 * it inside the SupportMapFragment. This method will only be triggered once the user has
		 * installed Google Play services and returned to the app.
		 */
		if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			// TODO: Consider overriding
			// public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
			// to handle the case where the user grants the permission.

			// Permissions strings to access user's location
			String[] permissions = new String[]{
					Manifest.permission.ACCESS_COARSE_LOCATION,
					Manifest.permission.ACCESS_FINE_LOCATION
			};
			// Request permission in Marshmallow style
			ActivityCompat.requestPermissions(getActivity(), permissions, 1);
		} else {
			googleMap.setMyLocationEnabled(true);
			googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
			googleMap.setOnMyLocationChangeListener(this);
			googleMap.getMyLocation();
		}
		return view;
	}

	public double convertToRadians(double x) {
		return x * PI / 180;
	}

	/**
	 * Calculates the spherical distance with respect to two nodes on a path.
	 * Since our planet is a spheroid, I've taken angles into consideration, along with the radius of the Earth
	 *
	 * @param first  (Usually) the previous node
	 * @param second (Usually) the next node
	 *
	 * @return Calculated distance, after compensating for spherical shit
	 */
	public double calculateDistance(LatLng first, LatLng second) {
		double longitude = convertToRadians(second.longitude - first.longitude);
		double latitude = convertToRadians(second.latitude - first.longitude);

		double someAngleThing = (Math.sin(latitude / 2) * Math.sin(latitude / 2)) + Math.cos(convertToRadians(first.latitude)) * Math.cos(convertToRadians(second.latitude)) * (Math.sin(longitude / 2) * Math.sin(longitude / 2));
		double angle = 2 * Math.atan2(Math.sqrt(someAngleThing), Math.sqrt(1 - someAngleThing));

		// Returns absolute value, because I don't care about negative distances
		return Math.abs(angle * RADIUS);
	}

	/**
	 * Helper function to check if the user is approximately on the correct path or not.
	 * Since I'm taking 3 nodes - previous, current and next, I've taken the minimum threshold distance to be twice that of the
	 * pseudo-shortest path (distance between previous and next node).
	 *
	 * @param previous The node just before the current node
	 * @param current  The node corresponding to the user's current location
	 * @param next     The node that must be visited after the current node
	 *
	 * @return Boolean value, set if calculated path is within the threshold
	 */
	public boolean isPathCorrect(LatLng previous, LatLng current, LatLng next) {
		double distanceFromPrevious = calculateDistance(previous, current);
		double distanceToNext = calculateDistance(current, next);
		double kindaShortestDistance = calculateDistance(previous, next);

		// Threshold is twice the shortest distance
		return distanceFromPrevious + distanceToNext <= 2 * kindaShortestDistance;
	}

	/**
	 * Listener for location updates: Adding/Deleting markers and paths in this
	 *
	 * @param location Current location
	 */
	@Override
	public void onMyLocationChange(Location location) {
		LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
		// Add visited point to list
		points.add(currentLocation);

		// Remove all previous markers
		googleMap.clear();
		// Redraw first & last marker
		googleMap.addMarker(new MarkerOptions().position(currentLocation).title("You are here"));
		googleMap.addMarker(new MarkerOptions().position(points.get(0)).title("You started here"));

		googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 16));

		Polyline polyline = googleMap.addPolyline(new PolylineOptions().add(currentLocation));
		polyline.setWidth(5);
		polyline.setColor(Color.GREEN);
		// Redraw polyline with all visited points
		polyline.setPoints(points);

//		// TODO: Take real location data
//		if (isPathCorrect(currentLocation, currentLocation, currentLocation)) {
//			Vibrator vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
//			vibrator.vibrate(250);
//		}
	}

	@Override
	public void onResume() {
		super.onResume();
		mapView.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
		mapView.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mapView.onDestroy();
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		mapView.onLowMemory();
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		Vibrator vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
		vibrator.vibrate(250);
		return true;
	}
}
