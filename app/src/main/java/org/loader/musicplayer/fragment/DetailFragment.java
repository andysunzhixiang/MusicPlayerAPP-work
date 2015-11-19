package org.loader.musicplayer.fragment;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import org.loader.musicplayer.R;
import org.loader.musicplayer.activity.MainActivity;
import org.loader.musicplayer.application.App;
import org.loader.musicplayer.ui.CDView;
import org.loader.musicplayer.utils.ImageTools;
import org.loader.musicplayer.utils.LocalMusicUtils;
import org.loader.musicplayer.utils.MusicIconLoader;

/**
 * A simple {@link Fragment} subclass.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>
{


    public static final String LOG_TAG = DetailFragment.class.getSimpleName();

    private static final int MUSIC_DETAIL_LOADER = 0;

    static final String DETAIL_URI = "URI";
    private Uri mUri;

    private MainActivity mActivity;


    private static final String[] MUSIC_COLUMNS = {
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM_ID
    };

    public static final int COL_MUSIC_ID = 0;
    public static final int COL_MUSIC_TITLE = 1;
    public static final int COL_MUSIC_ARTIST = 2;
    public static final int COL_MUSIC_DURATION = 3;
    public static final int COL_MUSIC_ALBUM_ID = 4;

    private ImageView mPlayBackImageView; // back button
    private TextView mMusicArtist;
    private CDView mCdView; // cd
    private SeekBar mPlaySeekBar; // seekbar
    private ImageButton mStartPlayButton; // start or pause
    private TextView mSingerTextView; // singer


    public DetailFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(MUSIC_DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (MainActivity) activity;
    }

    @SuppressLint("InflateParams")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(DetailFragment.DETAIL_URI);
        }

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        mMusicArtist =(TextView)rootView.findViewById(R.id.play_singer);
        mCdView = (CDView)rootView.findViewById(R.id.play_cd_view);


        return rootView;
    }


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String sortOrder = MediaStore.Audio.Media.DEFAULT_SORT_ORDER;


        if(null != mUri){
            return new CursorLoader(getActivity(),
                    mUri,
                    MUSIC_COLUMNS,
                    null,
                    null,
                    sortOrder);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {
            String music_singer = data.getString(COL_MUSIC_ARTIST);
            mMusicArtist.setText(music_singer);
            String cdImage = LocalMusicUtils.getAlbumImage(data.getInt(COL_MUSIC_ALBUM_ID));
            Bitmap bmp = MusicIconLoader.getInstance().load(cdImage);
            if (bmp == null)
                bmp = BitmapFactory.decodeResource(getResources(),
                        R.drawable.detail_fragment_view);
            mCdView.setImage(ImageTools.scaleBitmap(bmp,
                    (int) (App.sScreenWidth * 0.8)));
                mCdView.setImage(bmp);
            }


        }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }


}
