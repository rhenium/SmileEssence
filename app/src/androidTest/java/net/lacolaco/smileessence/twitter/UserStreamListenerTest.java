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

import android.test.ActivityInstrumentationTestCase2;
import net.lacolaco.smileessence.activity.MainActivity;
import net.lacolaco.smileessence.entity.Tweet;
import net.lacolaco.smileessence.entity.User;
import net.lacolaco.smileessence.util.TwitterMock;
import net.lacolaco.smileessence.viewmodel.EventViewModel;
import net.lacolaco.smileessence.viewmodel.StatusViewModel;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;

public class UserStreamListenerTest extends ActivityInstrumentationTestCase2<MainActivity> {

    TwitterMock mock;
    UserStreamListener listener;

    public UserStreamListenerTest() {
        super(MainActivity.class);
    }

    @Override
    public void setUp() throws Exception {
        mock = new TwitterMock(getInstrumentation().getContext());
        listener = new UserStreamListener(mock.getAccount());
    }

    public void testOnStatus() throws Exception {
        Status status = mock.getTweetRawMock();
        Container<StatusViewModel> a = new Container<>();
        Container<Long> b = new Container<>();
        StatusFilter.getInstance().register(this, StatusViewModel.class,
                gotVm -> a.object = gotVm,
                gotId -> b.object = gotId);
        listener.onStatus(status);
        assertEquals(Tweet.fromTwitter(status, 0), a.object.getTweet());
        listener.onDeletionNotice(new StatusDeletionNotice() {
            @Override
            public long getStatusId() {
                return status.getId();
            }

            @Override
            public long getUserId() {
                return status.getUser().getId();
            }

            @Override
            public int compareTo(StatusDeletionNotice another) {
                return 0;
            }
        });
        assertEquals(status.getId(), (long) b.object);
        StatusFilter.getInstance().unregister(this);
    }

    public void testOnFavorited() throws Exception {
        final Status status = mock.getReplyRawMock();
        final twitter4j.User source = mock.getUserRawMock();
        Container<EventViewModel> a = new Container<>();
        StatusFilter.getInstance().register(this, EventViewModel.class,
                gotVm -> a.object = gotVm,
                null);
        listener.onFavorite(source, source, status);
        assertNotNull(a.object);
        assertSame(User.fromTwitter(source), a.object.getSource());
        assertSame(Tweet.fromTwitter(status, mock.getUserMock().getId()), a.object.getTargetObject());
    }

    @Override
    protected void tearDown() throws Exception {
        getActivity().forceFinish();
    }

    static class Container<T> {
        public T object;
    }
}
