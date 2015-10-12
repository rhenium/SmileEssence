package net.lacolaco.smileessence.view.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import net.lacolaco.smileessence.util.UIHandler;
import net.lacolaco.smileessence.viewmodel.IViewModel;

import java.util.*;

public abstract class CustomListAdapter<T extends IViewModel> extends BaseAdapter {

    // ------------------------------ FIELDS ------------------------------

    protected boolean isNotifiable = true;
    private List<T> frozenList = new ArrayList<>();
    private Activity activity;

    // --------------------------- CONSTRUCTORS ---------------------------

    CustomListAdapter(Activity activity) {
        this.activity = activity;
    }

    // --------------------- GETTER / SETTER METHODS ---------------------

    public final void setNotifiable(boolean notifiable) {
        isNotifiable = notifiable;
    }

    // --------------------- Interface BaseAdapter ---------------------

    @Override
    public final int getCount() {
        return frozenList.size();
    }

    @Override
    public final T getItem(int position) {
        return frozenList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public final View getView(int position, View convertView, ViewGroup parent) {
        return getItem(position).getView(activity, activity.getLayoutInflater(), convertView);
    }

    // ------------------------ OVERRIDE METHODS ------------------------

    @Override
    public final void notifyDataSetChanged() {
        frozenList = getFrozenList();
        super.notifyDataSetChanged();
    }

    // -------------------------- OTHER METHODS --------------------------

    protected abstract List<T> getFrozenList();

    public void update() {
        if (isNotifiable) {
            updateForce();
        }
    }

    public void updateForce() {
        new UIHandler().post(this::notifyDataSetChanged);
    }
}