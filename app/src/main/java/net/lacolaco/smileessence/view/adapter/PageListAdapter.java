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

package net.lacolaco.smileessence.view.adapter;

import android.app.ActionBar;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.widget.ArrayAdapter;
import net.lacolaco.smileessence.R;
import net.lacolaco.smileessence.activity.MainActivity;
import net.lacolaco.smileessence.logging.Logger;
import net.lacolaco.smileessence.view.PageFragment;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class PageListAdapter extends FragmentPagerAdapter implements ViewPager.OnPageChangeListener, ActionBar.OnNavigationListener {

    // ------------------------------ FIELDS ------------------------------

    private final MainActivity context;
    private final ActionBar actionBar;
    private final ViewPager viewPager;
    private final List<PageInfo> pages = new ArrayList<>();

    // --------------------------- CONSTRUCTORS ---------------------------

    public PageListAdapter(MainActivity _activity, ViewPager _viewPager) {
        super(_activity.getFragmentManager());
        context = _activity;
        actionBar = _activity.getActionBar();
        viewPager = _viewPager;
        viewPager.setAdapter(this);
        viewPager.addOnPageChangeListener(this);
    }

    // --------------------- GETTER / SETTER METHODS ---------------------

    @Override
    public synchronized int getCount() {
        return pages.size();
    }

    // ------------------------ INTERFACE METHODS ------------------------


    // --------------------- Interface OnNavigationListener ---------------------

    @Override
    public synchronized boolean onNavigationItemSelected(int itemPosition, long itemId) {
        viewPager.setCurrentItem(itemPosition, true);
        return true;
    }

    // --------------------- Interface OnPageChangeListener ---------------------

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public synchronized void onPageSelected(int position) {
        //Synchronize pager and navigation.
        Logger.debug(String.format("Page selected:%d", position));
        actionBar.setSelectedNavigationItem(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    // -------------------------- OTHER METHODS --------------------------

    @Override
    public synchronized Fragment getItem(int position) {
        PageInfo info = pages.get(position);
        return info.instantiate(context);
    }

    public void addPage(Class<? extends PageFragment> klass, String name, Bundle args) {
        this.addPage(klass, name, args, true);
    }

    public void addPage(Class<? extends PageFragment> klass, String name, Bundle args, boolean notifyChanged) {
        pages.add(new PageInfo(klass, name, args));
        if (notifyChanged) notifyDataSetChanged();
    }

    public synchronized boolean removePage(int position) {
        //if (removePageWithoutNotify(position)) {
        //    refreshListNavigation();
        //    return true;
        //}
        return pages.remove(position) != null; // TODO
    }

    @Override
    public void notifyDataSetChanged() {
        ArrayList<String> itemList = new ArrayList<>();
        for (PageInfo f : pages) {
            itemList.add(f.getName());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.navigation_list_item, R.id.navigation_list_item_text, itemList);
        actionBar.setListNavigationCallbacks(adapter, this);
        super.notifyDataSetChanged();
    }

    @Deprecated
    public int getIndex(Class<? extends PageFragment> fragmentClass) {
        for (int i = 0; i < pages.size(); ++i) {
            if (pages.get(i).getFragmentClass() == fragmentClass) {
                return i;
            }
        }
        return -1;
    }

    @Deprecated @SuppressWarnings("unchecked")
    public <T extends PageFragment> T getFragment(Class<T> fragmentClass) {
        for(PageInfo info : pages) {
            if (info.getFragmentClass() == fragmentClass) {
                return (T) info.getCachedInstance();
            }
        }
        return null;
    }

    private static final class PageInfo {
        private final Class<? extends PageFragment> fragmentClass;
        private final Bundle args;
        private final String name;
        private WeakReference<PageFragment> fragmentCache;

        PageInfo(Class<? extends PageFragment> _fragmentClass, String _name, Bundle _args) {
            fragmentClass = _fragmentClass;
            name = _name;
            args = _args;
        }

        public Class<? extends PageFragment> getFragmentClass() {
            return fragmentClass;
        }

        public String getName() { return name; }

        public Bundle getArgs() {
            return args;
        }

        public PageFragment instantiate(Context context) {
            PageFragment fragment = (PageFragment) Fragment.instantiate(context, getFragmentClass().getName(), getArgs());
            fragmentCache = new WeakReference<>(fragment);
            return fragment;
        }

        public PageFragment getCachedInstance() {
            return fragmentCache.get();
        }
    }
}
