package com.ivanjukic.emojicon;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridView;

import com.ivanjukic.emojicon.emoji.Emojicon;
import com.ivanjukic.emojicon.emoji.People;
import com.ivanjukic.emojicon.recent.EmojiconRecent;

import java.util.Arrays;


public class EmojiconPopupGridView {

    public View rootView;
    protected EmojiconPopup mEmojiconPopup;
    protected EmojiconRecent mRecents;
    protected Emojicon[] mData;


    public EmojiconPopupGridView(Context context, Emojicon[] emojicons, EmojiconRecent recents, EmojiconPopup emojiconPopup) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        mEmojiconPopup = emojiconPopup;
        rootView = inflater.inflate(R.layout.emojicon_grid, null);
        setRecents(recents);
        GridView gridView = (GridView) rootView.findViewById(R.id.Emoji_GridView);
        if (emojicons== null) {
            mData = People.DATA;
        } else {
            Object[] o = (Object[]) emojicons;
            mData = Arrays.asList(o).toArray(new Emojicon[o.length]);
        }
        EmojiAdapter mAdapter = new EmojiAdapter(rootView.getContext(), mData);
        mAdapter.setEmojiClickListener(new OnEmojiconClickedListener() {
            @Override
            public void onEmojiconClicked(Emojicon emojicon) {
                if (mEmojiconPopup.onEmojiconClickedListener != null) {
                    mEmojiconPopup.onEmojiconClickedListener.onEmojiconClicked(emojicon);
                }
                if (mRecents != null) {
                    mRecents.addRecentEmoji(rootView.getContext(), emojicon);
                }
            }
        });
        gridView.setAdapter(mAdapter);
    }


    private void setRecents(EmojiconRecent recents) {
        mRecents = recents;
    }


    public interface OnEmojiconClickedListener {
        void onEmojiconClicked(Emojicon emojicon);
    }

}
