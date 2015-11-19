package org.loader.musicplayer.adapter;


import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.loader.musicplayer.R;
import org.loader.musicplayer.fragment.LocalMusicFragment;

public class MusicCursorAdapter extends CursorAdapter{
    private int mPlayingPosition;

    public void setPlayingPosition(int position) {
        mPlayingPosition = position;
    }

    public int getPlayingPosition() {
        return mPlayingPosition;
    }

    public MusicCursorAdapter(Context context, Cursor c, int flags)
    {
        super(context, c, flags);
    }

    public static class ViewHolder {
        public final ImageView icon;
        public final TextView title;
        public final TextView artist;
        public final View mark;

    public ViewHolder(View view) {
        title = (TextView) view.findViewById(R.id.tv_music_list_title);
        artist = (TextView) view.findViewById(R.id.tv_music_list_artist);
        icon = (ImageView) view.findViewById(R.id.music_list_icon);
        mark = view.findViewById(R.id.music_list_selected);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.music_list_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();
            String music_title = cursor.getString(LocalMusicFragment.COL_MUSIC_TITLE);
            holder.title.setText(music_title);
            String music_artist = cursor.getString(LocalMusicFragment.COL_MUSIC_ARTIST);
            holder.artist.setText(music_artist);
    }
}
