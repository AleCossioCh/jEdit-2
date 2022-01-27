/*
 * Autosave.java - Autosave manager
 * :tabSize=8:indentSize=8:noTabs=false:
 * :folding=explicit:collapseFolds=1:
 *
 * Copyright (C) 1998, 2003 Slava Pestov
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package org.gjt.sp.jedit;

//{{{ Imports
import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.gjt.sp.util.Log;
//}}}

/**
 * @author Slava Pestov
 * @version $Id: Autosave.java 4959 2004-01-25 01:38:30Z spestov $
 */
class Autosave implements ActionListener
{
	//{{{ setInterval() method
	public static void setInterval(int interval)
	{
		if(interval == 0)
		{
			if(timer != null)
			{
				timer.stop();
				timer = null;
			}

			return;
		}

		interval *= 1000;

		if(timer == null)
		{
			timer = new Timer(interval,new Autosave());
			timer.start();
		}
		else
			timer.setDelay(interval);
	} //}}}

	//{{{ stop() method
	public static void stop()
	{
		if(timer != null)
			timer.stop();
	} //}}}

	//{{{ actionPerformed() method
	public void actionPerformed(ActionEvent evt)
	{
		// might come in handy useful some time
		/* Runtime runtime = Runtime.getRuntime();
		int freeMemory = (int)(runtime.freeMemory() / 1024);
		int totalMemory = (int)(runtime.totalMemory() / 1024);
		int usedMemory = (totalMemory - freeMemory);

		Log.log(Log.DEBUG,this,"Java heap: " + usedMemory + "Kb / "
			+ totalMemory + "Kb, " + (usedMemory * 100 / totalMemory)
			+ "%"); */

		// save list of open files
		if(jEdit.getViewCount() != 0)
			PerspectiveManager.savePerspective(true);

		Buffer[] bufferArray = jEdit.getBuffers();
		for(int i = 0; i < bufferArray.length; i++)
			bufferArray[i].autosave();

		// flush log
		Log.flushStream();
	} //}}}

	//{{{ Private members
	private static Timer timer;

	private Autosave() {}
	//}}}
}
