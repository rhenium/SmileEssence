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
import android.os.Bundle;

import net.lacolaco.smileessence.activity.MainActivity;
import net.lacolaco.smileessence.command.Command;
import net.lacolaco.smileessence.command.CommandOpenHashtagDialog;
import net.lacolaco.smileessence.command.CommandOpenURL;
import net.lacolaco.smileessence.command.CommandOpenUserDetail;
import net.lacolaco.smileessence.command.CommandSaveAsTemplate;
import net.lacolaco.smileessence.entity.Account;
import net.lacolaco.smileessence.view.adapter.CustomListAdapter;

import java.util.ArrayList;
import java.util.List;

import net.lacolaco.smileessence.entity.DirectMessage;
import twitter4j.HashtagEntity;
import twitter4j.MediaEntity;
import twitter4j.URLEntity;

public class MessageMenuDialogFragment extends MenuDialogFragment {

    // ------------------------------ FIELDS ------------------------------

    private static final String KEY_MESSAGE_ID = "messageID";

    // --------------------- GETTER / SETTER METHODS ---------------------

    public long getMessageID() {
        return getArguments().getLong(KEY_MESSAGE_ID);
    }

    public void setMessageID(long messageID) {
        Bundle args = new Bundle();
        args.putLong(KEY_MESSAGE_ID, messageID);
        setArguments(args);
    }

    // ------------------------ OVERRIDE METHODS ------------------------

    @Override
    protected void setMenuItems(final CustomListAdapter<Command> adapter) {
        final MainActivity activity = (MainActivity) getActivity();
        final Account account = activity.getCurrentAccount();

        account.tryGetMessage(getMessageID(), new Account.MessageCallback() {
            @Override
            public void success(DirectMessage message) {
                List<Command> commands = getCommands(activity, message, account);
                Command.filter(commands);
                for (Command command : commands) {
                    adapter.addToBottom(command);
                }
                adapter.update();
            }

            @Override
            public void error() {
                dismiss();
            }
        });
    }

    // -------------------------- OTHER METHODS --------------------------

    public void addBottomCommands(Activity activity, DirectMessage message, Account account, ArrayList<Command> commands) {
        commands.add(new CommandSaveAsTemplate(activity, message.getText()));
        //User
        for (String screenName : message.getMentioningScreenNames()) {
            commands.add(new CommandOpenUserDetail(activity, screenName, account));
        }
        for (Command command : getHashtagCommands(activity, message)) {
            commands.add(command);
        }
        // Media
        if (message.getUrls() != null) {
            for (URLEntity urlEntity : message.getUrls()) {
                commands.add(new CommandOpenURL(activity, urlEntity.getExpandedURL()));
            }
        }
        for (MediaEntity mediaEntity : message.getMedia()) {
            commands.add(new CommandOpenURL(activity, mediaEntity.getMediaURL()));
        }
    }

    public boolean addMainCommands(Activity activity, DirectMessage message, Account account, ArrayList<Command> commands) {
        return commands.addAll(Command.getMessageCommands(activity, message, account));
    }

    public List<Command> getCommands(Activity activity, DirectMessage message, Account account) {
        ArrayList<Command> commands = new ArrayList<>();
        addMainCommands(activity, message, account, commands);
        addBottomCommands(activity, message, account, commands);
        return commands;
    }

    private ArrayList<Command> getHashtagCommands(Activity activity, DirectMessage status) {
        ArrayList<Command> commands = new ArrayList<>();
        if (status.getHashtags() != null) {
            for (HashtagEntity hashtagEntity : status.getHashtags()) {
                commands.add(new CommandOpenHashtagDialog(activity, hashtagEntity));
            }
        }
        return commands;
    }
}
