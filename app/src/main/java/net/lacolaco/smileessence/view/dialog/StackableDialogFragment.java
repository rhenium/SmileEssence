package net.lacolaco.smileessence.view.dialog;

import android.app.DialogFragment;

import net.lacolaco.smileessence.view.DialogHelper;

public class StackableDialogFragment extends DialogFragment {
    public int show() {
        return DialogHelper.showDialog(this.getActivity(), this);
    }

    @Override
    public void dismiss() {
        super.dismiss();
        DialogHelper.unregisterDialog(getTag());
    }

    @Override
    public void dismissAllowingStateLoss() {
        super.dismissAllowingStateLoss();
        DialogHelper.unregisterDialog(getTag());
    }
}