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
import android.widget.Button;
import android.widget.RatingBar;
import net.lacolaco.smileessence.R;
import net.lacolaco.smileessence.activity.MainActivity;
import net.lacolaco.smileessence.data.PostState;
import net.lacolaco.smileessence.entity.Tweet;
import net.lacolaco.smileessence.util.SystemServiceHelper;

public class ReviewDialogFragment extends StackableDialogFragment implements View.OnClickListener {

    // ------------------------------ FIELDS ------------------------------

    private static final String statusIDKey = "key";
    private long statusID;
    private RatingBar ratingBar;

    // --------------------- GETTER / SETTER METHODS ---------------------

    public void setStatusID(long statusID) {
        Bundle args = new Bundle();
        args.putLong(statusIDKey, statusID);
        setArguments(args);
    }

    // ------------------------ INTERFACE METHODS ------------------------


    // --------------------- Interface OnClickListener ---------------------

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.button_submit: {
                execute();
                break;
            }
        }
    }

    // ------------------------ OVERRIDE METHODS ------------------------

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        statusID = args.getLong(statusIDKey);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_review, null);
        ratingBar = (RatingBar) view.findViewById(R.id.rating_review);
        Button button = (Button) view.findViewById(R.id.button_submit);
        button.setOnClickListener(this);
        return new AlertDialog.Builder(getActivity())
                .setView(view)
                .create();
    }

    private void execute() {
        SystemServiceHelper.hideIM(getActivity(), ratingBar);
        Tweet tweet = Tweet.fetch(statusID).getOriginalTweet();
        int star = (int) ratingBar.getRating();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            if (i < star) {
                builder.append("★");
            } else {
                builder.append("☆");
            }
        }
        String formatString = getFormatString();
        String str = String.format(formatString,
                builder.toString(),
                tweet.getUser().getScreenName(),
                tweet.getTwitterUrl());
        PostState.newState().beginTransaction()
                .setText(str)
                .setInReplyTo(tweet)
                .setCursor(str.indexOf(":") + 2)
                .commitWithOpen((MainActivity) getActivity());
    }

    private String getFormatString() {
        return getString(R.string.format_status_review);
    }
}
