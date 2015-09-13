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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import net.lacolaco.smileessence.R;
import net.lacolaco.smileessence.activity.MainActivity;
import net.lacolaco.smileessence.command.Command;
import net.lacolaco.smileessence.command.CommandOpenURL;
import net.lacolaco.smileessence.entity.Account;
import net.lacolaco.smileessence.entity.Tweet;
import net.lacolaco.smileessence.entity.User;
import net.lacolaco.smileessence.twitter.TweetBuilder;
import net.lacolaco.smileessence.twitter.task.DeleteStatusTask;
import net.lacolaco.smileessence.twitter.task.FavoriteTask;
import net.lacolaco.smileessence.twitter.task.RetweetTask;
import net.lacolaco.smileessence.twitter.task.UnfavoriteTask;
import net.lacolaco.smileessence.twitter.util.TwitterUtils;
import net.lacolaco.smileessence.view.DialogHelper;
import net.lacolaco.smileessence.data.PostState;
import net.lacolaco.smileessence.view.adapter.StatusListAdapter;
import net.lacolaco.smileessence.view.listener.ListItemClickListener;
import net.lacolaco.smileessence.viewmodel.StatusViewModel;

import java.util.ArrayList;

import twitter4j.MediaEntity;
import twitter4j.URLEntity;

public class StatusDetailDialogFragment extends StackableDialogFragment implements View.OnClickListener {

    // ------------------------------ FIELDS ------------------------------

    private static final String KEY_STATUS_ID = "statusID";
    private Tweet tweet;

    // --------------------- GETTER / SETTER METHODS ---------------------

    public long getStatusID() {
        return getArguments().getLong(KEY_STATUS_ID);
    }

    public void setStatusID(long statusID) {
        Bundle args = new Bundle();
        args.putLong(KEY_STATUS_ID, statusID);
        setArguments(args);
    }

    // ------------------------ INTERFACE METHODS ------------------------


    // --------------------- Interface OnClickListener ---------------------

    @Override
    public void onClick(final View v) {
        final MainActivity activity = (MainActivity) getActivity();
        final Account account = activity.getCurrentAccount();

        switch (v.getId()) {
            case R.id.button_status_detail_reply: {
                replyToStatus(activity, account, tweet);
                break;
            }
            case R.id.button_status_detail_retweet: {
                final Long retweetID = (Long) v.getTag();
                toggleRetweet(activity, account, tweet, retweetID);
                break;
            }
            case R.id.button_status_detail_favorite: {
                Boolean isFavorited = (Boolean) v.getTag();
                toggleFavorite(activity, account, tweet, isFavorited);
                break;
            }
            case R.id.button_status_detail_delete: {
                deleteStatus(activity, account, tweet);
                break;
            }
            case R.id.button_status_detail_menu: {
                openMenu(activity);
                break;
            }
            default: {
                dismiss();
            }
        }
    }

    // ------------------------ OVERRIDE METHODS ------------------------

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final MainActivity activity = (MainActivity) getActivity();
        final Account account = activity.getCurrentAccount();

        tweet = Tweet.fetch(getStatusID());
        final StatusViewModel status = new StatusViewModel(tweet);
        View header = getTitleView(activity, account, status);
        ListView listView = (ListView) header.findViewById(R.id.listview_status_detail_reply_to);
        final StatusListAdapter adapter = new StatusListAdapter(getActivity());
        listView.setAdapter(adapter);
        long inReplyToStatusId = tweet.getInReplyTo();
        if (inReplyToStatusId == -1) {
            listView.setVisibility(View.GONE);
        } else {
            TwitterUtils.tryGetStatus(account, inReplyToStatusId, new TwitterUtils.StatusCallback() {
                @Override
                public void success(Tweet tweet) {
                    adapter.addToTop(new StatusViewModel(tweet));
                    adapter.updateForce();
                }

                @Override
                public void error() {
                }
            });
        }
        return new AlertDialog.Builder(getActivity()).setView(header).create();
    }

    private void confirm(MainActivity activity, Runnable onYes) {
        ConfirmDialogFragment.show(activity, getString(R.string.dialog_confirm_commands), onYes);
    }

    private void deleteStatus(final MainActivity activity, final Account account, final Tweet tweet) {
        confirm(activity, new Runnable() {
            @Override
            public void run() {
                new DeleteStatusTask(account.getTwitter(), tweet.getOriginalTweet().getId()).execute();
                dismiss();
            }
        });
    }

    private View getTitleView(MainActivity activity, Account account, StatusViewModel statusViewModel) {
        Tweet tweet = statusViewModel.getTweet();
        View view = activity.getLayoutInflater().inflate(R.layout.dialog_status_detail, null);
        View statusHeader = view.findViewById(R.id.layout_status_header);
        statusHeader = statusViewModel.getView(activity, activity.getLayoutInflater(), statusHeader);
        statusHeader.setClickable(false);
        int background = ((ColorDrawable) statusHeader.getBackground()).getColor();
        view.setBackgroundColor(background);
        ImageView favCountIcon = (ImageView) view.findViewById(R.id.image_status_detail_fav_count);
        ImageView rtCountIcon = (ImageView) view.findViewById(R.id.image_status_detail_rt_count);
        TextView favCountText = (TextView) view.findViewById(R.id.textview_status_detail_fav_count);
        TextView rtCountText = (TextView) view.findViewById(R.id.textview_status_detail_rt_count);
        int favoriteCount = statusViewModel.getTweet().getFavoriteCount();
        if (favoriteCount == 0) {
            favCountIcon.setVisibility(View.GONE);
            favCountText.setVisibility(View.GONE);
        } else {
            favCountText.setText(Integer.toString(favoriteCount));
        }
        int retweetCount = statusViewModel.getTweet().getOriginalTweet().getRetweetCount();
        if (retweetCount == 0) {
            rtCountIcon.setVisibility(View.GONE);
            rtCountText.setVisibility(View.GONE);
        } else {
            rtCountText.setText(Integer.toString(retweetCount));
        }
        ImageButton menu = (ImageButton) view.findViewById(R.id.button_status_detail_menu);
        ImageButton message = (ImageButton) view.findViewById(R.id.button_status_detail_reply);
        ImageButton retweet = (ImageButton) view.findViewById(R.id.button_status_detail_retweet);
        ImageButton favorite = (ImageButton) view.findViewById(R.id.button_status_detail_favorite);
        ImageButton delete = (ImageButton) view.findViewById(R.id.button_status_detail_delete);
        menu.setOnClickListener(this);
        message.setOnClickListener(this);
        retweet.setOnClickListener(this);
        favorite.setOnClickListener(this);
        delete.setOnClickListener(this);
        if (isNotRetweetable(account, tweet)) {
            retweet.setVisibility(View.GONE);
        } else if (isRetweetDeletable(account, tweet)) {
            retweet.setImageDrawable(getResources().getDrawable(R.drawable.icon_retweet_on));
            retweet.setTag(tweet.getId());
        } else {
            retweet.setTag(-1L);
        }
        favorite.setTag(statusViewModel.isFavorited());
        if (statusViewModel.isFavorited()) {
            favorite.setImageDrawable(getResources().getDrawable(R.drawable.icon_favorite_on));
        }
        boolean deletable = isDeletable(account, tweet);
        delete.setVisibility(deletable ? View.VISIBLE : View.GONE);
        LinearLayout commandsLayout = (LinearLayout) view.findViewById(R.id.linearlayout_status_detail_menu);
        commandsLayout.setClickable(true);
        ArrayList<Command> commands = getCommands(activity, tweet, account);
        Command.filter(commands);
        for (final Command command : commands) {
            View commandView = command.getView(activity, activity.getLayoutInflater(), null);
            commandView.setBackgroundColor(getResources().getColor(R.color.transparent));
            commandView.setOnClickListener(new ListItemClickListener(activity, new Runnable() {
                @Override
                public void run() {
                    command.execute();
                }
            }));
            commandsLayout.addView(commandView);
        }
        return view;
    }


    private ArrayList<Command> getCommands(Activity activity, Tweet tweet, Account account) {
        ArrayList<Command> commands = new ArrayList<>();
        // URL
        if (tweet.getUrls() != null) {
            for (URLEntity urlEntity : tweet.getUrls()) {
                commands.add(new CommandOpenURL(activity, urlEntity.getExpandedURL()));
            }
        }
        for (MediaEntity mediaEntity : tweet.getMedia()) {
            commands.add(new CommandOpenURL(activity, mediaEntity.getMediaURL()));
        }
        return commands;
    }

    private boolean isDeletable(Account account, Tweet tweet) {
        return tweet.getOriginalTweet().getUser().getId() == account.userID;
    }

    private boolean isNotRetweetable(Account account, Tweet tweet) {
        User user = tweet.getOriginalTweet().getUser();
        return user.isProtected() || user.getId() == account.userID;
    }

    private boolean isRetweetDeletable(Account account, Tweet tweet) {
        return tweet.isRetweet() && tweet.getUser().getId() == account.userID;
    }

    private void openMenu(MainActivity activity) {
        StatusMenuDialogFragment fragment = new StatusMenuDialogFragment();
        fragment.setStatusID(getStatusID());
        DialogHelper.showDialog(activity, fragment);
    }

    private void replyToStatus(MainActivity activity, Account account, Tweet tweet) {
        Tweet originalTweet = tweet.getOriginalTweet();
        TweetBuilder builder = new TweetBuilder();
        if (account.userID == originalTweet.getUser().getId()) {
            builder.addScreenName(account.screenName);
        }
        builder.addScreenNames(TwitterUtils.getScreenNames(originalTweet, account.screenName));

        String text = builder.buildText();
        int selStart = originalTweet.getUser().getScreenName().length() + 2; // "@" and " "

        PostState.newState().beginTransaction()
                .insertText(0, text)
                .setInReplyToStatusID(originalTweet.getId())
                .setSelection(selStart, text.length())
                .commitWithOpen(activity);
    }

    private void toggleFavorite(MainActivity activity, Account account, Tweet tweet, Boolean isFavorited) {
        long statusID = tweet.getOriginalTweet().getId();
        if (isFavorited) {
            new UnfavoriteTask(account.getTwitter(), statusID).execute();
        } else {
            new FavoriteTask(account.getTwitter(), statusID).execute();
        }
    }

    private void toggleRetweet(final MainActivity activity, final Account account, final Tweet tweet, final Long retweetID) {
        confirm(activity, new Runnable() {
            @Override
            public void run() {
                if (retweetID != -1L) {
                    new DeleteStatusTask(account.getTwitter(), retweetID).execute();
                } else {
                    new RetweetTask(account.getTwitter(), tweet.getOriginalTweet().getId()).execute();
                }
            }
        });
    }
}
