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

package net.lacolaco.smileessence.twitter;

import net.lacolaco.smileessence.util.Consumer;
import net.lacolaco.smileessence.viewmodel.IViewModel;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

public class StatusFilter {
    private Map<Class<?>, Map<Object, Consumer<?>>> handlers = new HashMap<>();
    private static StatusFilter instance = new StatusFilter();

    public static StatusFilter getInstance() {
        return instance;
    }

    public synchronized <T> void register(Object key, Class<T> klass, Consumer<T> handler) {
        Map<Object, Consumer<?>> map = handlers.get(klass);
        if (map == null) {
            map = new WeakHashMap<>();
            handlers.put(klass, map);
        }
        map.put(key, handler);
    }

    // -------------------------- STATIC METHODS --------------------------

    public <T extends IViewModel> void filter(T status) {
        Map<Object, Consumer<?>> map = handlers.get(status.getClass());
        if (map != null) {
            for(Consumer f_ : map.values()) {
                ((Consumer<T>) f_).accept(status);
            }
        }
    }

    // public void remove(Class<? extends IViewModel> klass, long id) {
    //     Map<Object, Consumer<?>> map = handlers.get(klass);
    //
    //     if (map != null) {
    //         for(Consumer f_ : map.values()) {
    //             ((Consumer<T>) f_).accept(status);
    //         }
    //     }
    // }
}
