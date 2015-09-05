/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2012-2014 lacolaco.net
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package net.lacolaco.smileessence.twitter.task;

import net.lacolaco.smileessence.entity.MuteUserIds;
import net.lacolaco.smileessence.logging.Logger;

import twitter4j.IDs;
import twitter4j.Twitter;
import twitter4j.TwitterException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MutesIDsTask extends TwitterTask<List<Long>> {

    // --------------------------- CONSTRUCTORS ---------------------------

    public MutesIDsTask(Twitter twitter) {
        super(twitter);
    }

    // ------------------------ OVERRIDE METHODS ------------------------

    @Override
    protected void onPostExecute(List<Long> mutesIDs) {
        for (Long mutesID : mutesIDs) {
            MuteUserIds.add(mutesID);
        }
    }

    @Override
    protected List<Long> doInBackground(Void... params) {
        try {
            List<Long> idList = new ArrayList<>();
            long cursor = -1;
            do {
                IDs mutesIDs = twitter.getMutesIDs(cursor);
                cursor = mutesIDs.getNextCursor();
                for (long id : mutesIDs.getIDs()) {
                    idList.add(id);
                }
            }
            while (cursor != 0);

            return idList;
        } catch (TwitterException e) {
            Logger.error(e);
            return Collections.emptyList();
        }
    }
}
