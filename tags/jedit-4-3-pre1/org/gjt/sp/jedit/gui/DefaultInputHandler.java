/*
 * DefaultInputHandler.java - Default implementation of an input handler
 * :tabSize=8:indentSize=8:noTabs=false:
 * :folding=explicit:collapseFolds=1:
 *
 * Copyright (C) 1999, 2003 Slava Pestov
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
import javax.swing.KeyStroke;
import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;
import java.awt.Toolkit;
import java.util.Hashtable;
import java.util.StringTokenizer;
import org.gjt.sp.jedit.*;
import org.gjt.sp.util.Log;
//}}}

/**
 * The default input handler. It maps sequences of keystrokes into actions
 * and inserts key typed events into the text area.
 * @author Slava Pestov
 * @version $Id: DefaultInputHandler.java 5134 2004-09-13 12:59:25Z spestov $
 */
public class DefaultInputHandler extends InputHandler
{
	//{{{ DefaultInputHandler constructor
	/**
	 * Creates a new input handler with no key bindings defined.
	 * @param view The view
	 * @param bindings An explicitly-specified set of key bindings,
	 * must not be null.
	 * @since jEdit 4.3pre1
	 */
	public DefaultInputHandler(View view, Hashtable bindings)
	{
		super(view);

		if(bindings == null)
			throw new NullPointerException();
		this.bindings = this.currentBindings = bindings;
	} //}}}

	//{{{ DefaultInputHandler constructor
	/**
	 * Creates a new input handler with no key bindings defined.
	 * @param view The view
	 */
	public DefaultInputHandler(View view)
	{
		this(view,new Hashtable());
	} //}}}

	//{{{ DefaultInputHandler constructor
	/**
	 * Creates a new input handler with the same set of key bindings
	 * as the one specified. Note that both input handlers share
	 * a pointer to exactly the same key binding table; so adding
	 * a key binding in one will also add it to the other.
	 * @param copy The input handler to copy key bindings from
	 * @param view The view
	 */
	public DefaultInputHandler(View view, DefaultInputHandler copy)
	{
		this(view,copy.bindings);
	} //}}}

	//{{{ addKeyBinding() method
	/**
	 * Adds a key binding to this input handler. The key binding is
	 * a list of white space separated key strokes of the form
	 * <i>[modifiers+]key</i> where modifier is C for Control, A for Alt,
	 * or S for Shift, and key is either a character (a-z) or a field
	 * name in the KeyEvent class prefixed with VK_ (e.g., BACK_SPACE)
	 * @param keyBinding The key binding
	 * @param action The action
	 * @since jEdit 4.2pre1
	 */
	public void addKeyBinding(String keyBinding, String action)
	{
		addKeyBinding(keyBinding,(Object)action);
	} //}}}

	//{{{ addKeyBinding() method
	/**
	 * Adds a key binding to this input handler. The key binding is
	 * a list of white space separated key strokes of the form
	 * <i>[modifiers+]key</i> where modifier is C for Control, A for Alt,
	 * or S for Shift, and key is either a character (a-z) or a field
	 * name in the KeyEvent class prefixed with VK_ (e.g., BACK_SPACE)
	 * @param keyBinding The key binding
	 * @param action The action
	 */
	public void addKeyBinding(String keyBinding, EditAction action)
	{
		addKeyBinding(keyBinding,(Object)action);
	} //}}}

	//{{{ addKeyBinding() method
	/**
	 * Adds a key binding to this input handler. The key binding is
	 * a list of white space separated key strokes of the form
	 * <i>[modifiers+]key</i> where modifier is C for Control, A for Alt,
	 * or S for Shift, and key is either a character (a-z) or a field
	 * name in the KeyEvent class prefixed with VK_ (e.g., BACK_SPACE)
	 * @param keyBinding The key binding
	 * @param action The action
	 * @since jEdit 4.3pre1
	 */
	public void addKeyBinding(String keyBinding, Object action)
	{
		Hashtable current = bindings;

		String prefixStr = null;

		StringTokenizer st = new StringTokenizer(keyBinding);
		while(st.hasMoreTokens())
		{
			String keyCodeStr = st.nextToken();
			if(prefixStr == null)
				prefixStr = keyCodeStr;
			else
				prefixStr = prefixStr + " " + keyCodeStr;

			KeyEventTranslator.Key keyStroke = KeyEventTranslator.parseKey(keyCodeStr);
			if(keyStroke == null)
				return;

			if(st.hasMoreTokens())
			{
				Object o = current.get(keyStroke);
				if(o instanceof Hashtable)
					current = (Hashtable)o;
				else
				{
					Hashtable hash = new Hashtable();
					hash.put(PREFIX_STR,prefixStr);
					o = hash;
					current.put(keyStroke,o);
					current = (Hashtable)o;
				}
			}
			else
				current.put(keyStroke,action);
		}
	} //}}}

	//{{{ _addKeyBinding() method
	/**
	 * @deprecated Use <code>addKeyBinding()</code> instead.
	 */
	public void _addKeyBinding(String keyBinding, Object action)
	{
		addKeyBinding(keyBinding,action);
	} //}}}

	//{{{ removeKeyBinding() method
	/**
	 * Removes a key binding from this input handler. This is not yet
	 * implemented.
	 * @param keyBinding The key binding
	 */
	public void removeKeyBinding(String keyBinding)
	{
		Hashtable current = bindings;

		StringTokenizer st = new StringTokenizer(keyBinding);
		while(st.hasMoreTokens())
		{
			String keyCodeStr = st.nextToken();
			KeyEventTranslator.Key keyStroke = KeyEventTranslator.parseKey(keyCodeStr);
			if(keyStroke == null)
				return;

			if(st.hasMoreTokens())
			{
				Object o = current.get(keyStroke);
				if(o instanceof Hashtable)
					current = ((Hashtable)o);
				else if(o != null)
				{
					// we have binding foo
					// but user asks to remove foo bar?
					current.remove(keyStroke);
					return;
				}
				else
				{
					// user asks to remove non-existent
					return;
				}
			}
			else
				current.remove(keyStroke);
		}
	} //}}}

	//{{{ removeAllKeyBindings() method
	/**
	 * Removes all key bindings from this input handler.
	 */
	public void removeAllKeyBindings()
	{
		bindings.clear();
	} //}}}

	//{{{ getKeyBinding() method
	/**
	 * Returns either an edit action, or a hashtable if the specified key
	 * is a prefix.
	 * @param keyBinding The key binding
	 * @since jEdit 3.2pre5
	 */
	public Object getKeyBinding(String keyBinding)
	{
		Hashtable current = bindings;
		StringTokenizer st = new StringTokenizer(keyBinding);

		while(st.hasMoreTokens())
		{
			KeyEventTranslator.Key keyStroke = KeyEventTranslator.parseKey(
				st.nextToken());
			if(keyStroke == null)
				return null;

			if(st.hasMoreTokens())
			{
				Object o = current.get(keyStroke);
				if(o instanceof Hashtable)
				{
					if(!st.hasMoreTokens())
						return o;
					else
						current = (Hashtable)o;
				}
				else
					return o;
			}
			else
			{
				return current.get(keyStroke);
			}
		}

		return null;
	} //}}}

	//{{{ isPrefixActive() method
	/**
	 * Returns if a prefix key has been pressed.
	 */
	public boolean isPrefixActive()
	{
		return bindings != currentBindings
			|| super.isPrefixActive();
	} //}}}

	//{{{ setBindings() method
	/**
	 * Replace the set of key bindings.
	 * @since jEdit 4.3pre1
	 */
	public void setBindings(Hashtable bindings)
	{
		this.bindings = this.currentBindings = bindings;
	} //}}}

	//{{{ setCurrentBindings() method
	public void setCurrentBindings(Hashtable bindings)
	{
		view.getStatus().setMessage((String)bindings.get(PREFIX_STR));
		currentBindings = bindings;
	} //}}}

	//{{{ handleKey() method
	/**
	 * Handles the given keystroke.
	 * @param keyStroke The key stroke
	 * @since jEdit 4.2pre5
	 */
	public boolean handleKey(KeyEventTranslator.Key keyStroke)
	{
		char input = '\0';
		if(keyStroke.modifiers == null
			|| keyStroke.modifiers.equals("S"))
		{
			switch(keyStroke.key)
			{
			case '\n':
			case '\t':
				input = (char)keyStroke.key;
				break;
			default:
				input = keyStroke.input;
				break;
			}
		}

		if(readNextChar != null)
		{
			if(input != '\0')
			{
				setCurrentBindings(bindings);
				invokeReadNextChar(input);
				repeatCount = 1;
				return true;
			}
			else
			{
				readNextChar = null;
				view.getStatus().setMessage(null);
			}
		}

		Object o = currentBindings.get(keyStroke);
		if(o == null)
		{
			// Don't beep if the user presses some
			// key we don't know about unless a
			// prefix is active. Otherwise it will
			// beep when caps lock is pressed, etc.
			if(currentBindings != bindings)
			{
				Toolkit.getDefaultToolkit().beep();
				// F10 should be passed on, but C+e F10
				// shouldn't
				repeatCount = 1;
				setCurrentBindings(bindings);
			}

			if(input != '\0')
				userInput(input);
			else
			{
				// this is retarded. excuse me while I drool
				// and make stupid noises
				switch(keyStroke.key)
				{
				case KeyEvent.VK_NUMPAD0:
				case KeyEvent.VK_NUMPAD1:
				case KeyEvent.VK_NUMPAD2:
				case KeyEvent.VK_NUMPAD3:
				case KeyEvent.VK_NUMPAD4:
				case KeyEvent.VK_NUMPAD5:
				case KeyEvent.VK_NUMPAD6:
				case KeyEvent.VK_NUMPAD7:
				case KeyEvent.VK_NUMPAD8:
				case KeyEvent.VK_NUMPAD9:
				case KeyEvent.VK_MULTIPLY:
				case KeyEvent.VK_ADD:
				/* case KeyEvent.VK_SEPARATOR: */
				case KeyEvent.VK_SUBTRACT:
				case KeyEvent.VK_DECIMAL:
				case KeyEvent.VK_DIVIDE:
					KeyEventWorkaround.numericKeypadKey();
					break;
				}
			}
		}
		else if(o instanceof Hashtable)
		{
			setCurrentBindings((Hashtable)o);
			return true;
		}
		else if(o instanceof String)
		{
			setCurrentBindings(bindings);
			invokeAction((String)o);
			return true;
		}
		else if(o instanceof EditAction)
		{
			setCurrentBindings(bindings);
			invokeAction((EditAction)o);
			return true;
		}

		return false;
	} //}}}

	//{{{ getSymbolicModifierName() method
	/**
	 * Returns a the symbolic modifier name for the specified Java modifier
	 * flag.
	 *
	 * @param mod A modifier constant from <code>InputEvent</code>
	 *
	 * @since jEdit 4.1pre3
	 */
	public static char getSymbolicModifierName(int mod)
	{
		return KeyEventTranslator.getSymbolicModifierName(mod);
	} //}}}

	//{{{ getModifierString() method
	/**
	 * Returns a string containing symbolic modifier names set in the
	 * specified event.
	 *
	 * @param evt The event
	 *
	 * @since jEdit 4.1pre3
	 */
	public static String getModifierString(InputEvent evt)
	{
		return KeyEventTranslator.getModifierString(evt);
	} //}}}

	//{{{ parseKeyStroke() method
	/**
	 * @deprecated We don't use Swing KeyStrokes anymore.
	 */
	public static KeyStroke parseKeyStroke(String keyStroke)
	{
		if(keyStroke == null)
			return null;
		int modifiers = 0;
		int index = keyStroke.indexOf('+');
		if(index != -1)
		{
			for(int i = 0; i < index; i++)
			{
				switch(Character.toUpperCase(keyStroke
					.charAt(i)))
				{
				case 'A':
					modifiers |= KeyEventTranslator.a;
					break;
				case 'C':
					modifiers |= KeyEventTranslator.c;
					break;
				case 'M':
					modifiers |= KeyEventTranslator.m;
					break;
				case 'S':
					modifiers |= KeyEventTranslator.s;
					break;
				}
			}
		}
		String key = keyStroke.substring(index + 1);
		if(key.length() == 1)
		{
			char ch = key.charAt(0);
			if(modifiers == 0)
				return KeyStroke.getKeyStroke(ch);
			else
			{
				return KeyStroke.getKeyStroke(Character.toUpperCase(ch),
					modifiers);
			}
		}
		else if(key.length() == 0)
		{
			Log.log(Log.ERROR,DefaultInputHandler.class,
				"Invalid key stroke: " + keyStroke);
			return null;
		}
		else
		{
			int ch;

			try
			{
				ch = KeyEvent.class.getField("VK_".concat(key))
					.getInt(null);
			}
			catch(Exception e)
			{
				Log.log(Log.ERROR,DefaultInputHandler.class,
					"Invalid key stroke: "
					+ keyStroke);
				return null;
			}

			return KeyStroke.getKeyStroke(ch,modifiers);
		}
	} //}}}

	//{{{ Private members

	// Stores prefix name in bindings hashtable
	private static Object PREFIX_STR = "PREFIX_STR";

	private Hashtable bindings;
	private Hashtable currentBindings;
	//}}}
}
