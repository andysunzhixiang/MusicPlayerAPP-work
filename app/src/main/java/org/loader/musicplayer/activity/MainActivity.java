package org.loader.musicplayer.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;

import org.loader.musicplayer.R;
import org.loader.musicplayer.fragment.DetailFragment;
import org.loader.musicplayer.fragment.LocalMusicFragment;
import org.loader.musicplayer.service.PlayService;
import org.loader.musicplayer.utils.L;
import org.loader.musicplayer.utils.MusicUtils;


public class MainActivity extends BaseActivity implements LocalMusicFragment.Callback {

	private static final String TAG = MainActivity.class.getSimpleName();

	private boolean mTwoPane;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		//// The detail container view will be present only in the large-screen land layouts
		if(findViewById(R.id.music_detail_container) != null){
			mTwoPane = true;

			if(savedInstanceState == null){
				getSupportFragmentManager().beginTransaction().replace(R.id.music_detail_container,
						new DetailFragment(),"DetailFragment_Tag").commit();
			}else{
				mTwoPane = false;
			}
		}
		registerReceiver();

	}
	/**
	 * Register broadcast Receiver
	 * Update Music list when delete song
	 */
	private void registerReceiver() {
		IntentFilter filter = new IntentFilter( Intent.ACTION_MEDIA_SCANNER_STARTED);
		filter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
		filter.addDataScheme("file");
		registerReceiver(mScanSDCardReceiver, filter);
	}

	/**
	 * Get play music service
	 * @return
	 */
	public PlayService getPlayService() {
		return mPlayService;
	}

	@Override
	public void onPublish(int progress) {

	}

	@Override
	public void onChange(int position) {

	}

	@Override
	protected void onDestroy() {
		unregisterReceiver(mScanSDCardReceiver);
		super.onDestroy();
	}
	
	private BroadcastReceiver mScanSDCardReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			L.l(TAG, "mScanSDCardReceiver---->onReceive()");
			if(intent.getAction().equals(Intent.ACTION_MEDIA_SCANNER_FINISHED)) {
				MusicUtils.initMusicList();
			}
		}
	};


	@Override
	public void onItemSelected(Uri contentUri) {
		if (mTwoPane) {
			// In two-pane mode, show the detail view in this activity by
			// adding or replacing the detail fragment using a
			// fragment transaction.
			Bundle args = new Bundle();
			args.putParcelable("URI", contentUri);

			DetailFragment fragment = new DetailFragment();
			fragment.setArguments(args);

			getSupportFragmentManager().beginTransaction()
					.replace(R.id.music_detail_container, fragment, "DETAILFRAGMENT_TAG")
					.commit();
		}
	}
}