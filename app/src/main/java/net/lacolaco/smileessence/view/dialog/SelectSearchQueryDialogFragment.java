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
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import net.lacolaco.smileessence.R;
import net.lacolaco.smileessence.activity.MainActivity;
import net.lacolaco.smileessence.command.Command;
import net.lacolaco.smileessence.command.CommandOpenSearch;
import net.lacolaco.smileessence.entity.SearchQuery;
import net.lacolaco.smileessence.notification.Notificator;
import net.lacolaco.smileessence.view.adapter.UnorderedCustomListAdapter;

import java.util.ArrayList;
import java.util.List;

public class SelectSearchQueryDialogFragment extends MenuDialogFragment implements AdapterView.OnItemLongClickListener {

    // --------------------- Interface OnItemLongClickListener ---------------------

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        final CommandOpenSearch command = (CommandOpenSearch) parent.getItemAtPosition(position);
        @SuppressWarnings("unchecked") final UnorderedCustomListAdapter<Command> adapter = (UnorderedCustomListAdapter<Command>) parent.getAdapter();

        ConfirmDialogFragment.show(getActivity(), getString(R.string.dialog_confirm_delete_query), () -> {
            adapter.removeItem(command);
            adapter.update();

            command.getQuery().delete();
            Notificator.getInstance().publish(R.string.notice_search_query_deleted);
        }, false);

        return true;
    }

    // ------------------------ OVERRIDE METHODS ------------------------

    @Override
    protected void setMenuItems(final UnorderedCustomListAdapter<Command> adapter) {
        List<Command> commands = getCommands();
        Command.filter(commands);
        adapter.addItemsToBottom(commands);
        adapter.update();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final MainActivity activity = (MainActivity) getActivity();
        View body = activity.getLayoutInflater().inflate(R.layout.dialog_menu_list, null);
        ListView listView = (ListView) body.findViewById(R.id.listview_dialog_menu_list);
        final UnorderedCustomListAdapter<Command> adapter = new UnorderedCustomListAdapter<>(activity);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);

        // addition
        listView.setOnItemLongClickListener(this);

        setMenuItems(adapter);

        // addition
        return new AlertDialog.Builder(activity)
                .setView(body)
                .setTitle(R.string.dialog_title_select_search_query)
                .create();
    }

    // -------------------------- OTHER METHODS --------------------------

    private List<Command> getCommands() {
        Activity activity = getActivity();
        ArrayList<Command> commands = new ArrayList<>();
        final List<SearchQuery> queries = SearchQuery.getAll();
        if (queries != null) {
            for (final SearchQuery query : queries) {
                commands.add(new CommandOpenSearch(activity, query));
            }
        }
        return commands;
    }
}
