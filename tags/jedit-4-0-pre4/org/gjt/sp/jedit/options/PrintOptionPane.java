/*
 * PrintOptionPane.java - Printing options panel
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

package org.gjt.sp.jedit.options;

//{{{ Imports
import javax.swing.*;
import java.awt.*;
import org.gjt.sp.jedit.gui.FontSelector;
import org.gjt.sp.jedit.*;
//}}}

public class PrintOptionPane extends AbstractOptionPane
{
	//{{{ PrintOptionPane constructor
	public PrintOptionPane()
	{
		super("print");
	} //}}}

	//{{{ _init() method
	protected void _init()
	{
		/* Font */
		font = new FontSelector(jEdit.getFontProperty("print.font"));
		addComponent(jEdit.getProperty("options.print.font"),font);

		/* Header */
		printHeader = new JCheckBox(jEdit.getProperty("options.print"
			+ ".header"));
		printHeader.setSelected(jEdit.getBooleanProperty("print.header"));
		addComponent(printHeader);

		/* Footer */
		printFooter = new JCheckBox(jEdit.getProperty("options.print"
			+ ".footer"));
		printFooter.setSelected(jEdit.getBooleanProperty("print.footer"));
		addComponent(printFooter);

		/* Line numbering */
		printLineNumbers = new JCheckBox(jEdit.getProperty("options.print"
			+ ".lineNumbers"));
		printLineNumbers.setSelected(jEdit.getBooleanProperty("print.lineNumbers"));
		addComponent(printLineNumbers);

		/* Color */
		color = new JCheckBox(jEdit.getProperty("options.print"
			+ ".color"));
		color.setSelected(jEdit.getBooleanProperty("print.color"));
		addComponent(color);

		/* Tab size */
		String[] tabSizes = { "2", "4", "8" };
		tabSize = new JComboBox(tabSizes);
		tabSize.setEditable(true);
		tabSize.setSelectedItem(jEdit.getProperty("print.tabSize"));
		addComponent(jEdit.getProperty("options.print.tabSize"),tabSize);
	} //}}}

	//{{{ _save() method
	protected void _save()
	{
		jEdit.setFontProperty("print.font",font.getFont());
		jEdit.setBooleanProperty("print.header",printHeader.isSelected());
		jEdit.setBooleanProperty("print.footer",printFooter.isSelected());
		jEdit.setBooleanProperty("print.lineNumbers",printLineNumbers.isSelected());
		jEdit.setBooleanProperty("print.color",color.isSelected());
	} //}}}

	//{{{ Private members
	private FontSelector font;
	private JCheckBox printHeader;
	private JCheckBox printFooter;
	private JCheckBox printLineNumbers;
	private JCheckBox color;
	private JComboBox tabSize;
	//}}}
}
