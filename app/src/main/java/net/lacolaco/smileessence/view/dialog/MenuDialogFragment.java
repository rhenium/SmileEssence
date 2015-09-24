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
import android.widget.AdapterView;
import android.widget.ListView;
import net.lacolaco.smileessence.R;
import net.lacolaco.smileessence.activity.MainActivity;
import net.lacolaco.smileessence.command.Command;
import net.lacolaco.smileessence.command.IConfirmable;
import net.lacolaco.smileessence.view.adapter.CustomListAdapter;

public abstract class MenuDialogFragment extends StackableDialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final MainActivity activity = (MainActivity) getActivity();
        View body = activity.getLayoutInflater().inflate(R.layout.dialog_menu_list, null);
        ListView listView = (ListView) body.findViewById(R.id.listview_dialog_menu_list);
        final CustomListAdapter<Command> adapter = new CustomListAdapter<>(activity);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(onItemClickListener);

        setMenuItems(adapter);

        return new AlertDialog.Builder(activity).setView(body).create();
    }

    protected abstract void setMenuItems(final CustomListAdapter<Command> adapter);

    protected final AdapterView.OnItemClickListener onItemClickListener = (adapterView, view, i, l) -> MenuDialogFragment.this.onItemClick(adapterView, i);

    protected void executeCommand(Command command) {
        dismiss();
        command.execute();
    }

    protected void onItemClick(AdapterView<?> adapterView, int i) {
        final Command command = (Command) adapterView.getItemAtPosition(i);
        if (command != null) {
            if (command instanceof IConfirmable) {
                ConfirmDialogFragment.show(getActivity(), getString(R.string.dialog_confirm_commands), () -> executeCommand(command));
            } else {
                executeCommand(command);
            }
        }
    }
}
