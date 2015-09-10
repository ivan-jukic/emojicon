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

package com.rockerhieu.emojicon.recent;

import java.util.ArrayList;
import java.util.StringTokenizer;

import com.rockerhieu.emojicon.emoji.Emojicon;

import android.content.Context;
import android.content.SharedPreferences;

/**
* @author Daniele Ricci
*/
public class EmojiconRecentManager extends ArrayList<Emojicon> {
    private static final String DELIMITER = ",";
    private static final String PREFERENCE_NAME = "emojicon";
    private static final String PREF_RECENTS = "recent_emojis";
    private static final String PREF_PAGE = "recent_page";

    private static final Object LOCK = new Object();
    private static EmojiconRecentManager sInstance;
    private static int maximumSize = 40;

    private Context mContext;

    private EmojiconRecentManager(Context context) {
        mContext = context.getApplicationContext();
        loadRecents();
    }

    public static EmojiconRecentManager getInstance(Context context) {
        if (sInstance == null) {
            synchronized (LOCK) {
                if (sInstance == null) {
                    sInstance = new EmojiconRecentManager(context);
                }
            }
        }
        return sInstance;
    }

    public int getRecentPage() {
        return getPreferences().getInt(PREF_PAGE, 0);
    }

    public void setRecentPage(int page) {
        getPreferences().edit().putInt(PREF_PAGE, page).commit();
    }

    public void push(Emojicon object) {
        // FIXME totally inefficient way of adding the emoji to the adapter
        // TODO this should be probably replaced by a deque
        if (contains(object)) {
            super.remove(object);
        }
        add(0, object);
    }

    @Override
    public boolean add(Emojicon object) {
        boolean ret = super.add(object);

        while (this.size() > EmojiconRecentManager.maximumSize) {
            super.remove(0);
        }

        saveRecents();
        return ret;
    }

    @Override
    public void add(int index, Emojicon object) {
        super.add(index, object);

        if (index == 0) {
            while (this.size() > EmojiconRecentManager.maximumSize) {
                super.remove(EmojiconRecentManager.maximumSize);
            }
        } else {
            while (this.size() > EmojiconRecentManager.maximumSize) {
                super.remove(0);
            }
        }

        saveRecents();
    }

    @Override
    public boolean remove(Object object) {
        boolean ret = super.remove(object);
        saveRecents();
        return ret;
    }

    private SharedPreferences getPreferences() {
        return mContext.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
    }

    private void loadRecents() {
        SharedPreferences prefs = getPreferences();
        String str = prefs.getString(PREF_RECENTS, "");
        StringTokenizer tokenizer = new StringTokenizer(str, EmojiconRecentManager.DELIMITER);
        while (tokenizer.hasMoreTokens()) {
            add(Emojicon.fromChars(tokenizer.nextToken()));
        }
    }

    private void saveRecents() {
        StringBuilder str = new StringBuilder();
        int c = size();
        for (int i = 0; i < c; i++) {
            Emojicon e = get(i);
            str.append(e.getEmoji());
            if (i < (c - 1)) {
                str.append(EmojiconRecentManager.DELIMITER);
            }
        }
        SharedPreferences prefs = getPreferences();
        prefs.edit().putString(PREF_RECENTS, str.toString()).commit();
    }

    public static void setMaximumSize(int maximumSize) {
        EmojiconRecentManager.maximumSize = maximumSize;
    }
}
