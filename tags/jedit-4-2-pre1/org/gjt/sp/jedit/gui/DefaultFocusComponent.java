/*
 * DefaultFocusComponent.java - Interface for dockable windows
 * :tabSize=8:indentSize=8:noTabs=false:
 * :folding=explicit:collapseFolds=1:
 *
 * Copyright (C) 2003 Slava Pestov
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

/**
 * An interface that provides a method for focusing on the default
 * component. The file system browser implements this in order to
 * focus on the file system view by default, for example.
 *
 * @since jEdit 4.2pre1
 * @author Slava Pestov
 * @version $Id: DefaultFocusComponent.java 4620 2003-04-14 05:28:12Z spestov $
 */
public interface DefaultFocusComponent
{
	/**
	 * Sets focus on the default component.
	 * @since jEdit 4.2pre1
	 */
	void focusOnDefaultComponent();
}
