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

package net.lacolaco.smileessence.view.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import net.lacolaco.smileessence.R;
import net.lacolaco.smileessence.activity.MainActivity;
import net.lacolaco.smileessence.entity.Account;
import net.lacolaco.smileessence.entity.Tweet;
import net.lacolaco.smileessence.twitter.TwitterApi;
import net.lacolaco.smileessence.twitter.task.GetTalkTask;
import net.lacolaco.smileessence.view.adapter.StatusListAdapter;

import net.lacolaco.smileessence.viewmodel.StatusViewModel;
import twitter4j.Twitter;

public class TalkChainDialogFragment extends StackableDialogFragment {

    // ------------------------------ FIELDS ------------------------------

    public static final String KEY_STATUS_ID = "statusID";

    // --------------------- GETTER / SETTER METHODS ---------------------

    private long getStatusID() {
        return getArguments().getLong(KEY_STATUS_ID);
    }

    public void setStatusID(long id) {
        Bundle bundle = new Bundle();
        bundle.putLong(KEY_STATUS_ID, id);
        setArguments(bundle);
    }

    // ------------------------ OVERRIDE METHODS ------------------------

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MainActivity activity = (MainActivity) getActivity();
        final Account account = activity.getCurrentAccount();
        Twitter twitter = TwitterApi.getTwitter(account);
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_talk_list, null);
        ListView listView = (ListView) view.findViewById(R.id.listview_dialog_talk_list);
        final StatusListAdapter adapter = new StatusListAdapter(getActivity());
        listView.setAdapter(adapter);

        new GetTalkTask(twitter, getStatusID()) {
            @Override
            protected void onProgressUpdate(Tweet... tweets) {
                Tweet tweet = tweets[0];
                adapter.addToBottom(new StatusViewModel(tweet));
                adapter.updateForce();
            }
        }.execute();

        return new AlertDialog.Builder(activity)
                .setTitle(R.string.dialog_title_talk_chain)
                .setView(view)
                .setCancelable(true)
                .create();
    }
}