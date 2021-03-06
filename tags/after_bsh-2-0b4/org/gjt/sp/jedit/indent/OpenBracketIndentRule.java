/*
 * OpenBracketIndentRule.java
 * :tabSize=8:indentSize=8:noTabs=false:
 * :folding=explicit:collapseFolds=1:
 *
 * Copyright (C) 2005 Slava Pestov
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

package org.gjt.sp.jedit.indent;

import java.util.List;
import org.gjt.sp.jedit.buffer.JEditBuffer;
import org.gjt.sp.jedit.TextUtilities;

public class OpenBracketIndentRule extends BracketIndentRule
{
	//{{{ OpenBracketIndentRule constructor
	public OpenBracketIndentRule(char openBracket, boolean aligned)
	{
		super(openBracket,
			TextUtilities.getComplementaryBracket(openBracket,
			new boolean[1]));
		this.aligned = aligned;
	} //}}}

	//{{{ apply() method
	public void apply(JEditBuffer buffer, int thisLineIndex,
		int prevLineIndex, int prevPrevLineIndex,
		List indentActions)
	{
		int prevOpenBracketCount = getOpenBracketCount(buffer,prevLineIndex);
		if(prevOpenBracketCount != 0)
		{
			handleCollapse(indentActions);
			boolean multiple = buffer.getBooleanProperty(
				"multipleBracketIndent");
			IndentAction increase = new IndentAction.Increase(
				multiple ? prevOpenBracketCount : 1);
			indentActions.add(increase);
		}
		else if(getOpenBracketCount(buffer,thisLineIndex) != 0)
		{
			handleCollapse(indentActions);
		}
	} //}}}

	//{{{ getOpenBracketCount() method
	private int getOpenBracketCount(JEditBuffer buffer, int line)
	{
		if(line == -1)
			return 0;
		else
			return getBrackets(buffer.getLineText(line)).openCount;
	} //}}}

	//{{{ handleCollapse() method
	private void handleCollapse(List indentActions)
	{
		if(indentActions.contains(new IndentAction.Collapse()))
			indentActions.clear();
	} //}}}

	private boolean aligned;
}
