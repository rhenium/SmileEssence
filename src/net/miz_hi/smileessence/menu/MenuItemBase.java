package net.miz_hi.smileessence.menu;

import net.miz_hi.smileessence.core.EventHandlerActivity;
import net.miz_hi.smileessence.dialog.DialogAdapter;
import net.miz_hi.smileessence.message.ToastMessage;
import android.os.Handler;

public abstract class MenuItemBase
{
	protected EventHandlerActivity activity;
	protected DialogAdapter adapter;
	private Handler handler;

	public MenuItemBase(EventHandlerActivity activity, DialogAdapter adapter)
	{
		this.activity = activity;
		this.adapter = adapter;
		handler = new Handler();
	}

	public abstract String getText();
	
	public boolean isVisible()
	{
		return true;
	}

	public void run()
	{
		handler.postDelayed(new Runnable()
		{
			public void run()
			{
				adapter.dispose();
				work();
			}
		}, 20);
	}

	public abstract void work();

	public void toast(String str)
	{
		activity.messenger.raise("toast", new ToastMessage(str));
	}

}
