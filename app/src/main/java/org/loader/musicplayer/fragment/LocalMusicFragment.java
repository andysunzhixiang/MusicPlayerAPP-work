package org.loader.musicplayer.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.loader.musicplayer.R;
import org.loader.musicplayer.activity.MainActivity;
import org.loader.musicplayer.activity.PlayActivity;
import org.loader.musicplayer.adapter.MusicCursorAdapter;
import org.loader.musicplayer.application.App;
import org.loader.musicplayer.pojo.Music;
import org.loader.musicplayer.utils.ImageTools;
import org.loader.musicplayer.utils.L;
import org.loader.musicplayer.utils.MusicIconLoader;
import org.loader.musicplayer.utils.MusicUtils;

import java.io.File;


public class LocalMusicFragment extends Fragment implements OnClickListener,LoaderManager.LoaderCallbacks<Cursor>
{

	public static final String LOG_TAG = LocalMusicFragment.class.getSimpleName();
	private MusicCursorAdapter mMusicAdapter;
	private ListView mMusicListView;
	private static final String SELECTED_KEY = "selected_position";

	private static final int MUSIC_LOADER = 0;
	private int mPosition = ListView.INVALID_POSITION;

	private ImageView mMusicIcon;
	private TextView mMusicTitle;
	private TextView mMusicArtist;

	private ImageView mPreImageView;
	private ImageView mPlayImageView;
	private ImageView mNextImageView;


	private MainActivity mActivity;

	private boolean isPause;

	private static final String[] MUSIC_COLUMNS = {
			MediaStore.Audio.Media._ID,
			MediaStore.Audio.Media.IS_MUSIC,
			MediaStore.Audio.Media.TITLE,
			MediaStore.Audio.Media.ARTIST,
			MediaStore.Audio.Media.DATA,
			MediaStore.Audio.Media.DURATION,
			MediaStore.Audio.Media.ALBUM_ID
	};

	public static final int COL_MUSIC_ID = 0;
	public static final int COL_MUSIC_IS_MUSIC = 1;
	public static final int COL_MUSIC_TITLE = 2;
	public static final int COL_MUSIC_ARTIST = 3;
	public static final int COL_MUSIC_DATA = 4;
	public static final int COL_MUSIC_DURATION = 5;
	public static final int COL_MUSIC_ALBUM_ID = 6;
	public interface Callback {
		/**
		 * DetailFragmentCallback for when an item has been selected.
		 */
		void onItemSelected(Uri musicUri);
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		getLoaderManager().initLoader(MUSIC_LOADER, null, this);
		super.onActivityCreated(savedInstanceState);
	}
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mActivity = (MainActivity) activity;
	}

	@SuppressLint("InflateParams")
	@Override
	public View onCreateView(LayoutInflater inflater,
			ViewGroup container, Bundle savedInstanceState) {

		mMusicAdapter = new MusicCursorAdapter(getActivity(), null, 0);
		View rootView = inflater.inflate(R.layout.local_music_layout, container, false);
		setupViews(rootView);
		//mMusicListView = (ListView) rootView.findViewById(R.id.music_list);
		mMusicListView.setAdapter(mMusicAdapter);

		mMusicListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
				Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);

				if(cursor != null) {
					((Callback) getActivity())
							.onItemSelected(ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, cursor.getLong(COL_MUSIC_ID)
							));
				}
				play(position);
				mPosition = position;
			}
		});




		if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
			// The listview probably hasn't even been populated yet.  Actually perform the
			mPosition = savedInstanceState.getInt(SELECTED_KEY);
		}

		return rootView;
	}

	/**
	 * After creating view, Callback to notify activity bind Play service.
	 */
	@Override
	public void onStart() {
		super.onStart();
		L.l("fragment", "onViewCreated");
		mActivity.allowBindService();
	}

	@Override
	public void onResume() {
		super.onResume();
		isPause = false;
	}

	@Override
	public void onPause() {
		isPause = true;
		super.onPause();
	}

	/**
	 * When stop， callback notify activity to unbind PlayService
	 */
	@Override
	public void onStop() {
		super.onStop();
		L.l("fragment", "onDestroyView");
		mActivity.allowUnbindService();
	}

	private void setupViews(View layout) {
		mMusicListView = (ListView) layout.findViewById(R.id.music_list);
		mMusicIcon = (ImageView) layout.findViewById(R.id.iv_play_icon);
		mMusicTitle = (TextView) layout.findViewById(R.id.tv_play_title);
		mMusicArtist = (TextView) layout.findViewById(R.id.tv_play_artist);

		mPreImageView = (ImageView) layout.findViewById(R.id.iv_pre);
		mPlayImageView = (ImageView) layout.findViewById(R.id.iv_play);
		mNextImageView = (ImageView) layout.findViewById(R.id.iv_next);

		mMusicIcon.setOnClickListener(this);
		mPreImageView.setOnClickListener(this);
		mPlayImageView.setOnClickListener(this);
		mNextImageView.setOnClickListener(this);

	}



	/**
	 * play music item
	 * 
	 * @param position
	 */
	private void play(int position) {
		int pos = mActivity.getPlayService().play(position);
		onPlay(pos);
	}

	/**
	 * Play music control panel.
	 * 
	 * @param position
	 */
	public void onPlay(int position) {
		if (MusicUtils.sMusicList.isEmpty() || position < 0)
			return;

		Music music = MusicUtils.sMusicList.get(position);
		Bitmap icon = MusicIconLoader.getInstance().load(music.getImage());
		mMusicIcon.setImageBitmap(icon == null ? ImageTools
				.scaleBitmap(R.drawable.ic_launcher) : ImageTools
				.scaleBitmap(icon));
		mMusicTitle.setText(music.getTitle());
		mMusicArtist.setText(music.getArtist());

		if (mActivity.getPlayService().isPlaying()) {
			mPlayImageView.setImageResource(android.R.drawable.ic_media_pause);
		} else {
			mPlayImageView.setImageResource(android.R.drawable.ic_media_play);
		}
		//   New a thread to update the Notification
		new Thread(){
			@Override
			public void run() {
				super.run();
				mActivity.getPlayService().setRemoteViews();
			}
		}.start();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.iv_play_icon:
			//Move to detail play
			startActivity(new Intent(mActivity, PlayActivity.class));
			break;
		case R.id.iv_play:
			if (mActivity.getPlayService().isPlaying()) {
				mActivity.getPlayService().pause(); // pause
				mPlayImageView
						.setImageResource(android.R.drawable.ic_media_play);
			} else {
				onPlay(mActivity.getPlayService().resume()); // play
			}
			break;
		case R.id.iv_next:
			int playingPosition = mActivity.getPlayService().next(); // next song
			Music music = MusicUtils.sMusicList.get(playingPosition);
			Bitmap icon = MusicIconLoader.getInstance().load(music.getImage());
			mMusicIcon.setImageBitmap(icon == null ? ImageTools
					.scaleBitmap(R.drawable.ic_launcher) : ImageTools
					.scaleBitmap(icon));
			mMusicTitle.setText(music.getTitle());
			mMusicArtist.setText(music.getArtist());

			if (mActivity.getPlayService().isPlaying()) {
				mPlayImageView.setImageResource(android.R.drawable.ic_media_pause);
			} else {
				mPlayImageView.setImageResource(android.R.drawable.ic_media_play);
			}
			break;
		case R.id.iv_pre:
			int playingPosition_pre= mActivity.getPlayService().pre(); // previous song
			Music music_pre = MusicUtils.sMusicList.get(playingPosition_pre);
			Bitmap icon_pre = MusicIconLoader.getInstance().load(music_pre.getImage());
			mMusicIcon.setImageBitmap(icon_pre == null ? ImageTools
					.scaleBitmap(R.drawable.ic_launcher) : ImageTools
					.scaleBitmap(icon_pre));
			mMusicTitle.setText(music_pre.getTitle());
			mMusicArtist.setText(music_pre.getArtist());
			break;
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// When tablets rotate, the currently selected list item needs to be saved.
		// When no item is selected, mPosition will be set to Listview.INVALID_POSITION,
		// so check for that before storing.
		if (mPosition != ListView.INVALID_POSITION) {
			outState.putInt(SELECTED_KEY, mPosition);
		}
		super.onSaveInstanceState(outState);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
		// This is called when a new Loader needs to be created.  This
		// fragment only uses one loader, so we don't care about checking the id.

		// To only show current and future dates, filter the query to return weather only for
		// dates after or including today.

		// Sort order:  Ascending, by date.
		String sortOrder = MediaStore.Audio.Media.DEFAULT_SORT_ORDER;

		Uri musicForLocationUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

		String dirName = getBaseDir();

		return new CursorLoader(getActivity(),
				musicForLocationUri,
				MUSIC_COLUMNS,
				MediaStore.Audio.Media.DATA + " like ?",
				new String[] { dirName + "%" },
				sortOrder);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		mMusicAdapter.swapCursor(data);
		Log.d(LOG_TAG,data.toString());
		if (mPosition != ListView.INVALID_POSITION) {
			// If we don't need to restart the loader, and there's a desired position to restore
			// to, do so now.
			mMusicListView.smoothScrollToPosition(mPosition);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mMusicAdapter.swapCursor(null);
	}

	public static String getBaseDir() {
		String dir = null;
		if (!Environment.getExternalStorageState().equals(Environment.MEDIA_UNMOUNTED)) {
			dir = Environment.getExternalStorageDirectory() + File.separator;
		} else {
			dir = App.sContext.getFilesDir() + File.separator;
		}

		return dir;
	}




}
