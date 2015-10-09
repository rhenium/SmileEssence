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
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.*;
import net.lacolaco.smileessence.Application;
import net.lacolaco.smileessence.R;
import net.lacolaco.smileessence.activity.MainActivity;
import net.lacolaco.smileessence.command.Command;
import net.lacolaco.smileessence.command.CommandOpenURL;
import net.lacolaco.smileessence.data.PostState;
import net.lacolaco.smileessence.entity.Account;
import net.lacolaco.smileessence.entity.RBinding;
import net.lacolaco.smileessence.entity.Tweet;
import net.lacolaco.smileessence.notification.Notificator;
import net.lacolaco.smileessence.twitter.TweetBuilder;
import net.lacolaco.smileessence.twitter.task.*;
import net.lacolaco.smileessence.util.Themes;
import net.lacolaco.smileessence.util.UIObserverBundle;
import net.lacolaco.smileessence.view.DialogHelper;
import net.lacolaco.smileessence.view.adapter.StatusListAdapter;
import net.lacolaco.smileessence.view.listener.ListItemClickListener;
import net.lacolaco.smileessence.viewmodel.StatusViewModel;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

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
        switch (v.getId()) {
            case R.id.button_status_detail_reply: {
                replyToStatus();
                break;
            }
            case R.id.button_status_detail_retweet: {
                toggleRetweet();
                break;
            }
            case R.id.button_status_detail_favorite: {
                toggleFavorite();
                break;
            }
            case R.id.button_status_detail_delete: {
                deleteStatus();
                break;
            }
            case R.id.button_status_detail_menu: {
                openMenu();
                break;
            }
            default: {
                dismiss();
            }
        }
    }

    // ------------------------ OVERRIDE METHODS ------------------------

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tweet = Tweet.fetch(getStatusID());
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (tweet == null) { // trying to open deleted tweet?
            Notificator.getInstance().publish(R.string.notice_error_show_status);
            return new DisposeDialog(getActivity());
        }

        Account account = Application.getInstance().getCurrentAccount();

        View header = getTitleView();

        ListView listView = (ListView) header.findViewById(R.id.listview_status_detail_reply_to);
        final StatusListAdapter adapter = new StatusListAdapter(getActivity());
        listView.setAdapter(adapter);

        if (tweet.getInReplyToStatusId() != -1) {
            listView.setVisibility(View.VISIBLE);
            Tweet replyToIfPresent = tweet.getInReplyToIfPresent();
            if (replyToIfPresent != null) {
                adapter.addToTop(new StatusViewModel(replyToIfPresent));
                adapter.updateForce();
            } else {
                new ShowStatusTask(account, tweet.getInReplyToStatusId())
                        .onDoneUI(replyTo -> {
                            adapter.addToTop(new StatusViewModel(replyTo));
                            adapter.updateForce();
                        })
                        .execute();
            }
        } else {
            listView.setVisibility(View.GONE);
        }

        return new AlertDialog.Builder(getActivity()).setView(header).create();
    }

    private View getTitleView() {
        MainActivity activity = ((MainActivity) getActivity());

        View view = activity.getLayoutInflater().inflate(R.layout.dialog_status_detail, null);
        UIObserverBundle bundle = new UIObserverBundle();
        view.setTag(bundle);

        View statusHeader = new StatusViewModel(tweet).getView(activity, activity.getLayoutInflater(), view.findViewById(R.id.layout_status_header));
        statusHeader.setClickable(false);

        view.setBackgroundColor(((ColorDrawable) statusHeader.getBackground()).getColor());
        updateViewReactions(view);
        updateViewButtons(view);
        updateViewMenu(view);

        final WeakReference<View> weakView = new WeakReference<>(view);
        bundle.attach(tweet.getOriginalTweet(), changes -> {
            View strongView = weakView.get();
            if (strongView != null && getActivity() != null) {
                if (changes.contains(RBinding.REACTION_COUNT))
                    updateViewReactions(strongView);
                if (changes.contains(RBinding.FAVORITERS) || changes.contains(RBinding.RETWEETERS))
                    updateViewButtons(strongView);
            }
        });

        return view;
    }

    private void updateViewReactions(View view) {
        //--- favs/RTs count
        ImageView favCountIcon = (ImageView) view.findViewById(R.id.image_status_detail_fav_count);
        TextView favCountText = (TextView) view.findViewById(R.id.textview_status_detail_fav_count);
        if (tweet.getFavoriteCount() > 0) {
            favCountText.setText(Integer.toString(tweet.getFavoriteCount()));
            favCountIcon.setVisibility(View.VISIBLE);
            favCountText.setVisibility(View.VISIBLE);
        } else {
            favCountIcon.setVisibility(View.GONE);
            favCountText.setVisibility(View.GONE);
        }

        ImageView rtCountIcon = (ImageView) view.findViewById(R.id.image_status_detail_rt_count);
        TextView rtCountText = (TextView) view.findViewById(R.id.textview_status_detail_rt_count);
        if (tweet.getRetweetCount() > 0) {
            rtCountText.setText(Integer.toString(tweet.getRetweetCount()));
            rtCountIcon.setVisibility(View.VISIBLE);
            rtCountText.setVisibility(View.VISIBLE);
        } else {
            rtCountIcon.setVisibility(View.GONE);
            rtCountText.setVisibility(View.GONE);
        }
    }

    private void updateViewButtons(View view) {
        Account account = Application.getInstance().getCurrentAccount();

        //--- buttons
        ImageButton message = (ImageButton) view.findViewById(R.id.button_status_detail_reply);
        message.setOnClickListener(this);

        ImageButton retweet = (ImageButton) view.findViewById(R.id.button_status_detail_retweet);
        retweet.setOnClickListener(this);
        if (tweet.getOriginalTweet().getUser().isProtected() ||
                tweet.getOriginalTweet().getUser().getId() == account.getUserId()) {
            retweet.setVisibility(View.GONE);
        } else {
            retweet.setVisibility(View.VISIBLE);
            if (tweet.isRetweetedBy(account.getUserId())) {
                retweet.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.icon_retweet_on));
            } else {
                retweet.setImageDrawable(Themes.getStyledDrawable(getActivity(), R.attr.icon_retweet_off));
            }
        }

        ImageButton favorite = (ImageButton) view.findViewById(R.id.button_status_detail_favorite);
        favorite.setOnClickListener(this);
        if (tweet.isFavoritedBy(account.getUserId())) {
            favorite.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.icon_favorite_on));
        } else {
            favorite.setImageDrawable(Themes.getStyledDrawable(getActivity(), R.attr.icon_favorite_off));
        }

        ImageButton delete = (ImageButton) view.findViewById(R.id.button_status_detail_delete);
        delete.setOnClickListener(this);
        delete.setVisibility(account.canDelete(tweet) ? View.VISIBLE : View.GONE);
    }

    private void updateViewMenu(View view) {
        MainActivity activity = ((MainActivity) getActivity());
        //--- menu
        ImageButton menu = (ImageButton) view.findViewById(R.id.button_status_detail_menu);
        menu.setOnClickListener(this);
        LinearLayout commandsLayout = (LinearLayout) view.findViewById(R.id.linearlayout_status_detail_menu);
        commandsLayout.setClickable(true);
        ArrayList<Command> commands = getCommands();
        Command.filter(commands);
        for (final Command command : commands) {
            View commandView = command.getView(activity, activity.getLayoutInflater(), null);
            commandView.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.transparent));
            commandView.setOnClickListener(new ListItemClickListener(activity, command::execute));
            commandsLayout.addView(commandView);
        }
    }

    private void confirm(Runnable onYes) {
        ConfirmDialogFragment.show(getActivity(), getString(R.string.dialog_confirm_commands), onYes);
    }

    private void deleteStatus() {
        confirm(() -> {
            Account account = Application.getInstance().getCurrentAccount();

            new DeleteStatusTask(account, tweet.getOriginalTweet().getId())
                    .onDone(t -> Notificator.getInstance().publish(R.string.notice_status_delete_succeeded))
                    .onFail(e -> Notificator.getInstance().alert(R.string.notice_status_delete_failed))
                    .execute();
            dismiss();
        });
    }

    private ArrayList<Command> getCommands() {
        ArrayList<Command> commands = new ArrayList<>();
        // URL
        for (String url : tweet.getUrlsExpanded()) {
            commands.add(new CommandOpenURL(getActivity(), url));
        }
        for (String url : tweet.getMediaUrls()) {
            commands.add(new CommandOpenURL(getActivity(), url));
        }
        return commands;
    }

    private void openMenu() {
        StatusMenuDialogFragment fragment = new StatusMenuDialogFragment();
        fragment.setStatusID(getStatusID());
        DialogHelper.showDialog(getActivity(), fragment);
    }

    private void replyToStatus() {
        Account account = Application.getInstance().getCurrentAccount();
        Tweet originalTweet = tweet.getOriginalTweet();

        TweetBuilder builder = new TweetBuilder();
        builder.addScreenName(originalTweet.getUser().getScreenName());
        for (String screenName : originalTweet.getMentions()) {
            if (!screenName.equals(account.getUser().getScreenName())) {
                builder.addScreenName(screenName);
            }
        }

        String text = builder.buildText();
        int selStart = originalTweet.getUser().getScreenName().length() + 2; // "@" and " "

        PostState.newState().beginTransaction()
                .insertText(0, text)
                .setInReplyTo(originalTweet)
                .setSelection(selStart, text.length())
                .commitWithOpen((MainActivity) getActivity());
    }

    private void toggleFavorite() {
        Account account = Application.getInstance().getCurrentAccount();
        if (tweet.isFavoritedBy(account.getUserId())) {
            new UnfavoriteTask(account, tweet.getId())
                    .onDone(x -> Notificator.getInstance().publish(R.string.notice_unfavorite_succeeded))
                    .onFail(x -> Notificator.getInstance().alert(R.string.notice_unfavorite_failed))
                    .execute();
        } else {
            new FavoriteTask(account, tweet.getId())
                    .onDone(x -> Notificator.getInstance().publish(R.string.notice_favorite_succeeded))
                    .onFail(x -> Notificator.getInstance().alert(R.string.notice_favorite_failed))
                    .execute();
        }
    }

    private void toggleRetweet() {
        Account account = Application.getInstance().getCurrentAccount();
        confirm(() -> {
            if (tweet.isRetweetedBy(account.getUserId())) {
                new DeleteStatusTask(account, tweet.getRetweetIdBy(account.getUserId()))
                        .onDone(t -> Notificator.getInstance().publish(R.string.notice_status_delete_succeeded))
                        .onFail(e -> Notificator.getInstance().alert(R.string.notice_status_delete_failed))
                        .execute();
                dismiss();
            } else {
                new RetweetTask(account, tweet.getId())
                        .onDone(x -> Notificator.getInstance().publish(R.string.notice_retweet_succeeded))
                        .onFail(x -> Notificator.getInstance().alert(R.string.notice_retweet_failed))
                        .execute();
            }
        });
    }
}
