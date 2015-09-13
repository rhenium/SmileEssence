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

package net.lacolaco.smileessence.command;

import android.test.ActivityInstrumentationTestCase2;

import net.lacolaco.smileessence.activity.MainActivity;
import net.lacolaco.smileessence.command.post.PostCommandInsert;
import net.lacolaco.smileessence.command.post.PostCommandMorse;
import net.lacolaco.smileessence.command.post.PostCommandZekamashi;
import net.lacolaco.smileessence.util.Morse;
import net.lacolaco.smileessence.data.PostState;

public class PostCommandsTest extends ActivityInstrumentationTestCase2<MainActivity> {

    public PostCommandsTest() {
        super(MainActivity.class);
    }

    @Override
    public void tearDown() throws Exception {
        getActivity().forceFinish();
    }

    public void testMorse() throws Exception {
        PostState.getState().removeListener();
        String s = "テスト（テスト）";
        PostCommandMorse morse = new PostCommandMorse(getActivity());
        assertEquals(Morse.jaToMorse(s), morse.build(s));
    }

    public void testSubString() throws Exception {
        String s = "テスト（テスト）";
        PostCommandMorse morse = new PostCommandMorse(getActivity());
        PostState.getState().removeListener();
        PostState.newState().beginTransaction().setText(s).setSelection(0, 3).commit();
        morse.execute();
        assertEquals(Morse.jaToMorse("テスト") + "（テスト）", PostState.getState().getText());
    }

    public void testInsert() throws Exception {
        String s = "テスト";
        String inserted = "AAA";
        PostCommandInsert insert = new PostCommandInsert(getActivity(), inserted);
        assertEquals("テストAAA", insert.build(s));
    }

    public void testZekamashi() throws Exception {
        String s = "(しまかぜ)";
        PostCommandZekamashi zekamashi = new PostCommandZekamashi(getActivity());
        assertEquals("(ぜかまし)", zekamashi.build(s));
    }
}
