/*
 * Copyright 2014 Hieu Rocker
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ivanjukic.emojicon.recent;

import com.ivanjukic.emojicon.EmojiAdapter;
import com.ivanjukic.emojicon.EmojiconGridFragment;
import com.ivanjukic.emojicon.R;
import com.ivanjukic.emojicon.emoji.Emojicon;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.GridView;

/**
 * @author Daniele Ricci
 */
public class EmojiconRecentGridFragment extends EmojiconGridFragment implements EmojiconRecent {
    private static final String USE_SYSTEM_DEFAULT_KEY = "useSystemDefaults";

    private EmojiAdapter mAdapter;
    private boolean mUseSystemDefault = false;


    protected static EmojiconRecentGridFragment newInstance() {
        return newInstance(false);
    }


    public static EmojiconRecentGridFragment newInstance(boolean useSystemDefault) {
        EmojiconRecentGridFragment fragment = new EmojiconRecentGridFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean(USE_SYSTEM_DEFAULT_KEY, useSystemDefault);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mUseSystemDefault = getArguments().getBoolean(USE_SYSTEM_DEFAULT_KEY);
        } else {
            mUseSystemDefault = false;
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        EmojiconRecentManager recents = EmojiconRecentManager.getInstance(view.getContext());

        mAdapter = new EmojiAdapter(view.getContext(), recents, mUseSystemDefault);
        GridView gridView = (GridView) view.findViewById(R.id.Emoji_GridView);
        gridView.setAdapter(mAdapter);
        gridView.setOnItemClickListener(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mAdapter = null;
    }

    @Override
    public void addRecentEmoji(Context context, Emojicon emojicon) {
        EmojiconRecentManager recents = EmojiconRecentManager.getInstance(context);
        recents.push(emojicon);

        // notify data set changed
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }
}
