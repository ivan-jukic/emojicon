package com.ivanjukic.emojicon.recent;

import android.content.Context;
import android.widget.GridView;

import com.ivanjukic.emojicon.EmojiAdapter;
import com.ivanjukic.emojicon.EmojiconPopup;
import com.ivanjukic.emojicon.EmojiconPopupGridView;
import com.ivanjukic.emojicon.R;
import com.ivanjukic.emojicon.emoji.Emojicon;


public class EmojiconRecentGridView extends EmojiconPopupGridView implements EmojiconRecent {
    EmojiAdapter mAdapter;

    public EmojiconRecentGridView(Context context, Emojicon[] emojicons,
                                   EmojiconRecent recent, EmojiconPopup emojiconsPopup) {

        super(context, emojicons, recent, emojiconsPopup);

        EmojiconRecentManager recentManager = EmojiconRecentManager.getInstance(rootView.getContext());
        mAdapter = new EmojiAdapter(rootView.getContext(),  recentManager);
        mAdapter.setEmojiClickListener(new OnEmojiconClickedListener() {
            @Override
            public void onEmojiconClicked(Emojicon emojicon) {
                if (mEmojiconPopup.onEmojiconClickedListener != null) {
                    mEmojiconPopup.onEmojiconClickedListener.onEmojiconClicked(emojicon);
                }
            }
        });
        GridView gridView = (GridView) rootView.findViewById(R.id.Emoji_GridView);
        gridView.setAdapter(mAdapter);
    }

    @Override
    public void addRecentEmoji(Context context, Emojicon emojicon) {
        EmojiconRecentManager recent = EmojiconRecentManager.getInstance(context);
        recent.push(emojicon);
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }
}
