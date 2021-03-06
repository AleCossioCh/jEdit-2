/*
 * KeyEventWorkaround.java - Works around bugs in Java event handling
 * :tabSize=8:indentSize=8:noTabs=false:
 * :folding=explicit:collapseFolds=1:
 *
 * Copyright (C) 2000 Slava Pestov
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

package org.gjt.sp.jedit.gui;

//{{{ Imports
import java.awt.event.*;
import java.awt.*;
import org.gjt.sp.jedit.OperatingSystem;
//}}}

public class KeyEventWorkaround
{
	//{{{ processKeyEvent() method
	public static KeyEvent processKeyEvent(KeyEvent evt)
	{
		int keyCode = evt.getKeyCode();
		char ch = evt.getKeyChar();

		switch(evt.getID())
		{
		//{{{ KEY_PRESSED...
		case KeyEvent.KEY_PRESSED:
			// get rid of keys we never need to handle
			switch(keyCode)
			{
			case KeyEvent.VK_CONTROL:
			case KeyEvent.VK_SHIFT:
			case KeyEvent.VK_ALT:
			case KeyEvent.VK_META:
			case '\0':
				return null;

			case KeyEvent.VK_NUMPAD0:   case KeyEvent.VK_NUMPAD1:
			case KeyEvent.VK_NUMPAD2:   case KeyEvent.VK_NUMPAD3:
			case KeyEvent.VK_NUMPAD4:   case KeyEvent.VK_NUMPAD5:
			case KeyEvent.VK_NUMPAD6:   case KeyEvent.VK_NUMPAD7:
			case KeyEvent.VK_NUMPAD8:   case KeyEvent.VK_NUMPAD9:
				// if NumLock is on, then ignore the numeric
				// keypad codes; if NumLock off, ignore the
				// resulting KeyTyped
				if(Toolkit.getDefaultToolkit()
					.getLockingKeyState(
					KeyEvent.VK_NUM_LOCK))
				{
					return null;
				}
				else
				{
					last = LAST_NUMKEYPAD;
					return null;
				}
			case KeyEvent.VK_MULTIPLY:  case KeyEvent.VK_ADD:
			/* case KeyEvent.VK_SEPARATOR: */ case KeyEvent.VK_SUBTRACT:
			case KeyEvent.VK_DECIMAL:   case KeyEvent.VK_DIVIDE:
				if(Toolkit.getDefaultToolkit()
					.getLockingKeyState(
					KeyEvent.VK_NUM_LOCK))
				{
					return null;
				}
				else
				{
					last = LAST_NUMKEYPAD;
					lastKeyTime = System.currentTimeMillis();
					break;
				}
			default:
				if(!OperatingSystem.isMacOS())
					handleBrokenKeys(evt,keyCode);
				break;
			}

			return evt;
		//}}}
		//{{{ KEY_TYPED...
		case KeyEvent.KEY_TYPED:
			// need to let \b through so that backspace will work
			// in HistoryTextFields
			if((ch < 0x20 || ch == 0x7f || ch == 0xff) && ch != '\b')
				return null;

			// "Alt" is the option key on MacOS, and it can generate
			// user input
			if(OperatingSystem.isMacOS())
			{
				if(evt.isControlDown() || evt.isMetaDown())
					return null;
			}
			else
			{
				if((evt.isControlDown() ^ evt.isAltDown())
					|| evt.isMetaDown())
					return null;

				// if the last key was a numeric keypad key
				// and NumLock is off, filter it out
				if(last == LAST_NUMKEYPAD && System.currentTimeMillis()
					- lastKeyTime < 750)
				{
					last = LAST_NOTHING;
					if(ch >= '0' && ch <= '9' || ch == '.'
						|| ch == '/' || ch == '*'
						|| ch == '-' || ch == '+')
					{
						return null;
					}
				}

				// if the last key was a broken key, filter
				// out all except 'a'-'z' that occur 750 ms after.
				if(last == LAST_BROKEN && System.currentTimeMillis()
					- lastKeyTime < 750 && !Character.isLetter(ch))
				{
					last = LAST_NOTHING;
					return null;
				}
				// otherwise, if it was ALT, filter out everything.
				else if(last == LAST_ALT && System.currentTimeMillis()
					- lastKeyTime < 750)
				{
					last = LAST_NOTHING;
					return null;
				}
			}

			return evt;
		//}}}
		default:
			return evt;
		}
	} //}}}

	//{{{ Private members

	//{{{ Static variables
	private static long lastKeyTime;

	private static int last;
	private static final int LAST_NOTHING = 0;
	private static final int LAST_ALTGR = 1;
	private static final int LAST_ALT = 2;
	private static final int LAST_BROKEN = 3;
	private static final int LAST_NUMKEYPAD = 3;
	//}}}

	//{{{ handleBrokenKeys() method
	private static void handleBrokenKeys(KeyEvent evt, int keyCode)
	{
		if(evt.isAltDown() && evt.isControlDown()
			&& !evt.isMetaDown())
		{
			last = LAST_ALTGR;
			return;
		}
		else if(!(evt.isAltDown() || evt.isControlDown() || evt.isMetaDown()))
		{
			last = LAST_NOTHING;
			return;
		}

		if(evt.isAltDown())
			last = LAST_ALT;
		else if((keyCode < KeyEvent.VK_A || keyCode > KeyEvent.VK_Z)
			&& keyCode != KeyEvent.VK_LEFT && keyCode != KeyEvent.VK_RIGHT
			&& keyCode != KeyEvent.VK_UP && keyCode != KeyEvent.VK_DOWN
			&& keyCode != KeyEvent.VK_DELETE && keyCode != KeyEvent.VK_BACK_SPACE
			 && keyCode != KeyEvent.VK_TAB && keyCode != KeyEvent.VK_ENTER)
			last = LAST_BROKEN;
		else
			last = LAST_NOTHING;

		lastKeyTime = System.currentTimeMillis();
	} //}}}

	//}}}
}
