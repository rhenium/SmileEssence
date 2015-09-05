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

package net.lacolaco.smileessence.entity;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class CommandSetting {
    public static ConcurrentHashMap<Integer, Boolean> cache;

    public static boolean isVisible(int key) {
        if (cache == null) {
            throw new IllegalStateException("Call .initialize first");
        }
        Boolean result = cache.get(key);
        if (result == null) {
            result = true; // default value
        }
        return result;
    }

    public static void setVisible(int key, boolean value) {
        if (cache == null) {
            throw new IllegalStateException("Call .initialize first");
        }
        Model model = new Select().from(Model.class).where("CommandKey = ?", key).executeSingle();
        if (model == null) {
            model = new Model();
        }
        model.commandKey = key;
        model.visibility = value;
        model.save();
        cache.put(key, value);
    }

    public static void initialize() {
        cache = new ConcurrentHashMap<>();
        List<Model> all = new Select().from(Model.class).execute();
        for(Model model : all) {
            cache.put(model.commandKey, model.visibility);
        }
    }

    @Table(name = "Commands")
    private static class Model extends com.activeandroid.Model {
        // ------------------------------ FIELDS ------------------------------
        @Column(name = "CommandKey")
        public int commandKey; // R.id はいってるらしいけどいいのこれ？（しらない）
        @Column(name = "Visibility")
        public boolean visibility;

        public Model() {
            super();
        }

        public Model(int commandKey, boolean visibility) {
            super();
            this.commandKey = commandKey;
            this.visibility = visibility;
        }
    }
}