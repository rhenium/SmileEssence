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

package net.lacolaco.smileessence.data;

import android.graphics.Bitmap;
import android.util.LruCache;
import com.android.volley.Cache;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.*;
import net.lacolaco.smileessence.Application;

public class ImageCache {

    // ------------------------------ FIELDS ------------------------------

    private static ImageCache instance = new ImageCache();
    private RequestQueue requestQueue;
    private ImageLoader imageLoader;

    // -------------------------- STATIC METHODS --------------------------

    private ImageCache() {
        int cacheSizeInBytes = 64 * 1024 * 1024; // 64MB
        Cache diskCache = new DiskBasedCache(Application.getInstance().getCacheDir(), cacheSizeInBytes);
        requestQueue = new RequestQueue(diskCache, new BasicNetwork(new HurlStack()));
        imageLoader = new ImageLoader(requestQueue, new ImageLruCache());
        requestQueue.start();
    }

    public static ImageCache getInstance() {
        return instance;
    }

    // -------------------------- OTHER METHODS --------------------------

    public void setImageToView(String imageURL, NetworkImageView view) {
        view.setImageUrl(imageURL, imageLoader);
    }

    private static class ImageLruCache implements ImageLoader.ImageCache {
        private LruCache<String, Bitmap> cache;

        // -------------------------- STATIC METHODS --------------------------

        private ImageLruCache() {
            long memoryBytes = Runtime.getRuntime().maxMemory();
            int maxCount = (int) (memoryBytes / (8 * 1024));
            cache = new LruCache<>(maxCount);
        }
        // --------------------- Interface ImageCache ---------------------

        @Override
        public Bitmap getBitmap(String url) {
            return cache.get(url);
        }

        @Override
        public void putBitmap(String url, Bitmap bitmap) {
            cache.put(url, bitmap);
        }
    }
}
