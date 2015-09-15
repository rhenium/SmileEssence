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

package net.lacolaco.smileessence.viewmodel;

import android.app.Activity;
import android.databinding.*;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import net.lacolaco.smileessence.BR;
import net.lacolaco.smileessence.R;
import net.lacolaco.smileessence.databinding.MenuItemCheckboxBinding;

public class CheckBoxModel implements IViewModel {

    // ------------------------------ FIELDS ------------------------------

    private final CheckedState state;

    // --------------------------- CONSTRUCTORS ---------------------------

    public CheckBoxModel(String _text, boolean _checked) {
        state = new CheckedState(_text, _checked);
    }

    // --------------------- GETTER / SETTER METHODS ---------------------

    public boolean isChecked() {
        return state.isChecked();
    }

    public void setChecked(boolean _checked) {
        state.setChecked(_checked);
    }

    // ------------------------ INTERFACE METHODS ------------------------


    // --------------------- Interface IViewModel ---------------------

    @Override
    public View getView(Activity activity, LayoutInflater inflater, View convertedView) {
        if (convertedView == null) {
            MenuItemCheckboxBinding binding = MenuItemCheckboxBinding.inflate(inflater, null, false);
            convertedView = binding.getRoot();
            convertedView.setTag(binding);
        }

        MenuItemCheckboxBinding binding = (MenuItemCheckboxBinding) convertedView.getTag();
        binding.setCheckedState(state);
        return convertedView;
    }

    public static class CheckedState extends BaseObservable {
        private String text;
        private boolean checked;

        CheckedState(String _text, boolean _checked) {
            text = _text;
            checked = _checked;
        }

        @Bindable
        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
            notifyPropertyChanged(BR.text);
        }

        @Bindable
        public boolean isChecked() {
            return checked;
        }

        public void setChecked(boolean checked) {
            this.checked = checked;
            notifyPropertyChanged(BR.checked);
        }
    }
}
