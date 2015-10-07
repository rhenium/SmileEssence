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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;


public class ExtractionWord {
    private static Map<Long, ExtractionWord> cache; // model id -> Account
    private Pattern pattern;
    private Model model;

    // --------------------- static methods ---------------------
    public static synchronized int count() {
        return cache.size();
    }

    public static synchronized List<ExtractionWord> all() {
        return new ArrayList<>(cache.values());
    }

    public static synchronized void load() {
        cache = new LinkedHashMap<>();
        List<Model> all = new Select().from(Model.class).execute();
        for (Model model : all) {
            cache.put(model.getId(), new ExtractionWord(model));
        }
    }

    public static synchronized ExtractionWord add(String patternString) {
        Model model = new Model();
        model.text = patternString;
        model.save();
        ExtractionWord extractionWord = new ExtractionWord(model);
        cache.put(model.getId(), extractionWord);
        return extractionWord;
    }

    public static synchronized ExtractionWord remove(long modelId) {
        ExtractionWord extractionWord = cache.remove(modelId);
        if (extractionWord != null) {
            Model.delete(Model.class, modelId);
        }
        return extractionWord;
    }

    // --------------------- instance methods ---------------------
    private ExtractionWord(Model model) {
        this.model = model;
        this.pattern = Pattern.compile(model.text);
    }

    public Pattern getPattern() {
        return pattern;
    }

    public String getPatternString() {
        return model.text;
    }

    public void remove() {
        Model.delete(Model.class, getModelId());
        cache.remove(getModelId());
    }

    public void update(String newString) {
        model.text = newString;
        model.save();
        pattern = Pattern.compile(newString);
    }

    public long getModelId() {
        return model.getId();
    }

    @Table(name = "Extraction")
    public static class Model extends com.activeandroid.Model {
        @Column(name = "Text", notNull = true)
        public String text;

        // required by ActiveAndroid
        public Model() {
            super();
        }
    }
}
