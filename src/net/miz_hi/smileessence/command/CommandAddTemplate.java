package net.miz_hi.smileessence.command;

import net.miz_hi.smileessence.data.Templates;
import net.miz_hi.smileessence.event.ToastManager;

public class CommandAddTemplate extends MenuCommand
{

	private String text;

	public CommandAddTemplate(String text)
	{
		this.text = text;
	}

	@Override
	public String getName()
	{
		return "定型文に追加";
	}

	@Override
	public void workOnUiThread()
	{
		Templates.addTemplate(text);
		ToastManager.show("追加しました");
	}
}
