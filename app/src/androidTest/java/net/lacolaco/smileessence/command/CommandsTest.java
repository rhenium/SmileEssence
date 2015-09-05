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
import net.lacolaco.smileessence.util.TwitterMock;

public class CommandsTest extends ActivityInstrumentationTestCase2<MainActivity> {

    TwitterMock mock;

    public CommandsTest() {
        super(MainActivity.class);
    }

    @Override
    public void setUp() throws Exception {
        mock = new TwitterMock(getInstrumentation().getContext());
    }

//    public void testStatusCommand() throws Exception
//    {
//        final Activity activity = getActivity();
//        final Account account = new Account(mock.getAccessToken(), mock.getAccessTokenSecret(), mock.getUserMock().getId(), mock.getUserMock().getScreenName());
//        final Status status = mock.getStatusMock();
//        Tweet.fromTwitter(status);
//        getActivity().runOnUiThread(new Runnable()
//        {
//            @Override
//            public void run()
//            {
//                StatusCommand reply = new StatusCommandReply(activity, status);
//                assertTrue(reply.isEnabled());
//                assertEquals(R.id.key_command_status_reply, reply.getKey());
//                assertEquals(activity.getString(R.string.command_status_reply), reply.getText());
//                assertTrue(reply.execute());
//                assertEquals("@laco0416_2 ", PostState.getState().getText());
//                assertEquals(status.getId(), PostState.getState().getInReplyToStatusID());
//                assertEquals(status.getUser().getScreenName(), PostState.getState().getInReplyToScreenName());
//                assertEquals(status.getText(), PostState.getState().getInReplyToText());
//                StatusCommandNanigaja nanigaja = new StatusCommandNanigaja(activity, status, account);
//                assertEquals("@laco0416_2 な～にがテスト #test http://t.co/nd7Bzal2EU http://t.co/yANfRHC4KWじゃ", StatusCommandNanigaja.build(activity, status, account));
//                assertEquals("？？？「テスト #test http://t.co/nd7Bzal2EU http://t.co/yANfRHC4KW」", StatusCommandMakeAnonymous.build(activity, status, account));
//            }
//        });
//    }
//
//    public void testUserCommand() throws Exception
//    {
//        final Activity activity = getActivity();
//        Account account = new Account(mock.getAccessToken(), mock.getAccessTokenSecret(), mock.getUserMock().getId(), mock.getUserMock().getScreenName());
//        final User user = mock.getUserMock();
//        getActivity().runOnUiThread(new Runnable()
//        {
//            @Override
//            public void run()
//            {
//                UserCommand reply = new UserCommandReply(activity, user);
//                assertTrue(reply.isEnabled());
//                assertEquals(activity.getString(R.string.command_user_reply), reply.getText());
//                assertTrue(reply.execute());
//                assertEquals("@laco0416 ", PostState.getState().getText());
//                assertEquals(-1, PostState.getState().getInReplyToStatusID());
//                assertEquals(user.getScreenName(), PostState.getState().getInReplyToScreenName());
//                assertEquals("", PostState.getState().getInReplyToText());
//                UserCommandAddToReply addToReply = new UserCommandAddToReply(activity, user);
//                addToReply.execute();
//                assertEquals("@laco0416 @laco0416 ", PostState.getState().getText());
//            }
//        });
//    }
//
//    public void testMessageCommand() throws Exception
//    {
//        final Activity activity = getActivity();
//        Account account = new Account(mock.getAccessToken(), mock.getAccessTokenSecret(), mock.getUserMock().getId(), mock.getUserMock().getScreenName());
//        final DirectMessage message = mock.getDirectMessageMock();
//        getActivity().runOnUiThread(new Runnable()
//        {
//            @Override
//            public void run()
//            {
//                MessageCommand reply = new MessageCommandReply(activity, message);
//                assertTrue(reply.isEnabled());
//                assertEquals(activity.getString(R.string.command_message_reply), reply.getText());
//                assertTrue(reply.execute());
//                assertEquals("", PostState.getState().getText());
//                assertEquals(-1, PostState.getState().getInReplyToStatusID());
//                assertEquals(message.getSenderScreenName(), PostState.getState().getInReplyToScreenName());
//                assertTrue(PostState.getState().isDirectMessage());
//                assertEquals(message.getText(), PostState.getState().getInReplyToText());
//            }
//        });
//
//    }
}
