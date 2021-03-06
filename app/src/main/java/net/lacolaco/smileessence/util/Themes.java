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

package net.lacolaco.smileessence.util;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import net.lacolaco.smileessence.Application;
import net.lacolaco.smileessence.R;

public class Themes {

    // ------------------------------ FIELDS ------------------------------

    public static final int THEME_DARK = 0;
    public static final int THEME_LIGHT = 1;

    // -------------------------- STATIC METHODS --------------------------

    public static int getThemeResId(int index) {
        switch (index) {
            case THEME_DARK: {
                return R.style.theme_dark;
            }
            case THEME_LIGHT: {
                return R.style.theme_light;
            }
            default: {
                return R.style.theme_dark;
            }
        }
    }

    public static int getStyledColor(Context context, int attribute) {
        TypedArray array = context.obtainStyledAttributes(Application.getInstance().getThemeResId(), new int[]{attribute});
        int color = array.getColor(0, 0);
        array.recycle();
        if (color == 0) {
            throw new RuntimeException("[BUG] can't get styled color from attr ID: " + attribute);
        } else {
            return color;
        }
    }

    public static Drawable getStyledDrawable(Context context, int attribute) {
        TypedArray array = context.obtainStyledAttributes(Application.getInstance().getThemeResId(), new int[]{attribute});
        Drawable drawable = array.getDrawable(0);
        array.recycle();
        return drawable;
    }
}
