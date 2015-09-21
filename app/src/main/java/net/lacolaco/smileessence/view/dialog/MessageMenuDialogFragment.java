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
import net.lacolaco.smileessence.command.*;
import net.lacolaco.smileessence.entity.Account;
import net.lacolaco.smileessence.entity.DirectMessage;
import net.lacolaco.smileessence.view.adapter.CustomListAdapter;

import java.util.ArrayList;
import java.util.List;

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

        DirectMessage message = DirectMessage.fetch(getMessageID());
        if (message != null) {
            List<Command> commands = getCommands(activity, message, account);
            Command.filter(commands);
            for (Command command : commands) {
                adapter.addToBottom(command);
            }
            adapter.update();
        } else {
            dismiss();
        }
    }

    // -------------------------- OTHER METHODS --------------------------

    public void addBottomCommands(Activity activity, DirectMessage message, Account account, ArrayList<Command> commands) {
        commands.add(new CommandSaveAsTemplate(activity, message.getText()));
        //User
        if (message.getSender() != message.getRecipient()) {
            commands.add(new CommandOpenUserDetail(activity, message.getRecipient().getScreenName(), account));
        }
        for (String screenName : message.getMentions()) {
            commands.add(new CommandOpenUserDetail(activity, screenName, account));
        }
        for (Command command : getHashtagCommands(activity, message)) {
            commands.add(command);
        }
        // Media
        for (String url : message.getUrlsExpanded()) {
            commands.add(new CommandOpenURL(activity, url));
        }
        for (String url : message.getMediaUrls()) {
            commands.add(new CommandOpenURL(activity, url));
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
            for (String hashtag : status.getHashtags()) {
                commands.add(new CommandOpenHashtagDialog(activity, hashtag));
            }
        }
        return commands;
    }
}
