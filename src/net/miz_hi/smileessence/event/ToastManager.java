package net.miz_hi.smileessence.event;

import net.miz_hi.smileessence.event.StatusEventModel.EnumStatusEventType;
import net.miz_hi.smileessence.util.CountUpInteger;
import net.miz_hi.smileessence.util.UiHandler;
import net.miz_hi.smileessence.view.MainActivity;
import twitter4j.User;
import android.app.Activity;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

public class ToastManager
{

	private static ToastManager instance;
	private Activity activity;
	private Toast toast;
	private View viewToastBase;
	private User enemyUser;
	private long lastUserId = -1;
	private long lastStatusId = -1;
	private CountUpInteger counterSourceUser = new CountUpInteger(5);
	private CountUpInteger counterTargetStatus = new CountUpInteger(5);

	private ToastManager(Activity activity)
	{
		this.activity = activity;
	}
	
	public static ToastManager getInstance()
	{
		if(instance == null)
		{
			instance = new ToastManager(MainActivity.getInstance());
		}
		return instance;
	}
	
	public static void show(String text)
	{
		getInstance().showToast(text);
	}
	
	private void showToast(final String text)
	{
		if(activity == null || activity.isFinishing())
		{
			return;
		}
		new UiHandler()
		{
			
			@Override
			public void run()
			{
				Toast.makeText(activity, text, Toast.LENGTH_SHORT).show();
			}
		}.postDelayed(200);
	}

	public void noticeEvent(final EventModel model)
	{
		if(model instanceof StatusEventModel)
		{
			if(((StatusEventModel)model).type != EnumStatusEventType.REPLY)
			{
				if (lastUserId == model.source.getId())
				{
					if (counterSourceUser.isOver())
					{
						return;
					}
					else if (counterSourceUser.countUp())
					{
						new UiHandler()
						{
							@Override
							public void run()
							{
								toast.cancel();
								show(enemyUser.getScreenName() + "に爆撃を受けています");
							}
						}.post();
						return;
					}
				}
				else
				{
					lastUserId = model.source.getId();
					counterSourceUser.reset();
					enemyUser = model.source;
				}
			}
		}
		
		new UiHandler()
		{
			@Override
			public void run()
			{
				if (toast == null)
				{
					toast = new Toast(activity);
				}
				viewToastBase = EventViewFactory.getToastView(activity, model, viewToastBase);
				toast.setView(viewToastBase);
				toast.setGravity(Gravity.BOTTOM, 0, 80);
				toast.setDuration(Toast.LENGTH_SHORT);
				toast.show();
			}
		}.post();
	}
}
