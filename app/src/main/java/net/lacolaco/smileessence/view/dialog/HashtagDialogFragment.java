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
import android.app.Dialog;
import android.os.Bundle;
import net.lacolaco.smileessence.activity.MainActivity;
import net.lacolaco.smileessence.command.Command;
import net.lacolaco.smileessence.command.CommandPasteToPost;
import net.lacolaco.smileessence.command.CommandSaveAsTemplate;
import net.lacolaco.smileessence.command.CommandSearchOnTwitter;
import net.lacolaco.smileessence.view.adapter.CustomListAdapter;

import java.util.ArrayList;
import java.util.List;

public class HashtagDialogFragment extends MenuDialogFragment {

    // ------------------------------ FIELDS ------------------------------

    public static final String KEY_TEXT = "text";

    // --------------------- GETTER / SETTER METHODS ---------------------

    private String getHashtagText() {
        return "#" + getArguments().get(KEY_TEXT);
    }

    public void setText(String text) {
        Bundle bundle = new Bundle();
        bundle.putString(KEY_TEXT, text);
        setArguments(bundle);
    }

    // ------------------------ OVERRIDE METHODS ------------------------

    @Override
    protected void setMenuItems(final CustomListAdapter<Command> adapter) {
        final MainActivity activity = (MainActivity) getActivity();

        String text = getHashtagText();
        List<Command> commands = getCommands(activity, text);
        Command.filter(commands);
        for (Command command : commands) {
            adapter.addToBottom(command);
        }
        adapter.update();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setTitle(getHashtagText());
        return dialog;
    }

    // -------------------------- OTHER METHODS --------------------------

    public List<Command> getCommands(Activity activity, String text) {
        ArrayList<Command> commands = new ArrayList<>();
        commands.add(new CommandSaveAsTemplate(activity, text));
        commands.add(new CommandPasteToPost(activity, text));
        commands.add(new CommandSearchOnTwitter(activity, text));
        return commands;
    }
}
