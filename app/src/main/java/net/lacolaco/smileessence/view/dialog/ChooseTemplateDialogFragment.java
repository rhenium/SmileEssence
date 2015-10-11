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

import net.lacolaco.smileessence.command.Command;
import net.lacolaco.smileessence.command.post.PostCommandUseTemplate;
import net.lacolaco.smileessence.entity.Template;
import net.lacolaco.smileessence.view.adapter.UnorderedCustomListAdapter;

import java.util.ArrayList;
import java.util.List;

public class ChooseTemplateDialogFragment extends MenuDialogFragment {

    // ------------------------ OVERRIDE METHODS ------------------------

    @Override
    protected void setMenuItems(final UnorderedCustomListAdapter<Command> adapter) {
        adapter.addItemsToBottom(getCommands());
        adapter.update();
    }

    // -------------------------- OTHER METHODS --------------------------

    private List<Command> getCommands() {
        ArrayList<Command> list = new ArrayList<>();
        List<Template> templates = Template.getAll();
        for (Template template : templates) {
            list.add(new PostCommandUseTemplate(getActivity(), template));
        }
        return list;
    }
}
