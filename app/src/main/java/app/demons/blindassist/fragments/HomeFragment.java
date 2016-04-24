package app.demons.blindassist.fragments;


import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import app.demons.blindassist.R;

/**
 * @author Adhiraj Singh Chauhan
 */

public class HomeFragment extends Fragment implements View.OnTouchListener {

	private static TabLayout tabLayout;
	private static ViewPager viewPager;
	private static final int int_items = 2;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.tab_layout, container, false);
		tabLayout = (TabLayout) view.findViewById(R.id.tabs);
		viewPager = (ViewPager) view.findViewById(R.id.viewpager);

		viewPager.setAdapter(new MyAdapter(getChildFragmentManager()));

		// setUpWithViewPager doesn't work without using a Runnable interface. Support library bug, maybe?
		tabLayout.post(new Runnable() {
			@Override
			public void run() {
				tabLayout.setupWithViewPager(viewPager);
			}
		});
		final float scale = getActivity().getResources().getDisplayMetrics().density;
		int elevation = (int) (4 * scale + 0.5f);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			tabLayout.setElevation(elevation);
		}
		return view;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		Vibrator vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
		vibrator.vibrate(250);
		return true;
	}

	class MyAdapter extends FragmentPagerAdapter {
		public MyAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			switch (position) {
				case 0:
					return new MapFragment();
				case 1:
					return new Camera2Fragment();
			}
			return null;
		}

		@Override
		public int getCount() {
			return int_items;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			switch (position) {
				case 0:
					return "Map";
				case 1:
					return "Camera";
			}
			return null;
		}
	}
}
