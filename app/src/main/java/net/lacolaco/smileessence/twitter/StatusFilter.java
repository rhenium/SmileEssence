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

import net.lacolaco.smileessence.entity.MuteUserIds;
import net.lacolaco.smileessence.util.Consumer;
import net.lacolaco.smileessence.viewmodel.EventViewModel;
import net.lacolaco.smileessence.viewmodel.IViewModel;
import net.lacolaco.smileessence.viewmodel.MessageViewModel;
import net.lacolaco.smileessence.viewmodel.StatusViewModel;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

public class StatusFilter {
    private Map<Class<?>, Map<Object, Consumer<?>>> addHandlers = new HashMap<>();
    private Map<Class<?>, Map<Object, Consumer<Long>>> removeHandlers = new HashMap<>();
    private static StatusFilter instance = new StatusFilter();

    public static StatusFilter getInstance() {
        return instance;
    }

    public synchronized <T> void register(Object key, Class<T> klass, Consumer<T> addHandler, Consumer<Long> removeHandler) {
        Map<Object, Consumer<?>> addMap = addHandlers.get(klass);
        if (addMap == null) {
            addMap = new WeakHashMap<>();
            addHandlers.put(klass, addMap);
        }
        addMap.put(key, addHandler);

        if (removeHandler != null) {
            Map<Object, Consumer<Long>> removeMap = removeHandlers.get(klass);
            if (removeMap == null) {
                removeMap = new WeakHashMap<>();
                removeHandlers.put(klass, removeMap);
            }
            removeMap.put(key, removeHandler);
        }
    }

    // -------------------------- STATIC METHODS --------------------------

    public void filter(StatusViewModel tweet) {
        if (!MuteUserIds.isMuted(tweet.getTweet().getOriginalTweet().getUser().getId())) {
            filter(StatusViewModel.class, tweet);
        }
    }

    public void filter(MessageViewModel message) {
        filter(MessageViewModel.class, message);
    }

    public void filter(EventViewModel event) {
        filter(EventViewModel.class, event);
    }

    public <T extends IViewModel> void filter(Class<T> klass, T status) {
        Map<Object, Consumer<?>> map = addHandlers.get(klass);
        if (map != null) {
            for(Consumer f_ : map.values()) {
                ((Consumer<T>) f_).accept(status);
            }
        }
    }

    public void remove(Class<? extends IViewModel> klass, long id) {
        Map<Object, Consumer<Long>> map = removeHandlers.get(klass);
        if (map != null) {
            for(Consumer<Long> f : map.values()) {
                f.accept(id);
            }
        }
    }
}
